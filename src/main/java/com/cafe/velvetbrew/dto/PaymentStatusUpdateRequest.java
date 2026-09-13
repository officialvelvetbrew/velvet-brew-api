package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to update an order's payment status - use this to mark a cash " +
        "payment as SUCCESS (paid) or back to PENDING (unpaid); Razorpay orders are updated " +
        "automatically by payment verification instead")
public class PaymentStatusUpdateRequest {

    @NotNull(message = "Payment status cannot be null")
    @Schema(description = "New payment status", example = "SUCCESS", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentStatus paymentStatus;
}
