package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.dto.CustomerRequest;
import com.cafe.velvetbrew.dto.CustomerResponse;
import com.cafe.velvetbrew.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ApiResponse<List<CustomerResponse>> getAll() {

        return ApiResponse.success(customerService.getAll());

    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getById(@PathVariable Long id) {

        return ApiResponse.success(customerService.getById(id));

    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        return ApiResponse.success(customerService.update(id, request));

    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {


        customerService.delete(id);

        return ApiResponse.success("Customer deleted successfully", null);

    }
}