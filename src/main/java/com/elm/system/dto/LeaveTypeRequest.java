package com.elm.system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LeaveTypeRequest {
    @NotBlank(message = "Leave type name is required")
    private String name;

    private String description;

    @NotNull(message = "Annual allocation is required")
    @Min(value = 0, message = "Annual allocation cannot be negative")
    private Integer annualAllocation;

    private boolean paid = true;
    private boolean active = true;

    public LeaveTypeRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getAnnualAllocation() { return annualAllocation; }
    public void setAnnualAllocation(Integer annualAllocation) { this.annualAllocation = annualAllocation; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
