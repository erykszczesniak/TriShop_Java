package com.trishop.app.presentation.profile.orders;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trishop.app.R;
import com.trishop.app.core.UiState;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentOrderHistoryBinding;
import com.trishop.app.domain.model.Order;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OrderHistoryFragment extends Fragment {

    private FragmentOrderHistoryBinding binding;
    private OrderHistoryViewModel viewModel;
    private OrderHistoryAdapter adapter;

    public OrderHistoryFragment() {
        super(R.layout.fragment_order_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentOrderHistoryBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);
        adapter = new OrderHistoryAdapter(order -> {
            Bundle args = new Bundle();
            args.putString("orderId", order.getId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_orders_to_order_detail, args);
        });

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.rvOrders.setAdapter(adapter);
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(
                    state instanceof UiState.Loading ? View.VISIBLE : View.GONE);
            if (state instanceof UiState.Success) {
                List<Order> orders = ((UiState.Success<List<Order>>) state).getData();
                adapter.submitList(orders);
                binding.emptyState.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (state instanceof UiState.Error) {
                ViewUtils.showSnackbar(binding.getRoot(),
                        ((UiState.Error<List<Order>>) state).getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.rvOrders.setAdapter(null);
        binding = null;
    }
}
