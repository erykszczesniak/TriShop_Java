package com.trishop.app.presentation.profile.orders;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trishop.app.core.UiState;
import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.usecase.GetOrdersUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class OrderHistoryViewModel extends ViewModel {

    private final GetOrdersUseCase getOrders;

    private final MutableLiveData<UiState<List<Order>>> state =
            new MutableLiveData<>(UiState.loading());

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public OrderHistoryViewModel(GetOrdersUseCase getOrders) {
        this.getOrders = getOrders;
        load();
    }

    @NonNull
    public LiveData<UiState<List<Order>>> getState() {
        return state;
    }

    public void load() {
        state.setValue(UiState.loading());
        disposables.add(getOrders.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orders -> state.setValue(UiState.success(orders)),
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
