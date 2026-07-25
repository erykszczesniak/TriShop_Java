package com.trishop.app.domain.repository;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Coupon;

import io.reactivex.rxjava3.core.Single;

public interface CouponRepository {

    @NonNull
    Single<Coupon> validate(@NonNull String code, double orderTotal);
}
