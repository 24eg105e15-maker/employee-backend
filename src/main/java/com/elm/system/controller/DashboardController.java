package com.elm.system.controller;

import com.elm.system.dto.AdminDashboardResponse;
import com.elm.system.dto.EmployeeDashboardResponse;
import com.elm.system.dto.LeaveApplicationResponse;
import com.elm.system.dto.ManagerDashboardResponse;
import com.elm.system.entity.Employee;
import com.elm.system.service.AuthService;
import com.elm.system.service.DashboardService;
import com.elm.system.service.LeaveApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    @GetMapping("/api/dashboard/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/api/dashboard/manager")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ManagerDashboardResponse> getManagerDashboard() {
        Employee manager = authService.getCurrentAuthenticatedEmployee();
        return ResponseEntity.ok(dashboardService.getManagerDashboard(manager.getId()));
    }

    @GetMapping("/api/dashboard/employee")
    public ResponseEntity<EmployeeDashboardResponse> getEmployeeDashboard() {
        Employee employee = authService.getCurrentAuthenticatedEmployee();
        return ResponseEntity.ok(dashboardService.getEmployeeDashboard(employee.getId()));
    }

    @GetMapping("/api/reports/leaves")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<LeaveApplicationResponse>> getLeaveReport(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(leaveApplicationService.searchLeaves(status, departmentId, leaveTypeId, keyword, PageRequest.of(page, size)));
    }

    @GetMapping("/api/reports/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDepartmentReport() {
        AdminDashboardResponse adminDash = dashboardService.getAdminDashboard();
        Map<String, Object> map = new HashMap<>();
        map.put("departmentEmployeeCounts", adminDash.getDepartmentEmployeeCounts());
        map.put("totalDepartments", adminDash.getTotalDepartments());
        map.put("totalEmployees", adminDash.getTotalEmployees());
        return ResponseEntity.ok(map);
    }

    @GetMapping("/api/reports/leave-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLeaveSummaryReport() {
        AdminDashboardResponse adminDash = dashboardService.getAdminDashboard();
        Map<String, Object> map = new HashMap<>();
        map.put("leaveTypeDistribution", adminDash.getLeaveTypeDistribution());
        map.put("totalPending", adminDash.getPendingLeaveRequests());
        map.put("totalApproved", adminDash.getApprovedLeaveRequests());
        map.put("totalRejected", adminDash.getRejectedLeaveRequests());
        map.put("currentlyOnLeave", adminDash.getEmployeesCurrentlyOnLeave());
        return ResponseEntity.ok(map);
    }
}
