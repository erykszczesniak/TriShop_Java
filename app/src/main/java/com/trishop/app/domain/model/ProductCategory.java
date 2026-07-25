package com.trishop.app.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * Fixed product taxonomy shared with the web and iOS clients.
 * {@link #getApiValue()} is the uppercase snake-case identifier the REST API uses.
 */
public enum ProductCategory {
    WETSUITS("WETSUITS", "Wetsuits", "SWIM"),
    BIKES("BIKES", "Bikes", "BIKE"),
    RUNNING_SHOES("RUNNING_SHOES", "Running Shoes", "RUN"),
    GPS_WATCHES("GPS_WATCHES", "GPS Watches", "WATCH"),
    NUTRITION("NUTRITION", "Nutrition", "FUEL"),
    GOGGLES("GOGGLES", "Goggles", "SWIM"),
    SWIM_CAPS("SWIM_CAPS", "Swim Caps", "SWIM"),
    SUNGLASSES("SUNGLASSES", "Sunglasses", "TRI"),
    HELMETS("HELMETS", "Helmets", "BIKE");

    private final String apiValue;
    private final String displayName;
    private final String kicker;

    ProductCategory(String apiValue, String displayName, String kicker) {
        this.apiValue = apiValue;
        this.displayName = displayName;
        this.kicker = kicker;
    }

    @NonNull
    public String getApiValue() {
        return apiValue;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    @NonNull
    public String getKicker() {
        return kicker;
    }

    @Nullable
    public static ProductCategory fromApiValue(@NonNull String value) {
        String normalized = value.toUpperCase(Locale.US);
        for (ProductCategory category : values()) {
            if (category.apiValue.equals(normalized)) {
                return category;
            }
        }
        return null;
    }
}
