package com.trishop.app.presentation.confirmation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.trishop.app.R;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentOrderConfirmationBinding;
import com.trishop.app.domain.model.OrderItem;

import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OrderConfirmationFragment extends Fragment {

    private FragmentOrderConfirmationBinding binding;
    private OrderConfirmationViewModel viewModel;

    public OrderConfirmationFragment() {
        super(R.layout.fragment_order_confirmation);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentOrderConfirmationBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(OrderConfirmationViewModel.class);

        binding.tvOrderNumber.setText(
                getString(R.string.confirmation_order_number, viewModel.getOrderId()));
        String rawTotal = requireArguments().getString("total");
        binding.tvTotal.setText(formatTotal(rawTotal));
        binding.tvDate.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()));

        binding.btnContinue.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.cartFragment, false));
        binding.btnInvoice.setOnClickListener(v -> viewModel.requestInvoice());

        animateCheckmark();
        observeState();
    }

    private static String formatTotal(@Nullable String rawTotal) {
        if (rawTotal == null) return "";
        try {
            return MoneyFormat.formatUsd(Double.parseDouble(rawTotal));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private void animateCheckmark() {
        binding.ivCheck.setScaleX(0f);
        binding.ivCheck.setScaleY(0f);
        binding.ivCheck.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    private void observeState() {
        viewModel.getOrder().observe(getViewLifecycleOwner(), order -> {
            if (order != null) {
                int count = 0;
                for (OrderItem item : order.getItems()) {
                    count += item.getQuantity();
                }
                binding.tvItems.setText(String.valueOf(count));
                binding.tvTotal.setText(MoneyFormat.formatUsd(order.getTotalPrice()));
            }
        });
        viewModel.getEvents().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof OrderConfirmationViewModel.Event.InvoiceReady) {
                shareInvoice(((OrderConfirmationViewModel.Event.InvoiceReady) event).pdf);
            } else if (event instanceof OrderConfirmationViewModel.Event.InvoiceFailed) {
                ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.orders_invoice_error));
            }
        });
    }

    private void shareInvoice(byte[] pdf) {
        try {
            File dir = new File(requireContext().getCacheDir(), "invoices");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File file = new File(dir, "invoice-" + viewModel.getOrderId() + ".pdf");
            Files.write(file.toPath(), pdf);
            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(
                    intent, getString(R.string.orders_download_invoice)));
        } catch (Exception e) {
            ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.orders_invoice_error));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
