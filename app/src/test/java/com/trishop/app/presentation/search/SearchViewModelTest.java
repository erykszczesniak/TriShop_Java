package com.trishop.app.presentation.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.trishop.app.data.local.CompareStore;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.domain.model.SortOption;
import com.trishop.app.domain.usecase.ClearRecentSearchesUseCase;
import com.trishop.app.domain.usecase.GetRecentSearchesUseCase;
import com.trishop.app.domain.usecase.SaveRecentSearchUseCase;
import com.trishop.app.domain.usecase.SearchProductsUseCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.schedulers.TestScheduler;

public class SearchViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTask = new InstantTaskExecutorRule();

    private final TestScheduler testScheduler = new TestScheduler();

    private SearchProductsUseCase searchProducts;
    private SaveRecentSearchUseCase saveRecent;
    private ClearRecentSearchesUseCase clearRecent;
    private GetRecentSearchesUseCase getRecent;
    private CompareStore compareStore;

    private static Product product(String id, String name, double price, String category) {
        return new Product(id, name, "", price, null, "", Collections.emptyList(),
                category, "TriBrand", 4.0f, 10, Collections.emptyList(), true);
    }

    @Before
    public void setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(s -> testScheduler);
        searchProducts = mock(SearchProductsUseCase.class);
        saveRecent = mock(SaveRecentSearchUseCase.class);
        clearRecent = mock(ClearRecentSearchesUseCase.class);
        getRecent = mock(GetRecentSearchesUseCase.class);
        compareStore = mock(CompareStore.class);
        when(getRecent.execute()).thenReturn(Flowable.just(Collections.emptyList()));
        when(saveRecent.execute(anyString())).thenReturn(Completable.complete());
        when(compareStore.items()).thenReturn(Flowable.never());
    }

    @After
    public void tearDown() {
        RxAndroidPlugins.reset();
        RxJavaPlugins.reset();
    }

    private SearchViewModel viewModel() {
        return new SearchViewModel(searchProducts, saveRecent, clearRecent, compareStore, getRecent);
    }

    @Test
    public void debouncedQueryTriggersServerSearch() {
        List<Product> hits =
                Collections.singletonList(product("1", "Wetsuit Pro", 299.0, "WETSUITS"));
        when(searchProducts.execute("wetsuit")).thenReturn(Single.just(hits));
        SearchViewModel vm = viewModel();
        vm.getResults().observeForever(products -> {
        });

        assertEquals(Collections.emptyList(), vm.getResults().getValue());
        vm.updateQuery("wetsuit");
        testScheduler.advanceTimeBy(301, TimeUnit.MILLISECONDS);

        assertEquals(hits, vm.getResults().getValue());
    }

    @Test
    public void filtersNarrowResultsClientSide() {
        Product wetsuit = product("1", "Wetsuit", 299.0, "WETSUITS");
        Product bike = product("2", "Bike", 2999.0, "BIKES");
        when(searchProducts.execute("tri")).thenReturn(Single.just(Arrays.asList(wetsuit, bike)));
        SearchViewModel vm = viewModel();
        vm.getResults().observeForever(products -> {
        });

        vm.updateQuery("tri");
        testScheduler.advanceTimeBy(301, TimeUnit.MILLISECONDS);
        assertEquals(Arrays.asList(wetsuit, bike), vm.getResults().getValue());

        vm.applyFilters(
                new ProductFilters(
                        new HashSet<>(Collections.singletonList(ProductCategory.BIKES)),
                        Collections.emptySet(),
                        Collections.emptySet()),
                SortOption.RELEVANCE);
        assertEquals(Collections.singletonList(bike), vm.getResults().getValue());
    }

    @Test
    public void sortOrdersResultsClientSide() {
        Product cheap = product("1", "Cheap", 10.0, "WETSUITS");
        Product pricey = product("2", "Pricey", 100.0, "WETSUITS");
        when(searchProducts.execute("gear")).thenReturn(Single.just(Arrays.asList(pricey, cheap)));
        SearchViewModel vm = viewModel();
        vm.getResults().observeForever(products -> {
        });

        vm.updateQuery("gear");
        testScheduler.advanceTimeBy(301, TimeUnit.MILLISECONDS);
        assertEquals(Arrays.asList(pricey, cheap), vm.getResults().getValue());

        vm.selectSort(SortOption.PRICE_ASCENDING);
        assertEquals(Arrays.asList(cheap, pricey), vm.getResults().getValue());
    }

    @Test
    public void submitQueryPersistsRecentSearch() {
        SearchViewModel vm = viewModel();
        vm.submitQuery("  goggles  ");
        verify(saveRecent).execute("goggles");
    }
}
