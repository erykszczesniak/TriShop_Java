package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.WishlistRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class SyncWishlistUseCase {

    private final WishlistRepository repository;

    @Inject
    public SyncWishlistUseCase(WishlistRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Completable execute() {
        return repository.sync();
    }
}
