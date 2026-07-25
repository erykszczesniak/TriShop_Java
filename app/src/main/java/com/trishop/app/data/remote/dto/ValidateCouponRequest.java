package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;

public final class ValidateCouponRequest {

    public final String code;
    public final double orderTotal;

    public ValidateCouponRequest(@NonNull String code, double orderTotal) {
        this.code = code;
        this.orderTotal = orderTotal;
    }
}
