package com.trishop.app.presentation.checkout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.trishop.app.core.UiState;
import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.model.User;
import com.trishop.app.domain.usecase.CheckoutUseCase;
import com.trishop.app.domain.usecase.GetProfileUseCase;
import com.trishop.app.domain.usecase.ObserveCartUseCase;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

@HiltViewModel
public class CheckoutViewModel extends ViewModel {

    private static final double TAX_RATE = 0.08;
    private static final double FREE_SHIPPING_THRESHOLD = 100.0;
    private static final double SHIPPING_COST = 9.99;

    public static final class Summary {
        public final int itemCount;
        public final double subtotal;
        public final double discount;
        /** null = free */
        @Nullable
        public final Double shipping;
        public final double tax;
        public final double total;

        Summary(int itemCount, double subtotal, double discount,
                @Nullable Double shipping, double tax, double total) {
            this.itemCount = itemCount;
            this.subtotal = subtotal;
            this.discount = discount;
            this.shipping = shipping;
            this.tax = tax;
            this.total = total;
        }
    }

    private final CheckoutUseCase checkout;

    @Nullable
    private final String couponCode;
    private final double discountPercent;

    private final BehaviorProcessor<List<CartItem>> items =
            BehaviorProcessor.createDefault(Collections.emptyList());

    private final LiveData<Summary> summary;
    /** Signed-in user used to prefill the address form. */
    private final MutableLiveData<User> user = new MutableLiveData<>(null);
    private final MutableLiveData<UiState<Order>> orderState = new MutableLiveData<>(null);

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public CheckoutViewModel(ObserveCartUseCase observeCart,
                             GetProfileUseCase getProfile,
                             CheckoutUseCase checkout,
                             SavedStateHandle savedState) {
        this.checkout = checkout;

        couponCode = savedState.get("couponCode");
        Float rawDiscount = savedState.get("discountPercent");
        discountPercent = rawDiscount != null ? rawDiscount.doubleValue() : 0.0;

        disposables.add(observeCart.execute().subscribe(items::onNext, error -> {
        }));

        /*
         * Cost breakdown mirroring the web/iOS clients: 8% tax on the discounted
         * subtotal; shipping (free over $100, else $9.99) is displayed but never
         * charged — web parity.
         */
        summary = LiveDataReactiveStreams.fromPublisher(items.map(current -> {
            double subtotal = 0.0;
            int itemCount = 0;
            for (CartItem item : current) {
                subtotal += item.getSubtotal();
                itemCount += item.getQuantity();
            }
            double discount = subtotal * discountPercent / 100.0;
            double discounted = subtotal - discount;
            Double shipping = discounted > FREE_SHIPPING_THRESHOLD ? null : SHIPPING_COST;
            double tax = discounted * TAX_RATE;
            return new Summary(itemCount, subtotal, discount, shipping, tax, discounted + tax);
        }));

        disposables.add(getProfile.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user::setValue, error -> {
                }));
    }

    @NonNull
    public LiveData<Summary> getSummary() {
        return summary;
    }

    @NonNull
    public LiveData<User> getUser() {
        return user;
    }

    @NonNull
    public LiveData<UiState<Order>> getOrderState() {
        return orderState;
    }

    public void placeOrder(@NonNull String address, @NonNull String city, @NonNull String zip) {
        orderState.setValue(UiState.loading());
        List<CartItem> current = items.getValue();
        disposables.add(checkout.execute(
                        current != null ? current : Collections.emptyList(),
                        address + ", " + city + ", " + zip,
                        couponCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(order -> orderState.setValue(UiState.success(order)),
                        error -> {
                            String message = error.getLocalizedMessage();
                            orderState.setValue(UiState.error(message != null ? message : "Error"));
                        }));
    }

    public void consumeOrderState() {
        orderState.setValue(null);
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
