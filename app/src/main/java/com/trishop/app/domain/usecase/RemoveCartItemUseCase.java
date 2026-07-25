package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.CartRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class RemoveCartItemUseCase {

    private final CartRepository repository;

    @Inject
    public RemoveCartItemUseCase(CartRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Completable execute(@NonNull String id) {
        return repository.remove(id);
    }
}
