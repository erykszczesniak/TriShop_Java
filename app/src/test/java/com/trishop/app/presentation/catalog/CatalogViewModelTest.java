package com.trishop.app.presentation.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.paging.PagingData;

import com.trishop.app.core.UiState;
import com.trishop.app.data.local.CompareStore;
import com.trishop.app.domain.model.Category;
import com.trishop.app.domain.usecase.GetCategoriesUseCase;
import com.trishop.app.domain.usecase.GetProductsUseCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CatalogViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTask = new InstantTaskExecutorRule();

    private GetProductsUseCase getProducts;
    private GetCategoriesUseCase getCategories;
    private CompareStore compareStore;

    @Before
    public void setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        getProducts = mock(GetProductsUseCase.class);
        getCategories = mock(GetCategoriesUseCase.class);
        compareStore = mock(CompareStore.class);
        when(getProducts.execute(any(), any())).thenReturn(Flowable.just(PagingData.empty()));
        when(compareStore.items()).thenReturn(Flowable.never());
    }

    @After
    public void tearDown() {
        RxAndroidPlugins.reset();
    }

    private CatalogViewModel viewModel() {
        return new CatalogViewModel(getProducts, getCategories, compareStore);
    }

    @Test
    public void loadsCategoriesOnInitAndExposesSuccessState() {
        List<Category> items = Arrays.asList(
                new Category("1", "Running", null), new Category("2", "Cycling", null));
        when(getCategories.execute()).thenReturn(Single.just(items));

        CatalogViewModel vm = viewModel();

        UiState<List<Category>> state = vm.getCategories().getValue();
        assertTrue(state instanceof UiState.Success);
        assertEquals(items, ((UiState.Success<List<Category>>) state).getData());
    }

    @Test
    public void exposesErrorStateWhenCategoriesFail() {
        when(getCategories.execute()).thenReturn(Single.error(new RuntimeException("boom")));

        CatalogViewModel vm = viewModel();

        UiState<List<Category>> state = vm.getCategories().getValue();
        assertTrue(state instanceof UiState.Error);
        assertEquals("boom", ((UiState.Error<List<Category>>) state).getMessage());
    }

    @Test
    public void toggleLayoutModeFlipsBetweenGridAndList() {
        when(getCategories.execute()).thenReturn(Single.just(Collections.emptyList()));
        CatalogViewModel vm = viewModel();

        assertEquals(LayoutMode.GRID, vm.currentLayoutMode());
        vm.toggleLayoutMode();
        assertEquals(LayoutMode.LIST, vm.currentLayoutMode());
        vm.toggleLayoutMode();
        assertEquals(LayoutMode.GRID, vm.currentLayoutMode());
    }

    @Test
    public void selectCategoryUpdatesSelectedCategoryStream() {
        when(getCategories.execute()).thenReturn(Single.just(Collections.emptyList()));
        CatalogViewModel vm = viewModel();
        vm.getSelectedCategory().observeForever(id -> {
        });

        assertEquals("", vm.getSelectedCategory().getValue());
        vm.selectCategory("running");
        assertEquals("running", vm.getSelectedCategory().getValue());
        vm.selectCategory(null);
        assertEquals("", vm.getSelectedCategory().getValue());
    }
}
