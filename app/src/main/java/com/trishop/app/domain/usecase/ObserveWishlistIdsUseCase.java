package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.WishlistRepository;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;

public class ObserveWishlistIdsUseCase {

    private final WishlistRepository repository;

    @Inject
    public ObserveWishlistIdsUseCase(WishlistRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Flowable<Set<String>> execute() {
        return repository.observeWishlistIds();
    }
}
