package com.cafe.velvetbrew.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String mobile;

    @Email
    private String email;
}