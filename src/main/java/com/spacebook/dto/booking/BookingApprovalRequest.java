package com.spacebook.dto.booking;

import jakarta.validation.constraints.NotNull;

public class BookingApprovalRequest {

    @NotNull(message = "Action is required")
    private ApprovalAction action;

    private String rejectionReason;

    public BookingApprovalRequest() {
    }

    public BookingApprovalRequest(ApprovalAction action, String rejectionReason) {
        this.action = action;
        this.rejectionReason = rejectionReason;
    }

    public ApprovalAction getAction() {
        return action;
    }

    public void setAction(ApprovalAction action) {
        this.action = action;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public enum ApprovalAction {
        APPROVE,
        REJECT
    }
}
