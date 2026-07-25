package com.trishop.app.data.remote.dto;

import androidx.annotation.Nullable;

/**
 * Payload sent to {@code POST /api/v1/cart} to push a local cart item into the
 * user's server-side cart. {@code quantity} defaults to 1 to mirror the backend.
 */
public final class CartItemRequest {

    public final long productId;
    public final int quantity;
    @Nullable
    public final String selectedSize;

    public CartItemRequest(long productId, int quantity, @Nullable String selectedSize) {
        this.productId = productId;
        this.quantity = quantity;
        this.selectedSize = selectedSize;
    }

    public CartItemRequest(long productId) {
        this(productId, 1, null);
    }
}
