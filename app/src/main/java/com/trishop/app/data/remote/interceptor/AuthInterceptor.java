package com.trishop.app.data.remote.interceptor;

import androidx.annotation.NonNull;

import com.trishop.app.core.network.TokenStore;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenStore tokenStore;

    @Inject
    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder()
                .header("Accept", "application/json");

        String token = tokenStore.getAccessToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(builder.build());

        if (response.code() == 401) {
            tokenStore.clear();
        }
        return response;
    }
}
