package com.elm.system.dto;

import jakarta.validation.constraints.NotNull;

public class LeaveBalanceUpdateRequest {
    @NotNull(message = "Allocated days is required")
    private Double allocatedDays;

    private Double usedDays;
    private Double carriedForwardDays;

    public LeaveBalanceUpdateRequest() {}

    public Double getAllocatedDays() { return allocatedDays; }
    public void setAllocatedDays(Double allocatedDays) { this.allocatedDays = allocatedDays; }

    public Double getUsedDays() { return usedDays; }
    public void setUsedDays(Double usedDays) { this.usedDays = usedDays; }

    public Double getCarriedForwardDays() { return carriedForwardDays; }
    public void setCarriedForwardDays(Double carriedForwardDays) { this.carriedForwardDays = carriedForwardDays; }
}
