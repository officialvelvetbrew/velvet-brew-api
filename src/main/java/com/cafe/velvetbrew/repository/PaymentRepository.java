package com.cafe.velvetbrew.repository;

import com.cafe.velvetbrew.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository
extends JpaRepository<Payment,Long>{

Optional<Payment> findByOrderNumber(String orderNumber);

Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

}