package com.trishop.app.presentation.filter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.trishop.app.R;
import com.trishop.app.databinding.BottomsheetFilterBinding;
import com.trishop.app.domain.model.PriceBucket;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.ProductFilters;
import com.trishop.app.domain.model.ProductSort;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Web-parity filter panel: multi-select categories, fixed price buckets,
 * backend brand list with show-more, and server sort order.
 * Shared by the home dashboard and the search screen via the Fragment Result API.
 */
@AndroidEntryPoint
public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String RESULT_KEY = "filter_result";
    public static final String ARG_CATEGORIES = "categories";
    public static final String ARG_BUCKETS = "priceBuckets";
    public static final String ARG_BRANDS = "brands";
    public static final String ARG_SORT = "sort";
    private static final int COLLAPSED_BRAND_COUNT = 6;

    /** The applied selection carried back through the Fragment Result API. */
    public static final class FilterResult {
        public final ProductFilters filters;
        public final ProductSort sort;

        FilterResult(ProductFilters filters, ProductSort sort) {
            this.filters = filters;
            this.sort = sort;
        }
    }

    private BottomsheetFilterBinding binding;
    private FilterSheetViewModel viewModel;

    private final Map<ProductCategory, MaterialCheckBox> categoryChecks =
            new EnumMap<>(ProductCategory.class);
    private final Map<Integer, MaterialCheckBox> bucketChecks = new HashMap<>();
    private final Map<String, MaterialCheckBox> brandChecks = new HashMap<>();
    private final Map<ProductSort, Chip> sortChips = new EnumMap<>(ProductSort.class);

    @NonNull
    public static FilterBottomSheetFragment newInstance(@NonNull ProductFilters filters,
                                                        @NonNull ProductSort sort) {
        FilterBottomSheetFragment fragment = new FilterBottomSheetFragment();
        fragment.setArguments(toBundle(filters, sort));
        return fragment;
    }

    private static Bundle toBundle(ProductFilters filters, ProductSort sort) {
        Bundle bundle = new Bundle();
        ArrayList<String> categories = new ArrayList<>();
        for (ProductCategory category : filters.getCategories()) {
            categories.add(category.getApiValue());
        }
        bundle.putStringArrayList(ARG_CATEGORIES, categories);
        int[] buckets = new int[filters.getPriceBuckets().size()];
        int i = 0;
        for (Integer bucket : filters.getPriceBuckets()) {
            buckets[i++] = bucket;
        }
        bundle.putIntArray(ARG_BUCKETS, buckets);
        bundle.putStringArrayList(ARG_BRANDS, new ArrayList<>(filters.getBrands()));
        bundle.putString(ARG_SORT, sort.name());
        return bundle;
    }

    /** Rebuilds the applied selection from a fragment-result bundle. */
    @NonNull
    public static FilterResult parseResult(@NonNull Bundle bundle) {
        Set<ProductCategory> categories = new HashSet<>();
        ArrayList<String> rawCategories = bundle.getStringArrayList(ARG_CATEGORIES);
        if (rawCategories != null) {
            for (String raw : rawCategories) {
                ProductCategory category = ProductCategory.fromApiValue(raw);
                if (category != null) {
                    categories.add(category);
                }
            }
        }
        Set<Integer> buckets = new HashSet<>();
        int[] rawBuckets = bundle.getIntArray(ARG_BUCKETS);
        if (rawBuckets != null) {
            for (int bucket : rawBuckets) {
                buckets.add(bucket);
            }
        }
        Set<String> brands = new HashSet<>();
        ArrayList<String> rawBrands = bundle.getStringArrayList(ARG_BRANDS);
        if (rawBrands != null) {
            brands.addAll(rawBrands);
        }
        ProductSort sort = ProductSort.NEWEST;
        String rawSort = bundle.getString(ARG_SORT);
        if (rawSort != null) {
            try {
                sort = ProductSort.valueOf(rawSort);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return new FilterResult(new ProductFilters(categories, buckets, brands), sort);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomsheetFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FilterSheetViewModel.class);

        buildSortChips();
        buildCategoryRows();
        buildBucketRows();

        binding.btnClearAll.setOnClickListener(v -> viewModel.clearAll());
        binding.btnShowMore.setOnClickListener(v -> viewModel.toggleBrandsExpanded());
        binding.btnApply.setOnClickListener(v -> {
            FilterSheetViewModel.State state = viewModel.getState().getValue();
            if (state == null) return;
            getParentFragmentManager().setFragmentResult(
                    RESULT_KEY, toBundle(state.filters, state.sort));
            dismiss();
        });

        observeState();
    }

    private void buildSortChips() {
        Map<ProductSort, Integer> labels = new EnumMap<>(ProductSort.class);
        labels.put(ProductSort.NEWEST, R.string.filter_sort_newest);
        labels.put(ProductSort.PRICE_ASCENDING, R.string.filter_sort_price_asc);
        labels.put(ProductSort.PRICE_DESCENDING, R.string.filter_sort_price_desc);
        labels.put(ProductSort.TOP_RATED, R.string.filter_sort_top_rated);
        for (ProductSort sort : ProductSort.values()) {
            Chip chip = new Chip(requireContext(), null,
                    com.google.android.material.R.attr.chipStyle);
            Integer label = labels.get(sort);
            if (label != null) {
                chip.setText(getString(label));
            }
            chip.setCheckable(true);
            chip.setOnClickListener(v -> viewModel.selectSort(sort));
            sortChips.put(sort, chip);
            binding.chipGroupSort.addView(chip);
        }
    }

    private void buildCategoryRows() {
        for (ProductCategory category : ProductCategory.values()) {
            MaterialCheckBox check = new MaterialCheckBox(requireContext());
            check.setText(category.getDisplayName());
            check.setOnClickListener(v ->
                    viewModel.toggleCategory(category, check.isChecked()));
            categoryChecks.put(category, check);
            binding.llCategories.addView(check);
        }
    }

    private void buildBucketRows() {
        for (int index = 0; index < PriceBucket.ALL.size(); index++) {
            PriceBucket bucket = PriceBucket.ALL.get(index);
            MaterialCheckBox check = new MaterialCheckBox(requireContext());
            check.setText(bucket.getDisplayName());
            final int bucketIndex = index;
            check.setOnClickListener(v ->
                    viewModel.toggleBucket(bucketIndex, check.isChecked()));
            bucketChecks.put(index, check);
            binding.llPriceBuckets.addView(check);
        }
    }

    private void renderBrands(List<String> visible, Set<String> selected) {
        binding.llBrands.removeAllViews();
        brandChecks.clear();
        for (String brand : visible) {
            MaterialCheckBox check = new MaterialCheckBox(requireContext());
            check.setText(brand);
            check.setChecked(selected.contains(brand));
            check.setOnClickListener(v -> viewModel.toggleBrand(brand, check.isChecked()));
            brandChecks.put(brand, check);
            binding.llBrands.addView(check);
        }
    }

    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            for (Map.Entry<ProductSort, Chip> entry : sortChips.entrySet()) {
                entry.getValue().setChecked(entry.getKey() == state.sort);
            }
            for (Map.Entry<ProductCategory, MaterialCheckBox> entry : categoryChecks.entrySet()) {
                entry.getValue().setChecked(state.filters.getCategories().contains(entry.getKey()));
            }
            for (Map.Entry<Integer, MaterialCheckBox> entry : bucketChecks.entrySet()) {
                entry.getValue().setChecked(state.filters.getPriceBuckets().contains(entry.getKey()));
            }

            List<String> visibleBrands = state.brandsExpanded
                    ? state.brands
                    : state.brands.subList(0, Math.min(COLLAPSED_BRAND_COUNT, state.brands.size()));
            renderBrands(visibleBrands, state.filters.getBrands());

            int hiddenCount = Math.max(0, state.brands.size() - COLLAPSED_BRAND_COUNT);
            binding.btnShowMore.setVisibility(
                    state.brands.size() > COLLAPSED_BRAND_COUNT ? View.VISIBLE : View.GONE);
            binding.btnShowMore.setText(state.brandsExpanded
                    ? getString(R.string.filter_show_less)
                    : getString(R.string.filter_show_more, hiddenCount));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
