package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class Review {

    private final String id;
    private final int rating;
    private final String comment;
    private final String authorName;
    private final String createdAt;

    public Review(@NonNull String id,
                  int rating,
                  @NonNull String comment,
                  @NonNull String authorName,
                  @NonNull String createdAt) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.authorName = authorName;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    @NonNull
    public String getComment() {
        return comment;
    }

    @NonNull
    public String getAuthorName() {
        return authorName;
    }

    @NonNull
    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof Review)) return false;
        Review other = (Review) o;
        return rating == other.rating
                && id.equals(other.id)
                && comment.equals(other.comment)
                && authorName.equals(other.authorName)
                && createdAt.equals(other.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rating, comment, authorName, createdAt);
    }

    @NonNull
    @Override
    public String toString() {
        return "Review(id=" + id + ", rating=" + rating + ", author=" + authorName + ")";
    }
}
