package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trishop.app.BuildConfig;
import com.trishop.app.domain.model.User;

public final class UserDto {

    public long id;
    public String email;
    public String firstName;
    public String lastName;
    public String role = "USER";
    public boolean emailVerified = false;
    @Nullable
    public String avatarUrl = null;
    @Nullable
    public String createdAt = null;

    public UserDto() {
    }

    @NonNull
    public User toDomain() {
        return new User(
                String.valueOf(id),
                email,
                (firstName + " " + lastName).trim(),
                absoluteAvatarUrl(avatarUrl),
                createdAt);
    }

    // Backend may return the avatar path relative to the API host.
    @Nullable
    private static String absoluteAvatarUrl(@Nullable String url) {
        if (url == null || url.trim().isEmpty()) return null;
        if (url.startsWith("/")) {
            return trimTrailingSlash(BuildConfig.BASE_URL) + url;
        }
        return url;
    }

    static String trimTrailingSlash(String base) {
        String result = base;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
