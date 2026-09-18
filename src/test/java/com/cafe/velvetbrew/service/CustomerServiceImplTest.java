package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.CustomerRequest;
import com.cafe.velvetbrew.entity.Customer;
import com.cafe.velvetbrew.repository.CustomerRepository;
import com.cafe.velvetbrew.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createsCustomerWithBothMobileAndEmail() {

        when(repository.findByMobile("9876543210")).thenReturn(Optional.empty());
        when(repository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerRequest request = request("John Doe", "9876543210", "john.doe@example.com");

        Customer customer = customerService.findOrCreate(request);

        assertThat(customer.getMobile()).isEqualTo("9876543210");
        assertThat(customer.getEmail()).isEqualTo("john.doe@example.com");
        verify(repository, never()).findByEmail(any());
    }

    @Test
    void createsCustomerWithEmailOnlyLookedUpByEmail() {

        when(repository.findByEmail("jane.smith@example.com")).thenReturn(Optional.empty());
        when(repository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerRequest request = request("Jane Smith", null, "jane.smith@example.com");

        Customer customer = customerService.findOrCreate(request);

        assertThat(customer.getMobile()).isNull();
        assertThat(customer.getEmail()).isEqualTo("jane.smith@example.com");
        verify(repository, never()).findByMobile(any());
    }

    @Test
    void createsCustomerWithMobileOnlyLookedUpByMobile() {

        when(repository.findByMobile("9123456789")).thenReturn(Optional.empty());
        when(repository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerRequest request = request("Alex Kim", "9123456789", null);

        Customer customer = customerService.findOrCreate(request);

        assertThat(customer.getMobile()).isEqualTo("9123456789");
        assertThat(customer.getEmail()).isNull();
        verify(repository, never()).findByEmail(any());
    }

    @Test
    void createsCustomerWithNoContactDetailsWithoutLookingUpByNull() {

        when(repository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerRequest request = request("Sam Patel", null, null);

        Customer customer = customerService.findOrCreate(request);

        assertThat(customer.getFullName()).isEqualTo("Sam Patel");
        assertThat(customer.getMobile()).isNull();
        assertThat(customer.getEmail()).isNull();
        // Neither lookup should run - findByEmail(null) would otherwise match
        // any existing customer whose email happens to be null.
        verify(repository, never()).findByMobile(any());
        verify(repository, never()).findByEmail(any());
    }

    private static CustomerRequest request(String fullName, String mobile, String email) {

        CustomerRequest request = new CustomerRequest();
        request.setFullName(fullName);
        request.setMobile(mobile);
        request.setEmail(email);
        return request;
    }
}
