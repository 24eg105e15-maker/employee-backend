package com.elm.system.dto;

import jakarta.validation.constraints.NotBlank;

public class DepartmentRequest {
    @NotBlank(message = "Department name is required")
    private String departmentName;

    private String description;
    private Long managerId;

    public DepartmentRequest() {}

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
}
