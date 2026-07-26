package com.trishop.app.presentation.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ChatMessage {

    public enum Role { USER, ASSISTANT, ERROR }

    private final String id;
    private final Role role;
    private final String text;
    private final List<ChatProduct> products;

    public ChatMessage(@NonNull String id,
                       @NonNull Role role,
                       @NonNull String text,
                       @NonNull List<ChatProduct> products) {
        this.id = id;
        this.role = role;
        this.text = text;
        this.products = Collections.unmodifiableList(products);
    }

    public ChatMessage(@NonNull Role role, @NonNull String text) {
        this(UUID.randomUUID().toString(), role, text, Collections.emptyList());
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public Role getRole() {
        return role;
    }

    @NonNull
    public String getText() {
        return text;
    }

    @NonNull
    public List<ChatProduct> getProducts() {
        return products;
    }

    @NonNull
    public ChatMessage withText(@NonNull String newText) {
        return new ChatMessage(id, role, newText, products);
    }

    @NonNull
    public ChatMessage withProducts(@NonNull List<ChatProduct> newProducts) {
        return new ChatMessage(id, role, text, newProducts);
    }

    @NonNull
    public ChatMessage asError(@NonNull String errorText) {
        return new ChatMessage(id, Role.ERROR, errorText, products);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage other = (ChatMessage) o;
        return id.equals(other.id)
                && role == other.role
                && text.equals(other.text)
                && products.equals(other.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, text, products);
    }
}
