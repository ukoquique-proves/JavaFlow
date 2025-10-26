package com.javaflow.application.dto.common;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Generic paginated response DTO
 */
@Value
@Builder
public class PageResponse<T> {
    
    List<T> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean first;
    boolean last;
    
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
    
    public static <T> PageResponse<T> single(List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements(content.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
    }
}