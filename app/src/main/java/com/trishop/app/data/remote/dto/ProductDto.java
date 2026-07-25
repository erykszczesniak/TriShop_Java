package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.BuildConfig;
import com.trishop.app.domain.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ProductDto {

    public long id;
    public String name;
    public String description;
    public double price;
    @Nullable
    public Double originalPrice = null;
    public String imageUrl;
    public List<String> images = Collections.emptyList();
    public String category;
    public String brand;
    public float rating = 0f;
    public int reviewCount = 0;
    public List<String> sizes = Collections.emptyList();
    public int stockQuantity = 0;
    public boolean inStock = true;

    public ProductDto() {
    }

    @NonNull
    public Product toDomain() {
        List<String> absoluteImages = new ArrayList<>();
        for (String image : images) {
            absoluteImages.add(absoluteImageUrl(image));
        }
        return new Product(
                String.valueOf(id),
                name,
                description,
                price,
                originalPrice,
                absoluteImageUrl(imageUrl),
                absoluteImages,
                category,
                brand,
                rating,
                reviewCount,
                sizes,
                inStock);
    }

    // Backend returns image paths relative to the API host.
    private static String absoluteImageUrl(String url) {
        return url.startsWith("/") ? UserDto.trimTrailingSlash(BuildConfig.BASE_URL) + url : url;
    }
}
