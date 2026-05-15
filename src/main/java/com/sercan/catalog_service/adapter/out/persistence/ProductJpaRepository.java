package com.sercan.catalog_service.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
    @Query("SELECT p.id FROM ProductEntity p WHERE p.id IN :ids")
    Set<UUID> findExistingIds(@Param("ids") List<UUID> ids);
}
