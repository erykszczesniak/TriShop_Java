package com.trishop.app.data.repository;

import androidx.annotation.NonNull;

import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.dto.NewsletterRequest;
import com.trishop.app.data.remote.dto.NewsletterResponse;
import com.trishop.app.domain.repository.NewsletterRepository;
import com.trishop.app.domain.repository.NewsletterResult;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import retrofit2.HttpException;

@Singleton
public class NewsletterRepositoryImpl implements NewsletterRepository {

    private final TriShopApi api;

    @Inject
    public NewsletterRepositoryImpl(TriShopApi api) {
        this.api = api;
    }

    @NonNull
    @Override
    public Single<NewsletterResult> subscribe(@NonNull String email) {
        return api.subscribeNewsletter(new NewsletterRequest(email))
                .map(response -> {
                    if (!response.isSuccessful()) throw new HttpException(response);
                    NewsletterResponse body = response.body();
                    String message = body != null && body.message != null ? body.message : "";
                    return "Already subscribed".equalsIgnoreCase(message)
                            ? NewsletterResult.ALREADY_SUBSCRIBED
                            : NewsletterResult.SUBSCRIBED;
                });
    }
}
