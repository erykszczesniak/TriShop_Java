package com.trishop.app.presentation.catalog.adapter;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trishop.app.R;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.databinding.ItemProductGridBinding;
import com.trishop.app.databinding.ItemProductListBinding;
import com.trishop.app.domain.model.Product;
import com.trishop.app.presentation.catalog.LayoutMode;
import com.trishop.app.presentation.compare.CompareUi;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public class ProductPagingAdapter extends PagingDataAdapter<Product, RecyclerView.ViewHolder> {

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

    private LayoutMode layoutMode = LayoutMode.GRID;
    /** Whether the compare toggle is offered — only when the list is category scoped. */
    private boolean showCompare = false;
    /** Ids of products currently selected for comparison, reflected on the cards. */
    private Set<String> compareSelectedIds = Collections.emptySet();

    public ProductPagingAdapter(@NonNull Consumer<Product> onItemClick,
                                @NonNull Consumer<Product> onCompareToggle) {
        super(DIFF);
        this.onItemClick = onItemClick;
        this.onCompareToggle = onCompareToggle;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLayoutMode(@NonNull LayoutMode value) {
        if (layoutMode != value) {
            layoutMode = value;
            notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setShowCompare(boolean value) {
        if (showCompare != value) {
            showCompare = value;
            notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCompareSelectedIds(@NonNull Set<String> value) {
        if (!compareSelectedIds.equals(value)) {
            compareSelectedIds = value;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return layoutMode.ordinal();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == LayoutMode.LIST.ordinal()) {
            return new ListViewHolder(ItemProductListBinding.inflate(inflater, parent, false));
        }
        return new GridViewHolder(ItemProductGridBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Product item = getItem(position);
        if (item == null) return;
        if (holder instanceof GridViewHolder) {
            ((GridViewHolder) holder).bind(item, onItemClick, onCompareToggle,
                    showCompare, compareSelectedIds.contains(item.getId()));
        } else if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(item, onItemClick);
        }
    }

    static class GridViewHolder extends RecyclerView.ViewHolder {

        private final ItemProductGridBinding binding;

        GridViewHolder(ItemProductGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product,
                  Consumer<Product> onClick,
                  Consumer<Product> onCompareToggle,
                  boolean showCompare,
                  boolean isComparing) {
            binding.tvName.setText(product.getName());
            binding.tvBrand.setText(product.getBrand());
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

            CompareUi.bindCompareToggle(
                    binding.btnCompare, product, showCompare, isComparing, onCompareToggle);
        }
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {

        private final ItemProductListBinding binding;

        ListViewHolder(ItemProductListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product, Consumer<Product> onClick) {
            binding.tvName.setText(product.getName());
            binding.tvBrand.setText(product.getBrand());
            binding.tvPrice.setText(MoneyFormat.formatUsd(product.getPrice()));
            binding.tvDescription.setText(product.getDescription());
            Glide.with(binding.getRoot())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(binding.ivProduct);
            binding.getRoot().setOnClickListener(v -> onClick.accept(product));
        }
    }
}
