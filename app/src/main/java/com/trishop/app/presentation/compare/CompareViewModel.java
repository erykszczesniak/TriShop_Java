package com.trishop.app.presentation.compare;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;

import com.trishop.app.data.local.CompareStore;
import com.trishop.app.domain.model.Product;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CompareViewModel extends ViewModel {

    private final CompareStore compareStore;
    private final LiveData<List<Product>> items;

    @Inject
    public CompareViewModel(CompareStore compareStore) {
        this.compareStore = compareStore;
        this.items = LiveDataReactiveStreams.fromPublisher(compareStore.items());
    }

    @NonNull
    public LiveData<List<Product>> getItems() {
        return items;
    }

    public void remove(@NonNull Product product) {
        compareStore.remove(product.getId());
    }

    public void clear() {
        compareStore.clear();
    }
}
