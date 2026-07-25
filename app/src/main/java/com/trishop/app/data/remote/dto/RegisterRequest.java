package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;

public final class RegisterRequest {

    public final String email;
    public final String password;
    public final String firstName;
    public final String lastName;

    public RegisterRequest(@NonNull String email,
                           @NonNull String password,
                           @NonNull String firstName,
                           @NonNull String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
