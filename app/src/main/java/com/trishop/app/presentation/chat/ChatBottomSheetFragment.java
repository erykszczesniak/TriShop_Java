package com.trishop.app.presentation.chat;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.trishop.app.R;
import com.trishop.app.databinding.BottomsheetChatBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "chat";

    private BottomsheetChatBinding binding;
    private ChatViewModel viewModel;
    private ChatMessagesAdapter adapter;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomsheetChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Present at full height, like the iOS large-detent sheet.
        View sheet = getDialog() instanceof BottomSheetDialog
                ? ((BottomSheetDialog) getDialog())
                        .findViewById(com.google.android.material.R.id.design_bottom_sheet)
                : null;
        if (sheet != null) {
            sheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            sheet.requestLayout();
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        adapter = new ChatMessagesAdapter(product -> {
            dismiss();
            Bundle args = new Bundle();
            args.putString("productId", String.valueOf(product.getId()));
            NavHostFragment.findNavController(this)
                    .navigate(R.id.productDetailFragment, args);
        });

        binding.rvMessages.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);

        viewModel.ensureGreeting(getString(R.string.chat_greeting));

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnSend.setOnClickListener(v -> send());
        binding.etMessage.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send();
                return true;
            }
            return false;
        });

        viewModel.getMessages().observe(getViewLifecycleOwner(), messages ->
                adapter.submitList(messages, () -> {
                    if (!messages.isEmpty() && binding != null) {
                        binding.rvMessages.scrollToPosition(messages.size() - 1);
                    }
                }));
        viewModel.getIsStreaming().observe(getViewLifecycleOwner(), streaming ->
                binding.btnSend.setEnabled(!streaming));
    }

    private void send() {
        Editable text = binding.etMessage.getText();
        binding.etMessage.setText("");
        viewModel.send(text != null ? text.toString() : "", error -> {
            switch (error) {
                case RATE_LIMITED:
                    return getString(R.string.chat_error_rate_limit);
                case UNAVAILABLE:
                    return getString(R.string.chat_error_unavailable);
                case NETWORK:
                default:
                    return getString(R.string.chat_error_network);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.rvMessages.setAdapter(null);
        binding = null;
    }
}
