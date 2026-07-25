package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.repository.SearchRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class SearchProductsUseCase {

    private final SearchRepository repository;

    @Inject
    public SearchProductsUseCase(SearchRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<List<Product>> execute(@NonNull String query) {
        return repository.searchProducts(query);
    }
}
