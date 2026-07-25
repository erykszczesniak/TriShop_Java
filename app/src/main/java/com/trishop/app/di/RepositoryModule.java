package com.trishop.app.di;

import com.trishop.app.data.repository.AuthRepositoryImpl;
import com.trishop.app.data.repository.CartRepositoryImpl;
import com.trishop.app.data.repository.CouponRepositoryImpl;
import com.trishop.app.data.repository.NewsletterRepositoryImpl;
import com.trishop.app.data.repository.OrderRepositoryImpl;
import com.trishop.app.data.repository.ProductRepositoryImpl;
import com.trishop.app.data.repository.SearchRepositoryImpl;
import com.trishop.app.data.repository.WishlistRepositoryImpl;
import com.trishop.app.domain.repository.AuthRepository;
import com.trishop.app.domain.repository.CartRepository;
import com.trishop.app.domain.repository.CouponRepository;
import com.trishop.app.domain.repository.NewsletterRepository;
import com.trishop.app.domain.repository.OrderRepository;
import com.trishop.app.domain.repository.ProductRepository;
import com.trishop.app.domain.repository.SearchRepository;
import com.trishop.app.domain.repository.WishlistRepository;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    @Singleton
    public abstract ProductRepository bindProductRepository(ProductRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract SearchRepository bindSearchRepository(SearchRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract CartRepository bindCartRepository(CartRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract CouponRepository bindCouponRepository(CouponRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract NewsletterRepository bindNewsletterRepository(NewsletterRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract OrderRepository bindOrderRepository(OrderRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract AuthRepository bindAuthRepository(AuthRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract WishlistRepository bindWishlistRepository(WishlistRepositoryImpl impl);
}
