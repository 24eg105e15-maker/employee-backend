package com.elm.system.controller;

import com.elm.system.dto.LeavePolicyRequest;
import com.elm.system.dto.LeaveTypeRequest;
import com.elm.system.entity.LeavePolicy;
import com.elm.system.entity.LeaveType;
import com.elm.system.service.LeavePolicyService;
import com.elm.system.service.LeaveTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class LeaveTypeController {

    @Autowired
    private LeaveTypeService leaveTypeService;

    @Autowired
    private LeavePolicyService leavePolicyService;

    // Leave Types
    @GetMapping("/api/leave-types")
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes() {
        return ResponseEntity.ok(leaveTypeService.getAllLeaveTypes());
    }

    @GetMapping("/api/leave-types/active")
    public ResponseEntity<List<LeaveType>> getActiveLeaveTypes() {
        return ResponseEntity.ok(leaveTypeService.getActiveLeaveTypes());
    }

    @GetMapping("/api/leave-types/{id}")
    public ResponseEntity<LeaveType> getLeaveTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveTypeService.getLeaveTypeById(id));
    }

    @PostMapping("/api/leave-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveType> createLeaveType(@Valid @RequestBody LeaveTypeRequest request) {
        return new ResponseEntity<>(leaveTypeService.createLeaveType(request), HttpStatus.CREATED);
    }

    @PutMapping("/api/leave-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveType> updateLeaveType(
            @PathVariable Long id, @Valid @RequestBody LeaveTypeRequest request) {
        return ResponseEntity.ok(leaveTypeService.updateLeaveType(id, request));
    }

    // Leave Policies
    @GetMapping("/api/leave-policies")
    public ResponseEntity<List<LeavePolicy>> getAllPolicies() {
        return ResponseEntity.ok(leavePolicyService.getAllPolicies());
    }

    @GetMapping("/api/leave-policies/{leaveTypeId}")
    public ResponseEntity<LeavePolicy> getPolicyByLeaveTypeId(@PathVariable Long leaveTypeId) {
        return ResponseEntity.ok(leavePolicyService.getPolicyByLeaveTypeId(leaveTypeId));
    }

    @PostMapping("/api/leave-policies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeavePolicy> createPolicy(@Valid @RequestBody LeavePolicyRequest request) {
        return new ResponseEntity<>(leavePolicyService.createOrUpdatePolicy(request), HttpStatus.CREATED);
    }

    @PutMapping("/api/leave-policies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeavePolicy> updatePolicy(
            @PathVariable Long id, @Valid @RequestBody LeavePolicyRequest request) {
        return ResponseEntity.ok(leavePolicyService.createOrUpdatePolicy(request));
    }
}
