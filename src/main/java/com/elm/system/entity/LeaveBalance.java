package com.elm.system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_balances", uniqueConstraints = {
    @UniqueConstraint(name = "uq_emp_leave_period", columnNames = {"employee_id", "leave_type_id", "allocation_period"})
})
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "allocation_period", nullable = false, length = 20)
    private String allocationPeriod = "2026";

    @Column(name = "allocated_days", nullable = false)
    private Double allocatedDays = 0.0;

    @Column(name = "used_days", nullable = false)
    private Double usedDays = 0.0;

    @Column(name = "carried_forward_days", nullable = false)
    private Double carriedForwardDays = 0.0;

    @Version
    private Long version = 0L;

    public LeaveBalance() {}

    public LeaveBalance(Employee employee, LeaveType leaveType, String allocationPeriod, Double allocatedDays, Double usedDays, Double carriedForwardDays) {
        this.employee = employee;
        this.leaveType = leaveType;
        this.allocationPeriod = allocationPeriod;
        this.allocatedDays = allocatedDays;
        this.usedDays = usedDays;
        this.carriedForwardDays = carriedForwardDays;
    }

    public Double getRemainingDays() {
        return (allocatedDays != null ? allocatedDays : 0.0) 
             + (carriedForwardDays != null ? carriedForwardDays : 0.0) 
             - (usedDays != null ? usedDays : 0.0);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }

    public String getAllocationPeriod() { return allocationPeriod; }
    public void setAllocationPeriod(String allocationPeriod) { this.allocationPeriod = allocationPeriod; }

    public Double getAllocatedDays() { return allocatedDays; }
    public void setAllocatedDays(Double allocatedDays) { this.allocatedDays = allocatedDays; }

    public Double getUsedDays() { return usedDays; }
    public void setUsedDays(Double usedDays) { this.usedDays = usedDays; }

    public Double getCarriedForwardDays() { return carriedForwardDays; }
    public void setCarriedForwardDays(Double carriedForwardDays) { this.carriedForwardDays = carriedForwardDays; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
