package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.CreatePaymentRequest;
import com.cafe.velvetbrew.dto.CreatePaymentResponse;
import com.cafe.velvetbrew.dto.OrderResponse;
import com.cafe.velvetbrew.dto.VerifyPaymentRequest;
import com.cafe.velvetbrew.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;
    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<OrderResponse> verifyPayment(
            @RequestBody VerifyPaymentRequest request) {

        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }
}
