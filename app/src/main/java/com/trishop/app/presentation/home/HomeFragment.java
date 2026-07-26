package com.trishop.app.presentation.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.trishop.app.R;
import com.trishop.app.databinding.FragmentHomeBinding;
import com.trishop.app.domain.model.Category;
import com.trishop.app.domain.model.Discipline;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.ProductCategoryCatalog;
import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.presentation.catalog.adapter.CategoryChipItem;
import com.trishop.app.presentation.catalog.adapter.CategoryChipsAdapter;
import com.trishop.app.presentation.compare.CompareUi;
import com.trishop.app.presentation.filter.FilterBottomSheetFragment;
import com.trishop.app.presentation.home.adapter.ArrivalsAdapter;
import com.trishop.app.presentation.home.adapter.BannerAdapter;
import com.trishop.app.presentation.home.adapter.DisciplinesAdapter;
import com.trishop.app.presentation.home.adapter.PicksAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private BannerAdapter bannerAdapter;
    private DisciplinesAdapter disciplinesAdapter;
    private ArrivalsAdapter arrivalsAdapter;
    private PicksAdapter recentlyViewedAdapter;
    private CategoryChipsAdapter chipsAdapter;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentHomeBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        bannerAdapter = new BannerAdapter(this::openDetail);
        disciplinesAdapter = new DisciplinesAdapter(this::onDisciplineSelected);
        arrivalsAdapter = new ArrivalsAdapter(
                this::openDetail,
                product -> CompareUi.showCompareFeedback(
                        binding.getRoot(), viewModel.toggleCompare(product)));
        recentlyViewedAdapter = new PicksAdapter(this::openDetail);
        chipsAdapter = new CategoryChipsAdapter(category ->
                viewModel.selectCategory(category != null
                        ? ProductCategory.fromApiValue(category.getId())
                        : null));

        binding.viewPagerBanners.setAdapter(bannerAdapter);
        binding.rvDisciplines.setAdapter(disciplinesAdapter);
        binding.rvArrivals.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvArrivals.setAdapter(arrivalsAdapter);
        binding.rvRecentlyViewed.setAdapter(recentlyViewedAdapter);
        binding.rvCategoryChips.setAdapter(chipsAdapter);
        chipsAdapter.submitList(buildChipItems());
        chipsAdapter.setSelectedId(null);
        disciplinesAdapter.submitList(Discipline.ALL);

        binding.avatarChip.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.profileFragment));
        binding.btnFilters.setOnClickListener(v ->
                FilterBottomSheetFragment.newInstance(
                                viewModel.currentFilters(), viewModel.currentSort())
                        .show(getChildFragmentManager(), "filters"));
        getChildFragmentManager().setFragmentResultListener(
                FilterBottomSheetFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    FilterBottomSheetFragment.FilterResult result =
                            FilterBottomSheetFragment.parseResult(bundle);
                    viewModel.applyFilters(result.filters, result.sort);
                });
        binding.swipeRefresh.setOnRefreshListener(() ->
                viewModel.loadDashboard(() -> {
                    if (binding != null) {
                        binding.swipeRefresh.setRefreshing(false);
                    }
                }));

        observeState();
    }

    private List<CategoryChipItem> buildChipItems() {
        List<CategoryChipItem> items = new ArrayList<>();
        items.add(new CategoryChipItem(null, getString(R.string.catalog_all), null));
        for (Category category : ProductCategoryCatalog.ALL) {
            items.add(new CategoryChipItem(category.getId(), category.getName(), category));
        }
        return items;
    }

    private void onDisciplineSelected(Discipline discipline) {
        ProductFilters current = viewModel.currentFilters();
        viewModel.applyFilters(
                new ProductFilters(
                        new HashSet<>(discipline.getCategories()),
                        current.getPriceBuckets(),
                        current.getBrands()),
                viewModel.currentSort());
        binding.scrollView.post(() -> {
            if (binding != null) {
                binding.scrollView.smoothScrollTo(0, binding.arrivalsHeader.getTop());
            }
        });
    }

    private void openDetail(Product product) {
        Bundle args = new Bundle();
        args.putString("productId", product.getId());
        NavHostFragment.findNavController(this).navigate(R.id.action_home_to_detail, args);
    }

    private void observeState() {
        viewModel.getFeatured().observe(getViewLifecycleOwner(), bannerAdapter::submitList);
        viewModel.getArrivals().observe(getViewLifecycleOwner(), arrivalsAdapter::submitList);
        viewModel.getTotalProducts().observe(getViewLifecycleOwner(), total ->
                binding.tvProductCount.setText(getString(R.string.home_product_count, total)));
        viewModel.getRecentlyViewed().observe(getViewLifecycleOwner(), products -> {
            binding.tvRecentHeader.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
            binding.rvRecentlyViewed.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
            recentlyViewedAdapter.submitList(products);
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(
                        loading && arrivalsAdapter.getItemCount() == 0
                                ? View.VISIBLE : View.GONE));
        viewModel.getSelectedCategory().observe(getViewLifecycleOwner(), selected ->
                chipsAdapter.setSelectedId(selected != null ? selected.getApiValue() : null));
        viewModel.getFilters().observe(getViewLifecycleOwner(), filters -> {
            binding.tvFilterBadge.setVisibility(
                    filters.getActiveCount() > 0 ? View.VISIBLE : View.GONE);
            binding.tvFilterBadge.setText(String.valueOf(filters.getActiveCount()));
            // The arrivals grid becomes a category-scoped shop list via chips or disciplines.
            arrivalsAdapter.setShowCompare(!filters.getCategories().isEmpty());
        });
        viewModel.getCompareSelectedIds().observe(getViewLifecycleOwner(),
                arrivalsAdapter::setCompareSelectedIds);
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            StringBuilder initials = new StringBuilder();
            if (user != null) {
                for (String part : user.getName().split(" ")) {
                    if (part.trim().isEmpty()) continue;
                    initials.append(Character.toUpperCase(part.charAt(0)));
                    if (initials.length() == 2) break;
                }
            }
            binding.tvAvatarInitials.setText(initials.toString());
            binding.tvAvatarInitials.setVisibility(
                    initials.length() == 0 ? View.GONE : View.VISIBLE);
            binding.ivAvatarIcon.setVisibility(
                    initials.length() == 0 ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.viewPagerBanners.setAdapter(null);
        binding.rvDisciplines.setAdapter(null);
        binding.rvArrivals.setAdapter(null);
        binding.rvRecentlyViewed.setAdapter(null);
        binding.rvCategoryChips.setAdapter(null);
        binding = null;
    }
}
