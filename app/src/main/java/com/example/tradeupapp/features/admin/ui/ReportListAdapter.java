package com.example.tradeupapp.features.admin.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ReportModel;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ReportViewHolder> {
    public interface OnReportClickListener {
        void onReportClick(ReportModel report);
    }

    private List<ReportModel> reportList;
    private final OnReportClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public ReportListAdapter(List<ReportModel> reportList, OnReportClickListener listener) {
        this.reportList = reportList;
        this.listener = listener;
    }

    public void setReportList(List<ReportModel> reportList) {
        this.reportList = reportList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportModel report = reportList.get(position);
        // Show report ID instead of summary
        holder.tvSummary.setText(report.getId());
        holder.tvStatus.setText(report.getStatus());
        holder.tvDate.setText(dateFormat.format(report.getCreatedAt().toDate()));
        holder.itemView.setOnClickListener(v -> listener.onReportClick(report));
    }

    @Override
    public int getItemCount() {
        return reportList != null ? reportList.size() : 0;
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvSummary, tvStatus, tvDate;
        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSummary = itemView.findViewById(R.id.tv_report_summary);
            tvStatus = itemView.findViewById(R.id.tv_report_status);
            tvDate = itemView.findViewById(R.id.tv_report_date);
        }
    }
}
