package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class HasStoredSessionUseCase {

    private final AuthRepository repository;

    @Inject
    public HasStoredSessionUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<Boolean> execute() {
        return repository.hasStoredSession();
    }
}
