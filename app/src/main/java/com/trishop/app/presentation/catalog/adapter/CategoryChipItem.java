package com.trishop.app.presentation.catalog.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.Category;

import java.util.Objects;

public final class CategoryChipItem {

    @Nullable
    private final String id;
    private final String label;
    @Nullable
    private final Category category;

    public CategoryChipItem(@Nullable String id, @NonNull String label, @Nullable Category category) {
        this.id = id;
        this.label = label;
        this.category = category;
    }

    @Nullable
    public String getId() {
        return id;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    @Nullable
    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryChipItem)) return false;
        CategoryChipItem other = (CategoryChipItem) o;
        return Objects.equals(id, other.id)
                && label.equals(other.label)
                && Objects.equals(category, other.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, category);
    }
}
