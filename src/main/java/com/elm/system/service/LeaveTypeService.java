package com.elm.system.service;

import com.elm.system.dto.LeaveTypeRequest;
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
public class LeaveTypeService {

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    public List<LeaveType> getActiveLeaveTypes() {
        return leaveTypeRepository.findByActiveTrue();
    }

    public LeaveType getLeaveTypeById(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + id));
    }

    @Transactional
    public LeaveType createLeaveType(LeaveTypeRequest request) {
        if (leaveTypeRepository.existsByName(request.getName())) {
            throw new BadRequestException("Leave type already exists: " + request.getName());
        }

        LeaveType type = new LeaveType();
        type.setName(request.getName());
        type.setDescription(request.getDescription());
        type.setAnnualAllocation(request.getAnnualAllocation());
        type.setPaid(request.isPaid());
        type.setActive(request.isActive());

        type = leaveTypeRepository.save(type);

        // Auto-create a default policy for this leave type if none exists
        LeavePolicy policy = new LeavePolicy();
        policy.setLeaveType(type);
        policy.setAllocationPeriod("ANNUAL");
        policy.setCarryForwardAllowed(false);
        policy.setMaximumCarryForward(0);
        policy.setApprovalRequired(true);
        policy.setActive(true);
        leavePolicyRepository.save(policy);

        return type;
    }

    @Transactional
    public LeaveType updateLeaveType(Long id, LeaveTypeRequest request) {
        LeaveType type = getLeaveTypeById(id);

        if (!type.getName().equalsIgnoreCase(request.getName()) &&
                leaveTypeRepository.existsByName(request.getName())) {
            throw new BadRequestException("Leave type already exists: " + request.getName());
        }

        type.setName(request.getName());
        type.setDescription(request.getDescription());
        type.setAnnualAllocation(request.getAnnualAllocation());
        type.setPaid(request.isPaid());
        type.setActive(request.isActive());

        return leaveTypeRepository.save(type);
    }
}
