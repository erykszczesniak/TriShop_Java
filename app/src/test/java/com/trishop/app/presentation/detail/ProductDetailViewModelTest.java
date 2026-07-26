package com.trishop.app.presentation.detail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.SavedStateHandle;

import com.trishop.app.core.UiState;
import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductPage;
import com.trishop.app.domain.repository.AuthRepository;
import com.trishop.app.domain.usecase.AddRecentlyViewedUseCase;
import com.trishop.app.domain.usecase.AddReviewUseCase;
import com.trishop.app.domain.usecase.AddToCartUseCase;
import com.trishop.app.domain.usecase.GetProductPageUseCase;
import com.trishop.app.domain.usecase.GetProductUseCase;
import com.trishop.app.domain.usecase.GetReviewsUseCase;
import com.trishop.app.domain.usecase.ObserveWishlistIdsUseCase;
import com.trishop.app.domain.usecase.ToggleWishlistUseCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProductDetailViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTask = new InstantTaskExecutorRule();

    private final Product sampleProduct = new Product(
            "p1", "Wetsuit Pro", "Long description", 299.0, null, "",
            Arrays.asList("a", "b"), "wetsuits", "TriBrand", 4.5f, 20,
            Arrays.asList("S", "M", "L"), true);

    private GetProductUseCase getProduct;
    private AddToCartUseCase addToCart;
    private ToggleWishlistUseCase toggleWishlist;
    private AddRecentlyViewedUseCase addRecentlyViewed;
    private GetReviewsUseCase getReviews;
    private AddReviewUseCase addReview;
    private GetProductPageUseCase getProductPage;
    private AuthRepository authRepository;
    private ObserveWishlistIdsUseCase observeWishlistIds;

    @Before
    public void setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        getProduct = mock(GetProductUseCase.class);
        addToCart = mock(AddToCartUseCase.class);
        toggleWishlist = mock(ToggleWishlistUseCase.class);
        addRecentlyViewed = mock(AddRecentlyViewedUseCase.class);
        getReviews = mock(GetReviewsUseCase.class);
        addReview = mock(AddReviewUseCase.class);
        getProductPage = mock(GetProductPageUseCase.class);
        authRepository = mock(AuthRepository.class);
        observeWishlistIds = mock(ObserveWishlistIdsUseCase.class);
        when(getProductPage.execute(anyInt(), anyInt(), any(), any()))
                .thenReturn(Single.just(new ProductPage(Collections.emptyList(), 0)));
        when(getReviews.execute(anyString())).thenReturn(Single.just(Collections.emptyList()));
        when(authRepository.isLoggedIn()).thenReturn(Flowable.just(true));
        when(addToCart.execute(any())).thenReturn(Completable.complete());
        when(toggleWishlist.execute(any())).thenReturn(Single.just(true));
        when(addRecentlyViewed.execute(any())).thenReturn(Completable.complete());
        when(observeWishlistIds.execute()).thenReturn(Flowable.just(Collections.emptySet()));
    }

    @After
    public void tearDown() {
        RxAndroidPlugins.reset();
    }

    private ProductDetailViewModel viewModel() {
        Map<String, Object> args = new HashMap<>();
        args.put("productId", "p1");
        SavedStateHandle handle = new SavedStateHandle(args);
        return new ProductDetailViewModel(
                getProduct, addToCart, toggleWishlist, addRecentlyViewed,
                getReviews, addReview, getProductPage, authRepository,
                observeWishlistIds, handle);
    }

    @Test
    public void loadsProductOnInitWithoutPreselectingAmongMultipleVariants() {
        when(getProduct.execute("p1")).thenReturn(Single.just(sampleProduct));
        ProductDetailViewModel vm = viewModel();

        UiState<Product> state = vm.getProduct().getValue();
        assertTrue(state instanceof UiState.Success);
        assertEquals(sampleProduct, ((UiState.Success<Product>) state).getData());
        assertNull(vm.getSelectedVariant().getValue());
    }

    @Test
    public void addToCartSavesItemAndEmitsAddedToCartEvent() {
        when(getProduct.execute("p1")).thenReturn(Single.just(sampleProduct));
        ProductDetailViewModel vm = viewModel();

        vm.selectVariant("S");
        List<ProductDetailViewModel.Event> events = new ArrayList<>();
        vm.getEvents().observeForever(events::add);

        vm.addToCart();

        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductDetailViewModel.Event.AddedToCart);
        assertEquals("Wetsuit Pro",
                ((ProductDetailViewModel.Event.AddedToCart) events.get(0)).productName);
        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(addToCart).execute(captor.capture());
        assertEquals("p1", captor.getValue().getProductId());
        assertEquals("S", captor.getValue().getVariant());
    }

    @Test
    public void addToCartWorksWithoutVariants() {
        Product noVariantProduct = new Product(
                "p1", "Wetsuit Pro", "Long description", 299.0, null, "",
                Arrays.asList("a", "b"), "wetsuits", "TriBrand", 4.5f, 20,
                Collections.emptyList(), true);
        when(getProduct.execute("p1")).thenReturn(Single.just(noVariantProduct));
        ProductDetailViewModel vm = viewModel();

        List<ProductDetailViewModel.Event> events = new ArrayList<>();
        vm.getEvents().observeForever(events::add);

        vm.addToCart();

        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductDetailViewModel.Event.AddedToCart);
        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(addToCart).execute(captor.capture());
        assertNull(captor.getValue().getVariant());
    }

    @Test
    public void toggleDescriptionFlipsExpandedState() {
        when(getProduct.execute("p1")).thenReturn(Single.just(sampleProduct));
        ProductDetailViewModel vm = viewModel();

        assertEquals(Boolean.FALSE, vm.getDescriptionExpanded().getValue());
        vm.toggleDescription();
        assertEquals(Boolean.TRUE, vm.getDescriptionExpanded().getValue());
        vm.toggleDescription();
        assertEquals(Boolean.FALSE, vm.getDescriptionExpanded().getValue());
    }

    @Test
    public void exposesErrorStateOnFailure() {
        when(getProduct.execute("p1")).thenReturn(Single.error(new RuntimeException("oops")));
        ProductDetailViewModel vm = viewModel();

        UiState<Product> state = vm.getProduct().getValue();
        assertTrue(state instanceof UiState.Error);
        assertEquals("oops", ((UiState.Error<Product>) state).getMessage());
    }
}
