package com.trishop.app.data.remote.interceptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.core.network.TokenStore;
import com.trishop.app.data.remote.api.AuthRefreshApi;
import com.trishop.app.data.remote.cookie.TriShopCookieJar;
import com.trishop.app.data.remote.dto.AuthResponse;

import javax.inject.Inject;

import dagger.Lazy;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    private static final String AUTH_HEADER = "Authorization";
    private static final int MAX_RETRIES = 2;

    private final TokenStore tokenStore;
    private final Lazy<AuthRefreshApi> refreshApi;
    private final TriShopCookieJar cookieJar;

    @Inject
    public TokenAuthenticator(TokenStore tokenStore,
                              Lazy<AuthRefreshApi> refreshApi,
                              TriShopCookieJar cookieJar) {
        this.tokenStore = tokenStore;
        this.refreshApi = refreshApi;
        this.cookieJar = cookieJar;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) {
        synchronized (this) {
            if (responseCount(response) >= MAX_RETRIES) return null;

            String currentAccess = tokenStore.getAccessToken();
            String sentHeader = response.request().header(AUTH_HEADER);
            // Another thread already refreshed the token while we were waiting
            // on the monitor — retry this request with the newer token instead
            // of kicking off a second refresh.
            if (currentAccess != null && sentHeader != null
                    && !sentHeader.equals("Bearer " + currentAccess)) {
                return response.request().newBuilder()
                        .header(AUTH_HEADER, "Bearer " + currentAccess)
                        .build();
            }

            try {
                // Refresh token travels in the HttpOnly cookie managed by
                // TriShopCookieJar; the refresh call itself has no body.
                retrofit2.Response<AuthResponse> refreshResponse =
                        refreshApi.get().refresh().execute();
                AuthResponse body = refreshResponse.body();
                if (refreshResponse.isSuccessful() && body != null) {
                    tokenStore.save(body.accessToken, "");
                    return response.request().newBuilder()
                            .header(AUTH_HEADER, "Bearer " + body.accessToken)
                            .build();
                } else {
                    tokenStore.clear();
                    cookieJar.clear();
                    return null;
                }
            } catch (Exception e) {
                tokenStore.clear();
                cookieJar.clear();
                return null;
            }
        }
    }

    private static int responseCount(Response response) {
        int count = 1;
        Response prior = response.priorResponse();
        while (prior != null) {
            count++;
            prior = prior.priorResponse();
        }
        return count;
    }
}
