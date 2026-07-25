package com.trishop.app.presentation.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trishop.app.R;
import com.trishop.app.core.UiState;
import com.trishop.app.domain.model.User;
import com.trishop.app.domain.usecase.HasStoredSessionUseCase;
import com.trishop.app.domain.usecase.LoginUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final LoginUseCase loginUseCase;
    private final HasStoredSessionUseCase hasStoredSession;

    private final MutableLiveData<UiState<User>> state = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> emailError = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> passwordError = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> biometricAvailable = new MutableLiveData<>(false);

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase, HasStoredSessionUseCase hasStoredSession) {
        this.loginUseCase = loginUseCase;
        this.hasStoredSession = hasStoredSession;
        disposables.add(hasStoredSession.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(biometricAvailable::setValue, error -> {
                }));
    }

    @NonNull
    public LiveData<UiState<User>> getState() {
        return state;
    }

    @NonNull
    public LiveData<Integer> getEmailError() {
        return emailError;
    }

    @NonNull
    public LiveData<Integer> getPasswordError() {
        return passwordError;
    }

    @NonNull
    public LiveData<Boolean> getBiometricAvailable() {
        return biometricAvailable;
    }

    public void login(@NonNull String email, @NonNull String password) {
        if (!validate(email, password)) return;
        state.setValue(UiState.loading());
        disposables.add(loginUseCase.execute(email.trim(), password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> state.setValue(UiState.success(user)),
                        error -> {
                            String message = error.getLocalizedMessage();
                            state.setValue(UiState.error(
                                    message != null ? message : "Login failed"));
                        }));
    }

    public void onBiometricUnlocked() {
        disposables.add(hasStoredSession.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stored -> {
                    if (stored) {
                        state.setValue(UiState.success(new User("self", "", "", null)));
                    }
                }, error -> {
                }));
    }

    public void clearState() {
        state.setValue(null);
    }

    private boolean validate(String email, String password) {
        boolean ok = true;
        if (!email.contains("@")) {
            ok = false;
            emailError.setValue(R.string.auth_error_email);
        } else {
            emailError.setValue(null);
        }
        if (password.length() < 8) {
            ok = false;
            passwordError.setValue(R.string.auth_error_password);
        } else {
            passwordError.setValue(null);
        }
        return ok;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
