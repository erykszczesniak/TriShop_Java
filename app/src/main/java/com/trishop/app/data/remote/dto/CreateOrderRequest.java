package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class CreateOrderRequest {

    public final String shippingAddress;
    @Nullable
    public final String couponCode;

    public CreateOrderRequest(@NonNull String shippingAddress, @Nullable String couponCode) {
        this.shippingAddress = shippingAddress;
        this.couponCode = couponCode;
    }
}
