package com.example.tradeupapp.features.admin.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeupapp.models.ReportModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class ReportViewModel extends ViewModel {

    private final MutableLiveData<Boolean> reportSubmissionResult = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public LiveData<Boolean> getReportSubmissionResult() {
        return reportSubmissionResult;
    }

    public void submitReport(String reportedItemId, String reportedUserId, String reason, String reportType) {
        if (auth.getCurrentUser() == null) {
            reportSubmissionResult.setValue(false);
            return;
        }

        String reporterId = auth.getCurrentUser().getUid();

        ReportModel report;

        if (reportedItemId != null && !reportedItemId.isEmpty()) {
            // This is a listing report
            report = new ReportModel(reporterId, reportedUserId, reportedItemId,
                    ReportModel.Reason.fromString(reason), null);
        } else {
            // This is a user report
            report = new ReportModel(reporterId, reportedUserId,
                    ReportModel.Reason.fromString(reason), null);
        }

        db.collection("reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    // Update the report with its generated ID
                    String reportId = documentReference.getId();
                    documentReference.update("id", reportId);

                    reportSubmissionResult.setValue(true);
                })
                .addOnFailureListener(e -> {
                    reportSubmissionResult.setValue(false);
                });
    }
}
