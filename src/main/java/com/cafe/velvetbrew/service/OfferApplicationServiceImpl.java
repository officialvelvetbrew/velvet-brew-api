package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.enums.OfferDiscountType;
import com.cafe.velvetbrew.common.exception.InvalidOfferException;
import com.cafe.velvetbrew.dto.ValidateOfferRequest;
import com.cafe.velvetbrew.dto.ValidateOfferResponse;
import com.cafe.velvetbrew.entity.Customer;
import com.cafe.velvetbrew.entity.Offer;
import com.cafe.velvetbrew.entity.OfferRedemption;
import com.cafe.velvetbrew.repository.CustomerRepository;
import com.cafe.velvetbrew.repository.OfferRedemptionRepository;
import com.cafe.velvetbrew.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferApplicationServiceImpl implements OfferApplicationService {

    private final OfferRepository offerRepository;
    private final OfferRedemptionRepository offerRedemptionRepository;
    private final CustomerRepository customerRepository;
    private final MenuPricingService menuPricingService;

    @Override
    @Transactional(readOnly = true)
    public ValidateOfferResponse preview(ValidateOfferRequest request) {

        BigDecimal subtotal = menuPricingService.subtotalOf(menuPricingService.price(request.getItems()));

        Long customerId = null;

        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            customerId = customerRepository.findByMobile(request.getMobile())
                    .map(Customer::getId)
                    .orElse(null);
        }

        String code = request.getCode().trim();

        Offer offer = offerRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new InvalidOfferException("Invalid offer code"));

        BigDecimal discount = validateAndComputeDiscount(offer, subtotal, customerId);

        return ValidateOfferResponse.builder()
                .code(offer.getCode())
                .subtotal(subtotal)
                .discountAmount(discount)
                .finalAmount(subtotal.subtract(discount))
                .build();
    }

    @Override
    @Transactional
    public OfferDiscount apply(String code, BigDecimal subtotal, Customer customer, String orderNumber) {

        Offer offer = offerRepository.findByCodeIgnoreCaseForUpdate(code.trim())
                .orElseThrow(() -> new InvalidOfferException("Invalid offer code"));

        BigDecimal discount = validateAndComputeDiscount(offer, subtotal, customer.getId());

        offer.setCurrentUses(offer.getCurrentUses() + 1);
        offerRepository.save(offer);

        OfferRedemption redemption = OfferRedemption.builder()
                .offer(offer)
                .customer(customer)
                .orderNumber(orderNumber)
                .discountAmount(discount)
                .build();

        offerRedemptionRepository.save(redemption);

        log.info("Offer {} redeemed by customer {} on order {} - discount {}",
                offer.getCode(), customer.getId(), orderNumber, discount);

        return new OfferDiscount(offer, discount);
    }

    @Override
    public BigDecimal recompute(Offer offer, BigDecimal subtotal) {
        return computeDiscountAmount(offer, subtotal);
    }

    private BigDecimal validateAndComputeDiscount(Offer offer, BigDecimal subtotal, Long customerId) {

        if (!Boolean.TRUE.equals(offer.getActive())) {
            log.warn("Offer {} rejected - inactive", offer.getCode());
            throw new InvalidOfferException("This offer is no longer active");
        }

        LocalDateTime now = LocalDateTime.now();

        if (offer.getStartsAt() != null && now.isBefore(offer.getStartsAt())) {
            throw new InvalidOfferException("This offer is not active yet");
        }

        if (offer.getEndsAt() != null && now.isAfter(offer.getEndsAt())) {
            throw new InvalidOfferException("This offer has expired");
        }

        if (subtotal.compareTo(offer.getMinOrderAmount()) < 0) {
            throw new InvalidOfferException(
                    "This offer requires a minimum order of " + offer.getMinOrderAmount());
        }

        if (offer.getMaxUsesTotal() != null && offer.getCurrentUses() >= offer.getMaxUsesTotal()) {
            log.warn("Offer {} rejected - usage limit reached ({}/{})",
                    offer.getCode(), offer.getCurrentUses(), offer.getMaxUsesTotal());
            throw new InvalidOfferException("This offer has reached its usage limit");
        }

        if (offer.getMaxUsesPerCustomer() != null && customerId != null) {

            long used = offerRedemptionRepository.countByOfferIdAndCustomerId(offer.getId(), customerId);

            if (used >= offer.getMaxUsesPerCustomer()) {
                throw new InvalidOfferException(
                        "You have already used this offer the maximum number of times");
            }
        }

        return computeDiscountAmount(offer, subtotal);
    }

    private BigDecimal computeDiscountAmount(Offer offer, BigDecimal subtotal) {

        BigDecimal rawDiscount;

        if (offer.getDiscountType() == OfferDiscountType.PERCENTAGE) {

            rawDiscount = subtotal
                    .multiply(offer.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (offer.getMaxDiscountAmount() != null) {
                rawDiscount = rawDiscount.min(offer.getMaxDiscountAmount());
            }

        } else {
            rawDiscount = offer.getDiscountValue();
        }

        // Never exceed the subtotal, and never negative (discountValue is
        // constrained > 0 at creation, but a stale row shouldn't be trusted).
        return rawDiscount.min(subtotal).max(BigDecimal.ZERO);
    }
}
