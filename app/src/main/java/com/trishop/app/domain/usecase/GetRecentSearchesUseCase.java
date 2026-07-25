package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.SearchRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;

public class GetRecentSearchesUseCase {

    private final SearchRepository repository;

    @Inject
    public GetRecentSearchesUseCase(SearchRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Flowable<List<String>> execute() {
        return repository.recentSearches();
    }
}
