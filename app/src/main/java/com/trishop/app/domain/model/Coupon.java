package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class Coupon {

    private final String code;
    private final double discountPercent;
    private final boolean valid;
    private final String message;

    public Coupon(@NonNull String code, double discountPercent, boolean valid, @Nullable String message) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.valid = valid;
        this.message = message;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public boolean isValid() {
        return valid;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof Coupon)) return false;
        Coupon other = (Coupon) o;
        return Double.compare(discountPercent, other.discountPercent) == 0
                && valid == other.valid
                && code.equals(other.code)
                && Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, discountPercent, valid, message);
    }

    @NonNull
    @Override
    public String toString() {
        return "Coupon(code=" + code + ", discountPercent=" + discountPercent + ", valid=" + valid + ")";
    }
}
