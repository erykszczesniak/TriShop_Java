package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.repository.ProductRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetProductUseCase {

    private final ProductRepository repository;

    @Inject
    public GetProductUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<Product> execute(@NonNull String id) {
        return repository.getProduct(id);
    }
}
