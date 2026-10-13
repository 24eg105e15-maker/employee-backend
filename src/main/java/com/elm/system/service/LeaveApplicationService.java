package com.elm.system.service;

import com.elm.system.dto.LeaveApplicationRequest;
import com.elm.system.dto.LeaveApplicationResponse;
import com.elm.system.dto.LeaveApprovalRequest;
import com.elm.system.entity.*;
import com.elm.system.exception.BadRequestException;
import com.elm.system.exception.ResourceNotFoundException;
import com.elm.system.exception.UnauthorizedException;
import com.elm.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveApplicationService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private LeaveStatusHistoryRepository leaveStatusHistoryRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuthService authService;

    public double calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        double workingDays = 0.0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            // Exclude Saturday and Sunday
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays += 1.0;
            }
            current = current.plusDays(1);
        }

        // If a request is strictly on weekends, count at least actual days or 1.0
        if (workingDays == 0.0) {
            workingDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }

        return workingDays;
    }

    @Transactional
    public LeaveApplicationResponse applyForLeave(LeaveApplicationRequest request) {
        Employee employee = authService.getCurrentAuthenticatedEmployee();

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must not be after end date");
        }

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + request.getLeaveTypeId()));

        if (!leaveType.isActive()) {
            throw new BadRequestException("Selected leave type is currently inactive");
        }

        // Calculate requested working days
        double requestedDays = calculateLeaveDays(request.getStartDate(), request.getEndDate());
        if (requestedDays <= 0) {
            throw new BadRequestException("Requested leave duration must be at least 1 day");
        }

        // Check for overlapping pending or approved applications
        List<LeaveApplication> overlapping = leaveApplicationRepository.findOverlappingLeaves(
                employee.getId(), request.getStartDate(), request.getEndDate(), null);
        if (!overlapping.isEmpty()) {
            LeaveApplication firstConflict = overlapping.get(0);
            throw new BadRequestException("You already have a " + firstConflict.getStatus() + 
                    " leave application overlapping this date range (" + 
                    firstConflict.getStartDate() + " to " + firstConflict.getEndDate() + ")");
        }

        // Check leave balance if it's a paid leave type
        if (leaveType.isPaid()) {
            LeaveBalance balance = leaveBalanceService.getBalanceEntity(employee.getId(), leaveType.getId());
            double remaining = balance.getRemainingDays();
            if (remaining < requestedDays) {
                throw new BadRequestException("Insufficient leave balance for " + leaveType.getName() + 
                        ". Requested: " + requestedDays + " days, Available: " + remaining + " days");
            }
        }

        // Create Leave Application
        LeaveApplication application = new LeaveApplication();
        application.setEmployee(employee);
        application.setLeaveType(leaveType);
        application.setStartDate(request.getStartDate());
        application.setEndDate(request.getEndDate());
        application.setLeaveDays(requestedDays);
        application.setReason(request.getReason());
        application.setStatus("PENDING");
        application.setSubmittedAt(LocalDateTime.now());

        application = leaveApplicationRepository.save(application);

        // Record Initial Status History
        LeaveStatusHistory history = new LeaveStatusHistory(
                application, null, "PENDING", employee.getUser().getEmail(), "Application submitted by employee");
        leaveStatusHistoryRepository.save(history);

        return mapToResponse(application);
    }

    public List<LeaveApplicationResponse> getMyApplications() {
        Employee employee = authService.getCurrentAuthenticatedEmployee();
        return leaveApplicationRepository.findByEmployeeOrderBySubmittedAtDesc(employee).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LeaveApplicationResponse getApplicationById(Long id) {
        LeaveApplication app = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found with id: " + id));

        User currentUser = authService.getCurrentAuthenticatedUser();
        // Access check: only owner, their manager, or Admin can view
        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            Employee currentEmployee = authService.getCurrentAuthenticatedEmployee();
            boolean isOwner = app.getEmployee().getId().equals(currentEmployee.getId());
            boolean isManager = app.getEmployee().getManager() != null && 
                    app.getEmployee().getManager().getId().equals(currentEmployee.getId());
            if (!isOwner && !isManager) {
                throw new UnauthorizedException("You are not authorized to view this leave application");
            }
        }

        return mapToResponse(app);
    }

    @Transactional
    public LeaveApplicationResponse cancelApplication(Long id) {
        Employee currentEmployee = authService.getCurrentAuthenticatedEmployee();
        LeaveApplication app = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found with id: " + id));

        if (!app.getEmployee().getId().equals(currentEmployee.getId())) {
            throw new UnauthorizedException("You can only cancel your own leave applications");
        }

        String currentStatus = app.getStatus();
        if ("CANCELLED".equals(currentStatus) || "REJECTED".equals(currentStatus)) {
            throw new BadRequestException("Application is already " + currentStatus.toLowerCase());
        }

        if ("APPROVED".equals(currentStatus)) {
            // If approved leave was already in the past, disallow cancellation
            if (app.getStartDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("Cannot cancel an approved leave that has already commenced or passed");
            }
            // Restore deducted balance
            if (app.getLeaveType().isPaid()) {
                leaveBalanceService.restoreLeaveDays(app.getEmployee().getId(), app.getLeaveType().getId(), app.getLeaveDays());
            }
        }

        app.setStatus("CANCELLED");
        app = leaveApplicationRepository.save(app);

        // Record history
        LeaveStatusHistory history = new LeaveStatusHistory(
                app, currentStatus, "CANCELLED", currentEmployee.getUser().getEmail(), "Cancelled by employee");
        leaveStatusHistoryRepository.save(history);

        return mapToResponse(app);
    }

    @Transactional
    public LeaveApplicationResponse approveLeave(Long id, LeaveApprovalRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        LeaveApplication app = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found with id: " + id));

        if (!"PENDING".equals(app.getStatus())) {
            throw new BadRequestException("Leave application is not in PENDING state. Current state: " + app.getStatus());
        }

        Employee currentEmployee = null;
        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            currentEmployee = authService.getCurrentAuthenticatedEmployee();

            // Self-approval check
            if (app.getEmployee().getId().equals(currentEmployee.getId())) {
                throw new BadRequestException("Employees cannot approve their own leave requests");
            }

            // Manager authorization check
            boolean isAssignedManager = app.getEmployee().getManager() != null &&
                    app.getEmployee().getManager().getId().equals(currentEmployee.getId());
            if (!isAssignedManager) {
                throw new UnauthorizedException("You are not authorized to approve leave requests for this employee");
            }
        } else {
            // Admin can approve; try to resolve admin's employee profile if available
            currentEmployee = employeeRepository.findByUser(currentUser).orElse(null);
        }

        // Deduct balance atomically if paid leave
        if (app.getLeaveType().isPaid()) {
            leaveBalanceService.deductLeaveDays(app.getEmployee().getId(), app.getLeaveType().getId(), app.getLeaveDays());
        }

        app.setStatus("APPROVED");
        app.setDecisionAt(LocalDateTime.now());
        app.setApprover(currentEmployee);
        app.setApproverComments(request != null ? request.getComments() : "Approved");

        app = leaveApplicationRepository.save(app);

        // Record history
        String approverEmail = currentUser.getEmail();
        LeaveStatusHistory history = new LeaveStatusHistory(
                app, "PENDING", "APPROVED", approverEmail, 
                (request != null && request.getComments() != null) ? request.getComments() : "Approved");
        leaveStatusHistoryRepository.save(history);

        return mapToResponse(app);
    }

    @Transactional
    public LeaveApplicationResponse rejectLeave(Long id, LeaveApprovalRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        LeaveApplication app = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found with id: " + id));

        if (!"PENDING".equals(app.getStatus())) {
            throw new BadRequestException("Leave application is not in PENDING state. Current state: " + app.getStatus());
        }

        Employee currentEmployee = null;
        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            currentEmployee = authService.getCurrentAuthenticatedEmployee();

            if (app.getEmployee().getId().equals(currentEmployee.getId())) {
                throw new BadRequestException("Employees cannot review their own leave requests");
            }

            boolean isAssignedManager = app.getEmployee().getManager() != null &&
                    app.getEmployee().getManager().getId().equals(currentEmployee.getId());
            if (!isAssignedManager) {
                throw new UnauthorizedException("You are not authorized to reject leave requests for this employee");
            }
        } else {
            currentEmployee = employeeRepository.findByUser(currentUser).orElse(null);
        }

        app.setStatus("REJECTED");
        app.setDecisionAt(LocalDateTime.now());
        app.setApprover(currentEmployee);
        app.setApproverComments(request != null ? request.getComments() : "Rejected");

        app = leaveApplicationRepository.save(app);

        // Record history
        String approverEmail = currentUser.getEmail();
        LeaveStatusHistory history = new LeaveStatusHistory(
                app, "PENDING", "REJECTED", approverEmail, 
                (request != null && request.getComments() != null) ? request.getComments() : "Rejected");
        leaveStatusHistoryRepository.save(history);

        return mapToResponse(app);
    }

    public List<LeaveApplicationResponse> getPendingLeavesForApprover() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return leaveApplicationRepository.findByStatusOrderBySubmittedAtDesc("PENDING").stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else {
            Employee currentEmployee = authService.getCurrentAuthenticatedEmployee();
            return leaveApplicationRepository.findByManagerIdAndStatus(currentEmployee.getId(), "PENDING").stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
    }

    public List<LeaveApplicationResponse> getTeamLeaves(Long managerId) {
        return leaveApplicationRepository.findByManagerId(managerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<LeaveApplicationResponse> searchLeaves(String status, Long deptId, Long leaveTypeId, String keyword, Pageable pageable) {
        return leaveApplicationRepository.searchLeaves(status, deptId, leaveTypeId, keyword, pageable)
                .map(this::mapToResponse);
    }

    public List<LeaveApplicationResponse> getCalendarLeaves(LocalDate start, LocalDate end) {
        return leaveApplicationRepository.findCalendarLeaves(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveApplicationResponse> getTeamCalendarLeaves(Long managerId, LocalDate start, LocalDate end) {
        return leaveApplicationRepository.findTeamCalendarLeaves(managerId, start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveStatusHistory> getStatusHistory(Long applicationId) {
        return leaveStatusHistoryRepository.findByLeaveApplicationIdOrderByChangedAtDesc(applicationId);
    }

    public LeaveApplicationResponse mapToResponse(LeaveApplication app) {
        LeaveApplicationResponse dto = new LeaveApplicationResponse();
        dto.setId(app.getId());
        dto.setEmployeeId(app.getEmployee().getId());
        dto.setEmployeeName(app.getEmployee().getFullName());
        dto.setEmployeeCode(app.getEmployee().getEmployeeCode());
        if (app.getEmployee().getDepartment() != null) {
            dto.setDepartmentName(app.getEmployee().getDepartment().getDepartmentName());
        }
        dto.setLeaveTypeId(app.getLeaveType().getId());
        dto.setLeaveTypeName(app.getLeaveType().getName());
        dto.setStartDate(app.getStartDate());
        dto.setEndDate(app.getEndDate());
        dto.setLeaveDays(app.getLeaveDays());
        dto.setReason(app.getReason());
        dto.setStatus(app.getStatus());
        dto.setSubmittedAt(app.getSubmittedAt());
        dto.setDecisionAt(app.getDecisionAt());
        if (app.getApprover() != null) {
            dto.setApproverId(app.getApprover().getId());
            dto.setApproverName(app.getApprover().getFullName());
        }
        dto.setApproverComments(app.getApproverComments());
        return dto;
    }
}
