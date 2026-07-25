package com.trishop.app.domain.model;

import android.util.Range;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/** Filter selection shared by the search screen and the home dashboard. */
public final class ProductFilters {

    public static final ProductFilters EMPTY = new ProductFilters(
            Collections.emptySet(), Collections.emptySet(), Collections.emptySet());

    private final Set<ProductCategory> categories;
    private final Set<Integer> priceBuckets;
    private final Set<String> brands;

    public ProductFilters(@NonNull Set<ProductCategory> categories,
                          @NonNull Set<Integer> priceBuckets,
                          @NonNull Set<String> brands) {
        this.categories = Collections.unmodifiableSet(categories);
        this.priceBuckets = Collections.unmodifiableSet(priceBuckets);
        this.brands = Collections.unmodifiableSet(brands);
    }

    @NonNull
    public Set<ProductCategory> getCategories() {
        return categories;
    }

    @NonNull
    public Set<Integer> getPriceBuckets() {
        return priceBuckets;
    }

    @NonNull
    public Set<String> getBrands() {
        return brands;
    }

    public boolean isActive() {
        return !equals(EMPTY);
    }

    public int getActiveCount() {
        return categories.size() + priceBuckets.size() + brands.size();
    }

    /** Combined min/max across the selected price buckets (like the web shop). */
    @Nullable
    public Range<Double> getPriceRange() {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        boolean any = false;
        for (Integer index : priceBuckets) {
            if (index == null || index < 0 || index >= PriceBucket.ALL.size()) continue;
            PriceBucket bucket = PriceBucket.ALL.get(index);
            min = Math.min(min, bucket.getMin());
            max = Math.max(max, bucket.getMax());
            any = true;
        }
        return any ? new Range<>(min, max) : null;
    }

    public boolean matches(@NonNull Product product) {
        if (!categories.isEmpty()
                && !categories.contains(ProductCategory.fromApiValue(product.getCategory()))) {
            return false;
        }
        Range<Double> range = getPriceRange();
        if (range != null && !range.contains(product.getPrice())) {
            return false;
        }
        return brands.isEmpty() || brands.contains(product.getBrand());
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductFilters)) return false;
        ProductFilters other = (ProductFilters) o;
        return categories.equals(other.categories)
                && priceBuckets.equals(other.priceBuckets)
                && brands.equals(other.brands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categories, priceBuckets, brands);
    }

    @NonNull
    @Override
    public String toString() {
        return "ProductFilters(categories=" + categories
                + ", priceBuckets=" + priceBuckets
                + ", brands=" + brands + ")";
    }
}
