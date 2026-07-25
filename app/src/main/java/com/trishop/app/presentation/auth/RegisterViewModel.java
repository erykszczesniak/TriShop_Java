package com.trishop.app.presentation.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trishop.app.R;
import com.trishop.app.core.UiState;
import com.trishop.app.domain.model.User;
import com.trishop.app.domain.usecase.RegisterUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private final RegisterUseCase registerUseCase;

    private final MutableLiveData<UiState<User>> state = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> nameError = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> emailError = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> passwordError = new MutableLiveData<>(null);

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public RegisterViewModel(RegisterUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
    }

    @NonNull
    public LiveData<UiState<User>> getState() {
        return state;
    }

    @NonNull
    public LiveData<Integer> getNameError() {
        return nameError;
    }

    @NonNull
    public LiveData<Integer> getEmailError() {
        return emailError;
    }

    @NonNull
    public LiveData<Integer> getPasswordError() {
        return passwordError;
    }

    public void register(@NonNull String name, @NonNull String email, @NonNull String password) {
        if (!validate(name, email, password)) return;
        state.setValue(UiState.loading());
        disposables.add(registerUseCase.execute(email.trim(), password, name.trim())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> state.setValue(UiState.success(user)),
                        error -> {
                            String message = error.getLocalizedMessage();
                            state.setValue(UiState.error(
                                    message != null ? message : "Registration failed"));
                        }));
    }

    public void clearState() {
        state.setValue(null);
    }

    private boolean validate(String name, String email, String password) {
        boolean ok = true;
        if (name.trim().length() < 2) {
            ok = false;
            nameError.setValue(R.string.auth_error_name);
        } else {
            nameError.setValue(null);
        }
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
