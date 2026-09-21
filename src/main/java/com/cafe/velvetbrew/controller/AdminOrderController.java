package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.dto.OrderResponse;
import com.cafe.velvetbrew.dto.OrderStatusUpdateRequest;
import com.cafe.velvetbrew.dto.PaymentStatusUpdateRequest;
import com.cafe.velvetbrew.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin Order Management", description = "APIs for administrators to manage orders, update status, and view order details")
public class AdminOrderController {

    private final OrderService orderService;

    @PatchMapping("/{orderNumber}/status")
    @Operation(summary = "Update order status",
            description = "Update the status of an order (e.g., PENDING → ACCEPTED → READY → COMPLETED). " +
                    "Only updates the order status without modifying items or pricing. " +
                    "Recipe ingredients are deducted from inventory once, when the order becomes COMPLETED (repeating COMPLETED does nothing). " +
                    "Moving a completed order to CANCELLED or REJECTED returns them; cancelling or rejecting an order that never completed leaves stock untouched. " +
                    "If stock is too low to complete the order the request fails with 409 and the status is unchanged.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body or invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Admin token required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Not enough inventory to complete the order; status left unchanged")
    })
    public ApiResponse<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order number to update", required = true, example = "VB-001234")
            @PathVariable String orderNumber,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        return ApiResponse.success(
                "Order status updated successfully",
                orderService.updateOrderStatus(orderNumber, request.getOrderStatus()));
    }

    @PatchMapping("/{orderNumber}/payment-status")
    @Operation(summary = "Update order payment status",
            description = "Manually mark an order's payment status - e.g. SUCCESS (paid) or PENDING " +
                    "(unpaid) for a cash payment collected at the counter. Razorpay orders normally " +
                    "get this set automatically by payment verification instead.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body or invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Admin token required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ApiResponse<OrderResponse> updatePaymentStatus(
            @Parameter(description = "Order number to update", required = true, example = "VB-001234")
            @PathVariable String orderNumber,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {

        return ApiResponse.success(
                "Payment status updated successfully",
                orderService.updatePaymentStatus(orderNumber, request.getPaymentStatus()));
    }
}
