package com.cafe.velvetbrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "Customer list with summary statistics and analytics")
public class CustomerListResponse {

    @Schema(description = "Total number of customers in the system", example = "15")
    private Long totalCustomers;

    @Schema(description = "Total lifetime revenue from all customers", example = "5000.00")
    private BigDecimal lifetimeRevenue;

    @Schema(description = "List of customers with their individual analytics")
    private List<CustomerListItemResponse> customers;
}
