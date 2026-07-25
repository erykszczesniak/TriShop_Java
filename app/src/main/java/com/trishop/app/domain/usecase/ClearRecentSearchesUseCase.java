package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.SearchRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class ClearRecentSearchesUseCase {

    private final SearchRepository repository;

    @Inject
    public ClearRecentSearchesUseCase(SearchRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Completable execute() {
        return repository.clearRecentSearches();
    }
}
