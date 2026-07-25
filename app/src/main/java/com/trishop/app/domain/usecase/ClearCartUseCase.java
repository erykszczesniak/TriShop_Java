package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.CartRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class ClearCartUseCase {

    private final CartRepository repository;

    @Inject
    public ClearCartUseCase(CartRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Completable execute() {
        return repository.clear();
    }
}
