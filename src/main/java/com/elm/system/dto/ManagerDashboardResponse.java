package com.elm.system.dto;

import java.util.List;

public class ManagerDashboardResponse {
    private long totalTeamMembers;
    private long pendingApprovalsCount;
    private long teamApprovedLeavesCount;
    private long teamCurrentlyOnLeaveCount;
    private List<EmployeeResponse> teamMembers;
    private List<LeaveApplicationResponse> pendingRequests;
    private List<LeaveApplicationResponse> upcomingTeamLeaves;

    public ManagerDashboardResponse() {}

    public long getTotalTeamMembers() { return totalTeamMembers; }
    public void setTotalTeamMembers(long totalTeamMembers) { this.totalTeamMembers = totalTeamMembers; }

    public long getPendingApprovalsCount() { return pendingApprovalsCount; }
    public void setPendingApprovalsCount(long pendingApprovalsCount) { this.pendingApprovalsCount = pendingApprovalsCount; }

    public long getTeamApprovedLeavesCount() { return teamApprovedLeavesCount; }
    public void setTeamApprovedLeavesCount(long teamApprovedLeavesCount) { this.teamApprovedLeavesCount = teamApprovedLeavesCount; }

    public long getTeamCurrentlyOnLeaveCount() { return teamCurrentlyOnLeaveCount; }
    public void setTeamCurrentlyOnLeaveCount(long teamCurrentlyOnLeaveCount) { this.teamCurrentlyOnLeaveCount = teamCurrentlyOnLeaveCount; }

    public List<EmployeeResponse> getTeamMembers() { return teamMembers; }
    public void setTeamMembers(List<EmployeeResponse> teamMembers) { this.teamMembers = teamMembers; }

    public List<LeaveApplicationResponse> getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(List<LeaveApplicationResponse> pendingRequests) { this.pendingRequests = pendingRequests; }

    public List<LeaveApplicationResponse> getUpcomingTeamLeaves() { return upcomingTeamLeaves; }
    public void setUpcomingTeamLeaves(List<LeaveApplicationResponse> upcomingTeamLeaves) { this.upcomingTeamLeaves = upcomingTeamLeaves; }
}
