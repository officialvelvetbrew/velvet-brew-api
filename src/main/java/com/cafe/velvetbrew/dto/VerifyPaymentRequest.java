package com.cafe.velvetbrew.dto;

import lombok.Data;

@Data
public class VerifyPaymentRequest {

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

}
