package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.data.local.prefs.RecentlyViewedStore;
import com.trishop.app.domain.model.Product;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;

public class ObserveRecentlyViewedUseCase {

    private final RecentlyViewedStore store;

    @Inject
    public ObserveRecentlyViewedUseCase(RecentlyViewedStore store) {
        this.store = store;
    }

    @NonNull
    public Flowable<List<Product>> execute() {
        return store.recentlyViewed();
    }
}
