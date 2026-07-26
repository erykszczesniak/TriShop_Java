package com.trishop.app.presentation.wishlist.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trishop.app.R;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.databinding.ItemWishlistBinding;
import com.trishop.app.domain.model.WishlistItem;

import java.util.function.Consumer;

public class WishlistAdapter extends ListAdapter<WishlistItem, WishlistAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<WishlistItem> DIFF =
            new DiffUtil.ItemCallback<WishlistItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull WishlistItem oldItem,
                                               @NonNull WishlistItem newItem) {
                    return oldItem.getProductId().equals(newItem.getProductId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull WishlistItem oldItem,
                                                  @NonNull WishlistItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final Consumer<WishlistItem> onItemClick;
    private final Consumer<WishlistItem> onRemove;
    private final Consumer<WishlistItem> onAddToBag;

    public WishlistAdapter(@NonNull Consumer<WishlistItem> onItemClick,
                           @NonNull Consumer<WishlistItem> onRemove,
                           @NonNull Consumer<WishlistItem> onAddToBag) {
        super(DIFF);
        this.onItemClick = onItemClick;
        this.onRemove = onRemove;
        this.onAddToBag = onAddToBag;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWishlistBinding binding = ItemWishlistBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClick, onRemove, onAddToBag);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemWishlistBinding binding;

        ViewHolder(ItemWishlistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WishlistItem item,
                  Consumer<WishlistItem> onItemClick,
                  Consumer<WishlistItem> onRemove,
                  Consumer<WishlistItem> onAddToBag) {
            binding.tvBrand.setText(item.getBrand());
            binding.tvName.setText(item.getName());
            binding.tvPrice.setText(MoneyFormat.formatUsd(item.getPrice()));
            Glide.with(binding.getRoot())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(binding.ivProduct);
            binding.getRoot().setOnClickListener(v -> onItemClick.accept(item));
            binding.btnAddToBag.setOnClickListener(v -> onAddToBag.accept(item));
            // Long-press removal mirrors the iOS context menu.
            binding.getRoot().setOnLongClickListener(v -> {
                onRemove.accept(item);
                return true;
            });
        }
    }
}
