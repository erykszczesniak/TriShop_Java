package com.trishop.app.domain.repository;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.WishlistItem;

import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface WishlistRepository {

    @NonNull
    Flowable<List<WishlistItem>> observeWishlist();

    @NonNull
    Flowable<Set<String>> observeWishlistIds();

    @NonNull
    Single<Boolean> contains(@NonNull String productId);

    /** Adds or removes the product; emits {@code true} when it ended up saved. */
    @NonNull
    Single<Boolean> toggle(@NonNull Product product);

    @NonNull
    Completable remove(@NonNull String productId);

    @NonNull
    Completable sync();
}
