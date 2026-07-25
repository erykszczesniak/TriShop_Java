package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** One page of products plus the total count reported by the backend. */
public final class ProductPage {

    private final List<Product> products;
    private final int totalItems;

    public ProductPage(@NonNull List<Product> products, int totalItems) {
        this.products = Collections.unmodifiableList(products);
        this.totalItems = totalItems;
    }

    @NonNull
    public List<Product> getProducts() {
        return products;
    }

    public int getTotalItems() {
        return totalItems;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductPage)) return false;
        ProductPage other = (ProductPage) o;
        return totalItems == other.totalItems && products.equals(other.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(products, totalItems);
    }

    @NonNull
    @Override
    public String toString() {
        return "ProductPage(products=" + products.size() + ", totalItems=" + totalItems + ")";
    }
}
