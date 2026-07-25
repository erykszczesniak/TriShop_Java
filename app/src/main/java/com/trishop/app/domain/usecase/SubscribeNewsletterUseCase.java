package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.NewsletterRepository;
import com.trishop.app.domain.repository.NewsletterResult;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class SubscribeNewsletterUseCase {

    private final NewsletterRepository repository;

    @Inject
    public SubscribeNewsletterUseCase(NewsletterRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<NewsletterResult> execute(@NonNull String email) {
        return repository.subscribe(email);
    }
}
