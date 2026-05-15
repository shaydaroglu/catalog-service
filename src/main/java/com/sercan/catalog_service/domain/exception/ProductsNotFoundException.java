package com.sercan.catalog_service.domain.exception;

import java.util.List;
import java.util.UUID;

public class ProductsNotFoundException extends RuntimeException {
    private final List<UUID> missingIds;

    public ProductsNotFoundException(List<UUID> missingIds) {
        super("Products not found: " + missingIds);
        this.missingIds = missingIds;
    }

    public List<UUID> getMissingIds() {
        return missingIds;
    }
}
