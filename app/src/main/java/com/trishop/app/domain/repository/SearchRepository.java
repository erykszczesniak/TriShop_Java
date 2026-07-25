package com.trishop.app.domain.repository;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Product;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface SearchRepository {

    /** Free-text server search; filtering and sorting happen client-side. */
    @NonNull
    Single<List<Product>> searchProducts(@NonNull String query);

    @NonNull
    Flowable<List<String>> recentSearches();

    @NonNull
    Completable saveRecentSearch(@NonNull String query);

    @NonNull
    Completable clearRecentSearches();
}
