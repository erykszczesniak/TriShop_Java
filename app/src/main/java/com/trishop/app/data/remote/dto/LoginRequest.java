package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;

public final class LoginRequest {

    public final String email;
    public final String password;

    public LoginRequest(@NonNull String email, @NonNull String password) {
        this.email = email;
        this.password = password;
    }
}
