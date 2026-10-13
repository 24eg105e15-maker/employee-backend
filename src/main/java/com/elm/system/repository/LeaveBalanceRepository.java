package com.elm.system.repository;

import com.elm.system.entity.Employee;
import com.elm.system.entity.LeaveBalance;
import com.elm.system.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployee(Employee employee);
    List<LeaveBalance> findByEmployeeId(Long employeeId);
    List<LeaveBalance> findByEmployeeIdAndAllocationPeriod(Long employeeId, String allocationPeriod);
    Optional<LeaveBalance> findByEmployeeAndLeaveTypeAndAllocationPeriod(Employee employee, LeaveType leaveType, String allocationPeriod);
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndAllocationPeriod(Long employeeId, Long leaveTypeId, String allocationPeriod);
}
