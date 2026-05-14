package com.sercan.catalog_service.adapter.out.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findAllByIdIn(List<UUID> ids);
}
