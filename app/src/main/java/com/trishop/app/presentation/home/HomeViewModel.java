package com.trishop.app.presentation.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trishop.app.data.local.CompareStore;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.domain.model.ProductPage;
import com.trishop.app.domain.model.ProductSort;
import com.trishop.app.domain.model.User;
import com.trishop.app.domain.repository.AuthRepository;
import com.trishop.app.domain.usecase.GetFilteredProductPageUseCase;
import com.trishop.app.domain.usecase.GetProductPageUseCase;
import com.trishop.app.domain.usecase.GetProfileUseCase;
import com.trishop.app.domain.usecase.ObserveRecentlyViewedUseCase;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final GetProductPageUseCase getProductPage;
    private final GetFilteredProductPageUseCase getFilteredProductPage;
    private final CompareStore compareStore;

    /** Ids currently selected for comparison, for reflecting toggle state on cards. */
    private final LiveData<Set<String>> compareSelectedIds;

    private final MutableLiveData<java.util.List<Product>> featured =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<java.util.List<Product>> arrivals =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> totalProducts = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    /** Quick single-category chip selection (null = All). */
    private final MutableLiveData<ProductCategory> selectedCategory = new MutableLiveData<>(null);
    /** Full filter state shared with the filter panel. */
    private final MutableLiveData<ProductFilters> filters =
            new MutableLiveData<>(ProductFilters.EMPTY);
    private final MutableLiveData<ProductSort> sort = new MutableLiveData<>(ProductSort.NEWEST);
    private final LiveData<java.util.List<Product>> recentlyViewed;
    /** Signed-in user for the avatar chip (initials), null when signed out. */
    private final MutableLiveData<User> user = new MutableLiveData<>(null);

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public HomeViewModel(GetProductPageUseCase getProductPage,
                         GetFilteredProductPageUseCase getFilteredProductPage,
                         GetProfileUseCase getProfile,
                         CompareStore compareStore,
                         AuthRepository authRepository,
                         ObserveRecentlyViewedUseCase observeRecentlyViewed) {
        this.getProductPage = getProductPage;
        this.getFilteredProductPage = getFilteredProductPage;
        this.compareStore = compareStore;

        compareSelectedIds = LiveDataReactiveStreams.fromPublisher(
                compareStore.items().map(list -> {
                    Set<String> ids = new HashSet<>();
                    for (Product product : list) {
                        ids.add(product.getId());
                    }
                    return ids;
                }));

        recentlyViewed = LiveDataReactiveStreams.fromPublisher(observeRecentlyViewed.execute());

        loadDashboard(null);
        disposables.add(authRepository.isLoggedIn()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loggedIn -> {
                    if (loggedIn) {
                        disposables.add(getProfile.execute()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(user::setValue, error -> user.setValue(null)));
                    } else {
                        user.setValue(null);
                    }
                }, error -> {
                }));
    }

    @NonNull
    public LiveData<Set<String>> getCompareSelectedIds() {
        return compareSelectedIds;
    }

    @NonNull
    public CompareStore.ToggleResult toggleCompare(@NonNull Product product) {
        return compareStore.toggle(product);
    }

    @NonNull
    public LiveData<java.util.List<Product>> getFeatured() {
        return featured;
    }

    @NonNull
    public LiveData<java.util.List<Product>> getArrivals() {
        return arrivals;
    }

    @NonNull
    public LiveData<Integer> getTotalProducts() {
        return totalProducts;
    }

    @NonNull
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @NonNull
    public LiveData<ProductCategory> getSelectedCategory() {
        return selectedCategory;
    }

    @NonNull
    public LiveData<ProductFilters> getFilters() {
        return filters;
    }

    @NonNull
    public LiveData<ProductSort> getSort() {
        return sort;
    }

    @NonNull
    public LiveData<java.util.List<Product>> getRecentlyViewed() {
        return recentlyViewed;
    }

    @NonNull
    public LiveData<User> getUser() {
        return user;
    }

    @NonNull
    public ProductFilters currentFilters() {
        ProductFilters value = filters.getValue();
        return value != null ? value : ProductFilters.EMPTY;
    }

    @NonNull
    public ProductSort currentSort() {
        ProductSort value = sort.getValue();
        return value != null ? value : ProductSort.NEWEST;
    }

    public void loadDashboard(@Nullable Runnable onDone) {
        isLoading.setValue(true);
        disposables.add(getProductPage.execute(0, 5, null, ProductSort.TOP_RATED)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(page -> {
                    featured.setValue(page.getProducts());
                    finishDashboardLoad(onDone);
                }, error -> {
                    featured.setValue(Collections.emptyList());
                    finishDashboardLoad(onDone);
                }));
    }

    private void finishDashboardLoad(@Nullable Runnable onDone) {
        loadArrivals(() -> {
            isLoading.setValue(false);
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    /** Loads the grid respecting chip selection, panel filters and sort — all server-side. */
    private void loadArrivals(@Nullable Runnable onDone) {
        ProductFilters currentFilters = currentFilters();
        Single<ProductPage> request = currentFilters.isActive()
                ? getFilteredProductPage.execute(currentFilters, 0, 20, currentSort())
                : getProductPage.execute(0, 20, null, currentSort());
        disposables.add(request
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(page -> {
                    arrivals.setValue(page.getProducts());
                    totalProducts.setValue(page.getTotalItems());
                    if (onDone != null) {
                        onDone.run();
                    }
                }, error -> {
                    arrivals.setValue(Collections.emptyList());
                    totalProducts.setValue(0);
                    if (onDone != null) {
                        onDone.run();
                    }
                }));
    }

    public void selectCategory(@Nullable ProductCategory category) {
        selectedCategory.setValue(category);
        ProductFilters current = currentFilters();
        Set<ProductCategory> categories = category != null
                ? Collections.singleton(category)
                : Collections.emptySet();
        filters.setValue(new ProductFilters(
                categories, current.getPriceBuckets(), current.getBrands()));
        loadArrivals(null);
    }

    public void applyFilters(@NonNull ProductFilters newFilters, @NonNull ProductSort newSort) {
        filters.setValue(newFilters);
        sort.setValue(newSort);
        selectedCategory.setValue(newFilters.getCategories().size() == 1
                ? newFilters.getCategories().iterator().next()
                : null);
        loadArrivals(null);
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
