package com.cafe.velvetbrew.service;

import java.util.List;

import com.cafe.velvetbrew.dto.CreateSupplierRequest;
import com.cafe.velvetbrew.dto.SupplierResponse;
import com.cafe.velvetbrew.dto.UpdateSupplierRequest;

public interface SupplierService {

	SupplierResponse create(CreateSupplierRequest request);

	List<SupplierResponse> getAll();

	SupplierResponse getById(Long id);

	SupplierResponse update(Long id, UpdateSupplierRequest request);

	void delete(Long id);
}