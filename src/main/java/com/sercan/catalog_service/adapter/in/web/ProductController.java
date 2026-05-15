package com.sercan.catalog_service.adapter.in.web;

import com.sercan.catalog_service.adapter.in.web.dto.PageResponse;
import com.sercan.catalog_service.adapter.in.web.dto.ProductResponse;
import com.sercan.catalog_service.application.port.in.ProductUseCase;
import com.sercan.catalog_service.domain.exception.ProductsNotFoundException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

    private final ProductUseCase productUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Fetching products page={} size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);

        PageResponse<ProductResponse> response = PageResponse.from(
                productUseCase.findAll(pageable).map(ProductResponse::from));
        log.debug("Returning {} products out of {}", response.items().size(), response.totalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        log.info("Fetching product id={}", id);
        return productUseCase.findById(id)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductsNotFoundException(List.of(id)));
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validate(@RequestBody @NotEmpty(message = "Product IDs list must not be empty") List<UUID> ids) {
        log.info("Validating {} product IDs", ids.size());
        productUseCase.verifyAllByIds(ids);
        log.debug("All product IDs are valid: {}", ids);
        return ResponseEntity.ok().build();
    }

}
