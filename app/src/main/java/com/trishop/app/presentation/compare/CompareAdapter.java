package com.trishop.app.presentation.compare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trishop.app.R;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.databinding.ItemCompareColumnBinding;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;

import java.util.function.Consumer;

public class CompareAdapter extends ListAdapter<Product, CompareAdapter.ColumnViewHolder> {

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

    private final Consumer<Product> onRemove;

    public CompareAdapter(@NonNull Consumer<Product> onRemove) {
        super(DIFF);
        this.onRemove = onRemove;
    }

    @NonNull
    @Override
    public ColumnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCompareColumnBinding binding = ItemCompareColumnBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ColumnViewHolder(binding, onRemove);
    }

    @Override
    public void onBindViewHolder(@NonNull ColumnViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ColumnViewHolder extends RecyclerView.ViewHolder {

        private final ItemCompareColumnBinding binding;
        private final Consumer<Product> onRemove;

        ColumnViewHolder(ItemCompareColumnBinding binding, Consumer<Product> onRemove) {
            super(binding.getRoot());
            this.binding = binding;
            this.onRemove = onRemove;
        }

        void bind(Product product) {
            Context context = binding.getRoot().getContext();
            binding.tvBrand.setText(product.getBrand());
            binding.tvName.setText(product.getName());
            binding.tvPrice.setText(MoneyFormat.formatUsd(product.getPrice()));
            ProductCategory category = ProductCategory.fromApiValue(product.getCategory());
            binding.tvCategory.setText(
                    category != null ? category.getDisplayName() : product.getCategory());
            binding.tvRating.setText(context.getString(
                    R.string.compare_rating_format, product.getRating(), product.getReviewCount()));
            binding.tvAvailability.setText(context.getString(product.isInStock()
                    ? R.string.compare_in_stock
                    : R.string.compare_out_of_stock));
            Glide.with(binding.getRoot())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(binding.ivProduct);
            binding.btnRemove.setOnClickListener(v -> onRemove.accept(product));
        }
    }
}
