package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.exception.ResourceNotFoundException;
import com.cafe.velvetbrew.dto.CreateOfferRequest;
import com.cafe.velvetbrew.dto.OfferResponse;
import com.cafe.velvetbrew.dto.PublicOfferResponse;
import com.cafe.velvetbrew.dto.UpdateOfferRequest;
import com.cafe.velvetbrew.entity.Offer;
import com.cafe.velvetbrew.mapper.OfferMapper;
import com.cafe.velvetbrew.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OfferServiceImpl implements OfferService {

    private final OfferRepository repository;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "offers", allEntries = true),
            @CacheEvict(value = "activeOffers", allEntries = true)
    })
    public OfferResponse create(CreateOfferRequest request) {

        String code = request.getCode().trim().toUpperCase();

        if (repository.existsByCodeIgnoreCase(code)) {
            log.warn("Offer creation rejected - duplicate code: {}", code);
            throw new IllegalArgumentException("An offer with code " + code + " already exists");
        }

        if (request.getStartsAt() != null && request.getEndsAt() != null
                && request.getEndsAt().isBefore(request.getStartsAt())) {
            throw new IllegalArgumentException("endsAt cannot be before startsAt");
        }

        Offer offer = Offer.builder()
                .code(code)
                .name(request.getName().trim())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : java.math.BigDecimal.ZERO)
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .maxUsesTotal(request.getMaxUsesTotal())
                .maxUsesPerCustomer(request.getMaxUsesPerCustomer())
                .active(true)
                .build();

        Offer saved = repository.save(offer);

        log.info("Created offer id={} code={}", saved.getId(), saved.getCode());

        return OfferMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "offers", key = "'all'")
    public List<OfferResponse> getAll() {

        return repository.findAll().stream().map(OfferMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "offerById", key = "#id")
    public OfferResponse getById(Long id) {

        Offer offer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found."));

        return OfferMapper.toResponse(offer);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "offers", allEntries = true),
            @CacheEvict(value = "offerById", key = "#id"),
            @CacheEvict(value = "activeOffers", allEntries = true)
    })
    public OfferResponse update(Long id, UpdateOfferRequest request) {

        Offer offer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found."));

        if (request.getName() != null && !request.getName().isBlank()) {
            offer.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            offer.setDescription(request.getDescription());
        }

        if (request.getDiscountType() != null) {
            offer.setDiscountType(request.getDiscountType());
        }

        if (request.getDiscountValue() != null) {
            offer.setDiscountValue(request.getDiscountValue());
        }

        if (request.getMaxDiscountAmount() != null) {
            offer.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }

        if (request.getMinOrderAmount() != null) {
            offer.setMinOrderAmount(request.getMinOrderAmount());
        }

        if (request.getStartsAt() != null) {
            offer.setStartsAt(request.getStartsAt());
        }

        if (request.getEndsAt() != null) {
            offer.setEndsAt(request.getEndsAt());
        }

        LocalDateTime starts = offer.getStartsAt();
        LocalDateTime ends = offer.getEndsAt();

        if (starts != null && ends != null && ends.isBefore(starts)) {
            throw new IllegalArgumentException("endsAt cannot be before startsAt");
        }

        if (request.getMaxUsesTotal() != null) {
            offer.setMaxUsesTotal(request.getMaxUsesTotal());
        }

        if (request.getMaxUsesPerCustomer() != null) {
            offer.setMaxUsesPerCustomer(request.getMaxUsesPerCustomer());
        }

        if (request.getActive() != null) {
            offer.setActive(request.getActive());
        }

        Offer updated = repository.save(offer);

        log.info("Updated offer id={} code={}", updated.getId(), updated.getCode());

        return OfferMapper.toResponse(updated);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "offers", allEntries = true),
            @CacheEvict(value = "offerById", key = "#id"),
            @CacheEvict(value = "activeOffers", allEntries = true)
    })
    public void delete(Long id) {

        Offer offer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found."));

        // Soft delete - redemption history references this row.
        offer.setActive(false);
        repository.save(offer);

        log.info("Deactivated offer id={} code={}", offer.getId(), offer.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activeOffers", key = "'current'")
    public List<PublicOfferResponse> getCurrentlyActive() {

        return repository.findCurrentlyActive(LocalDateTime.now()).stream()
                .map(OfferMapper::toPublicResponse)
                .toList();
    }
}
