package com.trishop.app.data.remote.dto;

import java.util.Collections;
import java.util.List;

public final class CartResponse {

    public List<CartItemDto> items = Collections.emptyList();
    public double totalPrice = 0.0;
    public int totalItems = 0;

    public CartResponse() {
    }
}
