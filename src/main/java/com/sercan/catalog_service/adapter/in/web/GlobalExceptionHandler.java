package com.sercan.catalog_service.adapter.in.web;

import com.sercan.catalog_service.adapter.in.web.dto.ErrorResponse;
import com.sercan.catalog_service.domain.exception.ProductsNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        log.warn("No resource found: {}", request.getRequestURI());
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(ProductsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductsNotFound(
            ProductsNotFoundException ex,
            HttpServletRequest request) {
        log.warn("Products not found: {}", ex.getMissingIds());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "Products Not Found",
                        "One or more product offerings could not be found",
                        URI.create(request.getRequestURI()),
                        Map.of("missingIds", ex.getMissingIds())
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        log.warn("Constraint violation on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.warn("Unreadable request body on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", "Request body is missing or malformed", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest request) {
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ErrorResponse.of(status.value(), title, detail, URI.create(request.getRequestURI())));
    }
}
