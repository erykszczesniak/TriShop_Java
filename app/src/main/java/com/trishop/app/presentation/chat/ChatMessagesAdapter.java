package com.trishop.app.presentation.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.trishop.app.R;
import com.trishop.app.databinding.ItemChatMessageBinding;
import com.trishop.app.domain.model.Product;
import com.trishop.app.presentation.home.adapter.PicksAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ChatMessagesAdapter extends ListAdapter<ChatMessage, ChatMessagesAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF =
            new DiffUtil.ItemCallback<ChatMessage>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final Consumer<ChatProduct> onProductClick;

    public ChatMessagesAdapter(@NonNull Consumer<ChatProduct> onProductClick) {
        super(DIFF);
        this.onProductClick = onProductClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, onProductClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatMessageBinding binding;
        private final PicksAdapter productsAdapter;
        private List<ChatProduct> products = Collections.emptyList();

        ViewHolder(ItemChatMessageBinding binding, Consumer<ChatProduct> onProductClick) {
            super(binding.getRoot());
            this.binding = binding;
            productsAdapter = new PicksAdapter(product -> {
                for (ChatProduct chatProduct : products) {
                    if (String.valueOf(chatProduct.getId()).equals(product.getId())) {
                        onProductClick.accept(chatProduct);
                        return;
                    }
                }
            });
            binding.rvProducts.setAdapter(productsAdapter);
        }

        void bind(ChatMessage message) {
            boolean isUser = message.getRole() == ChatMessage.Role.USER;
            if (message.getRole() == ChatMessage.Role.ASSISTANT) {
                binding.tvBubble.setText(ChatMarkdown.render(
                        message.getText().isEmpty() ? "…" : message.getText()));
            } else {
                binding.tvBubble.setText(message.getText());
            }
            binding.tvBubble.setBackgroundResource(
                    isUser ? R.drawable.bg_chat_user : R.drawable.bg_chat_assistant);
            // iOS: white text on the accent user bubble, standard text otherwise.
            binding.tvBubble.setTextColor(isUser
                    ? ContextCompat.getColor(binding.getRoot().getContext(), R.color.neutral_0)
                    : MaterialColors.getColor(binding.tvBubble,
                            com.google.android.material.R.attr.colorOnSurface));
            ((LinearLayout.LayoutParams) binding.tvBubble.getLayoutParams()).gravity =
                    isUser ? Gravity.END : Gravity.START;
            binding.tvBubble.requestLayout();

            products = message.getProducts();
            binding.rvProducts.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
            List<Product> mapped = new ArrayList<>();
            for (ChatProduct chat : products) {
                mapped.add(new Product(
                        String.valueOf(chat.getId()),
                        chat.getName(),
                        "",
                        chat.getPrice(),
                        null,
                        chat.getImageUrl(),
                        Collections.emptyList(),
                        chat.getCategory(),
                        chat.getBrand(),
                        0f,
                        0,
                        Collections.emptyList(),
                        true));
            }
            productsAdapter.submitList(mapped);
        }
    }
}
