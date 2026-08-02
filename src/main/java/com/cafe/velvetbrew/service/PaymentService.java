package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.CreatePaymentRequest;
import com.cafe.velvetbrew.dto.CreatePaymentResponse;
import com.cafe.velvetbrew.dto.OrderResponse;
import com.cafe.velvetbrew.dto.VerifyPaymentRequest;

public interface PaymentService {

    CreatePaymentResponse createPayment(CreatePaymentRequest request);

    OrderResponse verifyPayment(VerifyPaymentRequest request);

}