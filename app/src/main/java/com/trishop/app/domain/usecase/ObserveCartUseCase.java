package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.repository.CartRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;

public class ObserveCartUseCase {

    private final CartRepository repository;

    @Inject
    public ObserveCartUseCase(CartRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Flowable<List<CartItem>> execute() {
        return repository.observeCart();
    }
}
