package com.cafe.velvetbrew.mapper;

import com.cafe.velvetbrew.dto.CustomerResponse;
import com.cafe.velvetbrew.entity.Customer;

public class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerResponse toResponse(Customer customer) {

        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .mobile(customer.getMobile())
                .email(customer.getEmail())
                .build();

    }

}