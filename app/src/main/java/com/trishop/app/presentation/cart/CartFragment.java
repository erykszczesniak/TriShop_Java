package com.trishop.app.presentation.cart;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;
import com.trishop.app.R;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.databinding.FragmentCartBinding;
import com.trishop.app.domain.model.CartItem;
import com.trishop.app.presentation.cart.adapter.CartAdapter;
import com.trishop.app.presentation.cart.adapter.SwipeToDeleteCallback;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartViewModel viewModel;
    private CartAdapter adapter;

    public CartFragment() {
        super(R.layout.fragment_cart);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentCartBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(CartViewModel.class);
        adapter = new CartAdapter(viewModel::increment, viewModel::decrement);

        setupRecycler();
        setupToolbarMenu();
        setupPromo();
        observeState();
    }

    private void setupToolbarMenu() {
        binding.toolbar.inflateMenu(R.menu.cart_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_cart) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.cart_clear_title)
                        .setMessage(R.string.cart_clear_message)
                        .setPositiveButton(R.string.filter_clear,
                                (dialog, which) -> viewModel.clear())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
            return false;
        });
    }

    private void setupPromo() {
        binding.btnPromo.setOnClickListener(v -> {
            if (viewModel.currentPromoState() instanceof CartViewModel.PromoState.Applied) {
                viewModel.removePromo();
            } else {
                Editable code = binding.etPromo.getText();
                viewModel.applyPromo(code != null ? code.toString() : "");
            }
        });
    }

    private void setupRecycler() {
        binding.rvCart.setAdapter(adapter);
        binding.rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        ItemTouchHelper helper = new ItemTouchHelper(new SwipeToDeleteCallback(position -> {
            List<CartItem> current = adapter.getCurrentList();
            if (position < 0 || position >= current.size()) return;
            viewModel.remove(current.get(position));
        }));
        helper.attachToRecyclerView(binding.rvCart);
    }

    private void observeState() {
        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            boolean isEmpty = items.isEmpty();
            binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.checkoutGroup.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            // iOS shows a mono "N ITEMS" line under the large title.
            binding.tvItemsCount.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            int count = 0;
            for (CartItem item : items) {
                count += item.getQuantity();
            }
            binding.tvItemsCount.setText(getString(R.string.cart_items_count, count));
        });
        viewModel.getSummary().observe(getViewLifecycleOwner(), summary -> {
            binding.tvSubtotal.setText(MoneyFormat.formatUsd(summary.subtotal));
            binding.rowDiscount.setVisibility(summary.discount > 0 ? View.VISIBLE : View.GONE);
            binding.tvDiscount.setText("-" + MoneyFormat.formatUsd(summary.discount));
            binding.tvTax.setText(MoneyFormat.formatUsd(summary.tax));
            binding.tvTotal.setText(MoneyFormat.formatUsd(summary.total));
        });
        viewModel.getPromoState().observe(getViewLifecycleOwner(), state -> {
            boolean applied = state instanceof CartViewModel.PromoState.Applied;
            binding.btnPromo.setText(applied
                    ? R.string.cart_summary_promo_remove
                    : R.string.cart_summary_promo_apply);
            binding.btnPromo.setEnabled(!(state instanceof CartViewModel.PromoState.Validating));
            binding.etPromo.setEnabled(!applied);
            binding.tvPromoStatus.setVisibility(
                    applied || state instanceof CartViewModel.PromoState.Error
                            ? View.VISIBLE : View.GONE);
            if (state instanceof CartViewModel.PromoState.Applied) {
                binding.tvPromoStatus.setText(getString(R.string.cart_summary_promo_applied));
                // iOS colors the applied confirmation success-green.
                binding.tvPromoStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.success));
            } else if (state instanceof CartViewModel.PromoState.Error) {
                String message = ((CartViewModel.PromoState.Error) state).message;
                Editable promoText = binding.etPromo.getText();
                if (message != null) {
                    binding.tvPromoStatus.setText(message);
                } else if (promoText == null || promoText.toString().trim().isEmpty()) {
                    binding.tvPromoStatus.setText(R.string.cart_summary_promo_empty_error);
                } else {
                    binding.tvPromoStatus.setText(R.string.cart_summary_promo_invalid);
                }
                binding.tvPromoStatus.setTextColor(MaterialColors.getColor(
                        binding.tvPromoStatus, com.google.android.material.R.attr.colorError));
            }
        });
        viewModel.getEvents().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof CartViewModel.Event.Removed) {
                Snackbar.make(
                        binding.getRoot(),
                        getString(R.string.cart_removed,
                                ((CartViewModel.Event.Removed) event).item.getName()),
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.rvCart.setAdapter(null);
        binding = null;
    }
}
