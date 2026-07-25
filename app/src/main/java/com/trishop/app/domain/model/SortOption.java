package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Sort choices offered on the search screen; relevance is client-only. */
public enum SortOption {
    RELEVANCE,
    PRICE_ASCENDING,
    PRICE_DESCENDING,
    RATING_DESCENDING,
    NEWEST;

    /** Matching server-side sort order, if one exists. */
    @Nullable
    public ProductSort getServerSort() {
        switch (this) {
            case PRICE_ASCENDING:
                return ProductSort.PRICE_ASCENDING;
            case PRICE_DESCENDING:
                return ProductSort.PRICE_DESCENDING;
            case RATING_DESCENDING:
                return ProductSort.TOP_RATED;
            case NEWEST:
                return ProductSort.NEWEST;
            case RELEVANCE:
            default:
                return null;
        }
    }

    @NonNull
    public static SortOption from(@NonNull ProductSort serverSort) {
        switch (serverSort) {
            case PRICE_ASCENDING:
                return PRICE_ASCENDING;
            case PRICE_DESCENDING:
                return PRICE_DESCENDING;
            case TOP_RATED:
                return RATING_DESCENDING;
            case NEWEST:
            default:
                return NEWEST;
        }
    }
}
