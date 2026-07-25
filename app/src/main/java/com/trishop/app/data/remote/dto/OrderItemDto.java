package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.OrderItem;

public final class OrderItemDto {

    public long id;
    public long productId;
    public String productName;
    public String productImageUrl;
    public int quantity;
    public double price;
    @Nullable
    public String selectedSize = null;

    public OrderItemDto() {
    }

    @NonNull
    public OrderItem toDomain() {
        return new OrderItem(
                String.valueOf(id),
                String.valueOf(productId),
                productName,
                productImageUrl,
                quantity,
                price,
                selectedSize);
    }
}
