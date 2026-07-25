package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Fixed price ranges offered by the filter panel (mirrors the web shop). */
public final class PriceBucket {

    public static final double UNBOUNDED_MAX = 999_999.0;

    public static final List<PriceBucket> ALL = Collections.unmodifiableList(Arrays.asList(
            new PriceBucket(0.0, 50.0),
            new PriceBucket(50.0, 200.0),
            new PriceBucket(200.0, 500.0),
            new PriceBucket(500.0, 1000.0),
            new PriceBucket(1000.0, UNBOUNDED_MAX)));

    private final double min;
    private final double max;

    public PriceBucket(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @NonNull
    public String getDisplayName() {
        if (min == 0.0) {
            return "Under " + formatted(max);
        }
        if (max >= UNBOUNDED_MAX) {
            return "Over " + formatted(min);
        }
        return formatted(min) + " – " + formatted(max);
    }

    private static String formatted(double value) {
        return "$" + (int) value;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof PriceBucket)) return false;
        PriceBucket other = (PriceBucket) o;
        return Double.compare(min, other.min) == 0 && Double.compare(max, other.max) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @NonNull
    @Override
    public String toString() {
        return "PriceBucket(min=" + min + ", max=" + max + ")";
    }
}
