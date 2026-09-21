package com.cafe.velvetbrew.controller;


import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.OrderResponse;
import com.cafe.velvetbrew.service.OrderService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Customer Orders", description = "Place, edit and look up orders. Guest checkout is allowed for placing an order and viewing it by order number.")
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

    @Operation(summary = "Place an order",
            description = "Prices the items, applies an optional offer code and creates the order as PENDING. " +
                    "For menu items with an enabled recipe, the ingredient stock (summed across all lines) must cover the order or it is rejected with 409 and nothing is saved. " +
                    "Stock is not reserved or deducted here; it is deducted when the order is marked COMPLETED.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed, unavailable menu item or invalid offer"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Not enough ingredient stock, or an ingredient is disabled. The whole order is rejected and every short item is listed in one message, e.g. \"Not enough stock to make Espresso: Nescafe Coffee needs 2 KG but only 1 KG is available\"")
    })
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return ApiResponse.success(
                "Order created successfully",
                orderService.createOrder(request));
    }

    @Operation(summary = "Edit an order",
            description = "Replaces the order's items and customer details (ADMIN/STAFF). The edited items go through the same ingredient stock check as a new order. " +
                    "If the order was already COMPLETED its old ingredients are returned and the new ones deducted; on a 409 nothing changes.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Not enough ingredient stock for the edited items")
    })
    @PatchMapping
    public ApiResponse<OrderResponse> updateOrder(@RequestParam String orderNumber,
            @Valid @RequestBody CreateOrderRequest request) {

        return ApiResponse.success(
                "Order updated successfully",
                orderService.updateOrder(orderNumber,request));
    }

    @GetMapping("/{orderNumber}")
    public ApiResponse<OrderResponse> getOrder(
            @PathVariable String orderNumber) {

        return ApiResponse.success(
                orderService.getOrder(orderNumber));
    }

    /**
     * Orders placed by the caller's own account - requires a JWT, unlike
     * the guest-friendly endpoints above. Matched ahead of
     * GET /{orderNumber} in SecurityConfig so "mine" is never treated as
     * an order number.
     */
    @GetMapping("/mine")
    public ApiResponse<List<OrderResponse>> getMyOrders() {

        return ApiResponse.success(orderService.getMyOrders());
    }
}