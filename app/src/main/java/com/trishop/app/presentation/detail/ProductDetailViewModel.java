package com.trishop.app.presentation.detail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.trishop.app.core.UiState;
import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.Review;
import com.trishop.app.domain.repository.AuthRepository;
import com.trishop.app.domain.usecase.AddRecentlyViewedUseCase;
import com.trishop.app.domain.usecase.AddReviewUseCase;
import com.trishop.app.domain.usecase.AddToCartUseCase;
import com.trishop.app.domain.usecase.GetProductPageUseCase;
import com.trishop.app.domain.usecase.GetProductUseCase;
import com.trishop.app.domain.usecase.GetReviewsUseCase;
import com.trishop.app.domain.usecase.ObserveWishlistIdsUseCase;
import com.trishop.app.domain.usecase.ToggleWishlistUseCase;
import com.trishop.app.presentation.common.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class ProductDetailViewModel extends ViewModel {

    public abstract static class Event {

        public static final class AddedToCart extends Event {
            public final String productName;

            AddedToCart(String productName) {
                this.productName = productName;
            }
        }

        public static final class CannotAddWithoutVariant extends Event {
        }

        public static final class WishlistToggled extends Event {
            public final boolean added;

            WishlistToggled(boolean added) {
                this.added = added;
            }
        }

        public static final class ReviewSubmitted extends Event {
        }

        public static final class ReviewFailed extends Event {
        }

        public static final class SignInRequiredForReview extends Event {
        }
    }

    private final GetProductUseCase getProductUseCase;
    private final AddToCartUseCase addToCartUseCase;
    private final ToggleWishlistUseCase toggleWishlistUseCase;
    private final AddRecentlyViewedUseCase addRecentlyViewed;
    private final GetReviewsUseCase getReviews;
    private final AddReviewUseCase addReview;
    private final GetProductPageUseCase getProductPage;
    private final AuthRepository authRepository;

    private final String productId;

    private final MutableLiveData<UiState<Product>> product =
            new MutableLiveData<>(UiState.loading());
    private final MutableLiveData<String> selectedVariant = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> descriptionExpanded = new MutableLiveData<>(false);
    private final LiveData<Boolean> inWishlist;
    private final SingleLiveEvent<Event> events = new SingleLiveEvent<>();
    private final MutableLiveData<List<Review>> reviews =
            new MutableLiveData<>(Collections.emptyList());
    /** Same-category products, excluding the current one (like iOS). */
    private final MutableLiveData<List<Product>> related =
            new MutableLiveData<>(Collections.emptyList());

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public ProductDetailViewModel(GetProductUseCase getProductUseCase,
                                  AddToCartUseCase addToCartUseCase,
                                  ToggleWishlistUseCase toggleWishlistUseCase,
                                  AddRecentlyViewedUseCase addRecentlyViewed,
                                  GetReviewsUseCase getReviews,
                                  AddReviewUseCase addReview,
                                  GetProductPageUseCase getProductPage,
                                  AuthRepository authRepository,
                                  ObserveWishlistIdsUseCase observeWishlistIds,
                                  SavedStateHandle savedStateHandle) {
        this.getProductUseCase = getProductUseCase;
        this.addToCartUseCase = addToCartUseCase;
        this.toggleWishlistUseCase = toggleWishlistUseCase;
        this.addRecentlyViewed = addRecentlyViewed;
        this.getReviews = getReviews;
        this.addReview = addReview;
        this.getProductPage = getProductPage;
        this.authRepository = authRepository;

        productId = Objects.requireNonNull(savedStateHandle.get("productId"));

        inWishlist = LiveDataReactiveStreams.fromPublisher(
                observeWishlistIds.execute().map(ids -> ids.contains(productId)));

        load();
    }

    @NonNull
    public LiveData<UiState<Product>> getProduct() {
        return product;
    }

    @NonNull
    public LiveData<String> getSelectedVariant() {
        return selectedVariant;
    }

    @NonNull
    public LiveData<Boolean> getDescriptionExpanded() {
        return descriptionExpanded;
    }

    @NonNull
    public LiveData<Boolean> getInWishlist() {
        return inWishlist;
    }

    @NonNull
    public LiveData<Event> getEvents() {
        return events;
    }

    @NonNull
    public LiveData<List<Review>> getReviews() {
        return reviews;
    }

    @NonNull
    public LiveData<List<Product>> getRelated() {
        return related;
    }

    public void load() {
        product.setValue(UiState.loading());
        disposables.add(getProductUseCase.execute(productId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loaded -> {
                    product.setValue(UiState.success(loaded));
                    // iOS parity: auto-select only when there is a single size.
                    selectedVariant.setValue(
                            loaded.getSizes().size() == 1 ? loaded.getSizes().get(0) : null);
                    disposables.add(addRecentlyViewed.execute(loaded)
                            .subscribe(() -> {
                            }, error -> {
                            }));
                    loadReviews();
                    loadRelated(loaded);
                }, error -> {
                    String message = error.getLocalizedMessage();
                    product.setValue(UiState.error(message != null ? message : "Error"));
                }));
    }

    public void selectVariant(@NonNull String variant) {
        selectedVariant.setValue(variant);
    }

    public void toggleDescription() {
        Boolean current = descriptionExpanded.getValue();
        descriptionExpanded.setValue(current == null || !current);
    }

    public void addToCart() {
        Product current = successProduct();
        if (current == null) return;
        String variant = selectedVariant.getValue();
        if (!current.getSizes().isEmpty() && variant == null) {
            events.setValue(new Event.CannotAddWithoutVariant());
            return;
        }
        String itemId = variant == null ? current.getId() : current.getId() + "_" + variant;
        CartItem item = new CartItem(
                itemId,
                current.getId(),
                current.getName(),
                current.getBrand(),
                current.getPrice(),
                current.getImageUrl(),
                variant,
                1);
        disposables.add(addToCartUseCase.execute(item)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> events.setValue(new Event.AddedToCart(current.getName())),
                        error -> {
                        }));
    }

    private void loadRelated(Product loaded) {
        ProductCategory category = ProductCategory.fromApiValue(loaded.getCategory());
        if (category == null) return;
        disposables.add(getProductPage.execute(0, 10, category, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(page -> {
                    List<Product> others = new ArrayList<>();
                    for (Product candidate : page.getProducts()) {
                        if (!candidate.getId().equals(loaded.getId())) {
                            others.add(candidate);
                        }
                    }
                    related.setValue(others);
                }, error -> {
                }));
    }

    public void loadReviews() {
        disposables.add(getReviews.execute(productId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(reviews::setValue, error -> {
                }));
    }

    /** Auth-gated like iOS: prompts sign-in when no session exists. */
    public void requestReviewComposer(@NonNull Runnable onAllowed) {
        disposables.add(authRepository.isLoggedIn()
                .firstOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loggedIn -> {
                    if (loggedIn) {
                        onAllowed.run();
                    } else {
                        events.setValue(new Event.SignInRequiredForReview());
                    }
                }, error -> {
                }));
    }

    public void submitReview(int rating, @NonNull String comment) {
        disposables.add(addReview.execute(productId, rating, comment)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(review -> {
                    events.setValue(new Event.ReviewSubmitted());
                    loadReviews();
                }, error -> events.setValue(new Event.ReviewFailed())));
    }

    public void toggleWishlist() {
        Product current = successProduct();
        if (current == null) return;
        disposables.add(toggleWishlistUseCase.execute(current)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(added -> events.setValue(new Event.WishlistToggled(added)),
                        error -> {
                        }));
    }

    @Nullable
    private Product successProduct() {
        UiState<Product> state = product.getValue();
        if (state instanceof UiState.Success) {
            return ((UiState.Success<Product>) state).getData();
        }
        return null;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
