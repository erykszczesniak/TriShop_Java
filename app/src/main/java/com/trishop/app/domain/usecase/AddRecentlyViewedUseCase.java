package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.data.local.prefs.RecentlyViewedStore;
import com.trishop.app.domain.model.Product;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class AddRecentlyViewedUseCase {

    private final RecentlyViewedStore store;

    @Inject
    public AddRecentlyViewedUseCase(RecentlyViewedStore store) {
        this.store = store;
    }

    @NonNull
    public Completable execute(@NonNull Product product) {
        return store.add(product);
    }
}
