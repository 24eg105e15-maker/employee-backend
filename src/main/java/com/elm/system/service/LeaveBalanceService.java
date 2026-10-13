package com.elm.system.service;

import com.elm.system.dto.LeaveBalanceResponse;
import com.elm.system.dto.LeaveBalanceUpdateRequest;
import com.elm.system.entity.Employee;
import com.elm.system.entity.LeaveBalance;
import com.elm.system.entity.LeaveType;
import com.elm.system.exception.BadRequestException;
import com.elm.system.exception.ResourceNotFoundException;
import com.elm.system.repository.EmployeeRepository;
import com.elm.system.repository.LeaveBalanceRepository;
import com.elm.system.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveBalanceService {

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private String getCurrentPeriod() {
        return String.valueOf(LocalDate.now().getYear());
    }

    @Transactional
    public void initializeBalancesForEmployee(Employee employee) {
        String period = getCurrentPeriod();
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();

        for (LeaveType type : leaveTypes) {
            if (leaveBalanceRepository.findByEmployeeAndLeaveTypeAndAllocationPeriod(employee, type, period).isEmpty()) {
                LeaveBalance balance = new LeaveBalance();
                balance.setEmployee(employee);
                balance.setLeaveType(type);
                balance.setAllocationPeriod(period);
                balance.setAllocatedDays(Double.valueOf(type.getAnnualAllocation()));
                balance.setUsedDays(0.0);
                balance.setCarriedForwardDays(0.0);
                balance.setVersion(0L);
                leaveBalanceRepository.save(balance);
            }
        }
    }

    public List<LeaveBalanceResponse> getBalancesForEmployee(Long employeeId) {
        String period = getCurrentPeriod();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // Ensure balances exist for current period
        initializeBalancesForEmployee(employee);

        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeIdAndAllocationPeriod(employeeId, period);
        return balances.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public LeaveBalance getBalanceEntity(Long employeeId, Long leaveTypeId) {
        String period = getCurrentPeriod();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        LeaveType type = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + leaveTypeId));

        return leaveBalanceRepository.findByEmployeeAndLeaveTypeAndAllocationPeriod(employee, type, period)
                .orElseGet(() -> {
                    LeaveBalance b = new LeaveBalance();
                    b.setEmployee(employee);
                    b.setLeaveType(type);
                    b.setAllocationPeriod(period);
                    b.setAllocatedDays(Double.valueOf(type.getAnnualAllocation()));
                    b.setUsedDays(0.0);
                    b.setCarriedForwardDays(0.0);
                    b.setVersion(0L);
                    return leaveBalanceRepository.save(b);
                });
    }

    @Transactional
    public void deductLeaveDays(Long employeeId, Long leaveTypeId, Double days) {
        LeaveBalance balance = getBalanceEntity(employeeId, leaveTypeId);
        double currentUsed = balance.getUsedDays() != null ? balance.getUsedDays() : 0.0;
        balance.setUsedDays(currentUsed + days);
        leaveBalanceRepository.save(balance);
    }

    @Transactional
    public void restoreLeaveDays(Long employeeId, Long leaveTypeId, Double days) {
        LeaveBalance balance = getBalanceEntity(employeeId, leaveTypeId);
        double currentUsed = balance.getUsedDays() != null ? balance.getUsedDays() : 0.0;
        double newUsed = Math.max(0.0, currentUsed - days);
        balance.setUsedDays(newUsed);
        leaveBalanceRepository.save(balance);
    }

    @Transactional
    public LeaveBalanceResponse updateBalance(Long employeeId, Long leaveTypeId, LeaveBalanceUpdateRequest request) {
        LeaveBalance balance = getBalanceEntity(employeeId, leaveTypeId);

        if (request.getAllocatedDays() != null) {
            if (request.getAllocatedDays() < 0) {
                throw new BadRequestException("Allocated days cannot be negative");
            }
            balance.setAllocatedDays(request.getAllocatedDays());
        }
        if (request.getUsedDays() != null) {
            if (request.getUsedDays() < 0) {
                throw new BadRequestException("Used days cannot be negative");
            }
            balance.setUsedDays(request.getUsedDays());
        }
        if (request.getCarriedForwardDays() != null) {
            if (request.getCarriedForwardDays() < 0) {
                throw new BadRequestException("Carried forward days cannot be negative");
            }
            balance.setCarriedForwardDays(request.getCarriedForwardDays());
        }

        balance = leaveBalanceRepository.save(balance);
        return mapToResponse(balance);
    }

    private LeaveBalanceResponse mapToResponse(LeaveBalance b) {
        LeaveBalanceResponse dto = new LeaveBalanceResponse();
        dto.setId(b.getId());
        dto.setEmployeeId(b.getEmployee().getId());
        dto.setEmployeeName(b.getEmployee().getFullName());
        dto.setEmployeeCode(b.getEmployee().getEmployeeCode());
        dto.setLeaveTypeId(b.getLeaveType().getId());
        dto.setLeaveTypeName(b.getLeaveType().getName());
        dto.setAllocationPeriod(b.getAllocationPeriod());
        dto.setAllocatedDays(b.getAllocatedDays());
        dto.setUsedDays(b.getUsedDays());
        dto.setCarriedForwardDays(b.getCarriedForwardDays());
        dto.setRemainingDays(b.getRemainingDays());
        return dto;
    }
}
