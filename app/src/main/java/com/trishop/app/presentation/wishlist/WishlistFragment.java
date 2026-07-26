package com.trishop.app.presentation.wishlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.trishop.app.R;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentWishlistBinding;
import com.trishop.app.presentation.wishlist.adapter.WishlistAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WishlistFragment extends Fragment {

    private FragmentWishlistBinding binding;
    private WishlistViewModel viewModel;
    private WishlistAdapter adapter;

    public WishlistFragment() {
        super(R.layout.fragment_wishlist);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentWishlistBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(WishlistViewModel.class);

        adapter = new WishlistAdapter(
                item -> openDetail(item.getProductId()),
                viewModel::remove,
                item -> viewModel.addToBag(
                        item,
                        () -> {
                            ViewUtils.showSnackbar(binding.getRoot(),
                                    getString(R.string.wishlist_select_size_first));
                            openDetail(item.getProductId());
                        },
                        () -> ViewUtils.showSnackbar(binding.getRoot(),
                                getString(R.string.detail_added_to_cart, item.getName()))));

        binding.rvWishlist.setAdapter(adapter);
        binding.rvWishlist.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.toolbar.inflateMenu(R.menu.wishlist_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_share_wishlist) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, viewModel.shareText());
                startActivity(Intent.createChooser(intent, getString(R.string.wishlist_share)));
                return true;
            }
            return false;
        });

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            binding.emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            binding.rvWishlist.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void openDetail(String productId) {
        Bundle args = new Bundle();
        args.putString("productId", productId);
        NavHostFragment.findNavController(this).navigate(R.id.action_wishlist_to_detail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.rvWishlist.setAdapter(null);
        binding = null;
    }
}
