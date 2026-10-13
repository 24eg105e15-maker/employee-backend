package com.elm.system.repository;

import com.elm.system.entity.Employee;
import com.elm.system.entity.LeaveApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByEmployeeOrderBySubmittedAtDesc(Employee employee);
    Page<LeaveApplication> findByEmployeeOrderBySubmittedAtDesc(Employee employee, Pageable pageable);

    List<LeaveApplication> findByStatusOrderBySubmittedAtDesc(String status);
    Page<LeaveApplication> findByStatusOrderBySubmittedAtDesc(String status, Pageable pageable);

    // Manager queries: leave applications from employees reporting to this manager
    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.manager.id = :managerId ORDER BY la.submittedAt DESC")
    List<LeaveApplication> findByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.manager.id = :managerId AND la.status = :status ORDER BY la.submittedAt DESC")
    List<LeaveApplication> findByManagerIdAndStatus(@Param("managerId") Long managerId, @Param("status") String status);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.manager.id = :managerId ORDER BY la.submittedAt DESC")
    Page<LeaveApplication> findByManagerIdPaged(@Param("managerId") Long managerId, Pageable pageable);

    // Overlapping leave check:
    // Any existing application for this employee where status in ('PENDING', 'APPROVED') and dates overlap
    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.id = :employeeId " +
           "AND la.status IN ('PENDING', 'APPROVED') " +
           "AND (:excludeId IS NULL OR la.id <> :excludeId) " +
           "AND (la.startDate <= :endDate AND la.endDate >= :startDate)")
    List<LeaveApplication> findOverlappingLeaves(@Param("employeeId") Long employeeId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("excludeId") Long excludeId);

    // Admin / Manager filtered search
    @Query("SELECT la FROM LeaveApplication la WHERE " +
           "(:status IS NULL OR la.status = :status) AND " +
           "(:deptId IS NULL OR la.employee.department.id = :deptId) AND " +
           "(:leaveTypeId IS NULL OR la.leaveType.id = :leaveTypeId) AND " +
           "(:keyword IS NULL OR LOWER(la.employee.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(la.employee.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY la.submittedAt DESC")
    Page<LeaveApplication> searchLeaves(@Param("status") String status,
                                        @Param("deptId") Long deptId,
                                        @Param("leaveTypeId") Long leaveTypeId,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    // Dashboard Statistics
    long countByStatus(String status);

    @Query("SELECT COUNT(la) FROM LeaveApplication la WHERE la.employee.manager.id = :managerId AND la.status = :status")
    long countByManagerIdAndStatus(@Param("managerId") Long managerId, @Param("status") String status);

    // Currently on leave: status APPROVED and today between startDate and endDate
    @Query("SELECT COUNT(DISTINCT la.employee.id) FROM LeaveApplication la WHERE la.status = 'APPROVED' AND :today BETWEEN la.startDate AND la.endDate")
    long countEmployeesCurrentlyOnLeave(@Param("today") LocalDate today);

    // Currently on leave list
    @Query("SELECT la FROM LeaveApplication la WHERE la.status = 'APPROVED' AND :today BETWEEN la.startDate AND la.endDate")
    List<LeaveApplication> findEmployeesCurrentlyOnLeave(@Param("today") LocalDate today);

    // Upcoming approved leaves within next N days
    @Query("SELECT la FROM LeaveApplication la WHERE la.status = 'APPROVED' AND la.startDate > :today AND la.startDate <= :upcomingLimit ORDER BY la.startDate ASC")
    List<LeaveApplication> findUpcomingApprovedLeaves(@Param("today") LocalDate today, @Param("upcomingLimit") LocalDate upcomingLimit);

    // Leave requests in current month
    @Query("SELECT COUNT(la) FROM LeaveApplication la WHERE la.startDate >= :monthStart AND la.startDate <= :monthEnd")
    long countLeavesInMonth(@Param("monthStart") LocalDate monthStart, @Param("monthEnd") LocalDate monthEnd);

    // Leaves by date range for calendar
    @Query("SELECT la FROM LeaveApplication la WHERE (la.startDate <= :rangeEnd AND la.endDate >= :rangeStart) AND la.status IN ('APPROVED', 'PENDING')")
    List<LeaveApplication> findCalendarLeaves(@Param("rangeStart") LocalDate rangeStart, @Param("rangeEnd") LocalDate rangeEnd);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.manager.id = :managerId AND (la.startDate <= :rangeEnd AND la.endDate >= :rangeStart) AND la.status IN ('APPROVED', 'PENDING')")
    List<LeaveApplication> findTeamCalendarLeaves(@Param("managerId") Long managerId, @Param("rangeStart") LocalDate rangeStart, @Param("rangeEnd") LocalDate rangeEnd);
}
