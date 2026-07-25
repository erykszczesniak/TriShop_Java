package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Review;
import com.trishop.app.domain.repository.ProductRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetReviewsUseCase {

    private final ProductRepository repository;

    @Inject
    public GetReviewsUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<List<Review>> execute(@NonNull String productId) {
        return repository.getReviews(productId);
    }
}
