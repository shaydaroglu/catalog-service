package com.sercan.catalog_service.adapter.out.persistence;

import com.sercan.catalog_service.application.port.out.ProductRepository;
import com.sercan.catalog_service.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductPersistenceAdapter implements ProductRepository {
    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    @Override
    public Optional<Product> findById(UUID id) {
        log.debug("DB lookup product id={}", id);
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAllByIds(List<UUID> ids) {
        log.debug("DB lookup {} product IDs", ids.size());
        return jpaRepository.findAllByIdIn(ids)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        log.debug("DB fetch products page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }
}
