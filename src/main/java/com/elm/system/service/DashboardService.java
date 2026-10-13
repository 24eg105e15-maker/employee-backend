package com.elm.system.service;

import com.elm.system.dto.*;
import com.elm.system.entity.Department;
import com.elm.system.entity.Employee;
import com.elm.system.entity.LeaveType;
import com.elm.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    @Autowired
    private EmployeeService employeeService;

    public AdminDashboardResponse getAdminDashboard() {
        AdminDashboardResponse response = new AdminDashboardResponse();

        response.setTotalEmployees(employeeRepository.count());
        response.setTotalDepartments(departmentRepository.count());
        response.setPendingLeaveRequests(leaveApplicationRepository.countByStatus("PENDING"));
        response.setApprovedLeaveRequests(leaveApplicationRepository.countByStatus("APPROVED"));
        response.setRejectedLeaveRequests(leaveApplicationRepository.countByStatus("REJECTED"));

        LocalDate today = LocalDate.now();
        response.setEmployeesCurrentlyOnLeave(leaveApplicationRepository.countEmployeesCurrentlyOnLeave(today));

        List<LeaveApplicationResponse> upcoming = leaveApplicationRepository
                .findUpcomingApprovedLeaves(today, today.plusDays(30)).stream()
                .map(leaveApplicationService::mapToResponse)
                .collect(Collectors.toList());
        response.setUpcomingApprovedLeaves(upcoming);
        response.setUpcomingApprovedLeavesCount(upcoming.size());

        YearMonth currentYearMonth = YearMonth.now();
        LocalDate monthStart = currentYearMonth.atDay(1);
        LocalDate monthEnd = currentYearMonth.atEndOfMonth();
        response.setLeaveRequestsThisMonth(leaveApplicationRepository.countLeavesInMonth(monthStart, monthEnd));

        // Recent 6 applications
        List<LeaveApplicationResponse> recent = leaveApplicationRepository
                .findAll(PageRequest.of(0, 6, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "submittedAt")))
                .stream()
                .map(leaveApplicationService::mapToResponse)
                .collect(Collectors.toList());
        response.setRecentApplications(recent);

        // Department-wise employee counts
        Map<String, Long> deptCounts = new HashMap<>();
        for (Department dept : departmentRepository.findAll()) {
            deptCounts.put(dept.getDepartmentName(), employeeRepository.countByDepartment(dept));
        }
        response.setDepartmentEmployeeCounts(deptCounts);

        // Leave type distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        for (LeaveType type : leaveTypeRepository.findAll()) {
            long count = leaveApplicationRepository.findAll().stream()
                    .filter(a -> a.getLeaveType().getId().equals(type.getId()))
                    .count();
            typeDistribution.put(type.getName(), count);
        }
        response.setLeaveTypeDistribution(typeDistribution);

        return response;
    }

    public ManagerDashboardResponse getManagerDashboard(Long managerEmployeeId) {
        ManagerDashboardResponse response = new ManagerDashboardResponse();

        Employee manager = employeeRepository.findById(managerEmployeeId).orElse(null);
        if (manager == null) return response;

        List<Employee> team = employeeRepository.findByManager(manager);
        response.setTotalTeamMembers(team.size());
        response.setTeamMembers(team.stream().map(employeeService::mapToResponse).collect(Collectors.toList()));

        response.setPendingApprovalsCount(leaveApplicationRepository.countByManagerIdAndStatus(managerEmployeeId, "PENDING"));
        response.setTeamApprovedLeavesCount(leaveApplicationRepository.countByManagerIdAndStatus(managerEmployeeId, "APPROVED"));

        LocalDate today = LocalDate.now();
        long onLeave = leaveApplicationRepository.findEmployeesCurrentlyOnLeave(today).stream()
                .filter(a -> a.getEmployee().getManager() != null && a.getEmployee().getManager().getId().equals(managerEmployeeId))
                .count();
        response.setTeamCurrentlyOnLeaveCount(onLeave);

        List<LeaveApplicationResponse> pending = leaveApplicationRepository.findByManagerIdAndStatus(managerEmployeeId, "PENDING").stream()
                .map(leaveApplicationService::mapToResponse)
                .collect(Collectors.toList());
        response.setPendingRequests(pending);

        List<LeaveApplicationResponse> upcoming = leaveApplicationRepository.findUpcomingApprovedLeaves(today, today.plusDays(30)).stream()
                .filter(a -> a.getEmployee().getManager() != null && a.getEmployee().getManager().getId().equals(managerEmployeeId))
                .map(leaveApplicationService::mapToResponse)
                .collect(Collectors.toList());
        response.setUpcomingTeamLeaves(upcoming);

        return response;
    }

    public EmployeeDashboardResponse getEmployeeDashboard(Long employeeId) {
        EmployeeDashboardResponse response = new EmployeeDashboardResponse();

        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) return response;

        response.setProfile(employeeService.mapToResponse(employee));
        response.setBalances(leaveBalanceService.getBalancesForEmployee(employeeId));

        var allMy = leaveApplicationRepository.findByEmployeeOrderBySubmittedAtDesc(employee);
        response.setTotalAppliedLeaves(allMy.size());
        response.setPendingLeavesCount(allMy.stream().filter(a -> "PENDING".equals(a.getStatus())).count());
        response.setApprovedLeavesCount(allMy.stream().filter(a -> "APPROVED".equals(a.getStatus())).count());
        response.setRejectedLeavesCount(allMy.stream().filter(a -> "REJECTED".equals(a.getStatus())).count());

        response.setRecentApplications(allMy.stream().limit(5).map(leaveApplicationService::mapToResponse).collect(Collectors.toList()));

        LocalDate today = LocalDate.now();
        response.setUpcomingApprovedLeaves(allMy.stream()
                .filter(a -> "APPROVED".equals(a.getStatus()) && a.getStartDate().isAfter(today))
                .map(leaveApplicationService::mapToResponse)
                .collect(Collectors.toList()));

        return response;
    }
}
