package com.trishop.app.presentation.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/** Compact product payload streamed by the assistant's {@code products} SSE event. */
public final class ChatProduct {

    private final long id;
    private final String name;
    private final String brand;
    private final String category;
    private final double price;
    private final String imageUrl;

    public ChatProduct(long id,
                       @NonNull String name,
                       @NonNull String brand,
                       @NonNull String category,
                       double price,
                       @NonNull String imageUrl) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getBrand() {
        return brand;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatProduct)) return false;
        ChatProduct other = (ChatProduct) o;
        return id == other.id
                && Double.compare(price, other.price) == 0
                && name.equals(other.name)
                && brand.equals(other.brand)
                && category.equals(other.category)
                && imageUrl.equals(other.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, brand, category, price, imageUrl);
    }
}
