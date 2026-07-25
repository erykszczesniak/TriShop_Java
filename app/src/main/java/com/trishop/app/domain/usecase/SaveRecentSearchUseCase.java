package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.SearchRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class SaveRecentSearchUseCase {

    private final SearchRepository repository;

    @Inject
    public SaveRecentSearchUseCase(SearchRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Completable execute(@NonNull String query) {
        return repository.saveRecentSearch(query);
    }
}
