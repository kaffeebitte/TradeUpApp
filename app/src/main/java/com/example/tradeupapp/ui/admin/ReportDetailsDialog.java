package com.example.tradeupapp.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tradeupapp.R;
import com.example.tradeupapp.databinding.DialogReportDetailsBinding;
import com.example.tradeupapp.models.Report;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReportDetailsDialog extends DialogFragment {

    public interface StatusUpdateListener {
        void onStatusUpdate(String reportId, String newStatus);
    }

    private DialogReportDetailsBinding binding;
    private FirebaseFirestore db;
    private String reportId;
    private StatusUpdateListener statusUpdateListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public static ReportDetailsDialog newInstance(String reportId) {
        ReportDetailsDialog dialog = new ReportDetailsDialog();
        Bundle args = new Bundle();
        args.putString("reportId", reportId);
        dialog.setArguments(args);
        return dialog;
    }

    public void setStatusUpdateListener(StatusUpdateListener listener) {
        this.statusUpdateListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);

        if (getArguments() != null) {
            reportId = getArguments().getString("reportId");
        }

        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogReportDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupStatusSpinner();
        loadReportDetails();
        setupButtons();
    }

    private void setupStatusSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.report_status_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(adapter);
    }

    private void loadReportDetails() {
        if (reportId == null) {
            dismiss();
            return;
        }

        db.collection("reports").document(reportId).get()
            .addOnSuccessListener(this::displayReportDetails)
            .addOnFailureListener(e -> dismiss());
    }

    private void displayReportDetails(DocumentSnapshot document) {
        if (!document.exists()) {
            dismiss();
            return;
        }

        Report report = document.toObject(Report.class);
        if (report == null) {
            dismiss();
            return;
        }

        binding.tvReportReason.setText(report.getReason());
        binding.tvReportDate.setText(dateFormat.format(report.getCreatedAt()));

        // Set current status in spinner
        String status = report.getStatus();
        int position = getStatusPosition(status);
        binding.spinnerStatus.setSelection(position);

        // Load reporter and reported user info
        loadUserInfo(report.getReporterId(), binding.tvReporterName);
        loadUserInfo(report.getReportedUserId(), binding.tvReportedUserName);

        // Set details if available
        if (report.getDetails() != null && !report.getDetails().isEmpty()) {
            binding.tvReportDetails.setText(report.getDetails());
        } else {
            binding.tvReportDetails.setText("No additional details provided");
        }
    }

    private int getStatusPosition(String status) {
        switch (status) {
            case "pending":
                return 0;
            case "under review":
                return 1;
            case "resolved":
                return 2;
            case "dismissed":
                return 3;
            default:
                return 0;
        }
    }

    private void loadUserInfo(String userId, android.widget.TextView textView) {
        if (userId == null) {
            textView.setText("Unknown User");
            return;
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener(userDoc -> {
                if (userDoc.exists()) {
                    String name = userDoc.getString("name");
                    textView.setText(name != null ? name : userId);
                } else {
                    textView.setText(userId);
                }
            })
            .addOnFailureListener(e -> textView.setText(userId));
    }

    private void setupButtons() {
        binding.btnUpdateStatus.setOnClickListener(v -> updateStatus());
        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    private void updateStatus() {
        String newStatus = binding.spinnerStatus.getSelectedItem().toString().toLowerCase();

        if (statusUpdateListener != null) {
            statusUpdateListener.onStatusUpdate(reportId, newStatus);
        }

        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
