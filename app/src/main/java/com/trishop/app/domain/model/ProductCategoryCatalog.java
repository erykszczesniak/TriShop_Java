package com.trishop.app.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static list of product categories, mirroring the iOS {@code ProductCategory} enum.
 * The backend does not expose a {@code /categories} endpoint, so the catalog is
 * derived from the shared {@link ProductCategory} taxonomy.
 */
public final class ProductCategoryCatalog {

    public static final List<Category> ALL;

    static {
        List<Category> categories = new ArrayList<>();
        for (ProductCategory category : ProductCategory.values()) {
            categories.add(new Category(category.getApiValue(), category.getDisplayName(), null));
        }
        ALL = Collections.unmodifiableList(categories);
    }

    private ProductCategoryCatalog() {
    }
}
