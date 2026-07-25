package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;

public class LogoutUseCase {

    private final AuthRepository repository;

    @Inject
    public LogoutUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Completable execute() {
        return repository.logout();
    }
}
