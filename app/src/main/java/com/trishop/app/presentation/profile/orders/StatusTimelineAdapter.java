package com.trishop.app.presentation.profile.orders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trishop.app.R;
import com.trishop.app.databinding.ItemStatusStepBinding;

public class StatusTimelineAdapter extends ListAdapter<StatusStep, StatusTimelineAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<StatusStep> DIFF =
            new DiffUtil.ItemCallback<StatusStep>() {
                @Override
                public boolean areItemsTheSame(@NonNull StatusStep oldItem, @NonNull StatusStep newItem) {
                    return oldItem.getLabel().equals(newItem.getLabel());
                }

                @Override
                public boolean areContentsTheSame(@NonNull StatusStep oldItem, @NonNull StatusStep newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public StatusTimelineAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStatusStepBinding binding = ItemStatusStepBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemStatusStepBinding binding;

        ViewHolder(ItemStatusStepBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(StatusStep step) {
            binding.tvLabel.setText(step.getLabel());
            // iOS: reached steps tint accent orange, future ones stay separator-grey.
            int tint = step.isReached() ? R.color.accent : R.color.neutral_200;
            binding.viewDot.setBackgroundTintList(
                    ContextCompat.getColorStateList(binding.getRoot().getContext(), tint));
            binding.tvLabel.setTextColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    step.isReached() ? R.color.brand_primary : R.color.neutral_400));
        }
    }
}
