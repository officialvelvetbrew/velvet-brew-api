package com.cafe.velvetbrew.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafe.velvetbrew.dto.CreateSupplierRequest;
import com.cafe.velvetbrew.dto.SupplierResponse;
import com.cafe.velvetbrew.dto.UpdateSupplierRequest;
import com.cafe.velvetbrew.entity.Supplier;
import com.cafe.velvetbrew.mapper.SupplierMapper;
import com.cafe.velvetbrew.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;
    private final SupplierMapper mapper;

    @Override
    public SupplierResponse create(
            CreateSupplierRequest request) {

        String name = request.getName().trim();

        if (repository.existsByNameIgnoreCase(name)) {
            log.warn("Supplier creation rejected - duplicate name: {}", name);
            throw new IllegalArgumentException(
                    "Supplier already exists: " + name
            );
        }

        Supplier supplier = new Supplier();

        supplier.setName(name);
        supplier.setContactPerson(
                request.getContactPerson()
        );
        supplier.setPhone(
                request.getPhone()
        );
        supplier.setEmail(
                request.getEmail()
        );
        supplier.setAddress(
                request.getAddress()
        );
        supplier.setTaxNumber(
                request.getTaxNumber()
        );
        supplier.setEnabled(true);

        Supplier saved = repository.save(supplier);

        log.info("Created supplier id={} name={}", saved.getId(), saved.getName());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getById(Long id) {

        Supplier supplier = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Supplier not found: " + id
                        )
                );

        return mapper.toResponse(supplier);
    }

    @Override
    public SupplierResponse update(
            Long id,
            UpdateSupplierRequest request) {

        Supplier supplier = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Supplier not found: " + id
                        )
                );

        if (request.getName() != null
                && !request.getName().isBlank()) {

            String name = request.getName().trim();

            if (!name.equalsIgnoreCase(
                    supplier.getName())) {

                if (repository.existsByNameIgnoreCase(name)) {
                    throw new IllegalArgumentException(
                            "Supplier already exists: " + name
                    );
                }

                supplier.setName(name);
            }
        }

        if (request.getContactPerson() != null) {
            supplier.setContactPerson(
                    request.getContactPerson()
            );
        }

        if (request.getPhone() != null) {
            supplier.setPhone(
                    request.getPhone()
            );
        }

        if (request.getEmail() != null) {
            supplier.setEmail(
                    request.getEmail()
            );
        }

        if (request.getAddress() != null) {
            supplier.setAddress(
                    request.getAddress()
            );
        }

        if (request.getTaxNumber() != null) {
            supplier.setTaxNumber(
                    request.getTaxNumber()
            );
        }

        if (request.getEnabled() != null) {
            supplier.setEnabled(
                    request.getEnabled()
            );
        }

        Supplier updated = repository.save(supplier);

        log.info("Updated supplier id={} name={}", updated.getId(), updated.getName());

        return mapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {

        Supplier supplier = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Supplier not found: " + id
                        )
                );

        // Soft delete
        supplier.setEnabled(false);

        repository.save(supplier);

        log.info("Deleted (disabled) supplier id={} name={}", supplier.getId(), supplier.getName());
    }
}