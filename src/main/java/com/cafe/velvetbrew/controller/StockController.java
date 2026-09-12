package com.cafe.velvetbrew.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cafe.velvetbrew.dto.StockAdjustmentRequest;
import com.cafe.velvetbrew.dto.StockMovementResponse;
import com.cafe.velvetbrew.dto.StockOperationRequest;
import com.cafe.velvetbrew.dto.StockOperationResponse;
import com.cafe.velvetbrew.service.StockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory/items")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/{itemId}/stock")
    public ResponseEntity<StockOperationResponse> addStock(
            @PathVariable Long itemId,
            @Valid @RequestBody StockOperationRequest request) {

        return ResponseEntity.ok(stockService.addStock(itemId, request));
    }

    @PostMapping("/{itemId}/consume")
    public ResponseEntity<StockOperationResponse> consumeStock(
            @PathVariable Long itemId,
            @Valid @RequestBody StockOperationRequest request) {

        return ResponseEntity.ok(stockService.consumeStock(itemId, request));
    }

    @PostMapping("/{itemId}/wastage")
    public ResponseEntity<StockOperationResponse> wastage(
            @PathVariable Long itemId,
            @Valid @RequestBody StockOperationRequest request) {

        return ResponseEntity.ok(stockService.recordWastage(itemId, request));
    }

    @PostMapping("/{itemId}/adjustment")
    public ResponseEntity<StockOperationResponse> adjustment(
            @PathVariable Long itemId,
            @Valid @RequestBody StockAdjustmentRequest request) {

        return ResponseEntity.ok(stockService.adjustStock(itemId, request));
    }

    @PostMapping("/{itemId}/return")
    public ResponseEntity<StockOperationResponse> returnStock(
            @PathVariable Long itemId,
            @Valid @RequestBody StockOperationRequest request) {

        return ResponseEntity.ok(stockService.returnStock(itemId, request));
    }

    @GetMapping("/{itemId}/movements")
    public ResponseEntity<List<StockMovementResponse>> getMovements(
            @PathVariable Long itemId) {

        return ResponseEntity.ok(stockService.getMovements(itemId));
    }
}
