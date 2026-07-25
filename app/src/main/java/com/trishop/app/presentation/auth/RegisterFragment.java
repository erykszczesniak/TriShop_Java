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
import com.trishop.app.databinding.FragmentRegisterBinding;
import com.trishop.app.domain.model.User;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private RegisterViewModel viewModel;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentRegisterBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        binding.btnRegister.setOnClickListener(v -> viewModel.register(
                text(binding.etName.getText()),
                text(binding.etEmail.getText()),
                text(binding.etPassword.getText())));
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.btnGoToLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        observeState();
    }

    private static String text(@Nullable Editable editable) {
        return editable != null ? editable.toString() : "";
    }

    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(
                    state instanceof UiState.Loading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!(state instanceof UiState.Loading));
            binding.tvError.setVisibility(
                    state instanceof UiState.Error ? View.VISIBLE : View.GONE);
            if (state instanceof UiState.Success) {
                viewModel.clearState();
                NavHostFragment.findNavController(this).popBackStack(R.id.loginFragment, true);
            } else if (state instanceof UiState.Error) {
                binding.tvError.setText(((UiState.Error<User>) state).getMessage());
            }
        });
        viewModel.getNameError().observe(getViewLifecycleOwner(), id ->
                binding.tilName.setError(id != null ? getString(id) : null));
        viewModel.getEmailError().observe(getViewLifecycleOwner(), id ->
                binding.tilEmail.setError(id != null ? getString(id) : null));
        viewModel.getPasswordError().observe(getViewLifecycleOwner(), id ->
                binding.tilPassword.setError(id != null ? getString(id) : null));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
