package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.repository.OrderRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetOrderUseCase {

    private final OrderRepository repository;

    @Inject
    public GetOrderUseCase(OrderRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<Order> execute(@NonNull String id) {
        return repository.getOrder(id);
    }
}
