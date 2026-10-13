package com.elm.system.dto;

import jakarta.validation.constraints.NotBlank;

public class EmployeeStatusUpdateRequest {
    @NotBlank(message = "Employment status is required")
    private String status; // ACTIVE, INACTIVE, TERMINATED

    public EmployeeStatusUpdateRequest() {}

    public EmployeeStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
