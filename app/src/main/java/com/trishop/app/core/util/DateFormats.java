package com.trishop.app.core.util;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateFormats {

    private static final String[] ISO_PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
    };

    private DateFormats() {
    }

    /** Renders backend ISO timestamps as a medium date (e.g. "Jul 21, 2026"). */
    @NonNull
    public static String formatIsoDate(@NonNull String raw) {
        for (String pattern : ISO_PATTERNS) {
            try {
                Date parsed = new SimpleDateFormat(pattern, Locale.US).parse(raw);
                if (parsed != null) {
                    return DateFormat.getDateInstance(DateFormat.MEDIUM).format(parsed);
                }
            } catch (ParseException ignored) {
                // Fall through to the next pattern.
            }
        }
        return raw;
    }
}
