package com.trishop.app.presentation.chat;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

public abstract class ChatStreamEvent {

    private ChatStreamEvent() {
    }

    public static final class Token extends ChatStreamEvent {
        public final String text;

        public Token(@NonNull String text) {
            this.text = text;
        }
    }

    public static final class Products extends ChatStreamEvent {
        public final List<ChatProduct> products;

        public Products(@NonNull List<ChatProduct> products) {
            this.products = Collections.unmodifiableList(products);
        }
    }

    public static final class RateLimited extends ChatStreamEvent {
    }

    public static final class Error extends ChatStreamEvent {
    }

    public static final class Done extends ChatStreamEvent {
    }
}
