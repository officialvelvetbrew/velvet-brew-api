package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.exception.CustomerNotFoundException;
import com.cafe.velvetbrew.dto.CustomerDetailResponse;
import com.cafe.velvetbrew.dto.CustomerListItemResponse;
import com.cafe.velvetbrew.dto.CustomerListResponse;
import com.cafe.velvetbrew.dto.CustomerOrderHistoryResponse;
import com.cafe.velvetbrew.dto.CustomerRequest;
import com.cafe.velvetbrew.dto.CustomerResponse;
import com.cafe.velvetbrew.dto.OrderItemResponse;
import com.cafe.velvetbrew.entity.Customer;
import com.cafe.velvetbrew.entity.Order;
import com.cafe.velvetbrew.mapper.CustomerMapper;
import com.cafe.velvetbrew.repository.CustomerRepository;
import com.cafe.velvetbrew.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final OrderRepository orderRepository;

    @Override
    public Customer findOrCreate(CustomerRequest request) {

        // Mobile is the primary lookup key when present (matches how it's
        // always been), but mobile is now optional - a customer identified
        // only by email is looked up by that instead so repeat guest
        // checkouts by email still match the same customer record.
        Optional<Customer> existing = StringUtils.hasText(request.getMobile())
                ? repository.findByMobile(request.getMobile())
                : repository.findByEmail(request.getEmail());

        return existing
                .map(customer -> {
                    customer.setFullName(request.getFullName());
                    if (request.getEmail() != null) {
                        customer.setEmail(request.getEmail());
                    }
                    return customer;
                })
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
    public CustomerDetailResponse getCustomerWithOrders(Long id) {

        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        OrderRepository.CustomerStatsRow stats = orderRepository.getCustomerStats(id);
        List<Order> orders = orderRepository.findByCustomer_IdOrderByCreatedAtDesc(id);

        List<CustomerOrderHistoryResponse> orderHistory = orders.stream()
                .map(this::mapOrderToHistory)
                .toList();

        return CustomerDetailResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .mobile(customer.getMobile())
                .email(customer.getEmail())
                .lifetimeSpend(stats.getTotalSpent())
                .totalVisits(stats.getTotalOrders())
                .lastVisit(stats.getLastOrderDate())
                .orders(orderHistory)
                .build();
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
    @Transactional(readOnly = true)
    public CustomerListResponse getAllWithStats() {

        List<Customer> allCustomers = repository.findAll();
        OrderRepository.GlobalStatsRow globalStats = orderRepository.getGlobalStats();

        List<CustomerListItemResponse> customers = allCustomers.stream()
                .map(customer -> {
                    OrderRepository.CustomerStatsRow stats = orderRepository.getCustomerStats(customer.getId());
                    return CustomerListItemResponse.builder()
                            .id(customer.getId())
                            .fullName(customer.getFullName())
                            .mobile(customer.getMobile())
                            .email(customer.getEmail())
                            .lifetimeSpend(stats.getTotalSpent())
                            .totalVisits(stats.getTotalOrders())
                            .lastVisit(stats.getLastOrderDate())
                            .build();
                })
                .toList();

        return CustomerListResponse.builder()
                .totalCustomers((long) allCustomers.size())
                .lifetimeRevenue(globalStats.getLifetimeRevenue())
                .customers(customers)
                .build();
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {

        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());

        log.info("Updated customer id={}", id);

        return CustomerMapper.toResponse(customer);
    }

    @Override
    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

        repository.deleteById(id);

        log.info("Deleted customer id={}", id);
    }

    private CustomerOrderHistoryResponse mapOrderToHistory(Order order) {

        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .menuId(item.getMenuItem().getId())
                        .menuName(item.getMenuItem().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .toList();

        return CustomerOrderHistoryResponse.builder()
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .orderedAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}