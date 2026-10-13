package com.elm.system.repository;

import com.elm.system.entity.Department;
import com.elm.system.entity.Employee;
import com.elm.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByUser(User user);
    Optional<Employee> findByUserEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);

    List<Employee> findByManager(Employee manager);
    List<Employee> findByDepartment(Department department);
    long countByDepartment(Department department);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:deptId IS NULL OR e.department.id = :deptId) AND " +
           "(:status IS NULL OR e.employmentStatus = :status) AND " +
           "(:keyword IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> searchEmployees(@Param("keyword") String keyword,
                                   @Param("deptId") Long deptId,
                                   @Param("status") String status,
                                   Pageable pageable);
}
