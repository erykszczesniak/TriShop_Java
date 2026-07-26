package com.trishop.app.presentation.cart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;

import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.Coupon;
import com.trishop.app.domain.usecase.ClearCartUseCase;
import com.trishop.app.domain.usecase.ObserveCartUseCase;
import com.trishop.app.domain.usecase.RemoveCartItemUseCase;
import com.trishop.app.domain.usecase.UpdateCartQuantityUseCase;
import com.trishop.app.domain.usecase.ValidateCouponUseCase;
import com.trishop.app.presentation.common.SingleLiveEvent;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

@HiltViewModel
public class CartViewModel extends ViewModel {

    private static final double TAX_RATE = 0.08;

    public static final class Summary {
        public final double subtotal;
        public final double discount;
        public final double tax;
        public final double total;

        Summary(double subtotal, double discount, double tax, double total) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.tax = tax;
            this.total = total;
        }
    }

    public abstract static class PromoState {

        public static final class Idle extends PromoState {
        }

        public static final class Validating extends PromoState {
        }

        public static final class Applied extends PromoState {
            public final Coupon coupon;

            Applied(Coupon coupon) {
                this.coupon = coupon;
            }
        }

        public static final class Error extends PromoState {
            @Nullable
            public final String message;

            Error(@Nullable String message) {
                this.message = message;
            }
        }
    }

    public abstract static class Event {

        public static final class Removed extends Event {
            public final CartItem item;

            Removed(CartItem item) {
                this.item = item;
            }
        }
    }

    private final UpdateCartQuantityUseCase updateQuantity;
    private final RemoveCartItemUseCase removeItem;
    private final ValidateCouponUseCase validateCoupon;
    private final ClearCartUseCase clearCart;

    private final BehaviorProcessor<List<CartItem>> itemsProcessor =
            BehaviorProcessor.createDefault(Collections.emptyList());
    private final BehaviorProcessor<PromoState> promoProcessor =
            BehaviorProcessor.createDefault(new PromoState.Idle());

    private final LiveData<List<CartItem>> items;
    private final LiveData<PromoState> promoState;
    private final LiveData<Summary> summary;
    private final SingleLiveEvent<Event> events = new SingleLiveEvent<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public CartViewModel(ObserveCartUseCase observeCart,
                         UpdateCartQuantityUseCase updateQuantity,
                         RemoveCartItemUseCase removeItem,
                         ValidateCouponUseCase validateCoupon,
                         ClearCartUseCase clearCart) {
        this.updateQuantity = updateQuantity;
        this.removeItem = removeItem;
        this.validateCoupon = validateCoupon;
        this.clearCart = clearCart;

        disposables.add(observeCart.execute().subscribe(itemsProcessor::onNext, error -> {
        }));

        items = LiveDataReactiveStreams.fromPublisher(itemsProcessor);
        promoState = LiveDataReactiveStreams.fromPublisher(promoProcessor);

        /* Flat 8% tax on (subtotal − discount), mirroring the web/iOS clients. */
        summary = LiveDataReactiveStreams.fromPublisher(Flowable.combineLatest(
                itemsProcessor, promoProcessor, (current, promo) -> {
                    double subtotal = 0.0;
                    for (CartItem item : current) {
                        subtotal += item.getSubtotal();
                    }
                    double rate = promo instanceof PromoState.Applied
                            ? ((PromoState.Applied) promo).coupon.getDiscountPercent()
                            : 0.0;
                    double discount = subtotal * rate / 100.0;
                    double tax = (subtotal - discount) * TAX_RATE;
                    return new Summary(subtotal, discount, tax, subtotal - discount + tax);
                }));
    }

    @NonNull
    public LiveData<List<CartItem>> getItems() {
        return items;
    }

    @NonNull
    public LiveData<PromoState> getPromoState() {
        return promoState;
    }

    @NonNull
    public LiveData<Summary> getSummary() {
        return summary;
    }

    @NonNull
    public LiveData<Event> getEvents() {
        return events;
    }

    @NonNull
    public PromoState currentPromoState() {
        PromoState value = promoProcessor.getValue();
        return value != null ? value : new PromoState.Idle();
    }

    public void increment(@NonNull CartItem item) {
        disposables.add(updateQuantity.execute(item.getId(), item.getQuantity() + 1)
                .subscribe(() -> {
                }, error -> {
                }));
    }

    public void decrement(@NonNull CartItem item) {
        disposables.add(updateQuantity.execute(item.getId(), item.getQuantity() - 1)
                .subscribe(() -> {
                }, error -> {
                }));
    }

    public void remove(@NonNull CartItem item) {
        disposables.add(removeItem.execute(item.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> events.setValue(new Event.Removed(item)),
                        error -> events.setValue(new Event.Removed(item))));
    }

    public void applyPromo(@NonNull String code) {
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            promoProcessor.onNext(new PromoState.Error(null));
            return;
        }
        promoProcessor.onNext(new PromoState.Validating());
        double subtotal = 0.0;
        List<CartItem> current = itemsProcessor.getValue();
        if (current != null) {
            for (CartItem item : current) {
                subtotal += item.getSubtotal();
            }
        }
        disposables.add(validateCoupon.execute(trimmed, subtotal)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(coupon -> promoProcessor.onNext(coupon.isValid()
                                ? new PromoState.Applied(coupon)
                                : new PromoState.Error(coupon.getMessage())),
                        error -> promoProcessor.onNext(new PromoState.Error(null))));
    }

    public void removePromo() {
        promoProcessor.onNext(new PromoState.Idle());
    }

    public void clear() {
        disposables.add(clearCart.execute().subscribe(() -> {
        }, error -> {
        }));
    }

    @Nullable
    public String getAppliedCouponCode() {
        PromoState state = currentPromoState();
        return state instanceof PromoState.Applied
                ? ((PromoState.Applied) state).coupon.getCode()
                : null;
    }

    public double getAppliedDiscountPercent() {
        PromoState state = currentPromoState();
        return state instanceof PromoState.Applied
                ? ((PromoState.Applied) state).coupon.getDiscountPercent()
                : 0.0;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
