package com.cafe.velvetbrew.repository;

import com.cafe.velvetbrew.entity.OfferRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRedemptionRepository extends JpaRepository<OfferRedemption, Long> {

    long countByOfferIdAndCustomerId(Long offerId, Long customerId);
}
