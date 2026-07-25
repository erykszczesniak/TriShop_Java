package com.trishop.app.data.remote.api;

import com.trishop.app.data.remote.dto.AuthResponse;

import retrofit2.Call;
import retrofit2.http.POST;

public interface AuthRefreshApi {

    @POST("api/v1/auth/refresh")
    Call<AuthResponse> refresh();
}
