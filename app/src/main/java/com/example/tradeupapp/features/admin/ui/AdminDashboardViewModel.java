package com.example.tradeupapp.features.admin.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeupapp.models.Report;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardViewModel extends ViewModel {

    private final MutableLiveData<List<Report>> reports = new MutableLiveData<>();
    private final MutableLiveData<Boolean> statusUpdateResult = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Report>> getReports() {
        return reports;
    }

    public LiveData<Boolean> getStatusUpdateResult() {
        return statusUpdateResult;
    }

    public void loadReports() {
        db.collection("reports")
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Report> reportList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Report report = document.toObject(Report.class);
                    reportList.add(report);
                }
                reports.setValue(reportList);
            })
            .addOnFailureListener(e -> {
                // Handle error
                reports.setValue(new ArrayList<>());
            });
    }

    public void updateReportStatus(String reportId, String newStatus) {
        db.collection("reports")
            .document(reportId)
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> statusUpdateResult.setValue(true))
            .addOnFailureListener(e -> statusUpdateResult.setValue(false));
    }
}
