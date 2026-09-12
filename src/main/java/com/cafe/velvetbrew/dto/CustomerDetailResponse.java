package com.cafe.velvetbrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "Detailed customer profile with complete order history and analytics")
public class CustomerDetailResponse {

    @Schema(description = "Unique customer identifier", example = "1")
    private Long id;

    @Schema(description = "Customer's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Customer's mobile phone number", example = "9876543210")
    private String mobile;

    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Total amount spent by customer across all orders", example = "2500.50")
    private BigDecimal lifetimeSpend;

    @Schema(description = "Total number of orders placed by customer", example = "5")
    private Long totalVisits;

    @Schema(description = "Timestamp of customer's most recent order", example = "2026-09-08T15:30:00")
    private LocalDateTime lastVisit;

    @Schema(description = "Complete order history for this customer, ordered by most recent first")
    private List<CustomerOrderHistoryResponse> orders;
}
