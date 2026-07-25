package com.trishop.app.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.trishop.app.core.network.EmptyBodyException;
import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.dto.PagedResponse;
import com.trishop.app.data.remote.dto.ProductDto;
import com.trishop.app.domain.model.Product;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import retrofit2.HttpException;
import retrofit2.Response;

public class ProductPagingSource extends RxPagingSource<Integer, Product> {

    private final TriShopApi api;
    @Nullable
    private final String category;
    @Nullable
    private final String sort;

    public ProductPagingSource(@NonNull TriShopApi api,
                               @Nullable String category,
                               @Nullable String sort) {
        this.api = api;
        this.category = category;
        this.sort = sort;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, Product>> loadSingle(@NonNull LoadParams<Integer> params) {
        int page = params.getKey() != null ? params.getKey() : 0;
        Single<Response<PagedResponse<ProductDto>>> call = category != null
                ? api.getProductsByCategory(category, page, params.getLoadSize(), sort)
                : api.getProducts(page, params.getLoadSize(), sort);
        return call
                .map(response -> toLoadResult(response, page))
                .onErrorReturn(LoadResult.Error::new);
    }

    static LoadResult<Integer, Product> toLoadResult(Response<PagedResponse<ProductDto>> response,
                                                     int page) {
        if (!response.isSuccessful()) {
            return new LoadResult.Error<>(new HttpException(response));
        }
        PagedResponse<ProductDto> body = response.body();
        if (body == null) {
            return new LoadResult.Error<>(new EmptyBodyException());
        }
        List<Product> products = new ArrayList<>();
        for (ProductDto dto : body.content) {
            products.add(dto.toDomain());
        }
        return new LoadResult.Page<>(
                products,
                page == 0 ? null : page - 1,
                body.isLast() ? null : page + 1);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Product> state) {
        Integer anchor = state.getAnchorPosition();
        if (anchor == null) return null;
        LoadResult.Page<Integer, Product> closest = state.closestPageToPosition(anchor);
        if (closest == null) return null;
        if (closest.getPrevKey() != null) return closest.getPrevKey() + 1;
        if (closest.getNextKey() != null) return closest.getNextKey() - 1;
        return null;
    }
}
