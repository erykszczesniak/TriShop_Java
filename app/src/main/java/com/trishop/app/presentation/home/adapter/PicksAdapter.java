package com.trishop.app.presentation.home.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trishop.app.R;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.databinding.ItemPickBinding;
import com.trishop.app.domain.model.Product;

import java.util.function.Consumer;

public class PicksAdapter extends ListAdapter<Product, PicksAdapter.ViewHolder> {

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

    private final Consumer<Product> onClick;

    public PicksAdapter(@NonNull Consumer<Product> onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPickBinding binding = ItemPickBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemPickBinding binding;

        ViewHolder(ItemPickBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product, Consumer<Product> onClick) {
            binding.tvName.setText(product.getName());
            binding.tvPrice.setText(MoneyFormat.formatUsd(product.getPrice()));
            Glide.with(binding.getRoot())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(binding.ivPick);
            binding.getRoot().setOnClickListener(v -> onClick.accept(product));
        }
    }
}
