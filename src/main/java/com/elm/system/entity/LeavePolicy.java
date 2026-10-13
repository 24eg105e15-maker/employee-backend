package com.elm.system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_policies")
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id", nullable = false, unique = true)
    private LeaveType leaveType;

    @Column(name = "allocation_period", nullable = false, length = 20)
    private String allocationPeriod = "ANNUAL";

    @Column(name = "carry_forward_allowed", nullable = false)
    private boolean carryForwardAllowed = false;

    @Column(name = "maximum_carry_forward", nullable = false)
    private Integer maximumCarryForward = 0;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired = true;

    @Column(nullable = false)
    private boolean active = true;

    public LeavePolicy() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }

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
