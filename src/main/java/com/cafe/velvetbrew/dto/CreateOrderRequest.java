package com.cafe.velvetbrew.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @Valid
    private CustomerRequest customer;

    @Valid
    @NotEmpty(message = "Cart cannot be empty")
    private List<OrderItemRequest> items;

    private String specialInstructions;

}