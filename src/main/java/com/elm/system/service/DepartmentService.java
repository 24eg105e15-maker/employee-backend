package com.elm.system.service;

import com.elm.system.dto.DepartmentRequest;
import com.elm.system.dto.DepartmentResponse;
import com.elm.system.entity.Department;
import com.elm.system.entity.Employee;
import com.elm.system.exception.BadRequestException;
import com.elm.system.exception.ResourceNotFoundException;
import com.elm.system.repository.DepartmentRepository;
import com.elm.system.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToResponse(dept);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByDepartmentName(request.getDepartmentName())) {
            throw new BadRequestException("Department name already exists: " + request.getDepartmentName());
        }

        Department dept = new Department();
        dept.setDepartmentName(request.getDepartmentName());
        dept.setDescription(request.getDescription());
        dept.setManagerId(request.getManagerId());

        dept = departmentRepository.save(dept);
        return mapToResponse(dept);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (!dept.getDepartmentName().equalsIgnoreCase(request.getDepartmentName()) &&
                departmentRepository.existsByDepartmentName(request.getDepartmentName())) {
            throw new BadRequestException("Department name already exists: " + request.getDepartmentName());
        }

        dept.setDepartmentName(request.getDepartmentName());
        dept.setDescription(request.getDescription());
        dept.setManagerId(request.getManagerId());

        dept = departmentRepository.save(dept);
        return mapToResponse(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        long employeeCount = employeeRepository.countByDepartment(dept);
        if (employeeCount > 0) {
            throw new BadRequestException("Cannot delete department with " + employeeCount + 
                    " active employees. Please reassign the employees to another department first.");
        }

        departmentRepository.delete(dept);
    }

    private DepartmentResponse mapToResponse(Department dept) {
        DepartmentResponse dto = new DepartmentResponse();
        dto.setId(dept.getId());
        dto.setDepartmentName(dept.getDepartmentName());
        dto.setDescription(dept.getDescription());
        dto.setManagerId(dept.getManagerId());
        if (dept.getManagerId() != null) {
            employeeRepository.findById(dept.getManagerId())
                    .ifPresent(m -> dto.setManagerName(m.getFullName()));
        }
        dto.setEmployeeCount(employeeRepository.countByDepartment(dept));
        return dto;
    }
}
