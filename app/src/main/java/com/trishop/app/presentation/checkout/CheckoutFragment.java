package com.trishop.app.presentation.checkout;

import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.trishop.app.R;
import com.trishop.app.core.UiState;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentCheckoutBinding;
import com.trishop.app.domain.model.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private CheckoutViewModel viewModel;

    public CheckoutFragment() {
        super(R.layout.fragment_checkout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentCheckoutBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.btnPlaceOrder.setOnClickListener(v -> submit());

        observeState();
    }

    private void submit() {
        List<TextInputLayout> fields = Arrays.asList(
                binding.tilFirstName, binding.tilLastName, binding.tilEmail,
                binding.tilAddress, binding.tilCity, binding.tilZip);
        for (TextInputLayout field : fields) {
            field.setError(null);
        }

        List<TextInputLayout> empty = new ArrayList<>();
        for (TextInputLayout field : fields) {
            if (isBlank(field)) {
                empty.add(field);
            }
        }
        if (!empty.isEmpty()) {
            for (TextInputLayout field : empty) {
                field.setError(" ");
            }
            ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.checkout_error_fill_all));
            return;
        }
        String email = text(binding.etEmail.getText()).trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.checkout_error_invalid_email));
            return;
        }
        viewModel.placeOrder(
                text(binding.etAddress.getText()).trim(),
                text(binding.etCity.getText()).trim(),
                text(binding.etZip.getText()).trim());
    }

    private static boolean isBlank(TextInputLayout til) {
        return til.getEditText() == null
                || til.getEditText().getText() == null
                || til.getEditText().getText().toString().trim().isEmpty();
    }

    private static String text(@Nullable Editable editable) {
        return editable != null ? editable.toString() : "";
    }

    private static void prefill(TextInputLayout til, @Nullable String value) {
        if (til.getEditText() == null || value == null || value.trim().isEmpty()) return;
        Editable current = til.getEditText().getText();
        if (current == null || current.toString().trim().isEmpty()) {
            til.getEditText().setText(value);
        }
    }

    private void observeState() {
        viewModel.getSummary().observe(getViewLifecycleOwner(), summary -> {
            binding.tvItemCount.setText(getResources().getQuantityString(
                    R.plurals.checkout_item_count, summary.itemCount, summary.itemCount));
            binding.tvSubtotal.setText(MoneyFormat.formatUsd(summary.subtotal));
            binding.rowDiscount.setVisibility(summary.discount > 0 ? View.VISIBLE : View.GONE);
            binding.tvDiscount.setText("-" + MoneyFormat.formatUsd(summary.discount));
            binding.tvShipping.setText(summary.shipping != null
                    ? MoneyFormat.formatUsd(summary.shipping)
                    : getString(R.string.checkout_shipping_free));
            binding.tvTax.setText(MoneyFormat.formatUsd(summary.tax));
            binding.tvTotal.setText(MoneyFormat.formatUsd(summary.total));
            binding.tvTotalBig.setText(MoneyFormat.formatUsd(summary.total));
        });
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            String[] parts = user.getName().trim().split("\\s+");
            prefill(binding.tilFirstName, parts.length > 0 ? parts[0] : null);
            StringBuilder lastName = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (lastName.length() > 0) {
                    lastName.append(" ");
                }
                lastName.append(parts[i]);
            }
            prefill(binding.tilLastName, lastName.toString());
            prefill(binding.tilEmail, user.getEmail());
        });
        viewModel.getOrderState().observe(getViewLifecycleOwner(), state -> {
            binding.progressOverlay.setVisibility(
                    state instanceof UiState.Loading ? View.VISIBLE : View.GONE);
            if (state instanceof UiState.Success) {
                Order order = ((UiState.Success<Order>) state).getData();
                Bundle args = new Bundle();
                args.putString("orderId", order.getId());
                args.putString("total", String.valueOf(order.getTotalPrice()));
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_checkout_to_confirmation, args);
                viewModel.consumeOrderState();
            } else if (state instanceof UiState.Error) {
                ViewUtils.showSnackbar(binding.getRoot(),
                        ((UiState.Error<Order>) state).getMessage());
                viewModel.consumeOrderState();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
