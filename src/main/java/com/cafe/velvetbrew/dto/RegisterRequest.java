package com.cafe.velvetbrew.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @Email
    private String email;

    /**
     * Optional - registration works with either email or mobile, not both.
     * See isEmailOrMobileProvided() below for the cross-field check.
     */
    private String mobile;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @AssertTrue(message = "Either email or mobile is required")
    public boolean isEmailOrMobileProvided() {
        return hasText(email) || hasText(mobile);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
