package com.ombits.leavemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectLeaveRequestDto {

    @NotBlank(message = "Rejection reason is mandatory when rejecting a leave request")
    @Size(min = 3, max = 1000, message = "Rejection reason must be between 3 and 1000 characters")
    private String rejectionReason;

    public RejectLeaveRequestDto() {
    }

    public RejectLeaveRequestDto(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
