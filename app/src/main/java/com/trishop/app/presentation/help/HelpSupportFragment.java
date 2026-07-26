package com.trishop.app.presentation.help;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.trishop.app.R;
import com.trishop.app.databinding.FragmentHelpSupportBinding;
import com.trishop.app.databinding.ItemFaqBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HelpSupportFragment extends Fragment {

    private static final int[][] FAQ = {
            {R.string.help_faq_q1, R.string.help_faq_a1},
            {R.string.help_faq_q2, R.string.help_faq_a2},
            {R.string.help_faq_q3, R.string.help_faq_a3},
            {R.string.help_faq_q4, R.string.help_faq_a4},
            {R.string.help_faq_q5, R.string.help_faq_a5},
            {R.string.help_faq_q6, R.string.help_faq_a6},
            {R.string.help_faq_q7, R.string.help_faq_a7},
            {R.string.help_faq_q8, R.string.help_faq_a8}
    };

    private FragmentHelpSupportBinding binding;

    public HelpSupportFragment() {
        super(R.layout.fragment_help_support);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentHelpSupportBinding.bind(view);

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.btnContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + getString(R.string.help_contact_email)));
            try {
                startActivity(intent);
            } catch (Exception ignored) {
                // No email client installed.
            }
        });

        for (int index = 0; index < FAQ.length; index++) {
            if (index > 0) {
                // iOS puts a 1pt separator between FAQ rows.
                View divider = new View(requireContext());
                divider.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.neutral_200));
                binding.llFaq.addView(divider);
            }
            ItemFaqBinding item = ItemFaqBinding.inflate(getLayoutInflater(), binding.llFaq, false);
            item.tvQuestion.setText(FAQ[index][0]);
            item.tvAnswer.setText(FAQ[index][1]);
            item.rowQuestion.setOnClickListener(v -> {
                boolean expanding = item.tvAnswer.getVisibility() != View.VISIBLE;
                TransitionManager.beginDelayedTransition(
                        binding.llFaq, new AutoTransition().setDuration(250));
                item.tvAnswer.setVisibility(expanding ? View.VISIBLE : View.GONE);
                item.ivChevron.animate()
                        .rotation(expanding ? 180f : 0f)
                        .setDuration(250)
                        .start();
            });
            binding.llFaq.addView(item.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
