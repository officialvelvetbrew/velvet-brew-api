package com.cafe.velvetbrew.mapper;

import org.springframework.stereotype.Component;

import com.cafe.velvetbrew.dto.SupplierResponse;
import com.cafe.velvetbrew.entity.Supplier;

@Component
public class SupplierMapper {

    public SupplierResponse toResponse(Supplier supplier) {

        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .taxNumber(supplier.getTaxNumber())
                .enabled(supplier.getEnabled())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}