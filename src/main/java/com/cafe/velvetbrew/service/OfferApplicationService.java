package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.ValidateOfferRequest;
import com.cafe.velvetbrew.dto.ValidateOfferResponse;
import com.cafe.velvetbrew.entity.Customer;
import com.cafe.velvetbrew.entity.Offer;

import java.math.BigDecimal;

public interface OfferApplicationService {

    /**
     * Read-only preview: validates the code against a freshly-priced cart
     * and returns the discount that WOULD apply. No usage counters move and
     * no redemption is recorded.
     */
    ValidateOfferResponse preview(ValidateOfferRequest request);

    /**
     * The real thing: locks the offer row, re-validates from scratch (never
     * trusts a client-supplied discount), increments its usage counter, and
     * records a redemption. Must run inside the caller's transaction so a
     * failure after this point rolls the usage increment back too.
     */
    OfferDiscount apply(String code, BigDecimal subtotal, Customer customer, String orderNumber);

    /**
     * Recomputes an already-applied offer's discount against a new subtotal
     * (e.g. an admin edits an order's items) without re-checking eligibility
     * or touching usage counters - the redemption already happened.
     */
    BigDecimal recompute(Offer offer, BigDecimal subtotal);

    record OfferDiscount(Offer offer, BigDecimal discountAmount) {
    }
}
