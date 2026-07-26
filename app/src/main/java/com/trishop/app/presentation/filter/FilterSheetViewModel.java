package com.trishop.app.presentation.filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.domain.model.ProductSort;
import com.trishop.app.domain.usecase.GetBrandsUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class FilterSheetViewModel extends ViewModel {

    public static final class State {
        public final ProductFilters filters;
        public final ProductSort sort;
        public final List<String> brands;
        public final boolean brandsExpanded;

        State(ProductFilters filters, ProductSort sort, List<String> brands, boolean brandsExpanded) {
            this.filters = filters;
            this.sort = sort;
            this.brands = brands;
            this.brandsExpanded = brandsExpanded;
        }
    }

    private final GetBrandsUseCase getBrands;
    private final MutableLiveData<State> state;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public FilterSheetViewModel(GetBrandsUseCase getBrands, SavedStateHandle savedState) {
        this.getBrands = getBrands;

        Set<ProductCategory> categories = new HashSet<>();
        ArrayList<String> rawCategories =
                savedState.get(FilterBottomSheetFragment.ARG_CATEGORIES);
        if (rawCategories != null) {
            for (String raw : rawCategories) {
                ProductCategory category = ProductCategory.fromApiValue(raw);
                if (category != null) {
                    categories.add(category);
                }
            }
        }
        Set<Integer> buckets = new HashSet<>();
        int[] rawBuckets = savedState.get(FilterBottomSheetFragment.ARG_BUCKETS);
        if (rawBuckets != null) {
            for (int bucket : rawBuckets) {
                buckets.add(bucket);
            }
        }
        Set<String> brands = new HashSet<>();
        ArrayList<String> rawBrands = savedState.get(FilterBottomSheetFragment.ARG_BRANDS);
        if (rawBrands != null) {
            brands.addAll(rawBrands);
        }
        ProductSort sort = parseSort(savedState.get(FilterBottomSheetFragment.ARG_SORT));

        state = new MutableLiveData<>(new State(
                new ProductFilters(categories, buckets, brands),
                sort,
                Collections.emptyList(),
                false));

        reloadBrands();
    }

    private static ProductSort parseSort(@Nullable String raw) {
        if (raw == null) return ProductSort.NEWEST;
        try {
            return ProductSort.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return ProductSort.NEWEST;
        }
    }

    @NonNull
    public LiveData<State> getState() {
        return state;
    }

    @NonNull
    private State current() {
        State value = state.getValue();
        if (value == null) {
            return new State(ProductFilters.EMPTY, ProductSort.NEWEST, Collections.emptyList(), false);
        }
        return value;
    }

    public void selectSort(@NonNull ProductSort sort) {
        State current = current();
        state.setValue(new State(current.filters, sort, current.brands, current.brandsExpanded));
    }

    public void toggleCategory(@NonNull ProductCategory category, boolean checked) {
        State current = current();
        Set<ProductCategory> updated = new HashSet<>(current.filters.getCategories());
        if (checked) {
            updated.add(category);
        } else {
            updated.remove(category);
        }
        ProductFilters filters = new ProductFilters(
                updated, current.filters.getPriceBuckets(), current.filters.getBrands());
        state.setValue(new State(filters, current.sort, current.brands, current.brandsExpanded));
        reloadBrands();
    }

    public void toggleBucket(int index, boolean checked) {
        State current = current();
        Set<Integer> updated = new HashSet<>(current.filters.getPriceBuckets());
        if (checked) {
            updated.add(index);
        } else {
            updated.remove(index);
        }
        ProductFilters filters = new ProductFilters(
                current.filters.getCategories(), updated, current.filters.getBrands());
        state.setValue(new State(filters, current.sort, current.brands, current.brandsExpanded));
    }

    public void toggleBrand(@NonNull String brand, boolean checked) {
        State current = current();
        Set<String> updated = new HashSet<>(current.filters.getBrands());
        if (checked) {
            updated.add(brand);
        } else {
            updated.remove(brand);
        }
        ProductFilters filters = new ProductFilters(
                current.filters.getCategories(), current.filters.getPriceBuckets(), updated);
        state.setValue(new State(filters, current.sort, current.brands, current.brandsExpanded));
    }

    public void toggleBrandsExpanded() {
        State current = current();
        state.setValue(new State(
                current.filters, current.sort, current.brands, !current.brandsExpanded));
    }

    public void clearAll() {
        State current = current();
        state.setValue(new State(
                ProductFilters.EMPTY, current.sort, current.brands, current.brandsExpanded));
        reloadBrands();
    }

    /** Brand availability follows the selected categories, like the web shop. */
    private void reloadBrands() {
        disposables.add(getBrands.execute(current().filters.getCategories())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fetched -> {
                    State current = current();
                    Set<String> keptBrands = new HashSet<>();
                    for (String brand : current.filters.getBrands()) {
                        if (fetched.contains(brand)) {
                            keptBrands.add(brand);
                        }
                    }
                    ProductFilters filters = new ProductFilters(
                            current.filters.getCategories(),
                            current.filters.getPriceBuckets(),
                            keptBrands);
                    state.setValue(new State(
                            filters, current.sort, fetched, current.brandsExpanded));
                }, error -> {
                }));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
