package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.repository.OrderRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetOrdersUseCase {

    private final OrderRepository repository;

    @Inject
    public GetOrdersUseCase(OrderRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<List<Order>> execute() {
        return repository.getOrders();
    }
}
