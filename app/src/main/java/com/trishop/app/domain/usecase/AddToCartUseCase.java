package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.repository.CartRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class AddToCartUseCase {

    private final CartRepository repository;

    @Inject
    public AddToCartUseCase(CartRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Completable execute(@NonNull CartItem item) {
        return repository.upsert(item);
    }
}
