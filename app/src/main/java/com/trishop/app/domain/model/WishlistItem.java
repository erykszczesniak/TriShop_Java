package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WishlistItem {

    private final String productId;
    private final String name;
    private final String brand;
    private final double price;
    private final String imageUrl;
    private final List<String> sizes;

    public WishlistItem(@NonNull String productId,
                        @NonNull String name,
                        @NonNull String brand,
                        double price,
                        @NonNull String imageUrl,
                        @NonNull List<String> sizes) {
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.imageUrl = imageUrl;
        this.sizes = Collections.unmodifiableList(sizes);
    }

    public WishlistItem(@NonNull String productId,
                        @NonNull String name,
                        @NonNull String brand,
                        double price,
                        @NonNull String imageUrl) {
        this(productId, name, brand, price, imageUrl, Collections.emptyList());
    }

    @NonNull
    public String getProductId() {
        return productId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    @NonNull
    public List<String> getSizes() {
        return sizes;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof WishlistItem)) return false;
        WishlistItem other = (WishlistItem) o;
        return Double.compare(price, other.price) == 0
                && productId.equals(other.productId)
                && name.equals(other.name)
                && brand.equals(other.brand)
                && imageUrl.equals(other.imageUrl)
                && sizes.equals(other.sizes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, brand, price, imageUrl, sizes);
    }

    @NonNull
    @Override
    public String toString() {
        return "WishlistItem(productId=" + productId + ", name=" + name + ")";
    }
}
