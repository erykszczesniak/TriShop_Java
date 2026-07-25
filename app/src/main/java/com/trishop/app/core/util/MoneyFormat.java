package com.trishop.app.core.util;

import androidx.annotation.NonNull;

import java.util.Locale;

public final class MoneyFormat {

    private MoneyFormat() {
    }

    // Matches the web/iOS clients: prices are always rendered as "$%.2f" in USD.
    @NonNull
    public static String formatUsd(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }
}
