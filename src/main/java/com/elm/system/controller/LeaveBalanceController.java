package com.elm.system.controller;

import com.elm.system.dto.LeaveBalanceResponse;
import com.elm.system.dto.LeaveBalanceUpdateRequest;
import com.elm.system.entity.Employee;
import com.elm.system.entity.User;
import com.elm.system.service.AuthService;
import com.elm.system.service.LeaveBalanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private AuthService authService;

    @GetMapping("/api/leave-balances/me")
    public ResponseEntity<List<LeaveBalanceResponse>> getMyBalances() {
        Employee employee = authService.getCurrentAuthenticatedEmployee();
        return ResponseEntity.ok(leaveBalanceService.getBalancesForEmployee(employee.getId()));
    }

    @GetMapping("/api/employees/{employeeId}/leave-balances")
    public ResponseEntity<List<LeaveBalanceResponse>> getEmployeeBalances(@PathVariable Long employeeId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            Employee emp = authService.getCurrentAuthenticatedEmployee();
            if (!emp.getId().equals(employeeId) && 
                    (emp.getManager() == null || !emp.getId().equals(emp.getManager().getId()))) {
                // If not admin and not self, check if manager
                // Let service handle access or permit manager
            }
        }
        return ResponseEntity.ok(leaveBalanceService.getBalancesForEmployee(employeeId));
    }

    @PutMapping("/api/employees/{employeeId}/leave-balances/{leaveTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveBalanceResponse> updateBalance(
            @PathVariable Long employeeId,
            @PathVariable Long leaveTypeId,
            @Valid @RequestBody LeaveBalanceUpdateRequest request) {
        return ResponseEntity.ok(leaveBalanceService.updateBalance(employeeId, leaveTypeId, request));
    }
}
