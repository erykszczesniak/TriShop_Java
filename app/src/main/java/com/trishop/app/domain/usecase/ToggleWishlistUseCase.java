package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.repository.WishlistRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class ToggleWishlistUseCase {

    private final WishlistRepository repository;

    @Inject
    public ToggleWishlistUseCase(WishlistRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<Boolean> execute(@NonNull Product product) {
        return repository.toggle(product);
    }
}
