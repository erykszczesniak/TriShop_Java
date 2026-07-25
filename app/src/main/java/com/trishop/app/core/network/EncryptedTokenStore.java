package com.trishop.app.core.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class EncryptedTokenStore implements TokenStore {

    private static final String FILE_NAME = "trishop_secure_prefs";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";

    private final Context context;
    private volatile SharedPreferences prefs;

    @Inject
    public EncryptedTokenStore(@ApplicationContext Context context) {
        this.context = context;
    }

    private SharedPreferences prefs() {
        SharedPreferences local = prefs;
        if (local == null) {
            synchronized (this) {
                local = prefs;
                if (local == null) {
                    try {
                        MasterKey masterKey = new MasterKey.Builder(context)
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                .build();
                        local = EncryptedSharedPreferences.create(
                                context,
                                FILE_NAME,
                                masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
                    } catch (GeneralSecurityException | IOException e) {
                        throw new IllegalStateException("Unable to open encrypted preferences", e);
                    }
                    prefs = local;
                }
            }
        }
        return local;
    }

    @Nullable
    @Override
    public String getAccessToken() {
        return prefs().getString(KEY_ACCESS, null);
    }

    @Nullable
    @Override
    public String getRefreshToken() {
        return prefs().getString(KEY_REFRESH, null);
    }

    @Override
    public void save(@NonNull String access, @NonNull String refresh) {
        prefs().edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_REFRESH, refresh)
                .apply();
    }

    @Override
    public void clear() {
        prefs().edit().clear().apply();
    }
}
