package com.trishop.app.presentation.catalog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.trishop.app.core.UiState;
import com.trishop.app.data.local.CompareStore;
import com.trishop.app.domain.model.Category;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.usecase.GetCategoriesUseCase;
import com.trishop.app.domain.usecase.GetProductsUseCase;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

@HiltViewModel
public class CatalogViewModel extends ViewModel {

    private final GetCategoriesUseCase getCategoriesUseCase;
    private final CompareStore compareStore;

    /** Ids currently selected for comparison, for reflecting toggle state on cards. */
    private final LiveData<Set<String>> compareSelectedIds;

    /** Empty optional = "All"; category api values otherwise. */
    private final BehaviorProcessor<Optional<String>> selectedCategoryProcessor =
            BehaviorProcessor.createDefault(Optional.empty());
    private final LiveData<String> selectedCategory;
    private final MutableLiveData<LayoutMode> layoutMode = new MutableLiveData<>(LayoutMode.GRID);
    private final MutableLiveData<UiState<List<Category>>> categories =
            new MutableLiveData<>(UiState.loading());
    private final Flowable<PagingData<Product>> products;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public CatalogViewModel(GetProductsUseCase getProductsUseCase,
                            GetCategoriesUseCase getCategoriesUseCase,
                            CompareStore compareStore) {
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.compareStore = compareStore;

        compareSelectedIds = LiveDataReactiveStreams.fromPublisher(
                compareStore.items().map(list -> {
                    Set<String> ids = new HashSet<>();
                    for (Product product : list) {
                        ids.add(product.getId());
                    }
                    return ids;
                }));

        selectedCategory = LiveDataReactiveStreams.fromPublisher(
                selectedCategoryProcessor.map(optional -> optional.orElse("")));

        Flowable<PagingData<Product>> paged = selectedCategoryProcessor
                .switchMap(category ->
                        getProductsUseCase.execute(category.orElse(null), null));
        products = PagingRx.cachedIn(paged, ViewModelKt.getViewModelScope(this));

        loadCategories();
    }

    @NonNull
    public LiveData<Set<String>> getCompareSelectedIds() {
        return compareSelectedIds;
    }

    @NonNull
    public CompareStore.ToggleResult toggleCompare(@NonNull Product product) {
        return compareStore.toggle(product);
    }

    /** Emits the selected category api value, or the empty string for "All". */
    @NonNull
    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    @NonNull
    public LiveData<LayoutMode> getLayoutMode() {
        return layoutMode;
    }

    @NonNull
    public LayoutMode currentLayoutMode() {
        LayoutMode value = layoutMode.getValue();
        return value != null ? value : LayoutMode.GRID;
    }

    @NonNull
    public LiveData<UiState<List<Category>>> getCategories() {
        return categories;
    }

    @NonNull
    public Flowable<PagingData<Product>> getProducts() {
        return products;
    }

    public void loadCategories() {
        categories.setValue(UiState.loading());
        disposables.add(getCategoriesUseCase.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loaded -> categories.setValue(UiState.success(loaded)),
                        error -> {
                            String message = error.getLocalizedMessage();
                            categories.setValue(UiState.error(message != null ? message : "Error"));
                        }));
    }

    public void selectCategory(@Nullable String id) {
        selectedCategoryProcessor.onNext(Optional.ofNullable(id));
    }

    public void toggleLayoutMode() {
        layoutMode.setValue(currentLayoutMode() == LayoutMode.GRID
                ? LayoutMode.LIST
                : LayoutMode.GRID);
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
