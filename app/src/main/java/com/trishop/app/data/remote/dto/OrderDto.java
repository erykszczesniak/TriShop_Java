package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.model.OrderItem;
import com.trishop.app.domain.model.OrderStatusEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OrderDto {

    public long id;
    public List<OrderItemDto> items;
    public double totalPrice;
    public String status;
    public String shippingAddress;
    // Backend sends `LocalDateTime` as an ISO-8601 string ("2026-04-18T10:30:45").
    // Moshi decodes that straight into a String — no custom adapter needed.
    public String createdAt;
    public List<OrderStatusEventDto> statusHistory = Collections.emptyList();
    @Nullable
    public Integer discountPercent = null;
    @Nullable
    public String couponCode = null;

    public OrderDto() {
    }

    @NonNull
    public Order toDomain() {
        List<OrderItem> domainItems = new ArrayList<>();
        for (OrderItemDto item : items) {
            domainItems.add(item.toDomain());
        }
        List<OrderStatusEvent> domainHistory = new ArrayList<>();
        for (OrderStatusEventDto event : statusHistory) {
            domainHistory.add(event.toDomain());
        }
        return new Order(
                String.valueOf(id),
                domainItems,
                totalPrice,
                status,
                shippingAddress,
                createdAt,
                domainHistory,
                discountPercent,
                couponCode);
    }
}
