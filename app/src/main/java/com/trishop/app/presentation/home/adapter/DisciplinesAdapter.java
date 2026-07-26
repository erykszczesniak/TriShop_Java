package com.trishop.app.presentation.home.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trishop.app.databinding.ItemDisciplineBinding;
import com.trishop.app.domain.model.Discipline;

import java.util.function.Consumer;

public class DisciplinesAdapter extends ListAdapter<Discipline, DisciplinesAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Discipline> DIFF =
            new DiffUtil.ItemCallback<Discipline>() {
                @Override
                public boolean areItemsTheSame(@NonNull Discipline oldItem, @NonNull Discipline newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Discipline oldItem, @NonNull Discipline newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final Consumer<Discipline> onClick;

    public DisciplinesAdapter(@NonNull Consumer<Discipline> onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDisciplineBinding binding = ItemDisciplineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemDisciplineBinding binding;

        ViewHolder(ItemDisciplineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Discipline discipline, Consumer<Discipline> onClick) {
            binding.tvTitle.setText(discipline.getTitle());
            binding.tvDetail.setText(discipline.getDetail());
            Glide.with(binding.getRoot())
                    .load(discipline.getImageUrl())
                    .centerCrop()
                    .into(binding.ivDiscipline);
            binding.getRoot().setOnClickListener(v -> onClick.accept(discipline));
        }
    }
}
