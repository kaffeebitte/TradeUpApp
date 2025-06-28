package com.example.tradeupapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.Report;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final Context context;
    private final List<Report> reportList;
    private final OnReportClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    public ReportAdapter(Context context, List<Report> reportList, OnReportClickListener listener) {
        this.context = context;
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.tvReportReason.setText(report.getReason());
        holder.tvReportStatus.setText(report.getStatus());
        holder.tvReportDate.setText(dateFormat.format(report.getCreatedAt()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReportClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportReason, tvReportStatus, tvReportDate;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportReason = itemView.findViewById(R.id.tv_report_reason);
            tvReportStatus = itemView.findViewById(R.id.tv_report_status);
            tvReportDate = itemView.findViewById(R.id.tv_report_date);
        }
    }
}
