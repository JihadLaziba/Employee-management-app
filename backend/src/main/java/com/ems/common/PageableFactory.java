package com.ems.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageableFactory {

    private static final int MAX_SIZE = 100;

    private PageableFactory() {}

    public static Pageable of(int page, int size, String sortBy, String direction,
                              Set<String> allowedSortFields, String defaultSort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        String field = allowedSortFields.contains(sortBy) ? sortBy : defaultSort;
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(dir, field));
    }

    public static String normalizeSearch(String search) {
        return search == null ? "" : search.trim();
    }
}
