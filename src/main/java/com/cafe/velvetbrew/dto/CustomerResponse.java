package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CustomerResponse {

    private Long id;

    private String fullName;

    private String mobile;

    private String email;
}