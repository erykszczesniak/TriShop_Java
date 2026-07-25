package com.trishop.app.data.remote.dto;

public final class AuthResponse {

    public String accessToken;
    public long expiresIn;
    public UserDto user;

    public AuthResponse() {
    }
}
