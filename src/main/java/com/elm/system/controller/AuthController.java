package com.elm.system.controller;

import com.elm.system.dto.AuthResponse;
import com.elm.system.dto.LoginRequest;
import com.elm.system.dto.RegisterRequest;
import com.elm.system.dto.EmployeeResponse;
import com.elm.system.entity.Employee;
import com.elm.system.entity.User;
import com.elm.system.service.AuthService;
import com.elm.system.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        User user = authService.getCurrentAuthenticatedUser();
        Employee employee = null;
        try {
            employee = authService.getCurrentAuthenticatedEmployee();
        } catch (Exception ignored) {}

        Map<String, Object> map = new java.util.HashMap<>();
        map.put("userId", user.getId());
        map.put("email", user.getEmail());
        map.put("role", user.getRole());
        map.put("enabled", user.isEnabled());

        if (employee != null) {
            map.put("employeeId", employee.getId());
            map.put("employeeCode", employee.getEmployeeCode());
            map.put("fullName", employee.getFullName());
            map.put("designation", employee.getDesignation());
            map.put("phone", employee.getPhone());
            if (employee.getDepartment() != null) {
                map.put("departmentId", employee.getDepartment().getId());
                map.put("departmentName", employee.getDepartment().getDepartmentName());
            }
        }

        return ResponseEntity.ok(map);
    }
}
