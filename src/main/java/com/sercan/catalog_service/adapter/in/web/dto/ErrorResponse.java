package com.sercan.catalog_service.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        URI type,
        String title,
        int status,
        String detail,
        URI instance,
        OffsetDateTime timestamp,
        Object errors
) {
    public static ErrorResponse of(int status, String title, String detail, URI instance) {
        return new ErrorResponse(
                URI.create("about:blank"),
                title,
                status,
                detail,
                instance,
                OffsetDateTime.now(),
                null
        );
    }

    public static ErrorResponse of(int status, String title, String detail, URI instance, Object errors) {
        return new ErrorResponse(
                URI.create("about:blank"),
                title,
                status,
                detail,
                instance,
                OffsetDateTime.now(),
                errors
        );
    }
}
