package com.elm.system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_types")
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "annual_allocation", nullable = false)
    private Integer annualAllocation = 12;

    @Column(nullable = false)
    private boolean paid = true;

    @Column(nullable = false)
    private boolean active = true;

    public LeaveType() {}

    public LeaveType(String name, String description, Integer annualAllocation, boolean paid, boolean active) {
        this.name = name;
        this.description = description;
        this.annualAllocation = annualAllocation;
        this.paid = paid;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
