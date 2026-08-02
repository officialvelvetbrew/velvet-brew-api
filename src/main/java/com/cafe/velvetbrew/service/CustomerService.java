package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.CustomerRequest;
import com.cafe.velvetbrew.dto.CustomerResponse;
import com.cafe.velvetbrew.entity.Customer;

import java.util.List;

public interface CustomerService {

    Customer findOrCreate(CustomerRequest request);

    CustomerResponse getById(Long id);

    List<CustomerResponse> getAll();

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);

}