package com.elm.system.repository;

import com.elm.system.entity.LeavePolicy;
import com.elm.system.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {
    Optional<LeavePolicy> findByLeaveType(LeaveType leaveType);
    Optional<LeavePolicy> findByLeaveTypeId(Long leaveTypeId);
}
