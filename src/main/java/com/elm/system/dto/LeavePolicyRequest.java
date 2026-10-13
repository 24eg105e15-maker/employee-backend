package com.elm.system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class LeavePolicyRequest {
    @NotNull(message = "Leave type ID is required")
    private Long leaveTypeId;

    private String allocationPeriod = "ANNUAL";
    private boolean carryForwardAllowed = false;

    @Min(value = 0, message = "Maximum carry forward cannot be negative")
    private Integer maximumCarryForward = 0;

    private boolean approvalRequired = true;
    private boolean active = true;

    public LeavePolicyRequest() {}

    public Long getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Long leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public String getAllocationPeriod() { return allocationPeriod; }
    public void setAllocationPeriod(String allocationPeriod) { this.allocationPeriod = allocationPeriod; }

    public boolean isCarryForwardAllowed() { return carryForwardAllowed; }
    public void setCarryForwardAllowed(boolean carryForwardAllowed) { this.carryForwardAllowed = carryForwardAllowed; }

    public Integer getMaximumCarryForward() { return maximumCarryForward; }
    public void setMaximumCarryForward(Integer maximumCarryForward) { this.maximumCarryForward = maximumCarryForward; }

    public boolean isApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
