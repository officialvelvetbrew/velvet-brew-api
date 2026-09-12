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

import com.cafe.velvetbrew.dto.CreateInventoryCategoryRequest;
import com.cafe.velvetbrew.dto.InventoryCategoryResponse;
import com.cafe.velvetbrew.dto.UpdateInventoryCategoryRequest;
import com.cafe.velvetbrew.service.InventoryCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory/categories")
@RequiredArgsConstructor
public class InventoryCategoryController {

    private final InventoryCategoryService service;

    @PostMapping
    public ResponseEntity<InventoryCategoryResponse> create(
            @Valid
            @RequestBody
            CreateInventoryCategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<InventoryCategoryResponse>> getAll() {

        return ResponseEntity.ok(
                service.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryCategoryResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InventoryCategoryResponse> update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateInventoryCategoryRequest request) {

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