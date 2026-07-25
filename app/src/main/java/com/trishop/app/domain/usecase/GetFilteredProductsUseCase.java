package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingData;

import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.domain.model.ProductSort;
import com.trishop.app.domain.repository.ProductRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;

public class GetFilteredProductsUseCase {

    private final ProductRepository repository;

    @Inject
    public GetFilteredProductsUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Flowable<PagingData<Product>> execute(@NonNull ProductFilters filters, @Nullable ProductSort sort) {
        return repository.pagedFilteredProducts(filters, sort);
    }
}
