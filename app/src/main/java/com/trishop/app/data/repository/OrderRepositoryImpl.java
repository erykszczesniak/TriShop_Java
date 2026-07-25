package com.trishop.app.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.core.network.EmptyBodyException;
import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.dto.CreateOrderRequest;
import com.trishop.app.data.remote.dto.OrderDto;
import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

@Singleton
public class OrderRepositoryImpl implements OrderRepository {

    private static final int ORDERS_PAGE_SIZE = 50;

    private final TriShopApi api;

    @Inject
    public OrderRepositoryImpl(TriShopApi api) {
        this.api = api;
    }

    @NonNull
    @Override
    public Single<Order> placeOrder(@NonNull List<CartItem> items,
                                    @NonNull String shippingAddress,
                                    @Nullable String couponCode) {
        // The server cart is kept in sync as items are added, so the order
        // payload only carries shipping details; items come from `/cart`.
        return api.createOrder(new CreateOrderRequest(shippingAddress, couponCode))
                .map(OrderRepositoryImpl::requireOrder);
    }

    @NonNull
    @Override
    public Single<List<Order>> getOrders() {
        return api.getOrders(0, ORDERS_PAGE_SIZE).map(response -> {
            if (!response.isSuccessful()) throw new HttpException(response);
            if (response.body() == null) throw new EmptyBodyException();
            List<Order> orders = new ArrayList<>();
            for (OrderDto dto : response.body().content) {
                orders.add(dto.toDomain());
            }
            return orders;
        });
    }

    @NonNull
    @Override
    public Single<Order> getOrder(@NonNull String id) {
        return api.getOrder(id).map(OrderRepositoryImpl::requireOrder);
    }

    @NonNull
    @Override
    public Single<byte[]> downloadInvoice(@NonNull String id) {
        return api.getOrderInvoice(id).map(response -> {
            if (!response.isSuccessful()) throw new HttpException(response);
            ResponseBody body = response.body();
            if (body == null) throw new EmptyBodyException();
            return body.bytes();
        });
    }

    private static Order requireOrder(Response<OrderDto> response) {
        if (!response.isSuccessful()) throw new HttpException(response);
        OrderDto body = response.body();
        if (body == null) throw new EmptyBodyException();
        return body.toDomain();
    }
}
