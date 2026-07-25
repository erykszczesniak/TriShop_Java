package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.Coupon;

public final class CouponValidationResponse {

    public String code;
    public double discountPercent = 0.0;
    public boolean valid = false;
    @Nullable
    public String message = null;

    public CouponValidationResponse() {
    }

    @NonNull
    public Coupon toDomain() {
        return new Coupon(code, discountPercent, valid, message);
    }
}
