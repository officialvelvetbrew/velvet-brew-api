package com.cafe.velvetbrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for creating or updating a customer")
public class CustomerRequest {

    @NotBlank
    @Schema(description = "Customer's full name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @NotBlank
    @Schema(description = "Customer's mobile phone number", example = "9876543210", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mobile;

    @Email
    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String email;
}