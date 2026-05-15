package com.sercan.catalog_service.adapter.in.web.dto;

import com.sercan.catalog_service.domain.Product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.price()
        );
    }
}
