package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingData;

import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductSort;
import com.trishop.app.domain.repository.ProductRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;

public class GetProductsUseCase {

    private final ProductRepository repository;

    @Inject
    public GetProductsUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Flowable<PagingData<Product>> execute(@Nullable String category, @Nullable ProductSort sort) {
        return repository.pagedProducts(category, sort);
    }
}
