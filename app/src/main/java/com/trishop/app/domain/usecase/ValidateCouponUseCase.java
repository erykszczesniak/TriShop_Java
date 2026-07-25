package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Coupon;
import com.trishop.app.domain.repository.CouponRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class ValidateCouponUseCase {

    private final CouponRepository repository;

    @Inject
    public ValidateCouponUseCase(CouponRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<Coupon> execute(@NonNull String code, double orderTotal) {
        return repository.validate(code, orderTotal);
    }
}
