package com.trishop.app.data.local.prefs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;

/** Persistent assistant session id, minted on first use (like iOS). */
@Singleton
public class ChatSessionStore {

    private static final Preferences.Key<String> KEY_SESSION =
            PreferencesKeys.stringKey("chat_session_id");

    private final RxDataStore<Preferences> dataStore;

    @Inject
    public ChatSessionStore(@ApplicationContext Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, "trishop_chat_prefs").build();
    }

    @NonNull
    public Single<String> sessionId() {
        return dataStore.data().firstOrError().flatMap(prefs -> {
            String existing = prefs.get(KEY_SESSION);
            if (existing != null) {
                return Single.just(existing);
            }
            String minted = UUID.randomUUID().toString();
            return dataStore.updateDataAsync(current -> {
                MutablePreferences mutable = current.toMutablePreferences();
                mutable.set(KEY_SESSION, minted);
                return Single.just(mutable);
            }).map(ignored -> minted);
        });
    }
}
