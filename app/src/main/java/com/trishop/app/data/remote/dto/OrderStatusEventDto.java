package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.OrderStatusEvent;

public final class OrderStatusEventDto {

    public String status;
    public String timestamp;

    public OrderStatusEventDto() {
    }

    @NonNull
    public OrderStatusEvent toDomain() {
        return new OrderStatusEvent(status, timestamp);
    }
}
