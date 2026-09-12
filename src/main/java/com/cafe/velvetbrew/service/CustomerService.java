package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.CustomerDetailResponse;
import com.cafe.velvetbrew.dto.CustomerListResponse;
import com.cafe.velvetbrew.dto.CustomerRequest;
import com.cafe.velvetbrew.dto.CustomerResponse;
import com.cafe.velvetbrew.entity.Customer;

import java.util.List;

public interface CustomerService {

    Customer findOrCreate(CustomerRequest request);

    CustomerResponse getById(Long id);

    CustomerDetailResponse getCustomerWithOrders(Long id);

    List<CustomerResponse> getAll();

    CustomerListResponse getAllWithStats();

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);

}