package com.trishop.app.domain.model;

import androidx.annotation.NonNull;

/** Server-side sort orders supported by the products endpoints. */
public enum ProductSort {
    NEWEST("createdAt,desc"),
    PRICE_ASCENDING("price,asc"),
    PRICE_DESCENDING("price,desc"),
    TOP_RATED("rating,desc");

    private final String apiValue;

    ProductSort(String apiValue) {
        this.apiValue = apiValue;
    }

    @NonNull
    public String getApiValue() {
        return apiValue;
    }
}
