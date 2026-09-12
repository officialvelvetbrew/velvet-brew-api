package com.cafe.velvetbrew.mapper;

import com.cafe.velvetbrew.dto.OfferResponse;
import com.cafe.velvetbrew.dto.PublicOfferResponse;
import com.cafe.velvetbrew.entity.Offer;

public class OfferMapper {

    private OfferMapper() {
    }

    public static OfferResponse toResponse(Offer offer) {

        return OfferResponse.builder()
                .id(offer.getId())
                .code(offer.getCode())
                .name(offer.getName())
                .description(offer.getDescription())
                .discountType(offer.getDiscountType())
                .discountValue(offer.getDiscountValue())
                .maxDiscountAmount(offer.getMaxDiscountAmount())
                .minOrderAmount(offer.getMinOrderAmount())
                .startsAt(offer.getStartsAt())
                .endsAt(offer.getEndsAt())
                .maxUsesTotal(offer.getMaxUsesTotal())
                .maxUsesPerCustomer(offer.getMaxUsesPerCustomer())
                .currentUses(offer.getCurrentUses())
                .active(offer.getActive())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }

    public static PublicOfferResponse toPublicResponse(Offer offer) {

        return PublicOfferResponse.builder()
                .code(offer.getCode())
                .name(offer.getName())
                .description(offer.getDescription())
                .discountType(offer.getDiscountType())
                .discountValue(offer.getDiscountValue())
                .maxDiscountAmount(offer.getMaxDiscountAmount())
                .minOrderAmount(offer.getMinOrderAmount())
                .endsAt(offer.getEndsAt())
                .build();
    }
}
