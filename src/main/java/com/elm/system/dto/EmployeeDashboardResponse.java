package com.elm.system.dto;

import java.util.List;

public class EmployeeDashboardResponse {
    private EmployeeResponse profile;
    private List<LeaveBalanceResponse> balances;
    private long totalAppliedLeaves;
    private long pendingLeavesCount;
    private long approvedLeavesCount;
    private long rejectedLeavesCount;
    private List<LeaveApplicationResponse> recentApplications;
    private List<LeaveApplicationResponse> upcomingApprovedLeaves;

    public EmployeeDashboardResponse() {}

    public EmployeeResponse getProfile() { return profile; }
    public void setProfile(EmployeeResponse profile) { this.profile = profile; }

    public List<LeaveBalanceResponse> getBalances() { return balances; }
    public void setBalances(List<LeaveBalanceResponse> balances) { this.balances = balances; }

    public long getTotalAppliedLeaves() { return totalAppliedLeaves; }
    public void setTotalAppliedLeaves(long totalAppliedLeaves) { this.totalAppliedLeaves = totalAppliedLeaves; }

    public long getPendingLeavesCount() { return pendingLeavesCount; }
    public void setPendingLeavesCount(long pendingLeavesCount) { this.pendingLeavesCount = pendingLeavesCount; }

    public long getApprovedLeavesCount() { return approvedLeavesCount; }
    public void setApprovedLeavesCount(long approvedLeavesCount) { this.approvedLeavesCount = approvedLeavesCount; }

    public long getRejectedLeavesCount() { return rejectedLeavesCount; }
    public void setRejectedLeavesCount(long rejectedLeavesCount) { this.rejectedLeavesCount = rejectedLeavesCount; }

    public List<LeaveApplicationResponse> getRecentApplications() { return recentApplications; }
    public void setRecentApplications(List<LeaveApplicationResponse> recentApplications) { this.recentApplications = recentApplications; }

    public List<LeaveApplicationResponse> getUpcomingApprovedLeaves() { return upcomingApprovedLeaves; }
    public void setUpcomingApprovedLeaves(List<LeaveApplicationResponse> upcomingApprovedLeaves) { this.upcomingApprovedLeaves = upcomingApprovedLeaves; }
}
