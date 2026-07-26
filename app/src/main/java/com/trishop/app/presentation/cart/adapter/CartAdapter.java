package com.trishop.app.presentation.cart.adapter;

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
import com.trishop.app.databinding.ItemCartBinding;
import com.trishop.app.domain.model.CartItem;

import java.util.function.Consumer;

public class CartAdapter extends ListAdapter<CartItem, CartAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<CartItem> DIFF =
            new DiffUtil.ItemCallback<CartItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final Consumer<CartItem> onIncrement;
    private final Consumer<CartItem> onDecrement;

    public CartAdapter(@NonNull Consumer<CartItem> onIncrement,
                       @NonNull Consumer<CartItem> onDecrement) {
        super(DIFF);
        this.onIncrement = onIncrement;
        this.onDecrement = onDecrement;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onIncrement, onDecrement);
    }

    @NonNull
    public CartItem getItemAt(int position) {
        return getItem(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemCartBinding binding;

        ViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item, Consumer<CartItem> onIncrement, Consumer<CartItem> onDecrement) {
            binding.tvName.setText(item.getName());
            // iOS shows "Size X" as a mono micro-label (no brand line on cart rows).
            String variant = item.getVariant();
            boolean hasVariant = variant != null && !variant.trim().isEmpty();
            binding.tvVariant.setText(hasVariant
                    ? binding.getRoot().getContext().getString(R.string.cart_item_size, variant)
                    : "");
            binding.tvVariant.setVisibility(hasVariant ? View.VISIBLE : View.GONE);
            binding.tvPrice.setText(MoneyFormat.formatUsd(item.getSubtotal()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));
            Glide.with(binding.getRoot())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(binding.ivProduct);
            binding.btnPlus.setOnClickListener(v -> onIncrement.accept(item));
            binding.btnMinus.setOnClickListener(v -> onDecrement.accept(item));
        }
    }
}
