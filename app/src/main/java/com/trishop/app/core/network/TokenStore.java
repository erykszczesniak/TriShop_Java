package com.trishop.app.core.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface TokenStore {

    @Nullable
    String getAccessToken();

    @Nullable
    String getRefreshToken();

    void save(@NonNull String access, @NonNull String refresh);

    void clear();
}
