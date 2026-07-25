package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Review;
import com.trishop.app.domain.repository.ProductRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class AddReviewUseCase {

    private final ProductRepository repository;

    @Inject
    public AddReviewUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<Review> execute(@NonNull String productId, int rating, @NonNull String comment) {
        return repository.addReview(productId, rating, comment);
    }
}
