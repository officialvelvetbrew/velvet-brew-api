package com.cafe.velvetbrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
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

    @Schema(description = "Customer's mobile phone number - optional if email is provided", example = "9876543210")
    private String mobile;

    @Email
    @Schema(description = "Customer's email address - optional if mobile is provided", example = "john.doe@example.com")
    private String email;

    @AssertTrue(message = "Either mobile or email is required")
    public boolean isMobileOrEmailProvided() {
        return hasText(mobile) || hasText(email);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}