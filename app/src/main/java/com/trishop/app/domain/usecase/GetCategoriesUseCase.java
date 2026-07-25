package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.Category;
import com.trishop.app.domain.repository.ProductRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetCategoriesUseCase {

    private final ProductRepository repository;

    @Inject
    public GetCategoriesUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<List<Category>> execute() {
        return repository.getCategories();
    }
}
