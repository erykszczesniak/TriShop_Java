package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.WishlistItem;
import com.trishop.app.domain.repository.WishlistRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;

public class ObserveWishlistUseCase {

    private final WishlistRepository repository;

    @Inject
    public ObserveWishlistUseCase(WishlistRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Flowable<List<WishlistItem>> execute() {
        return repository.observeWishlist();
    }
}
