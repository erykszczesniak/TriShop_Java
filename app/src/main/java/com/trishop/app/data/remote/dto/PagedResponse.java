package com.trishop.app.data.remote.dto;

import java.util.List;

public final class PagedResponse<T> {

    public List<T> content;
    public PageInfo page;

    public PagedResponse() {
    }

    public boolean isLast() {
        return page.number >= page.totalPages - 1;
    }
}
