package com.trishop.app.presentation.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trishop.app.data.local.CompareStore;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.domain.model.SortOption;
import com.trishop.app.domain.usecase.ClearRecentSearchesUseCase;
import com.trishop.app.domain.usecase.GetRecentSearchesUseCase;
import com.trishop.app.domain.usecase.SaveRecentSearchUseCase;
import com.trishop.app.domain.usecase.SearchProductsUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

@HiltViewModel
public class SearchViewModel extends ViewModel {

    private static final long DEBOUNCE_MILLIS = 300L;

    private final SearchProductsUseCase searchProducts;
    private final SaveRecentSearchUseCase saveRecentSearch;
    private final ClearRecentSearchesUseCase clearRecentSearches;
    private final CompareStore compareStore;

    private final BehaviorProcessor<String> queryProcessor = BehaviorProcessor.createDefault("");
    private final BehaviorProcessor<SortOption> sortProcessor =
            BehaviorProcessor.createDefault(SortOption.RELEVANCE);
    private final BehaviorProcessor<ProductFilters> filtersProcessor =
            BehaviorProcessor.createDefault(ProductFilters.EMPTY);
    /** Raw server hits for the last query (relevance order). */
    private final BehaviorProcessor<List<Product>> serverResults =
            BehaviorProcessor.createDefault(Collections.emptyList());

    private final LiveData<String> query;
    private final LiveData<SortOption> sortOption;
    private final LiveData<List<Product>> results;
    private final LiveData<List<String>> recentSearches;
    /** Ids currently selected for comparison, for reflecting toggle state on cards. */
    private final LiveData<Set<String>> compareSelectedIds;
    private final MutableLiveData<Boolean> isSearching = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public SearchViewModel(SearchProductsUseCase searchProducts,
                           SaveRecentSearchUseCase saveRecentSearch,
                           ClearRecentSearchesUseCase clearRecentSearches,
                           CompareStore compareStore,
                           GetRecentSearchesUseCase getRecentSearches) {
        this.searchProducts = searchProducts;
        this.saveRecentSearch = saveRecentSearch;
        this.clearRecentSearches = clearRecentSearches;
        this.compareStore = compareStore;

        query = LiveDataReactiveStreams.fromPublisher(queryProcessor);
        sortOption = LiveDataReactiveStreams.fromPublisher(sortProcessor);

        compareSelectedIds = LiveDataReactiveStreams.fromPublisher(
                compareStore.items().map(list -> {
                    Set<String> ids = new HashSet<>();
                    for (Product product : list) {
                        ids.add(product.getId());
                    }
                    return ids;
                }));

        /* Server search hits with panel filters and sort applied client-side. */
        results = LiveDataReactiveStreams.fromPublisher(Flowable.combineLatest(
                serverResults, filtersProcessor, sortProcessor, SearchViewModel::applyClientSide));

        recentSearches = LiveDataReactiveStreams.fromPublisher(getRecentSearches.execute());

        disposables.add(queryProcessor
                .debounce(DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMap(searched -> {
                    if (searched.trim().isEmpty()) {
                        serverResults.onNext(Collections.emptyList());
                        return Flowable.empty();
                    }
                    isSearching.postValue(true);
                    return searchProducts.execute(searched)
                            .doOnSuccess(products -> {
                                serverResults.onNext(products);
                                isSearching.postValue(false);
                            })
                            .doOnError(throwable -> {
                                serverResults.onNext(Collections.emptyList());
                                String message = throwable.getLocalizedMessage();
                                error.postValue(message != null ? message : "Error");
                                isSearching.postValue(false);
                            })
                            .toFlowable()
                            .onErrorResumeNext(throwable -> Flowable.empty());
                })
                .subscribe(ignored -> {
                }, throwable -> {
                }));
    }

    private static List<Product> applyClientSide(List<Product> products,
                                                 ProductFilters filters,
                                                 SortOption sort) {
        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (filters.matches(product)) {
                filtered.add(product);
            }
        }
        switch (sort) {
            case PRICE_ASCENDING:
                Collections.sort(filtered, (a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                break;
            case PRICE_DESCENDING:
                Collections.sort(filtered, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case RATING_DESCENDING:
                Collections.sort(filtered, (a, b) -> Float.compare(b.getRating(), a.getRating()));
                break;
            case NEWEST:
                Collections.sort(filtered, (a, b) -> Long.compare(idOrZero(b), idOrZero(a)));
                break;
            case RELEVANCE:
            default:
                break;
        }
        return filtered;
    }

    private static long idOrZero(Product product) {
        try {
            return Long.parseLong(product.getId());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @NonNull
    public LiveData<String> getQuery() {
        return query;
    }

    @NonNull
    public LiveData<SortOption> getSortOption() {
        return sortOption;
    }

    @NonNull
    public LiveData<List<Product>> getResults() {
        return results;
    }

    @NonNull
    public LiveData<List<String>> getRecentSearches() {
        return recentSearches;
    }

    @NonNull
    public LiveData<Set<String>> getCompareSelectedIds() {
        return compareSelectedIds;
    }

    @NonNull
    public LiveData<Boolean> getIsSearching() {
        return isSearching;
    }

    @NonNull
    public LiveData<String> getError() {
        return error;
    }

    @NonNull
    public String currentQuery() {
        String value = queryProcessor.getValue();
        return value != null ? value : "";
    }

    @NonNull
    public SortOption currentSort() {
        SortOption value = sortProcessor.getValue();
        return value != null ? value : SortOption.RELEVANCE;
    }

    @NonNull
    public ProductFilters currentFilters() {
        ProductFilters value = filtersProcessor.getValue();
        return value != null ? value : ProductFilters.EMPTY;
    }

    public boolean isSearchingNow() {
        return Boolean.TRUE.equals(isSearching.getValue());
    }

    /** Brands seen in the current results — fallback list for the filter sheet. */
    @NonNull
    public List<String> getAvailableBrands() {
        List<Product> current = serverResults.getValue();
        if (current == null) return Collections.emptyList();
        List<String> brands = new ArrayList<>();
        for (Product product : current) {
            if (!brands.contains(product.getBrand())) {
                brands.add(product.getBrand());
            }
        }
        Collections.sort(brands);
        return brands;
    }

    @NonNull
    public CompareStore.ToggleResult toggleCompare(@NonNull Product product) {
        return compareStore.toggle(product);
    }

    public void updateQuery(@NonNull String value) {
        queryProcessor.onNext(value);
    }

    public void submitQuery(@NonNull String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return;
        disposables.add(saveRecentSearch.execute(trimmed).subscribe(() -> {
        }, throwable -> {
        }));
    }

    public void selectSort(@NonNull SortOption option) {
        sortProcessor.onNext(option);
    }

    public void applyFilters(@NonNull ProductFilters filters, @NonNull SortOption sort) {
        filtersProcessor.onNext(filters);
        sortProcessor.onNext(sort);
    }

    public void consumeError() {
        error.setValue(null);
    }

    public void clearHistory() {
        disposables.add(clearRecentSearches.execute().subscribe(() -> {
        }, throwable -> {
        }));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
