package com.cafe.velvetbrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for creating or updating a customer. Both mobile and email are " +
        "optional contact details and may be provided individually, together, or omitted entirely - " +
        "contact information is not required to create or update a customer.")
public class CustomerRequest {
    @NotBlank
    @Schema(description = "Customer's full name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "Mobile number must be 7-15 digits, optionally prefixed with +")
    @Schema(description = "Customer's mobile phone number. Optional - validated for format only when provided.",
            example = "9876543210", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String mobile;

    @Email
    @Schema(description = "Customer's email address. Optional - validated for format only when provided.",
            example = "john.doe@example.com", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String email;
}
