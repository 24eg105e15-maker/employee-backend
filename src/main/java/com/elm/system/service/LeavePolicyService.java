package com.elm.system.service;

import com.elm.system.dto.LeavePolicyRequest;
import com.elm.system.entity.LeavePolicy;
import com.elm.system.entity.LeaveType;
import com.elm.system.exception.BadRequestException;
import com.elm.system.exception.ResourceNotFoundException;
import com.elm.system.repository.LeavePolicyRepository;
import com.elm.system.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeavePolicyService {

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    public List<LeavePolicy> getAllPolicies() {
        return leavePolicyRepository.findAll();
    }

    public LeavePolicy getPolicyByLeaveTypeId(Long leaveTypeId) {
        return leavePolicyRepository.findByLeaveTypeId(leaveTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found for leave type id: " + leaveTypeId));
    }

    @Transactional
    public LeavePolicy createOrUpdatePolicy(LeavePolicyRequest request) {
        LeaveType type = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + request.getLeaveTypeId()));

        LeavePolicy policy = leavePolicyRepository.findByLeaveType(type)
                .orElseGet(() -> {
                    LeavePolicy p = new LeavePolicy();
                    p.setLeaveType(type);
                    return p;
                });

        policy.setAllocationPeriod(request.getAllocationPeriod() != null ? request.getAllocationPeriod() : "ANNUAL");
        policy.setCarryForwardAllowed(request.isCarryForwardAllowed());
        policy.setMaximumCarryForward(request.getMaximumCarryForward() != null ? request.getMaximumCarryForward() : 0);
        policy.setApprovalRequired(request.isApprovalRequired());
        policy.setActive(request.isActive());

        return leavePolicyRepository.save(policy);
    }
}
