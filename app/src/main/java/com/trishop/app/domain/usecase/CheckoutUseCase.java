package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.repository.CartRepository;
import com.trishop.app.domain.repository.OrderRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class CheckoutUseCase {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Inject
    public CheckoutUseCase(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    @NonNull
    public Single<Order> execute(@NonNull List<CartItem> items,
                                 @NonNull String shippingAddress,
                                 @Nullable String couponCode) {
        if (items.isEmpty()) {
            return Single.error(new IllegalStateException("Cart is empty"));
        }
        if (shippingAddress.trim().isEmpty()) {
            return Single.error(new IllegalStateException("Shipping address required"));
        }
        return orderRepository.placeOrder(items, shippingAddress, couponCode)
                .flatMap(order -> cartRepository.clear().andThen(Single.just(order)));
    }
}
