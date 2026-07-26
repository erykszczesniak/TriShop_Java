package com.trishop.app.presentation.profile.orders;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class StatusStep {

    private final String label;
    private final boolean reached;
    private final boolean isLast;
    private final boolean isCurrent;

    public StatusStep(@NonNull String label, boolean reached, boolean isLast, boolean isCurrent) {
        this.label = label;
        this.reached = reached;
        this.isLast = isLast;
        this.isCurrent = isCurrent;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    public boolean isReached() {
        return reached;
    }

    public boolean isLast() {
        return isLast;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusStep)) return false;
        StatusStep other = (StatusStep) o;
        return reached == other.reached
                && isLast == other.isLast
                && isCurrent == other.isCurrent
                && label.equals(other.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, reached, isLast, isCurrent);
    }
}
