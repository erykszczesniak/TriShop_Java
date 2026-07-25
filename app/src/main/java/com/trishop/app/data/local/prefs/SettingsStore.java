package com.trishop.app.data.local.prefs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class SettingsStore {

    private static final Preferences.Key<String> KEY_APPEARANCE =
            PreferencesKeys.stringKey("appearance");
    private static final Preferences.Key<Boolean> KEY_NOTIFICATIONS =
            PreferencesKeys.booleanKey("notifications_enabled");

    private final RxDataStore<Preferences> dataStore;

    @Inject
    public SettingsStore(@ApplicationContext Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, "trishop_settings_prefs").build();
    }

    @NonNull
    public Flowable<Appearance> appearance() {
        return dataStore.data().map(prefs -> parseAppearance(prefs.get(KEY_APPEARANCE)));
    }

    @NonNull
    public Flowable<Boolean> notificationsEnabled() {
        return dataStore.data().map(prefs -> {
            Boolean enabled = prefs.get(KEY_NOTIFICATIONS);
            return enabled != null ? enabled : Boolean.FALSE;
        });
    }

    /** Blocking read used once at process start, before any activity inflates. */
    @NonNull
    public Appearance currentAppearance() {
        return appearance().blockingFirst();
    }

    @NonNull
    public Completable setAppearance(@NonNull Appearance value) {
        return dataStore.updateDataAsync(current -> {
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.set(KEY_APPEARANCE, value.name());
            return Single.just(mutable);
        }).ignoreElement();
    }

    @NonNull
    public Completable setNotificationsEnabled(boolean enabled) {
        return dataStore.updateDataAsync(current -> {
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.set(KEY_NOTIFICATIONS, enabled);
            return Single.just(mutable);
        }).ignoreElement();
    }

    private static Appearance parseAppearance(String raw) {
        if (raw == null) return Appearance.SYSTEM;
        try {
            return Appearance.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return Appearance.SYSTEM;
        }
    }
}
