package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.data.local.prefs.RecentlyViewedStore;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class ClearRecentlyViewedUseCase {

    private final RecentlyViewedStore store;

    @Inject
    public ClearRecentlyViewedUseCase(RecentlyViewedStore store) {
        this.store = store;
    }

    @NonNull
    public Completable execute() {
        return store.clear();
    }
}
