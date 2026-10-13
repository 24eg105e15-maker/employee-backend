package com.elm.system.service;

import com.elm.system.dto.*;
import com.elm.system.entity.Department;
import com.elm.system.entity.Employee;
import com.elm.system.entity.User;
import com.elm.system.exception.BadRequestException;
import com.elm.system.exception.ResourceNotFoundException;
import com.elm.system.repository.DepartmentRepository;
import com.elm.system.repository.EmployeeRepository;
import com.elm.system.repository.UserRepository;
import com.elm.system.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Employee employee = employeeRepository.findByUser(user).orElse(null);

        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        if (employee != null) {
            response.setEmployeeId(employee.getId());
            response.setEmployeeCode(employee.getEmployeeCode());
            response.setFullName(employee.getFullName());
            response.setDesignation(employee.getDesignation());
            if (employee.getDepartment() != null) {
                response.setDepartmentName(employee.getDepartment().getDepartmentName());
            }
        } else {
            response.setFullName(user.getEmail());
        }

        return response;
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already registered: " + registerRequest.getEmail());
        }

        // Create User
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole("ROLE_EMPLOYEE");
        user.setEnabled(true);
        user = userRepository.save(user);

        // Generate unique employee code
        String code = "EMP-" + (1000 + employeeRepository.count() + 1);

        // Create Employee Profile
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setUser(user);
        employee.setFullName(registerRequest.getFullName());
        employee.setPhone(registerRequest.getPhone());
        employee.setDesignation(registerRequest.getDesignation() != null ? registerRequest.getDesignation() : "Staff Member");
        employee.setJoiningDate(LocalDate.now());
        employee.setEmploymentStatus("ACTIVE");

        if (registerRequest.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(registerRequest.getDepartmentId())
                    .orElse(null);
            employee.setDepartment(dept);
        }

        employee = employeeRepository.save(employee);

        // Initialize default leave balances for the new employee
        leaveBalanceService.initializeBalancesForEmployee(employee);

        // Authenticate & generate token
        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());

        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setEmployeeId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFullName(employee.getFullName());
        response.setDesignation(employee.getDesignation());
        if (employee.getDepartment() != null) {
            response.setDepartmentName(employee.getDepartment().getDepartmentName());
        }

        return response;
    }

    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new BadRequestException("No authenticated user found");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database"));
    }

    public Employee getCurrentAuthenticatedEmployee() {
        User user = getCurrentAuthenticatedUser();
        return employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile found for user: " + user.getEmail()));
    }
}
