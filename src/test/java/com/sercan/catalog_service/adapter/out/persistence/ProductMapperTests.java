package com.sercan.catalog_service.adapter.out.persistence;

import com.sercan.catalog_service.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductMapperTests {
    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    @DisplayName("should map all fields from entity to domain correctly")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        ProductEntity entity = ProductEntity.builder()
                .id(id)
                .name("Product Offering 1")
                .price(BigDecimal.valueOf(10.0000))
                .build();

        Product domain = mapper.toDomain(entity);

        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.name()).isEqualTo("Product Offering 1");
        assertThat(domain.price()).isEqualByComparingTo(BigDecimal.valueOf(10.0000));
    }

    @Test
    @DisplayName("should preserve price precision during mapping")
    void shouldPreservePricePrecision() {
        ProductEntity entity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product")
                .price(new BigDecimal("99.9999"))
                .build();

        Product domain = mapper.toDomain(entity);

        assertThat(domain.price()).isEqualByComparingTo(new BigDecimal("99.9999"));
    }

}
