package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.Product;

public final class CartItemDto {

    public ProductDto product;
    public int quantity;
    @Nullable
    public String selectedSize = null;

    public CartItemDto() {
    }

    @NonNull
    public CartItem toDomain() {
        Product domainProduct = product.toDomain();
        String id = selectedSize == null
                ? domainProduct.getId()
                : domainProduct.getId() + "_" + selectedSize;
        return new CartItem(
                id,
                domainProduct.getId(),
                domainProduct.getName(),
                domainProduct.getBrand(),
                domainProduct.getPrice(),
                domainProduct.getImageUrl(),
                selectedSize,
                quantity);
    }
}
