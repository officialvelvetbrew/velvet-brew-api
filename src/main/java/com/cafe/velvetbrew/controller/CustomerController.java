package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.dto.CustomerDetailResponse;
import com.cafe.velvetbrew.dto.CustomerListResponse;
import com.cafe.velvetbrew.dto.CustomerRequest;
import com.cafe.velvetbrew.dto.CustomerResponse;
import com.cafe.velvetbrew.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "APIs for managing customers, viewing order history, and customer analytics")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Get all customers with analytics",
            description = "Retrieve complete list of customers with lifetime spending, visit counts, and overall revenue summary")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved customer list with statistics",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Admin token required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ApiResponse<CustomerListResponse> getAll() {

        return ApiResponse.success(customerService.getAllWithStats());

    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer details with order history",
            description = "Retrieve a specific customer's profile along with their complete order history, " +
                    "lifetime spending, visit count, and last visit timestamp")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer found and returned with order history",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Admin token required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ApiResponse<CustomerDetailResponse> getById(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long id) {

        return ApiResponse.success(customerService.getCustomerWithOrders(id));

    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details",
            description = "Update customer's name and email address")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Admin token required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ApiResponse<CustomerResponse> update(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        return ApiResponse.success(customerService.update(id, request));

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer",
            description = "Permanently delete a customer record. Note: This will also delete associated orders and data.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Admin token required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ApiResponse<Void> delete(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long id) {

        customerService.delete(id);

        return ApiResponse.success("Customer deleted successfully", null);

    }
}