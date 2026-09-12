package com.cafe.velvetbrew.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cafe.velvetbrew.dto.CreateInventoryItemRequest;
import com.cafe.velvetbrew.dto.InventoryItemResponse;
import com.cafe.velvetbrew.dto.UpdateInventoryItemRequest;
import com.cafe.velvetbrew.service.InventoryItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory/items")
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventoryItemService service;

    @PostMapping
    public ResponseEntity<InventoryItemResponse> create(
            @Valid
            @RequestBody
            CreateInventoryItemRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> getAll() {

        return ResponseEntity.ok(
                service.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InventoryItemResponse> update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateInventoryItemRequest request) {

        return ResponseEntity.ok(
                service.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}