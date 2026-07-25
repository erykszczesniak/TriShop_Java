package com.trishop.app.data.repository;

import androidx.annotation.NonNull;

import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.dto.ProductDto;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.WishlistItem;
import com.trishop.app.domain.repository.AuthRepository;
import com.trishop.app.domain.repository.WishlistRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Server-backed wishlist mirroring the iOS {@code WishlistManager}: in-memory
 * state fetched from {@code /wishlist}, optimistic toggles synced fire-and-forget.
 */
@Singleton
public class WishlistRepositoryImpl implements WishlistRepository {

    private final TriShopApi api;
    private final io.reactivex.rxjava3.processors.BehaviorProcessor<List<WishlistItem>> items =
            io.reactivex.rxjava3.processors.BehaviorProcessor.createDefault(Collections.emptyList());

    @Inject
    public WishlistRepositoryImpl(TriShopApi api, AuthRepository authRepository) {
        this.api = api;
        fetch();
        authRepository.isLoggedIn()
                .skip(1)
                .subscribe(loggedIn -> {
                    if (loggedIn) {
                        fetch();
                    } else {
                        items.onNext(Collections.emptyList());
                    }
                }, error -> {
                });
    }

    private void fetch() {
        api.getWishlist().subscribe(response -> {
            List<ProductDto> body = response.body();
            if (!response.isSuccessful() || body == null) return;
            List<WishlistItem> mapped = new ArrayList<>();
            for (ProductDto dto : body) {
                mapped.add(toWishlistItem(dto.toDomain()));
            }
            items.onNext(Collections.unmodifiableList(mapped));
        }, error -> {
        });
    }

    @NonNull
    @Override
    public Flowable<List<WishlistItem>> observeWishlist() {
        return items.onBackpressureLatest();
    }

    @NonNull
    @Override
    public Flowable<Set<String>> observeWishlistIds() {
        return items.map(list -> {
            Set<String> ids = new HashSet<>();
            for (WishlistItem item : list) {
                ids.add(item.getProductId());
            }
            return ids;
        });
    }

    @NonNull
    @Override
    public Single<Boolean> contains(@NonNull String productId) {
        return Single.fromCallable(() -> containsNow(productId));
    }

    @NonNull
    @Override
    public Single<Boolean> toggle(@NonNull Product product) {
        return Single.fromCallable(() -> {
            boolean wasIn = containsNow(product.getId());
            if (wasIn) {
                removeLocally(product.getId());
                api.removeFromWishlist(product.getId()).subscribe(response -> {
                }, error -> {
                });
                return false;
            } else {
                List<WishlistItem> updated = new ArrayList<>(current());
                updated.add(toWishlistItem(product));
                items.onNext(Collections.unmodifiableList(updated));
                api.addToWishlist(product.getId()).subscribe(response -> {
                }, error -> {
                });
                return true;
            }
        });
    }

    @NonNull
    @Override
    public Completable remove(@NonNull String productId) {
        return Completable.defer(() -> {
            removeLocally(productId);
            return api.removeFromWishlist(productId).ignoreElement();
        });
    }

    @NonNull
    @Override
    public Completable sync() {
        return Completable.fromAction(this::fetch);
    }

    private List<WishlistItem> current() {
        List<WishlistItem> value = items.getValue();
        return value != null ? value : Collections.emptyList();
    }

    private boolean containsNow(String productId) {
        for (WishlistItem item : current()) {
            if (item.getProductId().equals(productId)) return true;
        }
        return false;
    }

    private void removeLocally(String productId) {
        List<WishlistItem> updated = new ArrayList<>();
        for (WishlistItem item : current()) {
            if (!item.getProductId().equals(productId)) {
                updated.add(item);
            }
        }
        items.onNext(Collections.unmodifiableList(updated));
    }

    private static WishlistItem toWishlistItem(Product product) {
        return new WishlistItem(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getPrice(),
                product.getImageUrl(),
                product.getSizes());
    }
}
