package com.sercan.catalog_service.application.service;

import com.sercan.catalog_service.application.port.in.ProductUseCase;
import com.sercan.catalog_service.application.port.out.ProductRepository;
import com.sercan.catalog_service.domain.Product;
import com.sercan.catalog_service.domain.exception.ProductsNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {
    private ProductRepository productRepository;

    @Override
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> findAllByIds(List<UUID> ids) {
        List<Product> found = productRepository.findAllByIds(ids);

        Set<UUID> productIds = found.stream()
                .map(Product::id)
                .collect(Collectors.toSet());

        List<UUID> missingIds = ids.stream()
                .filter(id -> !productIds.contains(id))
                .toList();

        if(!missingIds.isEmpty()) {
            throw new ProductsNotFoundException(missingIds);
        }

        return found;
    }
}
