package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class CartItem {

    private final String id;
    private final String productId;
    private final String name;
    private final String brand;
    private final double price;
    private final String imageUrl;
    private final String variant;
    private final int quantity;

    public CartItem(@NonNull String id,
                    @NonNull String productId,
                    @NonNull String name,
                    @NonNull String brand,
                    double price,
                    @NonNull String imageUrl,
                    @Nullable String variant,
                    int quantity) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.imageUrl = imageUrl;
        this.variant = variant;
        this.quantity = quantity;
    }

    @NonNull
    public String getId() {
        return id;
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

    @Nullable
    public String getVariant() {
        return variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    @NonNull
    public CartItem withQuantity(int newQuantity) {
        return new CartItem(id, productId, name, brand, price, imageUrl, variant, newQuantity);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem other = (CartItem) o;
        return Double.compare(price, other.price) == 0
                && quantity == other.quantity
                && id.equals(other.id)
                && productId.equals(other.productId)
                && name.equals(other.name)
                && brand.equals(other.brand)
                && imageUrl.equals(other.imageUrl)
                && Objects.equals(variant, other.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productId, name, brand, price, imageUrl, variant, quantity);
    }

    @NonNull
    @Override
    public String toString() {
        return "CartItem(id=" + id + ", productId=" + productId + ", quantity=" + quantity + ")";
    }
}
