package com.example.tradeupapp.features.admin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ReportModel;
import com.example.tradeupapp.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFragment extends Fragment {
    private RecyclerView rvReports, rvFlaggedUsers;
    private ReportListAdapter reportListAdapter;
    private UserListAdapter userListAdapter;
    private FirebaseFirestore db;
    private static final String TAG = "AdminDashboardFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        rvReports = view.findViewById(R.id.rvReports);
        rvFlaggedUsers = view.findViewById(R.id.rvFlaggedUsers);
        db = FirebaseFirestore.getInstance();
        setupRecyclerViews();
        loadReports();
        loadFlaggedUsers();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == AppCompatActivity.RESULT_OK) {
            // Reload reports after returning from details
            loadReports();
        }
    }

    private void setupRecyclerViews() {
        reportListAdapter = new ReportListAdapter(new ArrayList<>(), report -> {
            if (report == null || report.getId() == null || report.getId().isEmpty()) {
                Toast.makeText(getContext(), "Report ID is missing!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), ReportDetailsActivity.class);
            intent.putExtra(ReportDetailsActivity.EXTRA_REPORT_ID, report.getId());
            startActivityForResult(intent, 1001);
        });
        rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReports.setAdapter(reportListAdapter);

        userListAdapter = new UserListAdapter(new ArrayList<>(), user -> {
            // Optionally show user details or actions
            Toast.makeText(getContext(), "User: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
        });
        rvFlaggedUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFlaggedUsers.setAdapter(userListAdapter);
    }

    private void loadReports() {
        db.collection("reports")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<ReportModel> reports = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        ReportModel report = doc.toObject(ReportModel.class);
                        report.setId(doc.getId());
                        reports.add(report);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing report: " + doc.getId(), e);
                    }
                }
                Log.d(TAG, "Loaded reports: " + reports.size());
                reportListAdapter.setReportList(reports);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load reports", e);
                Toast.makeText(getContext(), "Failed to load reports", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadFlaggedUsers() {
        db.collection("users")
            .whereIn("status", java.util.Arrays.asList("flagged", "suspended"))
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<User> flaggedUsers = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        User user = doc.toObject(User.class);
                        user.setUid(doc.getId());
                        flaggedUsers.add(user);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user: " + e.getMessage());
                    }
                }
                userListAdapter.setUserList(flaggedUsers);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to load flagged users: " + e.getMessage()));
    }
}
