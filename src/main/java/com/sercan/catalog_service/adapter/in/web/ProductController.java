package com.sercan.catalog_service.adapter.in.web;

import com.sercan.catalog_service.adapter.in.web.dto.ProductResponse;
import com.sercan.catalog_service.application.port.in.ProductUseCase;
import com.sercan.catalog_service.domain.Product;
import com.sercan.catalog_service.domain.exception.ProductsNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        return productUseCase.findById(id)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductsNotFoundException(List.of(id)));
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validate(@RequestBody List<UUID> ids) {
        productUseCase.findAllByIds(ids);

        return ResponseEntity.ok().build();
    }

}
