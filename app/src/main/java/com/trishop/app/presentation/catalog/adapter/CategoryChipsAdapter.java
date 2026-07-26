package com.trishop.app.presentation.catalog.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trishop.app.databinding.ItemCategoryChipBinding;
import com.trishop.app.domain.model.Category;

import java.util.Objects;
import java.util.function.Consumer;

public class CategoryChipsAdapter extends ListAdapter<CategoryChipItem, CategoryChipsAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<CategoryChipItem> DIFF =
            new DiffUtil.ItemCallback<CategoryChipItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull CategoryChipItem oldItem,
                                               @NonNull CategoryChipItem newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CategoryChipItem oldItem,
                                                  @NonNull CategoryChipItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final Consumer<Category> onSelect;

    @Nullable
    private String selectedId = null;

    public CategoryChipsAdapter(@NonNull Consumer<Category> onSelect) {
        super(DIFF);
        this.onSelect = onSelect;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedId(@Nullable String value) {
        if (!Objects.equals(selectedId, value)) {
            selectedId = value;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryChipBinding binding = ItemCategoryChipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), selectedId, onSelect);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemCategoryChipBinding binding;

        ViewHolder(ItemCategoryChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CategoryChipItem item, @Nullable String selectedId, Consumer<Category> onSelect) {
            binding.chip.setText(item.getLabel());
            binding.chip.setChecked(Objects.equals(item.getId(), selectedId));
            binding.chip.setOnClickListener(v -> onSelect.accept(item.getCategory()));
        }
    }
}
