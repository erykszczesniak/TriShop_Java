package com.trishop.app.presentation.detail;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.trishop.app.R;
import com.trishop.app.databinding.BottomsheetReviewComposerBinding;

import java.util.ArrayList;
import java.util.List;

/** Star picker + comment box; hands the entry back via the Fragment Result API. */
public class ReviewComposerBottomSheet extends BottomSheetDialogFragment {

    public static final String RESULT_KEY = "review_composer_result";
    public static final String ARG_RATING = "rating";
    public static final String ARG_COMMENT = "comment";

    private BottomsheetReviewComposerBinding binding;

    // iOS pre-fills five stars.
    private int rating = 5;
    private final List<ImageView> stars = new ArrayList<>();

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomsheetReviewComposerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int gap = getResources().getDimensionPixelSize(R.dimen.spacing_s);
        int size = getResources().getDimensionPixelSize(R.dimen.spacing_xl);
        for (int index = 0; index < 5; index++) {
            ImageView star = new ImageView(requireContext());
            star.setImageResource(R.drawable.ic_star_outline);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            if (index > 0) {
                params.setMarginStart(gap);
            }
            star.setLayoutParams(params);
            star.setContentDescription(String.valueOf(index + 1));
            final int starRating = index + 1;
            star.setOnClickListener(v -> {
                rating = starRating;
                renderStars();
                updateSubmitEnabled();
            });
            stars.add(star);
            binding.llStars.addView(star);
        }
        renderStars();

        binding.etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSubmitEnabled();
            }
        });
        binding.btnSubmit.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putInt(ARG_RATING, rating);
            Editable comment = binding.etComment.getText();
            result.putString(ARG_COMMENT, comment != null ? comment.toString().trim() : "");
            getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
            dismiss();
        });
    }

    private void renderStars() {
        for (int index = 0; index < stars.size(); index++) {
            stars.get(index).setImageResource(
                    index < rating ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        }
    }

    private void updateSubmitEnabled() {
        Editable comment = binding.etComment.getText();
        binding.btnSubmit.setEnabled(
                rating > 0 && comment != null && !comment.toString().trim().isEmpty());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
