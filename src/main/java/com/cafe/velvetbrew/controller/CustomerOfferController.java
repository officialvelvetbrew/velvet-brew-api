package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.PublicOfferResponse;
import com.cafe.velvetbrew.dto.ValidateOfferRequest;
import com.cafe.velvetbrew.dto.ValidateOfferResponse;
import com.cafe.velvetbrew.service.OfferApplicationService;
import com.cafe.velvetbrew.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/offers")
@RequiredArgsConstructor
public class CustomerOfferController {

    private final OfferService offerService;
    private final OfferApplicationService offerApplicationService;

    @GetMapping
    public List<PublicOfferResponse> getActiveOffers() {

        return offerService.getCurrentlyActive();
    }

    /**
     * Read-only preview - lets a guest see the discount a code would give
     * before actually placing the order. Never moves usage counters.
     */
    @PostMapping("/validate")
    public ValidateOfferResponse validate(@Valid @RequestBody ValidateOfferRequest request) {

        return offerApplicationService.preview(request);
    }
}
