package com.spacebook.dto.booking;

import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    @NotNull(message = "Stall ID is required")
    private Long stallId;

    private String message;

    public BookingRequest() {
    }

    public BookingRequest(Long stallId, String message) {
        this.stallId = stallId;
        this.message = message;
    }

    public Long getStallId() {
        return stallId;
    }

    public void setStallId(Long stallId) {
        this.stallId = stallId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
