package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.CreateOfferRequest;
import com.cafe.velvetbrew.dto.OfferResponse;
import com.cafe.velvetbrew.dto.PublicOfferResponse;
import com.cafe.velvetbrew.dto.UpdateOfferRequest;

import java.util.List;

public interface OfferService {

    OfferResponse create(CreateOfferRequest request);

    List<OfferResponse> getAll();

    OfferResponse getById(Long id);

    OfferResponse update(Long id, UpdateOfferRequest request);

    void delete(Long id);

    List<PublicOfferResponse> getCurrentlyActive();
}
