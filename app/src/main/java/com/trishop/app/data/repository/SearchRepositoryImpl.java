package com.trishop.app.data.repository;

import androidx.annotation.NonNull;

import com.trishop.app.core.network.EmptyBodyException;
import com.trishop.app.data.local.prefs.RecentSearchesStore;
import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.dto.PagedResponse;
import com.trishop.app.data.remote.dto.ProductDto;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.repository.SearchRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.HttpException;

@Singleton
public class SearchRepositoryImpl implements SearchRepository {

    private static final int PAGE_SIZE = 20;

    private final TriShopApi api;
    private final RecentSearchesStore recentStore;

    @Inject
    public SearchRepositoryImpl(TriShopApi api, RecentSearchesStore recentStore) {
        this.api = api;
        this.recentStore = recentStore;
    }

    @NonNull
    @Override
    public Single<List<Product>> searchProducts(@NonNull String query) {
        return api.searchProducts(query, 0, PAGE_SIZE).map(response -> {
            if (!response.isSuccessful()) throw new HttpException(response);
            PagedResponse<ProductDto> body = response.body();
            if (body == null) throw new EmptyBodyException();
            List<Product> products = new ArrayList<>();
            for (ProductDto dto : body.content) {
                products.add(dto.toDomain());
            }
            return products;
        });
    }

    @NonNull
    @Override
    public Flowable<List<String>> recentSearches() {
        return recentStore.recentSearches();
    }

    @NonNull
    @Override
    public Completable saveRecentSearch(@NonNull String query) {
        return recentStore.addRecent(query);
    }

    @NonNull
    @Override
    public Completable clearRecentSearches() {
        return recentStore.clear();
    }
}
