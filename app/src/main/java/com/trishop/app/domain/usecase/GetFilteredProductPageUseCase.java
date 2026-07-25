package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.domain.model.ProductPage;
import com.trishop.app.domain.model.ProductSort;
import com.trishop.app.domain.repository.ProductRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetFilteredProductPageUseCase {

    private final ProductRepository repository;

    @Inject
    public GetFilteredProductPageUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<ProductPage> execute(@NonNull ProductFilters filters, int page, int size, @Nullable ProductSort sort) {
        return repository.getFilteredProductsPage(filters, page, size, sort);
    }
}
