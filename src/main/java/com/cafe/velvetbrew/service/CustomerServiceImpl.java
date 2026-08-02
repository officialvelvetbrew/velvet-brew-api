package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.exception.CustomerNotFoundException;
import com.cafe.velvetbrew.dto.CustomerRequest;
import com.cafe.velvetbrew.dto.CustomerResponse;
import com.cafe.velvetbrew.entity.Customer;
import com.cafe.velvetbrew.mapper.CustomerMapper;
import com.cafe.velvetbrew.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;

    @Override
    public Customer findOrCreate(CustomerRequest request) {

        return repository.findByMobile(request.getMobile())
                .orElseGet(() -> repository.save(
                        Customer.builder()
                                .fullName(request.getFullName())
                                .mobile(request.getMobile())
                                .email(request.getEmail())
                                .build()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {

        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return CustomerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {

        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());

        return CustomerMapper.toResponse(customer);
    }

    @Override
    public void delete(Long id) {

        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

    }
}