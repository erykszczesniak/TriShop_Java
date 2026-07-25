package com.trishop.app.presentation.auth;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

public final class BiometricHelper {

    private static final int SUPPORTED_AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_WEAK
                    | BiometricManager.Authenticators.DEVICE_CREDENTIAL;

    private BiometricHelper() {
    }

    public static boolean canAuthenticate(@NonNull FragmentActivity activity) {
        BiometricManager manager = BiometricManager.from(activity);
        return manager.canAuthenticate(SUPPORTED_AUTHENTICATORS)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void prompt(@NonNull FragmentActivity activity,
                              @NonNull String title,
                              @NonNull String subtitle,
                              @NonNull Runnable onSuccess,
                              @NonNull Consumer<String> onError) {
        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt prompt = new BiometricPrompt(
                activity,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        onSuccess.run();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        onError.accept(errString.toString());
                    }
                });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(SUPPORTED_AUTHENTICATORS)
                .build();
        prompt.authenticate(info);
    }
}
