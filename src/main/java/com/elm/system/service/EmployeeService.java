package com.elm.system.service;

import com.elm.system.dto.EmployeeRequest;
import com.elm.system.dto.EmployeeResponse;
import com.elm.system.entity.Department;
import com.elm.system.entity.Employee;
import com.elm.system.entity.User;
import com.elm.system.exception.BadRequestException;
import com.elm.system.exception.ResourceNotFoundException;
import com.elm.system.repository.DepartmentRepository;
import com.elm.system.repository.EmployeeRepository;
import com.elm.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }

        String employeeCode = request.getEmployeeCode();
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            employeeCode = "EMP-" + (1000 + employeeRepository.count() + 1);
        } else if (employeeRepository.existsByEmployeeCode(employeeCode)) {
            throw new BadRequestException("Employee code already in use: " + employeeCode);
        }

        // Create User
        User user = new User();
        user.setEmail(request.getEmail());
        String rawPassword = (request.getPassword() != null && !request.getPassword().trim().isEmpty()) 
                ? request.getPassword() : "admin123";
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(request.getRole() != null ? request.getRole() : "ROLE_EMPLOYEE");
        user.setEnabled(true);
        user = userRepository.save(user);

        // Create Employee
        Employee employee = new Employee();
        employee.setEmployeeCode(employeeCode);
        employee.setUser(user);
        employee.setFullName(request.getFullName());
        employee.setPhone(request.getPhone());
        employee.setDesignation(request.getDesignation());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setEmploymentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : "ACTIVE");

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
            employee.setDepartment(dept);
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
            employee.setManager(manager);
        }

        employee = employeeRepository.save(employee);

        // Initialize balances
        leaveBalanceService.initializeBalancesForEmployee(employee);

        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setFullName(request.getFullName());
        employee.setPhone(request.getPhone());
        employee.setDesignation(request.getDesignation());
        if (request.getJoiningDate() != null) {
            employee.setJoiningDate(request.getJoiningDate());
        }
        if (request.getEmploymentStatus() != null) {
            employee.setEmploymentStatus(request.getEmploymentStatus());
        }

        // Update User role/password if provided
        User user = employee.getUser();
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
            employee.setDepartment(dept);
        } else {
            employee.setDepartment(null);
        }

        if (request.getManagerId() != null) {
            if (request.getManagerId().equals(id)) {
                throw new BadRequestException("An employee cannot be their own manager");
            }
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
            employee.setManager(manager);
        } else {
            employee.setManager(null);
        }

        employee = employeeRepository.save(employee);
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmploymentStatus(Long id, String status) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setEmploymentStatus(status);
        if ("INACTIVE".equalsIgnoreCase(status) || "TERMINATED".equalsIgnoreCase(status)) {
            employee.getUser().setEnabled(false);
            userRepository.save(employee.getUser());
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            employee.getUser().setEnabled(true);
            userRepository.save(employee.getUser());
        }
        employee = employeeRepository.save(employee);
        return mapToResponse(employee);
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToResponse(employee);
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<EmployeeResponse> searchEmployees(String keyword, Long deptId, String status, Pageable pageable) {
        return employeeRepository.searchEmployees(keyword, deptId, status, pageable)
                .map(this::mapToResponse);
    }

    public List<EmployeeResponse> getTeamMembers(Long managerId) {
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));
        return employeeRepository.findByManager(manager).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EmployeeResponse mapToResponse(Employee e) {
        EmployeeResponse dto = new EmployeeResponse();
        dto.setId(e.getId());
        dto.setEmployeeCode(e.getEmployeeCode());
        if (e.getUser() != null) {
            dto.setUserId(e.getUser().getId());
            dto.setEmail(e.getUser().getEmail());
            dto.setRole(e.getUser().getRole());
        }
        dto.setFullName(e.getFullName());
        dto.setPhone(e.getPhone());
        if (e.getDepartment() != null) {
            dto.setDepartmentId(e.getDepartment().getId());
            dto.setDepartmentName(e.getDepartment().getDepartmentName());
        }
        if (e.getManager() != null) {
            dto.setManagerId(e.getManager().getId());
            dto.setManagerName(e.getManager().getFullName());
        }
        dto.setDesignation(e.getDesignation());
        dto.setJoiningDate(e.getJoiningDate());
        dto.setEmploymentStatus(e.getEmploymentStatus());
        return dto;
    }
}
