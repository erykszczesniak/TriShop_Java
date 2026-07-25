package com.trishop.app.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.paging.PagingData;

import com.trishop.app.domain.model.Category;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.domain.model.ProductPage;
import com.trishop.app.domain.model.ProductSort;
import com.trishop.app.domain.model.Review;

import java.util.Collection;
import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface ProductRepository {

    @NonNull
    Flowable<PagingData<Product>> pagedProducts(@Nullable String category, @Nullable ProductSort sort);

    @NonNull
    Flowable<PagingData<Product>> pagedFilteredProducts(@NonNull ProductFilters filters,
                                                        @Nullable ProductSort sort);

    @NonNull
    Single<ProductPage> getProductsPage(int page,
                                        int size,
                                        @Nullable ProductCategory category,
                                        @Nullable ProductSort sort);

    @NonNull
    Single<ProductPage> getFilteredProductsPage(@NonNull ProductFilters filters,
                                                int page,
                                                int size,
                                                @Nullable ProductSort sort);

    @NonNull
    Single<Product> getProduct(@NonNull String id);

    @NonNull
    Single<List<Review>> getReviews(@NonNull String productId);

    @NonNull
    Single<Review> addReview(@NonNull String productId, int rating, @NonNull String comment);

    @NonNull
    Single<List<Category>> getCategories();

    @NonNull
    Single<List<String>> getBrands(@NonNull Collection<ProductCategory> categories);
}
