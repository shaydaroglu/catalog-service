package com.sercan.catalog_service.application.port.in;

import com.sercan.catalog_service.domain.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductUseCase {
    Optional<Product> findById(UUID id);
    List<Product> findAllByIds(List<UUID> ids);
}
