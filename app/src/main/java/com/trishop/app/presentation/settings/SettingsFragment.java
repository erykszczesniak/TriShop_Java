package com.trishop.app.presentation.settings;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.trishop.app.BuildConfig;
import com.trishop.app.R;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.data.local.prefs.Appearance;
import com.trishop.app.databinding.FragmentSettingsBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentSettingsBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.tvVersion.setText(getString(R.string.settings_version, BuildConfig.VERSION_NAME));

        binding.toggleAppearance.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            Appearance value;
            if (checkedId == R.id.btnLight) {
                value = Appearance.LIGHT;
            } else if (checkedId == R.id.btnDark) {
                value = Appearance.DARK;
            } else {
                value = Appearance.SYSTEM;
            }
            if (value != viewModel.getAppearance().getValue()) {
                viewModel.setAppearance(value);
            }
        });
        binding.switchNotifications.setOnCheckedChangeListener((button, checked) -> {
            if (!Boolean.valueOf(checked).equals(viewModel.getNotificationsEnabled().getValue())) {
                viewModel.setNotifications(checked);
            }
        });
        binding.btnClearRecent.setOnClickListener(v -> {
            viewModel.clearRecent();
            ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.settings_recent_cleared));
        });

        observeState();
    }

    private void observeState() {
        viewModel.getAppearance().observe(getViewLifecycleOwner(), appearance -> {
            int id;
            switch (appearance) {
                case LIGHT:
                    id = R.id.btnLight;
                    break;
                case DARK:
                    id = R.id.btnDark;
                    break;
                case SYSTEM:
                default:
                    id = R.id.btnSystem;
                    break;
            }
            if (binding.toggleAppearance.getCheckedButtonId() != id) {
                binding.toggleAppearance.check(id);
            }
        });
        viewModel.getNotificationsEnabled().observe(getViewLifecycleOwner(), enabled -> {
            if (binding.switchNotifications.isChecked() != enabled) {
                binding.switchNotifications.setChecked(enabled);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
