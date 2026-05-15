package com.sercan.catalog_service.application.service;

import com.sercan.catalog_service.application.port.in.ProductUseCase;
import com.sercan.catalog_service.application.port.out.ProductRepository;
import com.sercan.catalog_service.domain.Product;
import com.sercan.catalog_service.domain.exception.ProductsNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements ProductUseCase {
    private final ProductRepository productRepository;

    @Override
    public Optional<Product> findById(UUID id) {
        log.debug("Looking up product id={}", id);
        Optional<Product> result = productRepository.findById(id);
        if (result.isEmpty()) {
            log.warn("Product not found id={}", id);
        }
        return result;
    }

    @Override
    public List<Product> findAllByIds(List<UUID> ids) {
        log.debug("Looking up {} product IDs", ids.size());
        List<Product> found = productRepository.findAllByIds(ids);

        Set<UUID> productIds = found.stream()
                .map(Product::id)
                .collect(Collectors.toSet());

        List<UUID> missingIds = ids.stream()
                .filter(id -> !productIds.contains(id))
                .toList();

        if(!missingIds.isEmpty()) {
            log.warn("Missing product IDs: {}", missingIds);
            throw new ProductsNotFoundException(missingIds);
        }

        log.debug("All {} products found", found.size());
        return found;
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        log.debug("Fetching products page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable);
    }
}
