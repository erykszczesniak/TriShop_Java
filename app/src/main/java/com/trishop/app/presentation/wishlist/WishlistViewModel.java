package com.trishop.app.presentation.wishlist;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;

import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.WishlistItem;
import com.trishop.app.domain.usecase.AddToCartUseCase;
import com.trishop.app.domain.usecase.ObserveWishlistUseCase;
import com.trishop.app.domain.usecase.RemoveFromWishlistUseCase;
import com.trishop.app.domain.usecase.SyncWishlistUseCase;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

@HiltViewModel
public class WishlistViewModel extends ViewModel {

    private final RemoveFromWishlistUseCase removeUseCase;
    private final AddToCartUseCase addToCart;

    private final BehaviorProcessor<List<WishlistItem>> itemsProcessor =
            BehaviorProcessor.createDefault(Collections.emptyList());
    private final LiveData<List<WishlistItem>> items;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public WishlistViewModel(ObserveWishlistUseCase observeWishlist,
                             SyncWishlistUseCase syncWishlist,
                             RemoveFromWishlistUseCase removeUseCase,
                             AddToCartUseCase addToCart) {
        this.removeUseCase = removeUseCase;
        this.addToCart = addToCart;

        disposables.add(observeWishlist.execute().subscribe(itemsProcessor::onNext, error -> {
        }));
        items = LiveDataReactiveStreams.fromPublisher(itemsProcessor);

        disposables.add(syncWishlist.execute().subscribe(() -> {
        }, error -> {
        }));
    }

    @NonNull
    public LiveData<List<WishlistItem>> getItems() {
        return items;
    }

    public void remove(@NonNull WishlistItem item) {
        disposables.add(removeUseCase.execute(item.getProductId()).subscribe(() -> {
        }, error -> {
        }));
    }

    /** Sizeless products go straight to the bag; sized ones open the PDP. */
    public void addToBag(@NonNull WishlistItem item,
                         @NonNull Runnable onNeedsSize,
                         @NonNull Runnable onAdded) {
        if (!item.getSizes().isEmpty()) {
            onNeedsSize.run();
            return;
        }
        CartItem cartItem = new CartItem(
                item.getProductId(),
                item.getProductId(),
                item.getName(),
                item.getBrand(),
                item.getPrice(),
                item.getImageUrl(),
                null,
                1);
        disposables.add(addToCart.execute(cartItem)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onAdded::run, error -> {
                }));
    }

    @NonNull
    public String shareText() {
        List<WishlistItem> current = itemsProcessor.getValue();
        if (current == null || current.isEmpty()) {
            return "My TriShop Wishlist is empty";
        }
        StringBuilder builder = new StringBuilder("My TriShop Wishlist:\n");
        for (int index = 0; index < current.size(); index++) {
            WishlistItem item = current.get(index);
            builder.append(index + 1)
                    .append(". ")
                    .append(item.getName())
                    .append(" — $")
                    .append(String.format(Locale.US, "%.2f", item.getPrice()))
                    .append("\n");
        }
        return builder.toString().trim();
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
