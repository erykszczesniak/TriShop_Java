package com.trishop.app.presentation.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.badge.BadgeDrawable;
import com.trishop.app.R;
import com.trishop.app.databinding.ActivityMainBinding;
import com.trishop.app.presentation.chat.ChatBottomSheetFragment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            return;
        }
        NavController navController = navHostFragment.getNavController();

        // Bottom-nav tabs always return to the tab's root, even from a pushed
        // screen (Product Detail, Login, …). We drive navigation manually instead
        // of setupWithNavController to avoid its saveState/restoreState behavior,
        // which restored the current nested destination and made tab taps a no-op.
        binding.bottomNav.setOnItemSelectedListener(item -> {
            NavDestination current = navController.getCurrentDestination();
            if (current != null && item.getItemId() == current.getId()) {
                return true;
            }
            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                    .build();
            try {
                navController.navigate(item.getItemId(), null, options);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });

        // The assistant FAB floats above the dock on the five tabs plus the auth
        // screens, mirroring iOS where it stays visible over the login sheet.
        Set<Integer> fabDestinations = new HashSet<>(Arrays.asList(
                R.id.homeFragment, R.id.searchFragment, R.id.cartFragment,
                R.id.wishlistFragment, R.id.profileFragment,
                R.id.loginFragment, R.id.registerFragment));
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Keep the highlighted tab in sync with the current destination.
            Menu menu = binding.bottomNav.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (isInHierarchy(destination, item.getItemId())) {
                    item.setChecked(true);
                }
            }
            if (fabDestinations.contains(destination.getId())) {
                binding.fabChat.show();
            } else {
                binding.fabChat.hide();
            }
        });
        binding.fabChat.setOnClickListener(v -> {
            Fragment host = navHostFragment.getChildFragmentManager()
                    .getPrimaryNavigationFragment();
            new ChatBottomSheetFragment().show(
                    host != null ? host.getChildFragmentManager() : getSupportFragmentManager(),
                    ChatBottomSheetFragment.TAG);
        });

        observeCartBadge();
    }

    private static boolean isInHierarchy(NavDestination destination, int itemId) {
        NavDestination current = destination;
        while (current != null) {
            if (current.getId() == itemId) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void observeCartBadge() {
        viewModel.getCartItemCount().observe(this, count -> {
            BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.cartFragment);
            // iOS: accent-orange badge with white text.
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.accent));
            badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.neutral_0));
            badge.setVisible(count > 0);
            badge.setNumber(count);
        });
        viewModel.getIsOnline().observe(this, online ->
                binding.offlineBanner.setVisibility(online ? View.GONE : View.VISIBLE));
    }
}
