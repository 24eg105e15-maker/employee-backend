package com.elm.system.dto;

public class LeaveApprovalRequest {
    private String comments;

    public LeaveApprovalRequest() {}

    public LeaveApprovalRequest(String comments) {
        this.comments = comments;
    }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
