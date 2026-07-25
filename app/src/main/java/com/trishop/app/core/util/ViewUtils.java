package com.trishop.app.core.util;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

public final class ViewUtils {

    private ViewUtils() {
    }

    public static void showSnackbar(@NonNull View view, @NonNull String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void visible(@NonNull View view) {
        view.setVisibility(View.VISIBLE);
    }

    public static void gone(@NonNull View view) {
        view.setVisibility(View.GONE);
    }

    public static void invisible(@NonNull View view) {
        view.setVisibility(View.INVISIBLE);
    }
}
