package com.trishop.app.domain.usecase;

import androidx.annotation.NonNull;

import com.trishop.app.domain.repository.OrderRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class DownloadInvoiceUseCase {

    private final OrderRepository repository;

    @Inject
    public DownloadInvoiceUseCase(OrderRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<byte[]> execute(@NonNull String orderId) {
        return repository.downloadInvoice(orderId);
    }
}
