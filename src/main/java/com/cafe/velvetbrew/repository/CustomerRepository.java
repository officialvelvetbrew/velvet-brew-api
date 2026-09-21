package com.cafe.velvetbrew.repository;

import com.cafe.velvetbrew.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository
extends JpaRepository<Customer,Long>{
Optional<Customer> findByMobile(String mobile);

Optional<Customer> findByEmail(String email);
}
