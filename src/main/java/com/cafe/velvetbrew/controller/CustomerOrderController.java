package com.cafe.velvetbrew.controller;


import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.OrderResponse;
import com.cafe.velvetbrew.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/orders")
@RequiredArgsConstructor
@Validated
public class CustomerOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrderList() {

        return ApiResponse.success(orderService.getOrderList());
    }

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return ApiResponse.success(
                "Order created successfully",
                orderService.createOrder(request));
    }

    @PatchMapping
    public ApiResponse<OrderResponse> updateOrder(@RequestParam String orderNumber,
            @Valid @RequestBody CreateOrderRequest request) {

        return ApiResponse.success(
                "Order created successfully",
                orderService.updateOrder(orderNumber,request));
    }

    @GetMapping("/{orderNumber}")
    public ApiResponse<OrderResponse> getOrder(
            @PathVariable String orderNumber) {

        return ApiResponse.success(
                orderService.getOrder(orderNumber));
    }
}