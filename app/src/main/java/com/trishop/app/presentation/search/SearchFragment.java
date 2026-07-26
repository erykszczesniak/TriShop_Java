package com.trishop.app.presentation.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trishop.app.R;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentSearchBinding;
import com.trishop.app.domain.model.ProductSort;
import com.trishop.app.domain.model.SortOption;
import com.trishop.app.presentation.compare.CompareUi;
import com.trishop.app.presentation.filter.FilterBottomSheetFragment;
import com.trishop.app.presentation.search.adapter.RecentSearchesAdapter;
import com.trishop.app.presentation.search.adapter.SearchResultsAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private SearchResultsAdapter resultsAdapter;
    private RecentSearchesAdapter recentAdapter;

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentSearchBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        resultsAdapter = new SearchResultsAdapter(
                product -> {
                    Bundle args = new Bundle();
                    args.putString("productId", product.getId());
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_search_to_detail, args);
                },
                product -> CompareUi.showCompareFeedback(
                        binding.getRoot(), viewModel.toggleCompare(product)));
        recentAdapter = new RecentSearchesAdapter(query ->
                binding.searchView.setQuery(query, true));

        setupToolbar();
        setupSearchView();
        setupRecycler();
        setupFilterResultListener();
        observeState();
    }

    private void setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.search_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_sort) {
                showSortDialog();
                return true;
            }
            if (id == R.id.action_filter) {
                ProductSort serverSort = viewModel.currentSort().getServerSort();
                FilterBottomSheetFragment.newInstance(
                                viewModel.currentFilters(),
                                serverSort != null ? serverSort : ProductSort.NEWEST)
                        .show(getChildFragmentManager(), "filters");
                return true;
            }
            if (id == R.id.action_clear_history) {
                viewModel.clearHistory();
                return true;
            }
            return false;
        });
    }

    private void showSortDialog() {
        SortOption[] options = SortOption.values();
        String[] labels = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labels[i] = getString(labelRes(options[i]));
        }
        int checked = viewModel.currentSort().ordinal();
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_sort)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    viewModel.selectSort(options[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static int labelRes(SortOption option) {
        switch (option) {
            case PRICE_ASCENDING:
                return R.string.filter_sort_price_asc;
            case PRICE_DESCENDING:
                return R.string.filter_sort_price_desc;
            case RATING_DESCENDING:
                return R.string.filter_sort_top_rated;
            case NEWEST:
                return R.string.filter_sort_newest;
            case RELEVANCE:
            default:
                return R.string.filter_sort_relevance;
        }
    }

    private void setupSearchView() {
        binding.searchView.setIconifiedByDefault(false);
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    viewModel.submitQuery(query);
                    viewModel.updateQuery(query);
                }
                binding.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.updateQuery(newText != null ? newText : "");
                return true;
            }
        });
    }

    private void setupRecycler() {
        binding.rvResults.setAdapter(resultsAdapter);
        binding.rvResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvRecent.setAdapter(recentAdapter);
        binding.rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupFilterResultListener() {
        getChildFragmentManager().setFragmentResultListener(
                FilterBottomSheetFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    FilterBottomSheetFragment.FilterResult result =
                            FilterBottomSheetFragment.parseResult(bundle);
                    viewModel.applyFilters(result.filters, SortOption.from(result.sort));
                });
    }

    private void observeState() {
        viewModel.getResults().observe(getViewLifecycleOwner(), products -> {
            resultsAdapter.submitList(products);
            boolean hasQuery = !viewModel.currentQuery().trim().isEmpty();
            binding.emptyState.setVisibility(
                    hasQuery && products.isEmpty() && !viewModel.isSearchingNow()
                            ? View.VISIBLE : View.GONE);
        });
        viewModel.getRecentSearches().observe(getViewLifecycleOwner(),
                recentAdapter::submitList);
        viewModel.getQuery().observe(getViewLifecycleOwner(), query -> {
            boolean hasQuery = !query.trim().isEmpty();
            binding.rvResults.setVisibility(hasQuery ? View.VISIBLE : View.GONE);
            binding.recentGroup.setVisibility(hasQuery ? View.GONE : View.VISIBLE);
            if (!hasQuery) {
                binding.emptyState.setVisibility(View.GONE);
            }
        });
        viewModel.getIsSearching().observe(getViewLifecycleOwner(), searching ->
                binding.progressBar.setVisibility(searching ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                ViewUtils.showSnackbar(binding.getRoot(), message);
                viewModel.consumeError();
            }
        });
        viewModel.getCompareSelectedIds().observe(getViewLifecycleOwner(),
                resultsAdapter::setCompareSelectedIds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.rvResults.setAdapter(null);
        binding.rvRecent.setAdapter(null);
        binding = null;
    }
}
