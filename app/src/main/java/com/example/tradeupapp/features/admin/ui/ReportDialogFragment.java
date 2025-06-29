package com.example.tradeupapp.features.admin.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tradeupapp.R;
import com.example.tradeupapp.databinding.DialogReportBinding;
import com.google.android.material.radiobutton.MaterialRadioButton;

public class ReportDialogFragment extends DialogFragment {

    private DialogReportBinding binding;
    private ReportViewModel viewModel;
    private String reportedItemId;
    private String reportedUserId;
    private String reportType; // "user", "listing", "chat"

    public static ReportDialogFragment newInstance(String reportedItemId, String reportedUserId, String reportType) {
        ReportDialogFragment fragment = new ReportDialogFragment();
        Bundle args = new Bundle();
        args.putString("reportedItemId", reportedItemId);
        args.putString("reportedUserId", reportedUserId);
        args.putString("reportType", reportType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);

        if (getArguments() != null) {
            reportedItemId = getArguments().getString("reportedItemId");
            reportedUserId = getArguments().getString("reportedUserId");
            reportType = getArguments().getString("reportType");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        // Set up UI
        setupUI();

        // Observe results
        observeViewModel();
    }

    private void setupUI() {
        binding.btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void observeViewModel() {
        viewModel.getReportSubmissionResult().observe(getViewLifecycleOwner(), result -> {
            if (result) {
                Toast.makeText(requireContext(), "Report submitted successfully", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(requireContext(), "Failed to submit report", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReport() {
        String reason = getSelectedReason();
        if (reason == null) {
            Toast.makeText(requireContext(), "Please select a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.submitReport(reportedItemId, reportedUserId, reason, reportType);
    }

    private String getSelectedReason() {
        if (binding.rbScam.isChecked()) {
            return "Scam/Fraud";
        } else if (binding.rbInappropriate.isChecked()) {
            return "Inappropriate Content";
        } else if (binding.rbSpam.isChecked()) {
            return "Spam";
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
