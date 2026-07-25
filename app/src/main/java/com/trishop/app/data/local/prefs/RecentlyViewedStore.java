package com.trishop.app.data.local.prefs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.trishop.app.domain.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

/** Last products the user opened, newest first (max 10) — mirrors iOS. */
@Singleton
public class RecentlyViewedStore {

    private static final Preferences.Key<String> KEY_RECENT =
            PreferencesKeys.stringKey("recently_viewed_products");
    private static final int MAX_RECENT = 10;

    private final RxDataStore<Preferences> dataStore;
    private final JsonAdapter<List<Product>> adapter;

    @Inject
    public RecentlyViewedStore(@ApplicationContext Context context, Moshi moshi) {
        dataStore = new RxPreferenceDataStoreBuilder(context, "trishop_recently_viewed_prefs").build();
        adapter = moshi.adapter(Types.newParameterizedType(List.class, Product.class));
    }

    @NonNull
    public Flowable<List<Product>> recentlyViewed() {
        return dataStore.data().map(prefs -> decode(prefs.get(KEY_RECENT)));
    }

    @NonNull
    public Completable add(@NonNull Product product) {
        return dataStore.updateDataAsync(current -> {
            List<Product> existing = decode(current.get(KEY_RECENT));
            List<Product> updated = new ArrayList<>();
            updated.add(product);
            for (Product item : existing) {
                if (!item.getId().equals(product.getId())) {
                    updated.add(item);
                }
            }
            if (updated.size() > MAX_RECENT) {
                updated = updated.subList(0, MAX_RECENT);
            }
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.set(KEY_RECENT, adapter.toJson(updated));
            return io.reactivex.rxjava3.core.Single.just(mutable);
        }).ignoreElement();
    }

    @NonNull
    public Completable clear() {
        return dataStore.updateDataAsync(current -> {
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.remove(KEY_RECENT);
            return io.reactivex.rxjava3.core.Single.just(mutable);
        }).ignoreElement();
    }

    private List<Product> decode(String json) {
        if (json == null) return Collections.emptyList();
        try {
            List<Product> parsed = adapter.fromJson(json);
            return parsed != null ? parsed : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
