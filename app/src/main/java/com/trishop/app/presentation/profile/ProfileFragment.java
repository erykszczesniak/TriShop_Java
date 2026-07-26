package com.trishop.app.presentation.profile;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.color.MaterialColors;
import com.trishop.app.R;
import com.trishop.app.core.UiState;
import com.trishop.app.core.util.DateFormats;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentProfileBinding;
import com.trishop.app.domain.model.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private static final int MAX_AVATAR_BYTES = 5 * 1024 * 1024;

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private boolean redirectedToLogin = false;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private final CompositeDisposable viewDisposables = new CompositeDisposable();

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        onAvatarSelected(uri);
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentProfileBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.ivAvatar.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
        binding.btnRemoveAvatar.setOnClickListener(v -> viewModel.deleteAvatar());
        binding.rowOrders.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_orders));
        binding.rowSettings.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_settings));
        binding.rowHelp.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_help));
        binding.btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.profile_logout_title)
                        .setMessage(R.string.profile_logout_message)
                        .setPositiveButton(R.string.profile_logout_title,
                                (dialog, which) -> viewModel.logout())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show());
        binding.btnSubscribe.setOnClickListener(v -> {
            Editable email = binding.etNewsletterEmail.getText();
            viewModel.subscribe(email != null ? email.toString() : "");
        });

        observeState();
    }

    private void observeState() {
        viewModel.getIsLoggedIn().observe(getViewLifecycleOwner(), loggedIn -> {
            if (loggedIn) {
                redirectedToLogin = false;
                viewModel.load();
            } else if (!redirectedToLogin) {
                redirectedToLogin = true;
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_login);
            } else {
                // Returned from Login still signed out — leave the tab.
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
        viewModel.getProfile().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(
                    state instanceof UiState.Loading ? View.VISIBLE : View.GONE);
            if (state instanceof UiState.Success) {
                User user = ((UiState.Success<User>) state).getData();
                binding.tvName.setText(user.getName());
                binding.tvEmail.setText(user.getEmail());
                binding.tvAvatarInitials.setText(initials(user.getName()));
                String since = user.getCreatedAt() != null
                        ? DateFormats.formatIsoDate(user.getCreatedAt())
                        : null;
                binding.tvMemberSince.setText(getString(
                        R.string.profile_member_since, since != null ? since : "—"));
                Editable newsletterEmail = binding.etNewsletterEmail.getText();
                if (newsletterEmail == null || newsletterEmail.toString().trim().isEmpty()) {
                    binding.etNewsletterEmail.setText(user.getEmail());
                }
            } else if (state instanceof UiState.Error) {
                ViewUtils.showSnackbar(binding.getRoot(), ((UiState.Error<User>) state).getMessage());
            }
        });
        viewModel.getAvatarUrl().observe(getViewLifecycleOwner(), url -> {
            binding.tvAvatarInitials.setVisibility(url == null ? View.VISIBLE : View.GONE);
            binding.btnRemoveAvatar.setVisibility(url == null ? View.GONE : View.VISIBLE);
            if (url == null) {
                binding.ivAvatar.setImageDrawable(null);
            } else {
                Glide.with(binding.getRoot())
                        .load(url)
                        .circleCrop()
                        .into(binding.ivAvatar);
            }
        });
        viewModel.getAvatarEvents().observe(getViewLifecycleOwner(), event -> {
            int message;
            switch (event) {
                case UPLOADED:
                    message = R.string.profile_avatar_updated;
                    break;
                case REMOVED:
                    message = R.string.profile_avatar_removed;
                    break;
                case TOO_LARGE:
                    message = R.string.profile_avatar_too_large;
                    break;
                case FAILED:
                default:
                    message = R.string.profile_avatar_failed;
                    break;
            }
            ViewUtils.showSnackbar(binding.getRoot(), getString(message));
        });
        viewModel.getNewsletterState().observe(getViewLifecycleOwner(), state -> {
            binding.btnSubscribe.setEnabled(state != ProfileViewModel.NewsletterState.SENDING);
            boolean showStatus = state == ProfileViewModel.NewsletterState.SUCCESS
                    || state == ProfileViewModel.NewsletterState.ALREADY_SUBSCRIBED
                    || state == ProfileViewModel.NewsletterState.FAILURE;
            binding.tvNewsletterStatus.setVisibility(showStatus ? View.VISIBLE : View.GONE);
            switch (state) {
                case SUCCESS:
                    binding.tvNewsletterStatus.setText(getString(R.string.newsletter_success));
                    binding.tvNewsletterStatus.setTextColor(MaterialColors.getColor(
                            binding.tvNewsletterStatus,
                            com.google.android.material.R.attr.colorPrimary));
                    break;
                case ALREADY_SUBSCRIBED:
                    binding.tvNewsletterStatus.setText(
                            getString(R.string.newsletter_already_subscribed));
                    binding.tvNewsletterStatus.setTextColor(MaterialColors.getColor(
                            binding.tvNewsletterStatus,
                            com.google.android.material.R.attr.colorPrimary));
                    break;
                case FAILURE:
                    binding.tvNewsletterStatus.setText(getString(R.string.newsletter_failure));
                    binding.tvNewsletterStatus.setTextColor(MaterialColors.getColor(
                            binding.tvNewsletterStatus,
                            com.google.android.material.R.attr.colorError));
                    break;
                default:
                    break;
            }
        });
    }

    private static String initials(String name) {
        StringBuilder initials = new StringBuilder();
        for (String part : name.split(" ")) {
            if (part.trim().isEmpty()) continue;
            initials.append(Character.toUpperCase(part.charAt(0)));
            if (initials.length() == 2) break;
        }
        return initials.toString();
    }

    private void onAvatarSelected(Uri uri) {
        ContentResolver resolver = requireContext().getContentResolver();
        String resolvedType = resolver.getType(uri);
        String mimeType = resolvedType != null ? resolvedType : "image/jpeg";
        viewDisposables.add(Single.fromCallable(() -> readBytes(resolver, uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> {
                    if (bytes.length == 0) {
                        ViewUtils.showSnackbar(binding.getRoot(),
                                getString(R.string.profile_avatar_failed));
                    } else if (bytes.length > MAX_AVATAR_BYTES) {
                        viewModel.onAvatarTooLarge();
                    } else {
                        viewModel.uploadAvatar(bytes, mimeType);
                    }
                }, error -> ViewUtils.showSnackbar(binding.getRoot(),
                        getString(R.string.profile_avatar_failed))));
    }

    private static byte[] readBytes(ContentResolver resolver, Uri uri) {
        try (InputStream stream = resolver.openInputStream(uri)) {
            if (stream == null) return new byte[0];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = stream.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewDisposables.clear();
        binding = null;
    }
}
