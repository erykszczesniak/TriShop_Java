package com.trishop.app.data.repository;

import androidx.annotation.NonNull;

import com.trishop.app.core.network.EmptyBodyException;
import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.dto.CouponValidationResponse;
import com.trishop.app.data.remote.dto.ValidateCouponRequest;
import com.trishop.app.domain.model.Coupon;
import com.trishop.app.domain.repository.CouponRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import retrofit2.HttpException;

@Singleton
public class CouponRepositoryImpl implements CouponRepository {

    private final TriShopApi api;

    @Inject
    public CouponRepositoryImpl(TriShopApi api) {
        this.api = api;
    }

    @NonNull
    @Override
    public Single<Coupon> validate(@NonNull String code, double orderTotal) {
        return api.validateCoupon(new ValidateCouponRequest(code, orderTotal))
                .map(response -> {
                    if (!response.isSuccessful()) throw new HttpException(response);
                    CouponValidationResponse body = response.body();
                    if (body == null) throw new EmptyBodyException();
                    return body.toDomain();
                });
    }
}
