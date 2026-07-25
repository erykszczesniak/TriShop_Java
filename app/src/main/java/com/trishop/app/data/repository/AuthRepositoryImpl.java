package com.trishop.app.data.repository;

import androidx.annotation.NonNull;

import com.trishop.app.core.network.EmptyBodyException;
import com.trishop.app.core.network.TokenStore;
import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.cookie.TriShopCookieJar;
import com.trishop.app.data.remote.dto.AuthResponse;
import com.trishop.app.data.remote.dto.LoginRequest;
import com.trishop.app.data.remote.dto.RegisterRequest;
import com.trishop.app.data.remote.dto.UserDto;
import com.trishop.app.domain.model.User;
import com.trishop.app.domain.repository.AuthRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;
import retrofit2.Response;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final TriShopApi api;
    private final TokenStore tokenStore;
    private final TriShopCookieJar cookieJar;
    private final BehaviorProcessor<Boolean> isLoggedIn;

    @Inject
    public AuthRepositoryImpl(TriShopApi api, TokenStore tokenStore, TriShopCookieJar cookieJar) {
        this.api = api;
        this.tokenStore = tokenStore;
        this.cookieJar = cookieJar;
        this.isLoggedIn = BehaviorProcessor.createDefault(tokenStore.getAccessToken() != null);
    }

    @NonNull
    @Override
    public Flowable<Boolean> isLoggedIn() {
        return isLoggedIn;
    }

    @NonNull
    @Override
    public Single<User> login(@NonNull String email, @NonNull String password) {
        return api.login(new LoginRequest(email, password)).map(this::persistSession);
    }

    @NonNull
    @Override
    public Single<User> register(@NonNull String email, @NonNull String password, @NonNull String name) {
        String[] parts = name.trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : parts[0];
        return api.register(new RegisterRequest(email, password, firstName, lastName))
                .map(this::persistSession);
    }

    // Option A: backend returns the refresh token as an HttpOnly cookie, not in
    // the body. Persist only the access token; the cookie jar handles the rest.
    private User persistSession(Response<AuthResponse> response) {
        if (!response.isSuccessful()) throw new HttpException(response);
        AuthResponse body = response.body();
        if (body == null) throw new EmptyBodyException();
        tokenStore.save(body.accessToken, "");
        isLoggedIn.onNext(true);
        return body.user.toDomain();
    }

    @NonNull
    @Override
    public Completable logout() {
        return Completable.fromAction(() -> {
            tokenStore.clear();
            cookieJar.clear();
            isLoggedIn.onNext(false);
        });
    }

    @NonNull
    @Override
    public Single<Boolean> hasStoredSession() {
        return Single.fromCallable(() -> tokenStore.getAccessToken() != null);
    }

    @NonNull
    @Override
    public Single<User> getProfile() {
        return api.getProfile().map(AuthRepositoryImpl::requireUser);
    }

    @NonNull
    @Override
    public Single<User> uploadAvatar(@NonNull byte[] bytes, @NonNull String mimeType) {
        String extension;
        if (mimeType.contains("png")) {
            extension = "png";
        } else if (mimeType.contains("webp")) {
            extension = "webp";
        } else {
            extension = "jpg";
        }
        RequestBody body = RequestBody.create(bytes, MediaType.parse(mimeType));
        MultipartBody.Part part =
                MultipartBody.Part.createFormData("file", "avatar." + extension, body);
        return api.uploadAvatar(part).map(AuthRepositoryImpl::requireUser);
    }

    @NonNull
    @Override
    public Single<User> deleteAvatar() {
        return api.deleteAvatar().map(AuthRepositoryImpl::requireUser);
    }

    private static User requireUser(Response<UserDto> response) {
        if (!response.isSuccessful()) throw new HttpException(response);
        UserDto body = response.body();
        if (body == null) throw new EmptyBodyException();
        return body.toDomain();
    }
}
