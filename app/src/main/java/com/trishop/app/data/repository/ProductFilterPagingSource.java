package com.trishop.app.data.repository;

import android.util.Range;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.ProductFilters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class ProductFilterPagingSource extends RxPagingSource<Integer, Product> {

    private final TriShopApi api;
    private final ProductFilters filters;
    @Nullable
    private final String sort;

    public ProductFilterPagingSource(@NonNull TriShopApi api,
                                     @NonNull ProductFilters filters,
                                     @Nullable String sort) {
        this.api = api;
        this.filters = filters;
        this.sort = sort;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, Product>> loadSingle(@NonNull LoadParams<Integer> params) {
        int page = params.getKey() != null ? params.getKey() : 0;
        Range<Double> range = filters.getPriceRange();
        return api.filterProducts(
                        page,
                        params.getLoadSize(),
                        joinSorted(categoryApiValues(filters)),
                        joinSorted(new ArrayList<>(filters.getBrands())),
                        range != null ? range.getLower() : null,
                        range != null ? range.getUpper() : null,
                        sort)
                .map(response -> ProductPagingSource.toLoadResult(response, page))
                .onErrorReturn(LoadResult.Error::new);
    }

    static List<String> categoryApiValues(ProductFilters filters) {
        List<String> values = new ArrayList<>();
        for (ProductCategory category : filters.getCategories()) {
            values.add(category.getApiValue());
        }
        return values;
    }

    @Nullable
    static String joinSorted(List<String> values) {
        if (values.isEmpty()) return null;
        Collections.sort(values);
        return String.join(",", values);
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
