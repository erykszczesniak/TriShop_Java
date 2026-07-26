package com.trishop.app.presentation.profile.orders;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.trishop.app.core.UiState;
import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.usecase.DownloadInvoiceUseCase;
import com.trishop.app.domain.usecase.GetOrderUseCase;
import com.trishop.app.presentation.common.SingleLiveEvent;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class OrderDetailViewModel extends ViewModel {

    public abstract static class Event {

        public static final class InvoiceReady extends Event {
            public final byte[] pdf;

            InvoiceReady(byte[] pdf) {
                this.pdf = pdf;
            }
        }

        public static final class InvoiceFailed extends Event {
        }
    }

    private final GetOrderUseCase getOrder;
    private final DownloadInvoiceUseCase downloadInvoice;

    private final String orderId;
    private final MutableLiveData<UiState<Order>> state = new MutableLiveData<>(UiState.loading());
    private final SingleLiveEvent<Event> events = new SingleLiveEvent<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public OrderDetailViewModel(GetOrderUseCase getOrder,
                                DownloadInvoiceUseCase downloadInvoice,
                                SavedStateHandle savedState) {
        this.getOrder = getOrder;
        this.downloadInvoice = downloadInvoice;
        orderId = Objects.requireNonNull(savedState.get("orderId"));
        load();
    }

    @NonNull
    public String getOrderId() {
        return orderId;
    }

    @NonNull
    public LiveData<UiState<Order>> getState() {
        return state;
    }

    @NonNull
    public LiveData<Event> getEvents() {
        return events;
    }

    public void requestInvoice() {
        disposables.add(downloadInvoice.execute(orderId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pdf -> events.setValue(new Event.InvoiceReady(pdf)),
                        error -> events.setValue(new Event.InvoiceFailed())));
    }

    public void load() {
        state.setValue(UiState.loading());
        disposables.add(getOrder.execute(orderId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(order -> state.setValue(UiState.success(order)),
                        error -> {
                            String message = error.getLocalizedMessage();
                            state.setValue(UiState.error(message != null ? message : "Error"));
                        }));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
