package com.trishop.app.data.remote.cookie;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Persists cookies (notably the HttpOnly {@code refreshToken} cookie scoped to
 * {@code /api/v1/auth}) in {@link EncryptedSharedPreferences}. Cookies are keyed
 * by {@code domain|path|name} so multiple cookies for different paths coexist.
 */
@Singleton
public class TriShopCookieJar implements CookieJar {

    private static final String FILE_NAME = "trishop_cookies_prefs";

    private final Context context;
    private volatile SharedPreferences prefs;

    @Inject
    public TriShopCookieJar(@ApplicationContext Context context) {
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
                        throw new IllegalStateException("Unable to open cookie preferences", e);
                    }
                    prefs = local;
                }
            }
        }
        return local;
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        List<Cookie> cookies = new ArrayList<>();
        for (String key : prefs().getAll().keySet()) {
            String raw = prefs().getString(key, null);
            if (raw == null) continue;
            Cookie cookie = Cookie.parse(url, raw);
            if (cookie != null && cookie.matches(url) && !isExpired(cookie)) {
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        SharedPreferences.Editor editor = prefs().edit();
        for (Cookie cookie : cookies) {
            String key = cookie.domain() + "|" + cookie.path() + "|" + cookie.name();
            if (cookie.expiresAt() < System.currentTimeMillis() || cookie.value().isEmpty()) {
                editor.remove(key);
            } else {
                editor.putString(key, cookie.toString());
            }
        }
        editor.apply();
    }

    public void clear() {
        prefs().edit().clear().apply();
    }

    private static boolean isExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }
}
