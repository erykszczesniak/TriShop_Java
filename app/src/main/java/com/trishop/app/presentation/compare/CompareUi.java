package com.trishop.app.presentation.compare;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.trishop.app.R;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.data.local.CompareStore;
import com.trishop.app.domain.model.Product;

import java.util.function.Consumer;

public final class CompareUi {

    private CompareUi() {
    }

    /**
     * Binds the compare toggle shown on a scoped product card. When {@code enabled}
     * is false the button is hidden entirely — the toggle only appears on category
     * scoped shop lists and search results, mirroring the web {@code showCompare} flag.
     */
    public static void bindCompareToggle(@NonNull ImageView view,
                                         @NonNull Product product,
                                         boolean enabled,
                                         boolean isSelected,
                                         @NonNull Consumer<Product> onToggle) {
        view.setVisibility(enabled ? View.VISIBLE : View.GONE);
        if (!enabled) {
            view.setOnClickListener(null);
            return;
        }
        view.setBackgroundResource(
                isSelected ? R.drawable.bg_compare_toggle_on : R.drawable.bg_compare_toggle);
        view.setImageResource(isSelected ? R.drawable.ic_check : R.drawable.ic_compare);
        view.setContentDescription(view.getContext().getString(
                isSelected ? R.string.compare_remove : R.string.compare_add));
        view.setOnClickListener(v -> onToggle.accept(product));
    }

    /** Shows the standard snackbar feedback for a compare toggle outcome. */
    public static void showCompareFeedback(@NonNull View view,
                                           @NonNull CompareStore.ToggleResult result) {
        String message;
        switch (result) {
            case ADDED:
                message = view.getContext().getString(R.string.compare_added);
                break;
            case REMOVED:
                message = view.getContext().getString(R.string.compare_removed);
                break;
            case WRONG_CATEGORY:
                message = view.getContext().getString(R.string.compare_wrong_category);
                break;
            case FULL:
            default:
                message = view.getContext().getString(R.string.compare_full, CompareStore.MAX_ITEMS);
                break;
        }
        ViewUtils.showSnackbar(view, message);
    }
}
