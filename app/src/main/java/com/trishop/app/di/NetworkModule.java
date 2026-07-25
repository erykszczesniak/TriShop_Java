package com.trishop.app.di;

import com.squareup.moshi.Moshi;
import com.trishop.app.BuildConfig;
import com.trishop.app.data.remote.api.AuthRefreshApi;
import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.cookie.TriShopCookieJar;
import com.trishop.app.data.remote.interceptor.AuthInterceptor;
import com.trishop.app.data.remote.interceptor.TokenAuthenticator;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public final class NetworkModule {

    private static final String CLIENT_MAIN = "main";
    private static final String CLIENT_REFRESH = "refresh";
    private static final String RETROFIT_MAIN = "retrofitMain";
    private static final String RETROFIT_REFRESH = "retrofitRefresh";

    private NetworkModule() {
    }

    @Provides
    @Singleton
    static Moshi provideMoshi() {
        return new Moshi.Builder().build();
    }

    private static HttpLoggingInterceptor loggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);
        return interceptor;
    }

    @Provides
    @Singleton
    @Named(CLIENT_REFRESH)
    static OkHttpClient provideRefreshClient(TriShopCookieJar cookieJar) {
        return new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(loggingInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    @Named(RETROFIT_REFRESH)
    static Retrofit provideRefreshRetrofit(@Named(CLIENT_REFRESH) OkHttpClient client, Moshi moshi) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
    }

    @Provides
    @Singleton
    static AuthRefreshApi provideAuthRefreshApi(@Named(RETROFIT_REFRESH) Retrofit retrofit) {
        return retrofit.create(AuthRefreshApi.class);
    }

    @Provides
    @Singleton
    @Named(CLIENT_MAIN)
    static OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor,
                                            TokenAuthenticator tokenAuthenticator,
                                            TriShopCookieJar cookieJar) {
        return new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(authInterceptor)
                .authenticator(tokenAuthenticator)
                .addInterceptor(loggingInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    @Named(RETROFIT_MAIN)
    static Retrofit provideRetrofit(@Named(CLIENT_MAIN) OkHttpClient client, Moshi moshi) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(
                        io.reactivex.rxjava3.schedulers.Schedulers.io()))
                .build();
    }

    @Provides
    @Singleton
    static TriShopApi provideTriShopApi(@Named(RETROFIT_MAIN) Retrofit retrofit) {
        return retrofit.create(TriShopApi.class);
    }
}
