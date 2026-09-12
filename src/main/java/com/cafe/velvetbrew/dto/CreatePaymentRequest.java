package com.cafe.velvetbrew.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreatePaymentRequest {

    @Valid
    private CustomerRequest customer;

    @NotEmpty
    private List<OrderItemRequest> items;

    private String specialInstructions;

    private String offerCode;

}
