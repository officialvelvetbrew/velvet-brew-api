package com.cafe.velvetbrew.service;

import java.util.List;

import com.cafe.velvetbrew.dto.StockAdjustmentRequest;
import com.cafe.velvetbrew.dto.StockMovementResponse;
import com.cafe.velvetbrew.dto.StockOperationRequest;
import com.cafe.velvetbrew.dto.StockOperationResponse;

public interface StockService {

	StockOperationResponse addStock(Long itemId, StockOperationRequest request);

	StockOperationResponse consumeStock(Long itemId, StockOperationRequest request);

	StockOperationResponse recordWastage(Long itemId, StockOperationRequest request);

	StockOperationResponse adjustStock(Long itemId, StockAdjustmentRequest request);

	StockOperationResponse returnStock(Long itemId, StockOperationRequest request);

	List<StockMovementResponse> getMovements(Long itemId);
}