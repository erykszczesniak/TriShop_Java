package com.trishop.app.presentation.catalog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trishop.app.databinding.ItemLoadStateBinding;

public class ProductLoadStateAdapter extends LoadStateAdapter<ProductLoadStateAdapter.ViewHolder> {

    private final Runnable onRetry;

    public ProductLoadStateAdapter(@NonNull Runnable onRetry) {
        this.onRetry = onRetry;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @NonNull LoadState loadState) {
        holder.bind(loadState, onRetry);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @NonNull LoadState loadState) {
        ItemLoadStateBinding binding = ItemLoadStateBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemLoadStateBinding binding;

        ViewHolder(ItemLoadStateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LoadState state, Runnable onRetry) {
            binding.progressBar.setVisibility(
                    state instanceof LoadState.Loading ? View.VISIBLE : View.GONE);
            binding.btnRetry.setVisibility(
                    state instanceof LoadState.Error ? View.VISIBLE : View.GONE);
            binding.tvError.setVisibility(
                    state instanceof LoadState.Error ? View.VISIBLE : View.GONE);
            if (state instanceof LoadState.Error) {
                binding.tvError.setText(
                        ((LoadState.Error) state).getError().getLocalizedMessage());
            }
            binding.btnRetry.setOnClickListener(v -> onRetry.run());
        }
    }
}
