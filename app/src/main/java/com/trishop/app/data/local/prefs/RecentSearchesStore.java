package com.trishop.app.data.local.prefs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class RecentSearchesStore {

    private static final Preferences.Key<String> KEY_RECENT =
            PreferencesKeys.stringKey("recent_searches");
    private static final String SEPARATOR = "";
    private static final int MAX_RECENT = 10;

    private final RxDataStore<Preferences> dataStore;

    @Inject
    public RecentSearchesStore(@ApplicationContext Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, "trishop_search_prefs").build();
    }

    @NonNull
    public Flowable<List<String>> recentSearches() {
        return dataStore.data().map(prefs -> decode(prefs.get(KEY_RECENT)));
    }

    @NonNull
    public Completable addRecent(@NonNull String query) {
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return Completable.complete();
        }
        return dataStore.updateDataAsync(current -> {
            List<String> existing = decode(current.get(KEY_RECENT));
            List<String> updated = new ArrayList<>();
            updated.add(trimmed);
            for (String item : existing) {
                if (!item.equalsIgnoreCase(trimmed)) {
                    updated.add(item);
                }
            }
            if (updated.size() > MAX_RECENT) {
                updated = updated.subList(0, MAX_RECENT);
            }
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.set(KEY_RECENT, String.join(SEPARATOR, updated));
            return Single.just(mutable);
        }).ignoreElement();
    }

    @NonNull
    public Completable clear() {
        return dataStore.updateDataAsync(current -> {
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.remove(KEY_RECENT);
            return Single.just(mutable);
        }).ignoreElement();
    }

    private static List<String> decode(String raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String item : raw.split(SEPARATOR)) {
            if (!item.trim().isEmpty()) {
                result.add(item);
            }
        }
        return result;
    }
}
