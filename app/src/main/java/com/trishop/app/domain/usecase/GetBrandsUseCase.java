package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.repository.ProductRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetBrandsUseCase {

    private final ProductRepository repository;

    @Inject
    public GetBrandsUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<List<String>> execute() {
        return execute(Collections.emptyList());
    }

    @NonNull
    public Single<List<String>> execute(@NonNull Collection<ProductCategory> categories) {
        return repository.getBrands(categories);
    }
}
