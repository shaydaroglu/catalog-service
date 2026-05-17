package com.sercan.catalog_service.adapter.in.web;

import com.sercan.catalog_service.adapter.in.web.dto.ErrorResponse;
import com.sercan.catalog_service.adapter.in.web.dto.PageResponse;
import com.sercan.catalog_service.adapter.in.web.dto.ProductResponse;
import com.sercan.catalog_service.application.port.in.ProductUseCase;
import com.sercan.catalog_service.domain.exception.ProductsNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "List all products",
            description = "Returns a paginated list of all available product offerings."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated list of products"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
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
    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a single product offering by its UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        log.info("Fetching product id={}", id);
        return productUseCase.findById(id)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductsNotFoundException(List.of(id)));
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Validate product offering IDs",
            description = """
                    Validates that all provided product offering IDs exist in the catalog.
                    Used internally by the order service before accepting an order.
                    Returns 200 if all IDs are valid, 404 with the list of missing IDs otherwise.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "All product offering IDs are valid"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Empty or malformed request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or more product offering IDs not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> validate(@RequestBody @NotEmpty(message = "Product IDs list must not be empty") List<UUID> ids) {
        log.info("Validating {} product IDs", ids.size());
        productUseCase.verifyAllByIds(ids);
        log.debug("All product IDs are valid: {}", ids);
        return ResponseEntity.ok().build();
    }

}
