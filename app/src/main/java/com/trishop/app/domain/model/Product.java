package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Product {

    private final String id;
    private final String name;
    private final String description;
    private final double price;
    private final Double originalPrice;
    private final String imageUrl;
    private final List<String> images;
    private final String category;
    private final String brand;
    private final float rating;
    private final int reviewCount;
    private final List<String> sizes;
    private final boolean inStock;

    public Product(@NonNull String id,
                   @NonNull String name,
                   @NonNull String description,
                   double price,
                   @Nullable Double originalPrice,
                   @NonNull String imageUrl,
                   @NonNull List<String> images,
                   @NonNull String category,
                   @NonNull String brand,
                   float rating,
                   int reviewCount,
                   @NonNull List<String> sizes,
                   boolean inStock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.imageUrl = imageUrl;
        this.images = Collections.unmodifiableList(images);
        this.category = category;
        this.brand = brand;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.sizes = Collections.unmodifiableList(sizes);
        this.inStock = inStock;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    @Nullable
    public Double getOriginalPrice() {
        return originalPrice;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    @NonNull
    public List<String> getImages() {
        return images;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    @NonNull
    public String getBrand() {
        return brand;
    }

    public float getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    @NonNull
    public List<String> getSizes() {
        return sizes;
    }

    public boolean isInStock() {
        return inStock;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product other = (Product) o;
        return Double.compare(price, other.price) == 0
                && Float.compare(rating, other.rating) == 0
                && reviewCount == other.reviewCount
                && inStock == other.inStock
                && id.equals(other.id)
                && name.equals(other.name)
                && description.equals(other.description)
                && Objects.equals(originalPrice, other.originalPrice)
                && imageUrl.equals(other.imageUrl)
                && images.equals(other.images)
                && category.equals(other.category)
                && brand.equals(other.brand)
                && sizes.equals(other.sizes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, price, originalPrice, imageUrl, images,
                category, brand, rating, reviewCount, sizes, inStock);
    }

    @NonNull
    @Override
    public String toString() {
        return "Product(id=" + id + ", name=" + name + ", price=" + price + ")";
    }
}
