package com.trishop.app.presentation.profile.orders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.trishop.app.R;
import com.trishop.app.core.UiState;
import com.trishop.app.core.util.DateFormats;
import com.trishop.app.core.util.MoneyFormat;
import com.trishop.app.core.util.ViewUtils;
import com.trishop.app.databinding.FragmentOrderDetailBinding;
import com.trishop.app.databinding.ItemOrderLineBinding;
import com.trishop.app.domain.model.Order;
import com.trishop.app.domain.model.OrderItem;
import com.trishop.app.domain.model.OrderStatusEvent;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OrderDetailFragment extends Fragment {

    private FragmentOrderDetailBinding binding;
    private OrderDetailViewModel viewModel;
    private final StatusTimelineAdapter timelineAdapter = new StatusTimelineAdapter();

    public OrderDetailFragment() {
        super(R.layout.fragment_order_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentOrderDetailBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_invoice) {
                viewModel.requestInvoice();
                return true;
            }
            return false;
        });

        binding.rvTimeline.setAdapter(timelineAdapter);

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(
                    state instanceof UiState.Loading ? View.VISIBLE : View.GONE);
            binding.content.setVisibility(
                    state instanceof UiState.Success ? View.VISIBLE : View.GONE);
            if (state instanceof UiState.Success) {
                render(((UiState.Success<Order>) state).getData());
            } else if (state instanceof UiState.Error) {
                ViewUtils.showSnackbar(binding.getRoot(),
                        ((UiState.Error<Order>) state).getMessage());
            }
        });
        viewModel.getEvents().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof OrderDetailViewModel.Event.InvoiceReady) {
                shareInvoice(((OrderDetailViewModel.Event.InvoiceReady) event).pdf);
            } else if (event instanceof OrderDetailViewModel.Event.InvoiceFailed) {
                ViewUtils.showSnackbar(binding.getRoot(), getString(R.string.orders_invoice_error));
            }
        });
    }

    private void render(Order order) {
        // iOS keeps the order id in the nav title; no separate header block.
        binding.toolbar.setTitle("#" + order.getId());
        binding.tvCreatedAt.setText(DateFormats.formatIsoDate(order.getCreatedAt()));
        // Prefer the backend-provided history when present; fall back to the
        // synthesized four-step timeline so older orders still render.
        List<StatusStep> timeline = order.getStatusHistory().isEmpty()
                ? buildTimeline(order.getStatus())
                : buildTimelineFromHistory(order);
        // iOS lays the steps out horizontally, fill-equally.
        binding.rvTimeline.setLayoutManager(new GridLayoutManager(
                requireContext(), Math.max(timeline.size(), 1)));
        timelineAdapter.submitList(timeline);
        renderItems(order);
        binding.tvShippingAddress.setText(order.getShippingAddress());
        renderPayment(order);
    }

    private void renderItems(Order order) {
        binding.llItems.removeAllViews();
        for (OrderItem item : order.getItems()) {
            ItemOrderLineBinding row = ItemOrderLineBinding.inflate(
                    getLayoutInflater(), binding.llItems, false);
            // iOS: text-only rows, "Name × N".
            row.tvName.setText(item.getProductName() + " × " + item.getQuantity());
            row.tvPrice.setText(MoneyFormat.formatUsd(item.getPrice() * item.getQuantity()));
            binding.llItems.addView(row.getRoot());
        }
    }

    /** iOS Payment card: method, subtotal, discount (if any), accent total. */
    private void renderPayment(Order order) {
        double subtotal = 0.0;
        for (OrderItem item : order.getItems()) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        binding.tvPaySubtotal.setText(MoneyFormat.formatUsd(subtotal));
        double discount = order.getDiscountPercent() != null
                ? subtotal * order.getDiscountPercent() / 100.0
                : 0.0;
        binding.rowPayDiscount.setVisibility(discount > 0 ? View.VISIBLE : View.GONE);
        binding.tvPayDiscount.setText("-" + MoneyFormat.formatUsd(discount));
        binding.tvTotal.setText(MoneyFormat.formatUsd(order.getTotalPrice()));
    }

    private static List<StatusStep> buildTimelineFromHistory(Order order) {
        List<OrderStatusEvent> events = order.getStatusHistory();
        List<StatusStep> steps = new ArrayList<>();
        for (int index = 0; index < events.size(); index++) {
            boolean last = index == events.size() - 1;
            steps.add(new StatusStep(events.get(index).getStatus(), true, last, last));
        }
        return steps;
    }

    private List<StatusStep> buildTimeline(String currentStatus) {
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.order_status_placed));
        labels.add(getString(R.string.order_status_confirmed));
        labels.add(getString(R.string.order_status_shipped));
        labels.add(getString(R.string.order_status_delivered));
        int statusIndex;
        switch (currentStatus.toLowerCase(Locale.US)) {
            case "placed":
            case "pending":
                statusIndex = 0;
                break;
            case "shipped":
                statusIndex = 2;
                break;
            case "delivered":
                statusIndex = 3;
                break;
            case "confirmed":
            default:
                statusIndex = 1;
                break;
        }
        List<StatusStep> steps = new ArrayList<>();
        for (int index = 0; index < labels.size(); index++) {
            steps.add(new StatusStep(
                    labels.get(index),
                    index <= statusIndex,
                    index == labels.size() - 1,
                    index == statusIndex));
        }
        return steps;
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
        binding.rvTimeline.setAdapter(null);
        binding = null;
    }
}
