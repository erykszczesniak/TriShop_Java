package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;

public final class CreateReviewRequest {

    public final int rating;
    public final String comment;

    public CreateReviewRequest(int rating, @NonNull String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
