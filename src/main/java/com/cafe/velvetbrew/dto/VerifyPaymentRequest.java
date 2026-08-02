package com.cafe.velvetbrew.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class VerifyPaymentRequest {

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

    @Valid
    private CustomerRequest customer;

    @Valid
    private List<OrderItemRequest> items;

    private String specialInstructions;

}
