package com.elm.system.repository;

import com.elm.system.entity.LeaveApplication;
import com.elm.system.entity.LeaveStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveStatusHistoryRepository extends JpaRepository<LeaveStatusHistory, Long> {
    List<LeaveStatusHistory> findByLeaveApplicationOrderByChangedAtDesc(LeaveApplication leaveApplication);
    List<LeaveStatusHistory> findByLeaveApplicationIdOrderByChangedAtDesc(Long leaveApplicationId);
}
