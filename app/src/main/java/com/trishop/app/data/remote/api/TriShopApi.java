package com.trishop.app.data.remote.api;

import androidx.annotation.Nullable;

import com.trishop.app.data.remote.dto.AuthResponse;
import com.trishop.app.data.remote.dto.CartItemRequest;
import com.trishop.app.data.remote.dto.CartResponse;
import com.trishop.app.data.remote.dto.CouponValidationResponse;
import com.trishop.app.data.remote.dto.CreateOrderRequest;
import com.trishop.app.data.remote.dto.CreateReviewRequest;
import com.trishop.app.data.remote.dto.LoginRequest;
import com.trishop.app.data.remote.dto.NewsletterRequest;
import com.trishop.app.data.remote.dto.NewsletterResponse;
import com.trishop.app.data.remote.dto.OrderDto;
import com.trishop.app.data.remote.dto.PagedResponse;
import com.trishop.app.data.remote.dto.ProductDto;
import com.trishop.app.data.remote.dto.RegisterRequest;
import com.trishop.app.data.remote.dto.ReviewDto;
import com.trishop.app.data.remote.dto.UserDto;
import com.trishop.app.data.remote.dto.ValidateCouponRequest;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TriShopApi {

    @GET("api/v1/health")
    Single<Response<Void>> health();

    @GET("api/v1/products")
    Single<Response<PagedResponse<ProductDto>>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Nullable @Query("sort") String sort);

    @GET("api/v1/products/category/{category}")
    Single<Response<PagedResponse<ProductDto>>> getProductsByCategory(
            @Path("category") String category,
            @Query("page") int page,
            @Query("size") int size,
            @Nullable @Query("sort") String sort);

    @GET("api/v1/products/filter")
    Single<Response<PagedResponse<ProductDto>>> filterProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Nullable @Query("categories") String categories,
            @Nullable @Query("brands") String brands,
            @Nullable @Query("minPrice") Double minPrice,
            @Nullable @Query("maxPrice") Double maxPrice,
            @Nullable @Query("sort") String sort);

    @GET("api/v1/products/brands")
    Single<Response<List<String>>> getBrands(@Nullable @Query("categories") String categories);

    @GET("api/v1/products/{id}")
    Single<Response<ProductDto>> getProduct(@Path("id") String id);

    @GET("api/v1/products/{id}/reviews")
    Single<Response<List<ReviewDto>>> getReviews(@Path("id") String productId);

    @POST("api/v1/products/{id}/reviews")
    Single<Response<ReviewDto>> addReview(
            @Path("id") String productId,
            @Body CreateReviewRequest request);

    @GET("api/v1/products/search")
    Single<Response<PagedResponse<ProductDto>>> searchProducts(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size);

    @POST("api/v1/coupons/validate")
    Single<Response<CouponValidationResponse>> validateCoupon(@Body ValidateCouponRequest request);

    @POST("api/v1/orders")
    Single<Response<OrderDto>> createOrder(@Body CreateOrderRequest request);

    @GET("api/v1/cart")
    Single<Response<CartResponse>> getCart();

    @POST("api/v1/cart")
    Single<Response<CartResponse>> addToServerCart(@Body CartItemRequest request);

    @PUT("api/v1/cart/{productId}")
    Single<Response<CartResponse>> updateCartQuantity(
            @Path("productId") long productId,
            @Query("quantity") int quantity);

    @DELETE("api/v1/cart/{productId}")
    Single<Response<Void>> removeFromServerCart(@Path("productId") long productId);

    @DELETE("api/v1/cart")
    Single<Response<Void>> clearServerCart();

    @GET("api/v1/orders")
    Single<Response<PagedResponse<OrderDto>>> getOrders(
            @Query("page") int page,
            @Query("size") int size);

    @GET("api/v1/orders/{id}")
    Single<Response<OrderDto>> getOrder(@Path("id") String id);

    @GET("api/v1/orders/{id}/invoice")
    Single<Response<ResponseBody>> getOrderInvoice(@Path("id") String id);

    @POST("api/v1/auth/login")
    Single<Response<AuthResponse>> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Single<Response<AuthResponse>> register(@Body RegisterRequest request);

    @POST("api/v1/newsletter")
    Single<Response<NewsletterResponse>> subscribeNewsletter(@Body NewsletterRequest request);

    @GET("api/v1/users/me")
    Single<Response<UserDto>> getProfile();

    @Multipart
    @POST("api/v1/users/me/avatar")
    Single<Response<UserDto>> uploadAvatar(@Part MultipartBody.Part file);

    @DELETE("api/v1/users/me/avatar")
    Single<Response<UserDto>> deleteAvatar();

    @GET("api/v1/wishlist")
    Single<Response<List<ProductDto>>> getWishlist();

    @POST("api/v1/wishlist/{productId}")
    Single<Response<Void>> addToWishlist(@Path("productId") String productId);

    @DELETE("api/v1/wishlist/{productId}")
    Single<Response<Void>> removeFromWishlist(@Path("productId") String productId);
}
