package com.trishop.app.presentation.search.adapter;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trishop.app.R;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.databinding.ItemProductGridBinding;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.presentation.compare.CompareUi;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

/** Search results — the same 2-column glass product card iOS reuses from the catalog. */
public class SearchResultsAdapter extends ListAdapter<Product, SearchResultsAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Product> DIFF =
            new DiffUtil.ItemCallback<Product>() {
                @Override
                public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final Consumer<Product> onItemClick;
    private final Consumer<Product> onCompareToggle;

    /** Ids of products currently selected for comparison, reflected on the cards. */
    private Set<String> compareSelectedIds = Collections.emptySet();

    public SearchResultsAdapter(@NonNull Consumer<Product> onItemClick,
                                @NonNull Consumer<Product> onCompareToggle) {
        super(DIFF);
        this.onItemClick = onItemClick;
        this.onCompareToggle = onCompareToggle;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCompareSelectedIds(@NonNull Set<String> value) {
        if (!compareSelectedIds.equals(value)) {
            compareSelectedIds = value;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductGridBinding binding = ItemProductGridBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = getItem(position);
        holder.bind(product, onItemClick, onCompareToggle,
                compareSelectedIds.contains(product.getId()));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemProductGridBinding binding;

        ViewHolder(ItemProductGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product,
                  Consumer<Product> onClick,
                  Consumer<Product> onCompareToggle,
                  boolean isComparing) {
            ProductCategory category = ProductCategory.fromApiValue(product.getCategory());
            binding.tvBrand.setText(category != null ? category.getKicker() : product.getCategory());
            binding.tvName.setText(product.getName());
            binding.tvPrice.setText(MoneyFormat.formatUsd(product.getPrice()));
            Double originalPrice = product.getOriginalPrice();
            boolean hasDiscount = originalPrice != null && originalPrice > product.getPrice();
            binding.tvOriginalPrice.setVisibility(hasDiscount ? View.VISIBLE : View.GONE);
            binding.tvOriginalPrice.setText(
                    hasDiscount ? MoneyFormat.formatUsd(originalPrice) : "");
            binding.tvOriginalPrice.setPaintFlags(
                    binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.tvDiscount.setVisibility(hasDiscount ? View.VISIBLE : View.GONE);
            if (hasDiscount) {
                int pct = (int) Math.round((1 - product.getPrice() / originalPrice) * 100);
                binding.tvDiscount.setText("-" + pct + "%");
            }
            Glide.with(binding.getRoot())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(binding.ivProduct);
            binding.getRoot().setOnClickListener(v -> onClick.accept(product));

            // Search results are always query scoped, so compare is always offered.
            CompareUi.bindCompareToggle(binding.btnCompare, product, true, isComparing, onCompareToggle);
        }
    }
}
