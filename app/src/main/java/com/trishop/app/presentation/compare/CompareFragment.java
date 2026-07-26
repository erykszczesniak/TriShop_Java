package com.trishop.app.presentation.compare;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.trishop.app.R;
import com.trishop.app.databinding.FragmentCompareBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CompareFragment extends Fragment {

    private FragmentCompareBinding binding;
    private CompareViewModel viewModel;
    private CompareAdapter adapter;

    public CompareFragment() {
        super(R.layout.fragment_compare);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentCompareBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(CompareViewModel.class);
        adapter = new CompareAdapter(viewModel::remove);

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_compare) {
                viewModel.clear();
                return true;
            }
            return false;
        });
        binding.rvCompare.setAdapter(adapter);

        viewModel.getItems().observe(getViewLifecycleOwner(), products -> {
            adapter.submitList(products);
            boolean isEmpty = products.isEmpty();
            binding.tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvCompare.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            MenuItem clearItem = binding.toolbar.getMenu().findItem(R.id.action_clear_compare);
            if (clearItem != null) {
                clearItem.setVisible(!isEmpty);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.rvCompare.setAdapter(null);
        binding = null;
    }
}
