package com.trishop.app.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

/**
 * Ephemeral, in-memory selection of products the user wants to compare.
 * Mirrors the web shop: products can only be compared within a single category,
 * and the selection is capped at {@link #MAX_ITEMS}. Product data itself comes
 * from the API — this store only holds the current UI selection for the session.
 */
@Singleton
public class CompareStore {

    public static final int MAX_ITEMS = 4;

    /** Outcome of a toggle, so the UI can surface the right feedback. */
    public enum ToggleResult { ADDED, REMOVED, WRONG_CATEGORY, FULL }

    private final BehaviorProcessor<List<Product>> items =
            BehaviorProcessor.createDefault(Collections.emptyList());

    @Inject
    public CompareStore() {
    }

    @NonNull
    public Flowable<List<Product>> items() {
        return items;
    }

    @NonNull
    public List<Product> currentItems() {
        List<Product> current = items.getValue();
        return current != null ? current : Collections.emptyList();
    }

    public boolean isSelected(@NonNull String productId) {
        for (Product product : currentItems()) {
            if (product.getId().equals(productId)) return true;
        }
        return false;
    }

    /** The category all current selections share, or null when empty. */
    @Nullable
    public String selectedCategory() {
        List<Product> current = currentItems();
        return current.isEmpty() ? null : current.get(0).getCategory();
    }

    @NonNull
    public ToggleResult toggle(@NonNull Product product) {
        List<Product> current = currentItems();
        if (isSelected(product.getId())) {
            removeInternal(current, product.getId());
            return ToggleResult.REMOVED;
        }
        String category = selectedCategory();
        if (category != null && !category.equalsIgnoreCase(product.getCategory())) {
            return ToggleResult.WRONG_CATEGORY;
        }
        if (current.size() >= MAX_ITEMS) {
            return ToggleResult.FULL;
        }
        List<Product> updated = new ArrayList<>(current);
        updated.add(product);
        items.onNext(Collections.unmodifiableList(updated));
        return ToggleResult.ADDED;
    }

    public void remove(@NonNull String productId) {
        removeInternal(currentItems(), productId);
    }

    public void clear() {
        items.onNext(Collections.emptyList());
    }

    private void removeInternal(List<Product> current, String productId) {
        List<Product> updated = new ArrayList<>();
        for (Product product : current) {
            if (!product.getId().equals(productId)) {
                updated.add(product);
            }
        }
        items.onNext(Collections.unmodifiableList(updated));
    }
}
