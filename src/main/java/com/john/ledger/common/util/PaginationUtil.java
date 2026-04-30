package com.john.ledger.common.util;

import org.springframework.data.domain.Page;

public class PaginationUtil {

    // Converts Spring Page<T> to PaginatedResponse<T>
    public static <T> PaginatedResponse<T> createPaginatedResponse(Page<T> page) {
        PaginationMeta meta = new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getNumberOfElements()
        );

        return new PaginatedResponse<>(page.getContent(), meta);
    }
}
