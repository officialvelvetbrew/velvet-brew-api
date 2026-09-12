package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.enums.PaymentStatus;
import com.cafe.velvetbrew.common.exception.OrderNotFoundException;
import com.cafe.velvetbrew.common.exception.ResourceNotFoundException;
import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.CreatePaymentRequest;
import com.cafe.velvetbrew.dto.CreatePaymentResponse;
import com.cafe.velvetbrew.dto.OrderResponse;
import com.cafe.velvetbrew.dto.VerifyPaymentRequest;
import com.cafe.velvetbrew.entity.Payment;
import com.cafe.velvetbrew.repository.OrderRepository;
import com.cafe.velvetbrew.repository.PaymentRepository;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Override
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {

        // The order (and its total) is always created and priced server-side from the
        // current menu prices via OrderService - the amount charged must never be trusted
        // from the client, otherwise a caller could request a Razorpay order for any amount.
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setCustomer(request.getCustomer());
        orderRequest.setItems(request.getItems());
        orderRequest.setSpecialInstructions(request.getSpecialInstructions());
        orderRequest.setOfferCode(request.getOfferCode());

        OrderResponse order = orderService.createOrder(orderRequest);

        try {

            RazorpayClient razorpayClient =
                    new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("amount", order.getTotalAmount().multiply(new BigDecimal("100")).intValue()); // paise
            options.put("currency", "INR");
            options.put("receipt", order.getOrderNumber());

            Order razorpayOrder = razorpayClient.orders.create(options);

            Payment payment = Payment.builder()
                    .orderNumber(order.getOrderNumber())
                    .amount(order.getTotalAmount())
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();

            paymentRepository.save(payment);

            log.info("Created Razorpay order {} for {} - amount {} {}",
                    razorpayOrder.get("id").toString(), order.getOrderNumber(),
                    order.getTotalAmount(), "INR");

            return CreatePaymentResponse.builder()
                    .orderId(razorpayOrder.get("id"))
                    .amount(order.getTotalAmount())
                    .currency("INR")
                    .key(keyId)
                    .build();

        } catch (Exception ex) {
            log.error("Failed to create Razorpay order for {}", order.getOrderNumber(), ex);
            throw new RuntimeException("Unable to create Razorpay Order", ex);
        }
    }

    @Override
    @Transactional
    public OrderResponse verifyPayment(VerifyPaymentRequest request) {

        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> {
                    log.warn("Payment verification failed - no payment for razorpay order {}",
                            request.getRazorpayOrderId());
                    return new ResourceNotFoundException(
                            "Payment not found for razorpay order : " + request.getRazorpayOrderId());
                });

        com.cafe.velvetbrew.entity.Order order = orderRepository
                .findByOrderNumber(payment.getOrderNumber())
                .orElseThrow(() -> {
                    log.error("Payment {} references missing order {}",
                            request.getRazorpayOrderId(), payment.getOrderNumber());
                    return new OrderNotFoundException(payment.getOrderNumber());
                });

        boolean verified;

        try {

            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getRazorpayOrderId());
            attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
            attributes.put("razorpay_signature", request.getRazorpaySignature());

            verified = Utils.verifyPaymentSignature(attributes, keySecret);

        } catch (Exception ex) {
            log.error("Payment signature verification threw for order {}", payment.getOrderNumber(), ex);
            throw new RuntimeException("Payment verification failed", ex);
        }

        if (verified) {

            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setPaymentStatus(PaymentStatus.SUCCESS);

            log.info("Payment verified SUCCESS for order {} (razorpay payment {})",
                    payment.getOrderNumber(), request.getRazorpayPaymentId());

        } else {

            payment.setPaymentStatus(PaymentStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);

            log.warn("Payment signature INVALID for order {} (razorpay order {})",
                    payment.getOrderNumber(), request.getRazorpayOrderId());
        }

        orderRepository.save(order);

        return orderService.getOrder(payment.getOrderNumber());
    }
}
