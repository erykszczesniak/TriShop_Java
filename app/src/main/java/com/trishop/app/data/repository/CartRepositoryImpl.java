package com.trishop.app.data.repository;

import androidx.annotation.NonNull;

import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.dto.CartItemDto;
import com.trishop.app.data.remote.dto.CartItemRequest;
import com.trishop.app.data.remote.dto.CartResponse;
import com.trishop.app.domain.model.CartItem;
import com.trishop.app.domain.repository.AuthRepository;
import com.trishop.app.domain.repository.CartRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import retrofit2.Response;

/**
 * Server-backed cart mirroring the iOS {@code CartManager}: the source of truth
 * is {@code /cart}, with optimistic in-memory updates that reconcile with the
 * server response. Failed writes fall back to a fresh fetch (or keep the
 * optimistic state for guests, whose cart lives only in memory).
 */
@Singleton
public class CartRepositoryImpl implements CartRepository {

    private final TriShopApi api;
    private final BehaviorProcessor<List<CartItem>> items =
            BehaviorProcessor.createDefault(Collections.emptyList());

    @Inject
    public CartRepositoryImpl(TriShopApi api, AuthRepository authRepository) {
        this.api = api;
        fetch();
        // The cart is per-user server-side: refresh it whenever the session flips.
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

    @NonNull
    @Override
    public Flowable<List<CartItem>> observeCart() {
        return items.onBackpressureLatest();
    }

    private void fetch() {
        api.getCart().subscribe(this::applyServerCart, error -> {
        });
    }

    private void applyServerCart(Response<CartResponse> response) {
        CartResponse body = response.body();
        if (!response.isSuccessful() || body == null) return;
        List<CartItem> mapped = new ArrayList<>();
        for (CartItemDto dto : body.items) {
            mapped.add(dto.toDomain());
        }
        items.onNext(Collections.unmodifiableList(mapped));
    }

    @NonNull
    @Override
    public Completable upsert(@NonNull CartItem item) {
        return Completable.fromAction(() -> {
            // Optimistic local update
            List<CartItem> current = current();
            List<CartItem> updated = new ArrayList<>();
            boolean merged = false;
            for (CartItem existing : current) {
                if (existing.getId().equals(item.getId())) {
                    updated.add(existing.withQuantity(existing.getQuantity() + item.getQuantity()));
                    merged = true;
                } else {
                    updated.add(existing);
                }
            }
            if (!merged) {
                updated.add(item);
            }
            items.onNext(Collections.unmodifiableList(updated));

            api.addToServerCart(new CartItemRequest(
                            Long.parseLong(item.getProductId()),
                            item.getQuantity(),
                            item.getVariant()))
                    .subscribe(this::applyServerCart, error -> {
                    });
        });
    }

    @NonNull
    @Override
    public Completable updateQuantity(@NonNull String id, int quantity) {
        return Completable.defer(() -> {
            CartItem item = findById(id);
            if (item == null) return Completable.complete();
            if (quantity <= 0) {
                return remove(id);
            }
            List<CartItem> updated = new ArrayList<>();
            for (CartItem existing : current()) {
                updated.add(existing.getId().equals(id) ? existing.withQuantity(quantity) : existing);
            }
            items.onNext(Collections.unmodifiableList(updated));

            api.updateCartQuantity(Long.parseLong(item.getProductId()), quantity)
                    .subscribe(response -> {
                        if (response.isSuccessful()) {
                            applyServerCart(response);
                        } else {
                            fetch();
                        }
                    }, error -> fetch());
            return Completable.complete();
        });
    }

    @NonNull
    @Override
    public Completable remove(@NonNull String id) {
        return Completable.fromAction(() -> {
            CartItem item = findById(id);
            if (item == null) return;
            List<CartItem> updated = new ArrayList<>();
            for (CartItem existing : current()) {
                if (!existing.getId().equals(id)) {
                    updated.add(existing);
                }
            }
            items.onNext(Collections.unmodifiableList(updated));

            api.removeFromServerCart(Long.parseLong(item.getProductId()))
                    .subscribe(response -> {
                        if (!response.isSuccessful()) fetch();
                    }, error -> fetch());
        });
    }

    @NonNull
    @Override
    public Completable clear() {
        return Completable.fromAction(() -> {
            items.onNext(Collections.emptyList());
            api.clearServerCart().subscribe(response -> {
            }, error -> {
            });
        });
    }

    private List<CartItem> current() {
        List<CartItem> value = items.getValue();
        return value != null ? value : Collections.emptyList();
    }

    private CartItem findById(String id) {
        for (CartItem item : current()) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }
}
