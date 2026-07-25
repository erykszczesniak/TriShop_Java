package com.trishop.app.presentation.detail.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trishop.app.R;
import com.trishop.app.databinding.ItemGalleryImageBinding;

public class GalleryPagerAdapter extends ListAdapter<String, GalleryPagerAdapter.ViewHolder> {

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

    public GalleryPagerAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryImageBinding binding = ItemGalleryImageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemGalleryImageBinding binding;

        ViewHolder(ItemGalleryImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String imageUrl) {
            Glide.with(binding.getRoot())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(binding.ivImage);
        }
    }
}
