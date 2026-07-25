package com.trishop.app.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Lifecycle of a screen's data: loading, loaded with a value, or failed with
 * a message. Closed hierarchy — instances are only created through the
 * static factories.
 */
public abstract class UiState<T> {

    private UiState() {
    }

    @SuppressWarnings("unchecked")
    public static <T> UiState<T> loading() {
        return (UiState<T>) Loading.INSTANCE;
    }

    public static <T> UiState<T> success(@NonNull T data) {
        return new Success<>(data);
    }

    public static <T> UiState<T> error(@NonNull String message) {
        return new Error<>(message);
    }

    public static final class Loading<T> extends UiState<T> {

        private static final Loading<?> INSTANCE = new Loading<>();

        private Loading() {
        }

        @NonNull
        @Override
        public String toString() {
            return "Loading";
        }
    }

    public static final class Success<T> extends UiState<T> {

        private final T data;

        private Success(@NonNull T data) {
            this.data = data;
        }

        @NonNull
        public T getData() {
            return data;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (!(o instanceof Success)) return false;
            Success<?> other = (Success<?>) o;
            return data.equals(other.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }

        @NonNull
        @Override
        public String toString() {
            return "Success(data=" + data + ")";
        }
    }

    public static final class Error<T> extends UiState<T> {

        private final String message;

        private Error(@NonNull String message) {
            this.message = message;
        }

        @NonNull
        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (!(o instanceof Error)) return false;
            Error<?> other = (Error<?>) o;
            return message.equals(other.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }

        @NonNull
        @Override
        public String toString() {
            return "Error(message=" + message + ")";
        }
    }
}
