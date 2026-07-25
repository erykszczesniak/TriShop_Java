package com.trishop.app.domain.repository;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.core.Single;

public interface NewsletterRepository {

    @NonNull
    Single<NewsletterResult> subscribe(@NonNull String email);
}
