package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.CreateOfferRequest;
import com.cafe.velvetbrew.dto.OfferResponse;
import com.cafe.velvetbrew.dto.UpdateOfferRequest;
import com.cafe.velvetbrew.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/offers")
@RequiredArgsConstructor
public class AdminOfferController {

    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<OfferResponse> create(
            @Valid @RequestBody CreateOfferRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OfferResponse>> getAll() {

        return ResponseEntity.ok(offerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferResponse> getById(@PathVariable Long id) {

        return ResponseEntity.ok(offerService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OfferResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOfferRequest request) {

        return ResponseEntity.ok(offerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        offerService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
