package com.trishop.app.data.remote.dto;

import androidx.annotation.NonNull;

public final class NewsletterRequest {

    public final String email;

    public NewsletterRequest(@NonNull String email) {
        this.email = email;
    }
}
