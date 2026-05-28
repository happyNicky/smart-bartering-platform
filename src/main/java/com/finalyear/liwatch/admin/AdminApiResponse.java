package com.finalyear.liwatch.admin;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standard wrapper for all admin API responses.
 * Keeps the API contract consistent whether returning a single item or a page.
 */
@Data
@Builder
public class AdminApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // pagination (populated only for list endpoints)
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    /** Wrap a single successful result. */
    public static <T> AdminApiResponse<T> ok(T data) {
        return AdminApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /** Wrap a single successful result with a message. */
    public static <T> AdminApiResponse<T> ok(T data, String message) {
        return AdminApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /** Wrap a Spring Data Page. */
    public static <T> AdminApiResponse<List<T>> ofPage(Page<T> page) {
        return AdminApiResponse.<List<T>>builder()
                .success(true)
                .data(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /** Wrap an error. */
    public static <T> AdminApiResponse<T> error(String message) {
        return AdminApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
