package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class Category {

    private final String id;
    private final String name;
    private final String iconUrl;

    public Category(@NonNull String id, @NonNull String name, @Nullable String iconUrl) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category other = (Category) o;
        return id.equals(other.id)
                && name.equals(other.name)
                && Objects.equals(iconUrl, other.iconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, iconUrl);
    }

    @NonNull
    @Override
    public String toString() {
        return "Category(id=" + id + ", name=" + name + ")";
    }
}
