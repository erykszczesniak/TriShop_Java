package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class OrderItem {

    private final String id;
    private final String productId;
    private final String productName;
    private final String productImageUrl;
    private final int quantity;
    private final double price;
    private final String selectedSize;

    public OrderItem(@NonNull String id,
                     @NonNull String productId,
                     @NonNull String productName,
                     @NonNull String productImageUrl,
                     int quantity,
                     double price,
                     @Nullable String selectedSize) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.quantity = quantity;
        this.price = price;
        this.selectedSize = selectedSize;
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
    public String getProductName() {
        return productName;
    }

    @NonNull
    public String getProductImageUrl() {
        return productImageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    @Nullable
    public String getSelectedSize() {
        return selectedSize;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem other = (OrderItem) o;
        return quantity == other.quantity
                && Double.compare(price, other.price) == 0
                && id.equals(other.id)
                && productId.equals(other.productId)
                && productName.equals(other.productName)
                && productImageUrl.equals(other.productImageUrl)
                && Objects.equals(selectedSize, other.selectedSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productId, productName, productImageUrl, quantity, price, selectedSize);
    }

    @NonNull
    @Override
    public String toString() {
        return "OrderItem(id=" + id + ", productName=" + productName + ", quantity=" + quantity + ")";
    }
}
