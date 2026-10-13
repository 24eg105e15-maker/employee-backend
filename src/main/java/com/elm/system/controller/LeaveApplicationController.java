package com.elm.system.controller;

import com.elm.system.dto.LeaveApplicationRequest;
import com.elm.system.dto.LeaveApplicationResponse;
import com.elm.system.dto.LeaveApprovalRequest;
import com.elm.system.entity.Employee;
import com.elm.system.entity.LeaveStatusHistory;
import com.elm.system.service.AuthService;
import com.elm.system.service.LeaveApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveApplicationController {

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<LeaveApplicationResponse> applyForLeave(
            @Valid @RequestBody LeaveApplicationRequest request) {
        return new ResponseEntity<>(leaveApplicationService.applyForLeave(request), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(leaveApplicationService.getMyApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveApplicationResponse> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveApplicationService.getApplicationById(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<LeaveApplicationResponse> cancelApplication(@PathVariable Long id) {
        return ResponseEntity.ok(leaveApplicationService.cancelApplication(id));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<LeaveApplicationResponse>> getPendingLeaves() {
        return ResponseEntity.ok(leaveApplicationService.getPendingLeavesForApprover());
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<LeaveApplicationResponse>> getTeamLeaves() {
        Employee manager = authService.getCurrentAuthenticatedEmployee();
        return ResponseEntity.ok(leaveApplicationService.getTeamLeaves(manager.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<LeaveApplicationResponse>> searchAllLeaves(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<LeaveApplicationResponse> leaves = leaveApplicationService.searchLeaves(
                status, departmentId, leaveTypeId, keyword, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(leaves);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveApplicationResponse> approveLeave(
            @PathVariable Long id, @RequestBody(required = false) LeaveApprovalRequest request) {
        return ResponseEntity.ok(leaveApplicationService.approveLeave(id, request));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveApplicationResponse> rejectLeave(
            @PathVariable Long id, @RequestBody(required = false) LeaveApprovalRequest request) {
        return ResponseEntity.ok(leaveApplicationService.rejectLeave(id, request));
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<LeaveApplicationResponse>> getCalendarLeaves(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (start == null) start = LocalDate.now().minusMonths(1);
        if (end == null) end = LocalDate.now().plusMonths(2);
        return ResponseEntity.ok(leaveApplicationService.getCalendarLeaves(start, end));
    }

    @GetMapping("/calendar/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<LeaveApplicationResponse>> getTeamCalendarLeaves(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        Employee manager = authService.getCurrentAuthenticatedEmployee();
        if (start == null) start = LocalDate.now().minusMonths(1);
        if (end == null) end = LocalDate.now().plusMonths(2);
        return ResponseEntity.ok(leaveApplicationService.getTeamCalendarLeaves(manager.getId(), start, end));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<LeaveStatusHistory>> getStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(leaveApplicationService.getStatusHistory(id));
    }
}
