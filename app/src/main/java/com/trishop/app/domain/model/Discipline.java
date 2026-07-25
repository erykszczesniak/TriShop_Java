package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** "Shop by Discipline" tile — mirrors the web/iOS Disciplines section. */
public final class Discipline {

    public static final List<Discipline> ALL = Collections.unmodifiableList(Arrays.asList(
            new Discipline(
                    "Swim",
                    "Wetsuits, goggles & swim gear",
                    Arrays.asList(ProductCategory.WETSUITS, ProductCategory.GOGGLES, ProductCategory.SWIM_CAPS),
                    "https://images.unsplash.com/photo-1530549387789-4c1017266635?w=900&q=80"),
            new Discipline(
                    "Bike",
                    "Bikes, helmets & cycling essentials",
                    Arrays.asList(ProductCategory.BIKES, ProductCategory.HELMETS),
                    "https://images.unsplash.com/photo-1541625602330-2277a4c46182?w=900&q=80"),
            new Discipline(
                    "Run",
                    "Shoes, apparel & running tech",
                    Collections.singletonList(ProductCategory.RUNNING_SHOES),
                    "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=900&q=80")));

    private final String title;
    private final String detail;
    private final List<ProductCategory> categories;
    private final String imageUrl;

    public Discipline(@NonNull String title,
                      @NonNull String detail,
                      @NonNull List<ProductCategory> categories,
                      @NonNull String imageUrl) {
        this.title = title;
        this.detail = detail;
        this.categories = Collections.unmodifiableList(categories);
        this.imageUrl = imageUrl;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getDetail() {
        return detail;
    }

    @NonNull
    public List<ProductCategory> getCategories() {
        return categories;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof Discipline)) return false;
        Discipline other = (Discipline) o;
        return title.equals(other.title)
                && detail.equals(other.detail)
                && categories.equals(other.categories)
                && imageUrl.equals(other.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, detail, categories, imageUrl);
    }

    @NonNull
    @Override
    public String toString() {
        return "Discipline(title=" + title + ")";
    }
}
