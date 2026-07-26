package com.trishop.app.presentation.profile.orders;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trishop.app.R;
import com.trishop.app.core.util.DateFormats;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.databinding.ItemOrderBinding;
import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.model.OrderItem;

import java.util.Locale;
import java.util.function.Consumer;

public class OrderHistoryAdapter extends ListAdapter<Order, OrderHistoryAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Order> DIFF =
            new DiffUtil.ItemCallback<Order>() {
                @Override
                public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final Consumer<Order> onClick;

    public OrderHistoryAdapter(@NonNull Consumer<Order> onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemOrderBinding binding;

        ViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order, Consumer<Order> onClick) {
            binding.tvOrderId.setText("#" + order.getId());
            binding.tvDate.setText(DateFormats.formatIsoDate(order.getCreatedAt()));
            binding.tvStatus.setText(order.getStatus());
            renderStatusColors(order.getStatus());
            binding.tvTotal.setText(MoneyFormat.formatUsd(order.getTotalPrice()));
            // iOS lists product names after the count: "3 items: Wetsuit, Goggles".
            String count = itemView.getResources().getQuantityString(
                    R.plurals.orders_item_count, order.getItemCount(), order.getItemCount());
            StringBuilder names = new StringBuilder();
            for (OrderItem item : order.getItems()) {
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(item.getProductName());
            }
            binding.tvItemCount.setText(
                    names.length() == 0 ? count : count + ": " + names);
            binding.getRoot().setOnClickListener(v -> onClick.accept(order));
        }

        /** iOS OrderCell: delivered=green, shipped=orange, confirmed=amber, else grey. */
        private void renderStatusColors(String status) {
            int colorRes;
            switch (status.toLowerCase(Locale.US)) {
                case "delivered":
                    colorRes = R.color.success;
                    break;
                case "shipped":
                    colorRes = R.color.accent;
                    break;
                case "confirmed":
                    colorRes = R.color.warning;
                    break;
                default:
                    colorRes = R.color.neutral_400;
                    break;
            }
            int base = ContextCompat.getColor(itemView.getContext(), colorRes);
            binding.tvStatus.setTextColor(base);
            binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(base, 38)));
        }
    }
}
