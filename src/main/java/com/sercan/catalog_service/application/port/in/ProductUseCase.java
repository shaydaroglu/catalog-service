package com.sercan.catalog_service.application.port.in;

import com.sercan.catalog_service.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductUseCase {
    Optional<Product> findById(UUID id);
    void verifyAllByIds(List<UUID> ids);
    Page<Product> findAll(Pageable pageable);
}
