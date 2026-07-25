package com.trishop.app.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.Order;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public interface OrderRepository {

    @NonNull
    Single<Order> placeOrder(@NonNull List<CartItem> items,
                             @NonNull String shippingAddress,
                             @Nullable String couponCode);

    @NonNull
    Single<List<Order>> getOrders();

    @NonNull
    Single<Order> getOrder(@NonNull String id);

    @NonNull
    Single<byte[]> downloadInvoice(@NonNull String id);
}
