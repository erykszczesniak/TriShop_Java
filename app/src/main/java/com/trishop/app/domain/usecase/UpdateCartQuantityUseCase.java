package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.CartRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class UpdateCartQuantityUseCase {

    private final CartRepository repository;

    @Inject
    public UpdateCartQuantityUseCase(CartRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Completable execute(@NonNull String id, int quantity) {
        return repository.updateQuantity(id, quantity);
    }
}
