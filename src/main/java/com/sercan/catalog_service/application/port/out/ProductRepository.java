package com.sercan.catalog_service.application.port.out;

import com.sercan.catalog_service.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
    Page<Product> findAll(Pageable pageable);
    Set<UUID> findExistingIds(List<UUID> ids);
}
