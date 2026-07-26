package com.trishop.app.presentation.catalog;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trishop.app.R;
import com.trishop.app.core.UiState;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentCatalogBinding;
import com.trishop.app.domain.model.Category;
import com.trishop.app.presentation.catalog.adapter.CategoryChipItem;
import com.trishop.app.presentation.catalog.adapter.CategoryChipsAdapter;
import com.trishop.app.presentation.catalog.adapter.ProductLoadStateAdapter;
import com.trishop.app.presentation.catalog.adapter.ProductPagingAdapter;
import com.trishop.app.presentation.compare.CompareUi;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CatalogFragment extends Fragment {

    private FragmentCatalogBinding binding;
    private CatalogViewModel viewModel;
    private ProductPagingAdapter productAdapter;
    private CategoryChipsAdapter categoryAdapter;

    public CatalogFragment() {
        super(R.layout.fragment_catalog);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentCatalogBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(CatalogViewModel.class);

        productAdapter = new ProductPagingAdapter(
                product -> NavHostFragment.findNavController(this).navigate(
                        CatalogFragmentDirections.actionCatalogToDetail(product.getId())),
                product -> CompareUi.showCompareFeedback(
                        binding.getRoot(), viewModel.toggleCompare(product)));
        categoryAdapter = new CategoryChipsAdapter(this::onCategorySelected);

        setupMenu();
        setupCategories();
        setupProductsGrid();
        observeState();
    }

    private void setupMenu() {
        binding.toolbar.setTitle(R.string.catalog_title);
        binding.toolbar.inflateMenu(R.menu.catalog_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_toggle_layout) {
                viewModel.toggleLayoutMode();
                return true;
            }
            if (id == R.id.action_open_search) {
                NavHostFragment.findNavController(this).navigate(R.id.action_catalog_to_search);
                return true;
            }
            if (id == R.id.action_compare) {
                NavHostFragment.findNavController(this).navigate(R.id.action_catalog_to_compare);
                return true;
            }
            if (id == R.id.action_sign_in) {
                NavHostFragment.findNavController(this).navigate(R.id.action_catalog_to_login);
                return true;
            }
            return false;
        });
    }

    private void setupCategories() {
        binding.rvCategories.setAdapter(categoryAdapter);
        binding.rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupProductsGrid() {
        final int spanCount = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        ConcatAdapter withLoadState = productAdapter.withLoadStateFooter(
                new ProductLoadStateAdapter(() -> productAdapter.retry()));
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                boolean isFooter = position == productAdapter.getItemCount()
                        && withLoadState.getItemCount() > productAdapter.getItemCount();
                boolean isListMode = viewModel.currentLayoutMode() == LayoutMode.LIST;
                return isFooter || isListMode ? spanCount : 1;
            }
        });
        binding.rvProducts.setLayoutManager(layoutManager);
        binding.rvProducts.setAdapter(withLoadState);
    }

    private void observeState() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof UiState.Success) {
                List<Category> data = ((UiState.Success<List<Category>>) state).getData();
                List<CategoryChipItem> items = new ArrayList<>();
                items.add(new CategoryChipItem(null, getString(R.string.catalog_all), null));
                for (Category category : data) {
                    items.add(new CategoryChipItem(category.getId(), category.getName(), category));
                }
                categoryAdapter.submitList(items);
            } else if (state instanceof UiState.Error) {
                ViewUtils.showSnackbar(binding.getRoot(),
                        ((UiState.Error<List<Category>>) state).getMessage());
            }
        });

        LiveDataReactiveStreams.fromPublisher(viewModel.getProducts())
                .observe(getViewLifecycleOwner(), data ->
                        productAdapter.submitData(getViewLifecycleOwner().getLifecycle(), data));

        viewModel.getLayoutMode().observe(getViewLifecycleOwner(), mode -> {
            productAdapter.setLayoutMode(mode);
            int icon = mode == LayoutMode.GRID ? R.drawable.ic_list_view : R.drawable.ic_grid_view;
            MenuItem item = binding.toolbar.getMenu().findItem(R.id.action_toggle_layout);
            if (item != null) {
                item.setIcon(icon);
            }
        });

        viewModel.getSelectedCategory().observe(getViewLifecycleOwner(), id -> {
            String selected = id.isEmpty() ? null : id;
            categoryAdapter.setSelectedId(selected);
            // The compare toggle only appears once the shop is scoped to a category.
            productAdapter.setShowCompare(selected != null);
        });

        productAdapter.addLoadStateListener(loadStates -> {
            if (binding == null) return kotlin.Unit.INSTANCE;
            LoadState refresh = loadStates.getRefresh();
            boolean isEmpty = productAdapter.getItemCount() == 0;
            boolean showShimmer = refresh instanceof LoadState.Loading && isEmpty;
            binding.shimmer.setVisibility(showShimmer ? View.VISIBLE : View.GONE);
            binding.rvProducts.setVisibility(showShimmer ? View.GONE : View.VISIBLE);
            binding.emptyState.setVisibility(
                    refresh instanceof LoadState.NotLoading && isEmpty
                            ? View.VISIBLE : View.GONE);
            if (showShimmer) {
                binding.shimmer.startShimmer();
            } else {
                binding.shimmer.stopShimmer();
            }
            if (refresh instanceof LoadState.Error) {
                String message = ((LoadState.Error) refresh).getError().getLocalizedMessage();
                ViewUtils.showSnackbar(binding.getRoot(),
                        message != null ? message : getString(R.string.common_error_generic));
            }
            return kotlin.Unit.INSTANCE;
        });

        viewModel.getCompareSelectedIds().observe(getViewLifecycleOwner(),
                productAdapter::setCompareSelectedIds);
    }

    private void onCategorySelected(@Nullable Category category) {
        viewModel.selectCategory(category != null ? category.getId() : null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.rvProducts.setAdapter(null);
        binding.rvCategories.setAdapter(null);
        binding = null;
    }
}
