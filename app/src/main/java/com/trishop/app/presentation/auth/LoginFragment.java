package com.trishop.app.presentation.auth;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.trishop.app.R;
import com.trishop.app.core.UiState;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentLoginBinding;
import com.trishop.app.domain.model.User;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentLoginBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding.btnLogin.setOnClickListener(v ->
                viewModel.login(text(binding.etEmail.getText()), text(binding.etPassword.getText())));
        binding.btnGoToRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));
        binding.btnBiometric.setOnClickListener(v -> promptBiometric());

        observeState();
    }

    private static String text(@Nullable Editable editable) {
        return editable != null ? editable.toString() : "";
    }

    private void promptBiometric() {
        if (!BiometricHelper.canAuthenticate(requireActivity())) {
            ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.auth_biometric_unavailable));
            return;
        }
        BiometricHelper.prompt(
                requireActivity(),
                getString(R.string.auth_biometric_title),
                getString(R.string.auth_biometric_subtitle),
                () -> viewModel.onBiometricUnlocked(),
                message -> ViewUtils.showSnackbar(binding.getRoot(), message));
    }

    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(
                    state instanceof UiState.Loading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!(state instanceof UiState.Loading));
            binding.tvError.setVisibility(
                    state instanceof UiState.Error ? View.VISIBLE : View.GONE);
            if (state instanceof UiState.Success) {
                viewModel.clearState();
                NavHostFragment.findNavController(this).popBackStack();
            } else if (state instanceof UiState.Error) {
                binding.tvError.setText(((UiState.Error<User>) state).getMessage());
            }
        });
        viewModel.getEmailError().observe(getViewLifecycleOwner(), id ->
                binding.tilEmail.setError(id != null ? getString(id) : null));
        viewModel.getPasswordError().observe(getViewLifecycleOwner(), id ->
                binding.tilPassword.setError(id != null ? getString(id) : null));
        viewModel.getBiometricAvailable().observe(getViewLifecycleOwner(), available ->
                binding.btnBiometric.setVisibility(
                        available && BiometricHelper.canAuthenticate(requireActivity())
                                ? View.VISIBLE : View.GONE));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
