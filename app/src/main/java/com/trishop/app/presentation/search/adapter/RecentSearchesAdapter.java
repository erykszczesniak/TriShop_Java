package com.trishop.app.presentation.search.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trishop.app.databinding.ItemRecentSearchBinding;

import java.util.function.Consumer;

public class RecentSearchesAdapter extends ListAdapter<String, RecentSearchesAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<String> DIFF =
            new DiffUtil.ItemCallback<String>() {
                @Override
                public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final Consumer<String> onClick;

    public RecentSearchesAdapter(@NonNull Consumer<String> onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecentSearchBinding binding = ItemRecentSearchBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemRecentSearchBinding binding;

        ViewHolder(ItemRecentSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String query, Consumer<String> onClick) {
            binding.tvQuery.setText(query);
            binding.getRoot().setOnClickListener(v -> onClick.accept(query));
        }
    }
}
