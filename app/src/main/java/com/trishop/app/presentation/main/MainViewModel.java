package com.trishop.app.presentation.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;

import com.trishop.app.core.network.NetworkMonitor;
import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.usecase.ObserveCartUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final LiveData<Boolean> isOnline;
    /** Total quantity across the cart, shown as the tab badge (mirrors iOS). */
    private final LiveData<Integer> cartItemCount;

    @Inject
    public MainViewModel(ObserveCartUseCase observeCart, NetworkMonitor networkMonitor) {
        isOnline = LiveDataReactiveStreams.fromPublisher(networkMonitor.isOnline());
        cartItemCount = LiveDataReactiveStreams.fromPublisher(
                observeCart.execute().map(items -> {
                    int count = 0;
                    for (CartItem item : items) {
                        count += item.getQuantity();
                    }
                    return count;
                }));
    }

    @NonNull
    public LiveData<Boolean> getIsOnline() {
        return isOnline;
    }

    @NonNull
    public LiveData<Integer> getCartItemCount() {
        return cartItemCount;
    }
}
