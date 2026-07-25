package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.ProductPage;
import com.trishop.app.domain.model.ProductSort;
import com.trishop.app.domain.repository.ProductRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetProductPageUseCase {

    private final ProductRepository repository;

    @Inject
    public GetProductPageUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<ProductPage> execute(int page, int size, @Nullable ProductCategory category, @Nullable ProductSort sort) {
        return repository.getProductsPage(page, size, category, sort);
    }
}
