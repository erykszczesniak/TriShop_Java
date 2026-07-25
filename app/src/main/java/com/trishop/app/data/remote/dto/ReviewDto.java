package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Review;

public final class ReviewDto {

    public long id;
    public int rating;
    public String comment;
    public String authorName;
    public String createdAt;

    public ReviewDto() {
    }

    @NonNull
    public Review toDomain() {
        return new Review(String.valueOf(id), rating, comment, authorName, createdAt);
    }
}
