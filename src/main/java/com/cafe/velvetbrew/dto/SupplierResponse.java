package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupplierResponse {

    private Long id;

    private String name;

    private String contactPerson;

    private String phone;

    private String email;

    private String address;

    private String taxNumber;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}