package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class User {

    private final String id;
    private final String email;
    private final String name;
    private final String avatarUrl;
    private final String createdAt;

    public User(@NonNull String id,
                @NonNull String email,
                @NonNull String name,
                @Nullable String avatarUrl,
                @Nullable String createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    public User(@NonNull String id,
                @NonNull String email,
                @NonNull String name,
                @Nullable String avatarUrl) {
        this(id, email, name, avatarUrl, null);
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Nullable
    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return id.equals(other.id)
                && email.equals(other.email)
                && name.equals(other.name)
                && Objects.equals(avatarUrl, other.avatarUrl)
                && Objects.equals(createdAt, other.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name, avatarUrl, createdAt);
    }

    @NonNull
    @Override
    public String toString() {
        return "User(id=" + id + ", email=" + email + ", name=" + name + ")";
    }
}
