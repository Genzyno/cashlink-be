package com.john.ledger.common.util;

import java.util.List;
public class PaginatedResponse<T> {

    private List<T> content;
    private PaginationMeta meta;

    public PaginatedResponse(List<T> content, PaginationMeta meta) {
        this.content = content;
        this.meta = meta;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PaginationMeta getMeta() {
        return meta;
    }

    public void setMeta(PaginationMeta meta) {
        this.meta = meta;
    }

    public static <T> PaginatedResponse<T> empty() {
        return new PaginatedResponse<>(java.util.Collections.emptyList(), new PaginationMeta(0, 0, 0, 0, true, true, 0));
    }
}
