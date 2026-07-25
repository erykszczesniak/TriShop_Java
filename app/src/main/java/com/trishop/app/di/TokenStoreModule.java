package com.trishop.app.di;

import com.trishop.app.core.network.EncryptedTokenStore;
import com.trishop.app.core.network.TokenStore;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class TokenStoreModule {

    @Binds
    @Singleton
    public abstract TokenStore bindTokenStore(EncryptedTokenStore impl);
}
