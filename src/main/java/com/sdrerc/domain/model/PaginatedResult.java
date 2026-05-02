package com.sdrerc.domain.model;

import java.util.Collections;
import java.util.List;

public class PaginatedResult<T> {

    private final List<T> data;
    private final int page;
    private final int pageSize;
    private final int totalRecords;
    private final int totalPages;

    public PaginatedResult(List<T> data, int page, int pageSize, int totalRecords, int totalPages) {
        this.data = data == null ? Collections.emptyList() : data;
        this.page = page;
        this.pageSize = pageSize;
        this.totalRecords = totalRecords;
        this.totalPages = totalPages;
    }

    public List<T> getData() {
        return data;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
