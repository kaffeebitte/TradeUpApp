package com.example.tradeupapp.features.admin.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeupapp.databinding.FragmentAdminDashboardBinding;
import com.example.tradeupapp.shared.adapters.ReportAdapter;
import com.example.tradeupapp.models.ReportModel;

import java.util.ArrayList;

public class AdminDashboardFragment extends Fragment implements ReportAdapter.OnReportClickListener {

    private FragmentAdminDashboardBinding binding;
    private AdminDashboardViewModel viewModel;
    private ReportAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        setupRecyclerView();
        observeViewModel();

        // Load reports
        viewModel.loadReports();
    }

    private void setupRecyclerView() {
        adapter = new ReportAdapter(requireContext(), new ArrayList<>(), this);
        binding.recyclerReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerReports.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getReports().observe(getViewLifecycleOwner(), reports -> {
            adapter = new ReportAdapter(requireContext(), reports, this);
            binding.recyclerReports.setAdapter(adapter);
        });

        viewModel.getStatusUpdateResult().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Report status updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to update report status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReportClick(ReportModel report) {
        showReportDetailsDialog(report);
    }

    private void showReportDetailsDialog(ReportModel report) {
        ReportDetailsDialog dialog = ReportDetailsDialog.newInstance(report.getId());
        dialog.setStatusUpdateListener((reportId, newStatus) ->
            viewModel.updateReportStatus(reportId, newStatus));
        dialog.show(getChildFragmentManager(), "report_details");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
