package com.trishop.app.domain.repository;

import androidx.annotation.NonNull;

import com.trishop.app.domain.model.User;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface AuthRepository {

    @NonNull
    Flowable<Boolean> isLoggedIn();

    @NonNull
    Single<User> login(@NonNull String email, @NonNull String password);

    @NonNull
    Single<User> register(@NonNull String email, @NonNull String password, @NonNull String name);

    @NonNull
    Completable logout();

    @NonNull
    Single<Boolean> hasStoredSession();

    @NonNull
    Single<User> getProfile();

    @NonNull
    Single<User> uploadAvatar(@NonNull byte[] bytes, @NonNull String mimeType);

    @NonNull
    Single<User> deleteAvatar();
}
