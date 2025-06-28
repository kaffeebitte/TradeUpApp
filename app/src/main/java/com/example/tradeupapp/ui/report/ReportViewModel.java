package com.example.tradeupapp.ui.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeupapp.models.Report;
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

        Report report = new Report(reporterId, reportedItemId, reportedUserId, reason, null);

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
