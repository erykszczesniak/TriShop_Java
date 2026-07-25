package com.trishop.app.domain.repository;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.CartItem;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

public interface CartRepository {

    @NonNull
    Flowable<List<CartItem>> observeCart();

    @NonNull
    Completable upsert(@NonNull CartItem item);

    @NonNull
    Completable updateQuantity(@NonNull String id, int quantity);

    @NonNull
    Completable remove(@NonNull String id);

    @NonNull
    Completable clear();
}
