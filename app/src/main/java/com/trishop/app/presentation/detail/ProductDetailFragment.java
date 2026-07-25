package com.trishop.app.presentation.detail;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.Chip;
import com.trishop.app.R;
import com.trishop.app.core.UiState;
import com.trishop.app.core.util.DateFormats;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentProductDetailBinding;
import com.trishop.app.databinding.ItemReviewBinding;
import com.trishop.app.domain.model.Product;
import com.trishop.app.domain.model.ProductCategory;
import com.trishop.app.domain.model.Review;
import com.trishop.app.presentation.detail.adapter.GalleryPagerAdapter;
import com.trishop.app.presentation.home.adapter.PicksAdapter;

import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private ProductDetailViewModel viewModel;
    private PicksAdapter relatedAdapter;
    private final GalleryPagerAdapter galleryAdapter = new GalleryPagerAdapter();
    private Boolean previousInWishlist = null;

    public ProductDetailFragment() {
        super(R.layout.fragment_product_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentProductDetailBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);

        relatedAdapter = new PicksAdapter(product -> {
            Bundle args = new Bundle();
            args.putString("productId", product.getId());
            NavHostFragment.findNavController(this).navigate(R.id.productDetailFragment, args);
        });

        setupToolbar();
        setupGallery();
        binding.rvRelated.setAdapter(relatedAdapter);
        setupInteractions();
        observeState();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.toolbar.inflateMenu(R.menu.detail_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_toggle_wishlist) {
                viewModel.toggleWishlist();
                return true;
            }
            if (id == R.id.action_share) {
                shareProduct();
                return true;
            }
            return false;
        });
    }

    private void shareProduct() {
        Product product = successProduct();
        if (product == null) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                product.getName() + " — " + MoneyFormat.formatUsd(product.getPrice()));
        startActivity(Intent.createChooser(intent, getString(R.string.detail_share)));
    }

    private void setupGallery() {
        binding.viewPagerGallery.setAdapter(galleryAdapter);
        binding.viewPagerGallery.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                int total = galleryAdapter.getItemCount();
                if (total > 0) {
                    binding.tvGalleryCounter.setText(
                            getString(R.string.detail_gallery_counter, position + 1, total));
                }
            }
        });
    }

    private void setupInteractions() {
        binding.btnWriteReview.setOnClickListener(v ->
                viewModel.requestReviewComposer(() ->
                        new ReviewComposerBottomSheet().show(getChildFragmentManager(), "composer")));
        getChildFragmentManager().setFragmentResultListener(
                ReviewComposerBottomSheet.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    String comment = bundle.getString(ReviewComposerBottomSheet.ARG_COMMENT);
                    viewModel.submitReview(
                            bundle.getInt(ReviewComposerBottomSheet.ARG_RATING),
                            comment != null ? comment : "");
                });
        binding.btnAddToCart.setOnClickListener(v ->
                binding.btnAddToCart.animate()
                        .scaleX(0.96f).scaleY(0.96f).setDuration(80)
                        .withEndAction(() -> {
                            binding.btnAddToCart.animate()
                                    .scaleX(1f).scaleY(1f).setDuration(120).start();
                            viewModel.addToCart();
                        }).start());
    }

    private void observeState() {
        viewModel.getProduct().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(
                    state instanceof UiState.Loading ? View.VISIBLE : View.GONE);
            binding.contentGroup.setVisibility(
                    state instanceof UiState.Success ? View.VISIBLE : View.GONE);
            if (state instanceof UiState.Success) {
                renderProduct(((UiState.Success<Product>) state).getData());
            } else if (state instanceof UiState.Error) {
                ViewUtils.showSnackbar(binding.getRoot(), ((UiState.Error<Product>) state).getMessage());
            }
        });
        viewModel.getEvents().observe(getViewLifecycleOwner(), this::handleEvent);
        viewModel.getSelectedVariant().observe(getViewLifecycleOwner(), selected -> {
            for (int i = 0; i < binding.chipGroupVariants.getChildCount(); i++) {
                View child = binding.chipGroupVariants.getChildAt(i);
                if (!(child instanceof Chip)) continue;
                Chip chip = (Chip) child;
                chip.setChecked(chip.getText().toString().equals(selected));
            }
            Product product = successProduct();
            if (product != null) {
                renderCta(product);
            }
        });
        viewModel.getInWishlist().observe(getViewLifecycleOwner(), inList -> {
            MenuItem menuItem = binding.toolbar.getMenu().findItem(R.id.action_toggle_wishlist);
            if (menuItem == null) return;
            int iconRes = inList ? R.drawable.avd_heart_pop : R.drawable.ic_heart_outline;
            Drawable drawable = AppCompatResources.getDrawable(requireContext(), iconRes);
            menuItem.setIcon(drawable);
            if (Boolean.FALSE.equals(previousInWishlist) && inList
                    && drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
            previousInWishlist = inList;
        });
        viewModel.getReviews().observe(getViewLifecycleOwner(), this::renderReviews);
        viewModel.getRelated().observe(getViewLifecycleOwner(), products -> {
            binding.relatedHeader.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
            binding.rvRelated.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
            relatedAdapter.submitList(products);
        });
    }

    private void renderProduct(Product product) {
        binding.tvName.setText(product.getName());
        ProductCategory category = ProductCategory.fromApiValue(product.getCategory());
        binding.tvBrand.setText(category != null
                ? product.getBrand() + " · " + category.getDisplayName()
                : product.getBrand());
        // iOS nav title is a mono category breadcrumb, e.g. "SWIM / Wetsuits".
        binding.toolbar.setTitle(category != null
                ? category.getKicker() + " / " + category.getDisplayName()
                : getString(R.string.detail_title));
        binding.tvPrice.setText(MoneyFormat.formatUsd(product.getPrice()));
        Double originalPrice = product.getOriginalPrice();
        boolean hasDiscount = originalPrice != null && originalPrice > product.getPrice();
        binding.tvOriginalPrice.setVisibility(hasDiscount ? View.VISIBLE : View.GONE);
        if (hasDiscount) {
            binding.tvOriginalPrice.setText(MoneyFormat.formatUsd(originalPrice));
            binding.tvOriginalPrice.setPaintFlags(
                    binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        binding.tvRating.setVisibility(product.getReviewCount() > 0 ? View.VISIBLE : View.GONE);
        binding.tvRating.setText(getString(
                R.string.detail_rating_format, product.getRating(), product.getReviewCount()));
        binding.tvDescription.setText(product.getDescription());
        List<String> gallery = product.getImages().isEmpty()
                ? Collections.singletonList(product.getImageUrl())
                : product.getImages();
        galleryAdapter.submitList(gallery);
        binding.tvGalleryCounter.setVisibility(gallery.size() > 1 ? View.VISIBLE : View.GONE);
        binding.tvSwipeHint.setVisibility(gallery.size() > 1 ? View.VISIBLE : View.GONE);
        binding.tvGalleryCounter.setText(
                getString(R.string.detail_gallery_counter, 1, gallery.size()));
        renderVariants(product.getSizes());
        renderCta(product);
    }

    /** Sticky CTA mirrors iOS: "Add to Bag — $X" / "Select a Size" / "Out of Stock". */
    private void renderCta(Product product) {
        String variant = viewModel.getSelectedVariant().getValue();
        if (!product.isInStock()) {
            binding.btnAddToCart.setEnabled(false);
            binding.btnAddToCart.setText(R.string.detail_out_of_stock);
        } else if (!product.getSizes().isEmpty() && variant == null) {
            binding.btnAddToCart.setEnabled(false);
            binding.btnAddToCart.setText(R.string.detail_select_size);
        } else {
            binding.btnAddToCart.setEnabled(true);
            binding.btnAddToCart.setText(getString(
                    R.string.detail_add_to_cart_with_price,
                    MoneyFormat.formatUsd(product.getPrice())));
        }
    }

    private void renderReviews(List<Review> reviews) {
        binding.reviewsHeader.setText(getString(R.string.reviews_title, reviews.size()));
        binding.tvReviewsEmpty.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
        binding.llReviews.removeAllViews();
        for (int index = 0; index < reviews.size(); index++) {
            Review review = reviews.get(index);
            if (index > 0) {
                // iOS puts a 1pt separator between review rows.
                View divider = new View(requireContext());
                divider.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.neutral_200));
                binding.llReviews.addView(divider);
            }
            ItemReviewBinding item =
                    ItemReviewBinding.inflate(getLayoutInflater(), binding.llReviews, false);
            item.tvStars.setText(starRow(review.getRating()));
            item.tvAuthor.setText(review.getAuthorName());
            item.tvDate.setText(DateFormats.formatIsoDate(review.getCreatedAt()));
            item.tvComment.setText(review.getComment());
            binding.llReviews.addView(item.getRoot());
        }
    }

    private static String starRow(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    private void renderVariants(List<String> variants) {
        binding.chipGroupVariants.removeAllViews();
        binding.variantsHeader.setVisibility(variants.isEmpty() ? View.GONE : View.VISIBLE);
        binding.chipGroupVariants.setVisibility(variants.isEmpty() ? View.GONE : View.VISIBLE);
        for (String variant : variants) {
            Chip chip = (Chip) getLayoutInflater().inflate(
                    R.layout.item_size_chip, binding.chipGroupVariants, false);
            chip.setText(variant);
            chip.setChecked(variant.equals(viewModel.getSelectedVariant().getValue()));
            chip.setOnClickListener(v -> viewModel.selectVariant(variant));
            binding.chipGroupVariants.addView(chip);
        }
    }

    private void handleEvent(ProductDetailViewModel.Event event) {
        if (event instanceof ProductDetailViewModel.Event.AddedToCart) {
            ViewUtils.showSnackbar(binding.getRoot(), getString(
                    R.string.detail_added_to_cart,
                    ((ProductDetailViewModel.Event.AddedToCart) event).productName));
        } else if (event instanceof ProductDetailViewModel.Event.CannotAddWithoutVariant) {
            ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.detail_select_variant));
        } else if (event instanceof ProductDetailViewModel.Event.WishlistToggled) {
            boolean added = ((ProductDetailViewModel.Event.WishlistToggled) event).added;
            ViewUtils.showSnackbar(binding.getRoot(),
                    getString(added ? R.string.wishlist_added : R.string.wishlist_removed));
        } else if (event instanceof ProductDetailViewModel.Event.ReviewSubmitted) {
            ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.reviews_submit) + " ✓");
        } else if (event instanceof ProductDetailViewModel.Event.ReviewFailed) {
            ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.reviews_submit_error));
        } else if (event instanceof ProductDetailViewModel.Event.SignInRequiredForReview) {
            ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.reviews_sign_in_prompt));
        }
    }

    @Nullable
    private Product successProduct() {
        UiState<Product> state = viewModel.getProduct().getValue();
        if (state instanceof UiState.Success) {
            return ((UiState.Success<Product>) state).getData();
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.viewPagerGallery.setAdapter(null);
        binding = null;
    }
}
