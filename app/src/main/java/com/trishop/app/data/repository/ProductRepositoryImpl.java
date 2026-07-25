package com.trishop.app.data.repository;

import android.util.Range;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.trishop.app.core.network.EmptyBodyException;
import com.trishop.app.data.remote.api.TriShopApi;
import com.trishop.app.data.remote.dto.CreateReviewRequest;
import com.trishop.app.data.remote.dto.PagedResponse;
import com.trishop.app.data.remote.dto.ProductDto;
import com.trishop.app.data.remote.dto.ReviewDto;
import com.trishop.app.domain.model.Category;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.ProductCategoryCatalog;
import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.domain.model.ProductPage;
import com.trishop.app.domain.model.ProductSort;
import com.trishop.app.domain.model.Review;
import com.trishop.app.domain.repository.ProductRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.HttpException;
import retrofit2.Response;

@Singleton
public class ProductRepositoryImpl implements ProductRepository {

    private static final int PAGE_SIZE = 20;

    private final TriShopApi api;

    @Inject
    public ProductRepositoryImpl(TriShopApi api) {
        this.api = api;
    }

    @NonNull
    @Override
    public Flowable<PagingData<Product>> pagedProducts(@Nullable String category,
                                                       @Nullable ProductSort sort) {
        String sortValue = sort != null ? sort.getApiValue() : null;
        Pager<Integer, Product> pager = new Pager<>(
                pagingConfig(),
                () -> new ProductPagingSource(api, category, sortValue));
        return PagingRx.getFlowable(pager);
    }

    @NonNull
    @Override
    public Flowable<PagingData<Product>> pagedFilteredProducts(@NonNull ProductFilters filters,
                                                               @Nullable ProductSort sort) {
        String sortValue = sort != null ? sort.getApiValue() : null;
        Pager<Integer, Product> pager = new Pager<>(
                pagingConfig(),
                () -> new ProductFilterPagingSource(api, filters, sortValue));
        return PagingRx.getFlowable(pager);
    }

    private static PagingConfig pagingConfig() {
        return new PagingConfig(PAGE_SIZE, PAGE_SIZE, false, PAGE_SIZE);
    }

    @NonNull
    @Override
    public Single<ProductPage> getProductsPage(int page,
                                               int size,
                                               @Nullable ProductCategory category,
                                               @Nullable ProductSort sort) {
        String sortValue = sort != null ? sort.getApiValue() : null;
        Single<Response<PagedResponse<ProductDto>>> call = category != null
                ? api.getProductsByCategory(category.getApiValue(), page, size, sortValue)
                : api.getProducts(page, size, sortValue);
        return call.map(ProductRepositoryImpl::toProductPage);
    }

    @NonNull
    @Override
    public Single<ProductPage> getFilteredProductsPage(@NonNull ProductFilters filters,
                                                       int page,
                                                       int size,
                                                       @Nullable ProductSort sort) {
        Range<Double> range = filters.getPriceRange();
        return api.filterProducts(
                        page,
                        size,
                        ProductFilterPagingSource.joinSorted(
                                ProductFilterPagingSource.categoryApiValues(filters)),
                        ProductFilterPagingSource.joinSorted(new ArrayList<>(filters.getBrands())),
                        range != null ? range.getLower() : null,
                        range != null ? range.getUpper() : null,
                        sort != null ? sort.getApiValue() : null)
                .map(ProductRepositoryImpl::toProductPage);
    }

    private static ProductPage toProductPage(Response<PagedResponse<ProductDto>> response) {
        if (!response.isSuccessful()) throw new HttpException(response);
        PagedResponse<ProductDto> body = response.body();
        if (body == null) throw new EmptyBodyException();
        List<Product> products = new ArrayList<>();
        for (ProductDto dto : body.content) {
            products.add(dto.toDomain());
        }
        return new ProductPage(products, (int) body.page.totalElements);
    }

    @NonNull
    @Override
    public Single<Product> getProduct(@NonNull String id) {
        return api.getProduct(id).map(response -> {
            if (!response.isSuccessful()) throw new HttpException(response);
            ProductDto body = response.body();
            if (body == null) throw new EmptyBodyException();
            return body.toDomain();
        });
    }

    @NonNull
    @Override
    public Single<List<Review>> getReviews(@NonNull String productId) {
        return api.getReviews(productId).map(response -> {
            if (!response.isSuccessful()) throw new HttpException(response);
            List<ReviewDto> body = response.body();
            if (body == null) throw new EmptyBodyException();
            List<Review> reviews = new ArrayList<>();
            for (ReviewDto dto : body) {
                reviews.add(dto.toDomain());
            }
            return reviews;
        });
    }

    @NonNull
    @Override
    public Single<Review> addReview(@NonNull String productId, int rating, @NonNull String comment) {
        return api.addReview(productId, new CreateReviewRequest(rating, comment))
                .map(response -> {
                    if (!response.isSuccessful()) throw new HttpException(response);
                    ReviewDto body = response.body();
                    if (body == null) throw new EmptyBodyException();
                    return body.toDomain();
                });
    }

    // Backend does not expose /categories; the catalog is hard-coded client-side.
    @NonNull
    @Override
    public Single<List<Category>> getCategories() {
        return Single.just(ProductCategoryCatalog.ALL);
    }

    @NonNull
    @Override
    public Single<List<String>> getBrands(@NonNull Collection<ProductCategory> categories) {
        List<String> values = new ArrayList<>();
        for (ProductCategory category : categories) {
            values.add(category.getApiValue());
        }
        return api.getBrands(ProductFilterPagingSource.joinSorted(values)).map(response -> {
            if (!response.isSuccessful()) throw new HttpException(response);
            List<String> body = response.body();
            if (body == null) throw new EmptyBodyException();
            return body;
        });
    }
}
