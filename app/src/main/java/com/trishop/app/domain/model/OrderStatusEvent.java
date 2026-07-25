package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class OrderStatusEvent {

    private final String status;
    private final String timestamp;

    public OrderStatusEvent(@NonNull String status, @NonNull String timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    @NonNull
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderStatusEvent)) return false;
        OrderStatusEvent other = (OrderStatusEvent) o;
        return status.equals(other.status) && timestamp.equals(other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, timestamp);
    }

    @NonNull
    @Override
    public String toString() {
        return "OrderStatusEvent(status=" + status + ", timestamp=" + timestamp + ")";
    }
}
