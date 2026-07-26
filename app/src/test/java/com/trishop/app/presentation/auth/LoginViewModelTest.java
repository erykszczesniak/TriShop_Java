package com.trishop.app.presentation.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.trishop.app.core.UiState;
import com.trishop.app.domain.model.User;
import com.trishop.app.domain.usecase.HasStoredSessionUseCase;
import com.trishop.app.domain.usecase.LoginUseCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTask = new InstantTaskExecutorRule();

    private LoginUseCase loginUseCase;
    private HasStoredSessionUseCase hasStoredSession;

    @Before
    public void setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(s -> Schedulers.trampoline());
        loginUseCase = mock(LoginUseCase.class);
        hasStoredSession = mock(HasStoredSessionUseCase.class);
        when(hasStoredSession.execute()).thenReturn(Single.just(false));
    }

    @After
    public void tearDown() {
        RxAndroidPlugins.reset();
    }

    private LoginViewModel viewModel() {
        return new LoginViewModel(loginUseCase, hasStoredSession);
    }

    @Test
    public void loginEmitsLoadingThenSuccessOnValidCredentials() {
        User user = new User("u1", "x@example.com", "X", null);
        when(loginUseCase.execute("user@example.com", "password123"))
                .thenReturn(Single.just(user));

        LoginViewModel vm = viewModel();
        vm.login("user@example.com", "password123");

        UiState<User> state = vm.getState().getValue();
        assertTrue(state instanceof UiState.Success);
        assertEquals(user, ((UiState.Success<User>) state).getData());
    }

    @Test
    public void loginRejectsInvalidEmailWithoutCallingUseCase() {
        LoginViewModel vm = viewModel();
        vm.login("notanemail", "password123");

        assertNull(vm.getState().getValue());
        assertNotNull(vm.getEmailError().getValue());
        verify(loginUseCase, never()).execute(anyString(), anyString());
    }

    @Test
    public void loginRejectsShortPasswordWithoutCallingUseCase() {
        LoginViewModel vm = viewModel();
        vm.login("user@example.com", "123");

        assertNull(vm.getState().getValue());
        assertNotNull(vm.getPasswordError().getValue());
        verify(loginUseCase, never()).execute(anyString(), anyString());
    }

    @Test
    public void loginEmitsErrorWhenUseCaseFails() {
        when(loginUseCase.execute(anyString(), anyString()))
                .thenReturn(Single.error(new RuntimeException("bad")));

        LoginViewModel vm = viewModel();
        vm.login("user@example.com", "password123");

        UiState<User> state = vm.getState().getValue();
        assertTrue(state instanceof UiState.Error);
        assertEquals("bad", ((UiState.Error<User>) state).getMessage());
    }

    @Test
    public void biometricAvailableReflectsStoredSession() {
        when(hasStoredSession.execute()).thenReturn(Single.just(true));
        LoginViewModel vm = viewModel();
        assertEquals(Boolean.TRUE, vm.getBiometricAvailable().getValue());
    }
}
