package com.elm.system.dto;

import java.util.List;
import java.util.Map;

public class AdminDashboardResponse {
    private long totalEmployees;
    private long totalDepartments;
    private long pendingLeaveRequests;
    private long approvedLeaveRequests;
    private long rejectedLeaveRequests;
    private long employeesCurrentlyOnLeave;
    private long upcomingApprovedLeavesCount;
    private long leaveRequestsThisMonth;
    private List<LeaveApplicationResponse> recentApplications;
    private List<LeaveApplicationResponse> upcomingApprovedLeaves;
    private Map<String, Long> departmentEmployeeCounts;
    private Map<String, Long> leaveTypeDistribution;

    public AdminDashboardResponse() {}

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(long totalDepartments) { this.totalDepartments = totalDepartments; }

    public long getPendingLeaveRequests() { return pendingLeaveRequests; }
    public void setPendingLeaveRequests(long pendingLeaveRequests) { this.pendingLeaveRequests = pendingLeaveRequests; }

    public long getApprovedLeaveRequests() { return approvedLeaveRequests; }
    public void setApprovedLeaveRequests(long approvedLeaveRequests) { this.approvedLeaveRequests = approvedLeaveRequests; }

    public long getRejectedLeaveRequests() { return rejectedLeaveRequests; }
    public void setRejectedLeaveRequests(long rejectedLeaveRequests) { this.rejectedLeaveRequests = rejectedLeaveRequests; }

    public long getEmployeesCurrentlyOnLeave() { return employeesCurrentlyOnLeave; }
    public void setEmployeesCurrentlyOnLeave(long employeesCurrentlyOnLeave) { this.employeesCurrentlyOnLeave = employeesCurrentlyOnLeave; }

    public long getUpcomingApprovedLeavesCount() { return upcomingApprovedLeavesCount; }
    public void setUpcomingApprovedLeavesCount(long upcomingApprovedLeavesCount) { this.upcomingApprovedLeavesCount = upcomingApprovedLeavesCount; }

    public long getLeaveRequestsThisMonth() { return leaveRequestsThisMonth; }
    public void setLeaveRequestsThisMonth(long leaveRequestsThisMonth) { this.leaveRequestsThisMonth = leaveRequestsThisMonth; }

    public List<LeaveApplicationResponse> getRecentApplications() { return recentApplications; }
    public void setRecentApplications(List<LeaveApplicationResponse> recentApplications) { this.recentApplications = recentApplications; }

    public List<LeaveApplicationResponse> getUpcomingApprovedLeaves() { return upcomingApprovedLeaves; }
    public void setUpcomingApprovedLeaves(List<LeaveApplicationResponse> upcomingApprovedLeaves) { this.upcomingApprovedLeaves = upcomingApprovedLeaves; }

    public Map<String, Long> getDepartmentEmployeeCounts() { return departmentEmployeeCounts; }
    public void setDepartmentEmployeeCounts(Map<String, Long> departmentEmployeeCounts) { this.departmentEmployeeCounts = departmentEmployeeCounts; }

    public Map<String, Long> getLeaveTypeDistribution() { return leaveTypeDistribution; }
    public void setLeaveTypeDistribution(Map<String, Long> leaveTypeDistribution) { this.leaveTypeDistribution = leaveTypeDistribution; }
}
