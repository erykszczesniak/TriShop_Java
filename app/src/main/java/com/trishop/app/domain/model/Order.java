package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Order {

    private final String id;
    private final List<OrderItem> items;
    private final double totalPrice;
    private final String status;
    private final String shippingAddress;
    private final String createdAt;
    private final List<OrderStatusEvent> statusHistory;
    private final Integer discountPercent;
    private final String couponCode;

    public Order(@NonNull String id,
                 @NonNull List<OrderItem> items,
                 double totalPrice,
                 @NonNull String status,
                 @NonNull String shippingAddress,
                 @NonNull String createdAt,
                 @NonNull List<OrderStatusEvent> statusHistory,
                 @Nullable Integer discountPercent,
                 @Nullable String couponCode) {
        this.id = id;
        this.items = Collections.unmodifiableList(items);
        this.totalPrice = totalPrice;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.createdAt = createdAt;
        this.statusHistory = Collections.unmodifiableList(statusHistory);
        this.discountPercent = discountPercent;
        this.couponCode = couponCode;
    }

    public Order(@NonNull String id,
                 @NonNull List<OrderItem> items,
                 double totalPrice,
                 @NonNull String status,
                 @NonNull String shippingAddress,
                 @NonNull String createdAt) {
        this(id, items, totalPrice, status, shippingAddress, createdAt,
                Collections.emptyList(), null, null);
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public List<OrderItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    @NonNull
    public String getShippingAddress() {
        return shippingAddress;
    }

    @NonNull
    public String getCreatedAt() {
        return createdAt;
    }

    @NonNull
    public List<OrderStatusEvent> getStatusHistory() {
        return statusHistory;
    }

    @Nullable
    public Integer getDiscountPercent() {
        return discountPercent;
    }

    @Nullable
    public String getCouponCode() {
        return couponCode;
    }

    public int getItemCount() {
        int count = 0;
        for (OrderItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order other = (Order) o;
        return Double.compare(totalPrice, other.totalPrice) == 0
                && id.equals(other.id)
                && items.equals(other.items)
                && status.equals(other.status)
                && shippingAddress.equals(other.shippingAddress)
                && createdAt.equals(other.createdAt)
                && statusHistory.equals(other.statusHistory)
                && Objects.equals(discountPercent, other.discountPercent)
                && Objects.equals(couponCode, other.couponCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, items, totalPrice, status, shippingAddress, createdAt,
                statusHistory, discountPercent, couponCode);
    }

    @NonNull
    @Override
    public String toString() {
        return "Order(id=" + id + ", status=" + status + ", totalPrice=" + totalPrice + ")";
    }
}
