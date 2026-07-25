package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.User;
import com.trishop.app.domain.repository.AuthRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetProfileUseCase {

    private final AuthRepository repository;

    @Inject
    public GetProfileUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<User> execute() {
        return repository.getProfile();
    }
}
