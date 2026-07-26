package com.trishop.app.presentation.cart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.Coupon;
import com.trishop.app.domain.usecase.ClearCartUseCase;
import com.trishop.app.domain.usecase.ObserveCartUseCase;
import com.trishop.app.domain.usecase.RemoveCartItemUseCase;
import com.trishop.app.domain.usecase.UpdateCartQuantityUseCase;
import com.trishop.app.domain.usecase.ValidateCouponUseCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CartViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTask = new InstantTaskExecutorRule();

    private final CartItem sampleItem = new CartItem(
            "p1_S", "p1", "Wetsuit Pro", "TriBrand", 299.0, "", "S", 1);

    private ObserveCartUseCase observeCart;
    private UpdateCartQuantityUseCase updateQuantity;
    private RemoveCartItemUseCase removeItem;
    private ValidateCouponUseCase validateCoupon;
    private ClearCartUseCase clearCart;

    @Before
    public void setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        observeCart = mock(ObserveCartUseCase.class);
        updateQuantity = mock(UpdateCartQuantityUseCase.class);
        removeItem = mock(RemoveCartItemUseCase.class);
        validateCoupon = mock(ValidateCouponUseCase.class);
        clearCart = mock(ClearCartUseCase.class);
        when(clearCart.execute()).thenReturn(Completable.complete());
        when(observeCart.execute())
                .thenReturn(Flowable.just(Collections.singletonList(sampleItem)));
        when(updateQuantity.execute(anyString(), anyInt())).thenReturn(Completable.complete());
        when(removeItem.execute(anyString())).thenReturn(Completable.complete());
    }

    @After
    public void tearDown() {
        RxAndroidPlugins.reset();
    }

    private CartViewModel viewModel() {
        return new CartViewModel(observeCart, updateQuantity, removeItem, validateCoupon, clearCart);
    }

    @Test
    public void incrementRaisesQuantityByOne() {
        CartViewModel vm = viewModel();
        vm.increment(sampleItem);
        verify(updateQuantity).execute("p1_S", 2);
    }

    @Test
    public void decrementLowersQuantityByOne() {
        CartViewModel vm = viewModel();
        vm.decrement(sampleItem.withQuantity(3));
        verify(updateQuantity).execute("p1_S", 2);
    }

    @Test
    public void removeInvokesRemoveItemAndEmitsRemovedEvent() {
        CartViewModel vm = viewModel();
        List<CartViewModel.Event> events = new ArrayList<>();
        vm.getEvents().observeForever(events::add);

        vm.remove(sampleItem);

        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof CartViewModel.Event.Removed);
        assertEquals(sampleItem, ((CartViewModel.Event.Removed) events.get(0)).item);
        verify(removeItem).execute("p1_S");
    }

    @Test
    public void validPromoAppliesDiscountToSummary() {
        when(validateCoupon.execute(eq("SAVE10"), anyDouble()))
                .thenReturn(Single.just(new Coupon("SAVE10", 10.0, true, null)));
        CartViewModel vm = viewModel();
        vm.getItems().observeForever(items -> {
        });
        List<CartViewModel.Summary> summaries = new ArrayList<>();
        vm.getSummary().observeForever(summaries::add);

        vm.applyPromo("SAVE10");

        assertTrue(vm.currentPromoState() instanceof CartViewModel.PromoState.Applied);
        CartViewModel.Summary summary = summaries.get(summaries.size() - 1);
        assertEquals(299.0, summary.subtotal, 0.001);
        assertEquals(29.9, summary.discount, 0.001);
        assertEquals((299.0 - 29.9) * 0.08, summary.tax, 0.001);
    }

    @Test
    public void invalidPromoSurfacesErrorState() {
        when(validateCoupon.execute(eq("NOPE"), anyDouble()))
                .thenReturn(Single.just(new Coupon("NOPE", 0.0, false, "expired")));
        CartViewModel vm = viewModel();
        vm.getItems().observeForever(items -> {
        });

        vm.applyPromo("NOPE");

        CartViewModel.PromoState state = vm.currentPromoState();
        assertTrue(state instanceof CartViewModel.PromoState.Error);
        assertEquals("expired", ((CartViewModel.PromoState.Error) state).message);
    }
}
