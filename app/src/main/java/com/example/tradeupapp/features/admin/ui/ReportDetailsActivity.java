package com.example.tradeupapp.features.admin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ReportModel;
import com.example.tradeupapp.models.User;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReportDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_REPORT_ID = "report_id";
    private String reportId;
    private FirebaseFirestore db;
    private ReportModel currentReport;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        Intent intent = getIntent();
        reportId = intent.getStringExtra(EXTRA_REPORT_ID);
        if (reportId == null) {
            Toast.makeText(this, "No report ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind views
        TextView tvReportId = findViewById(R.id.tv_report_id);
        TextView tvReason = findViewById(R.id.tv_report_reason);
        TextView tvDate = findViewById(R.id.tv_report_date);
        TextView tvReporter = findViewById(R.id.tv_reporter_name);
        TextView tvReportedUser = findViewById(R.id.tv_reported_user_name);
        TextView tvDetails = findViewById(R.id.tv_report_details);
        TextView tvReportType = findViewById(R.id.tv_report_type);
        TextView tvReportedItemId = findViewById(R.id.tv_reported_item_id);
        TextView tvAdminNotes = findViewById(R.id.tv_admin_notes);
        TextView tvEvidence = findViewById(R.id.tv_evidence);
        MaterialAutoCompleteTextView spinnerStatus = findViewById(R.id.spinner_status);
        Button btnUpdateStatus = findViewById(R.id.btn_update_status);
        Button btnClose = findViewById(R.id.btn_close);
        Button btnSuspend = findViewById(R.id.btn_suspend_user);
        Button btnWarn = findViewById(R.id.btn_warn_user);
        Button btnDeleteUser = findViewById(R.id.btn_delete_user);
        Button btnDeleteListing = findViewById(R.id.btn_delete_listing);
        Button btnDeleteReport = findViewById(R.id.btn_delete_report);
        Button btnSuspendListing = findViewById(R.id.btn_suspend_listing);
        Button btnWarnListing = findViewById(R.id.btn_warn_listing);

        db = FirebaseFirestore.getInstance();

        // Populate status spinner from string-array
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.report_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Set report ID
        tvReportId.setText(getString(R.string.report_id) + " " + reportId);

        // Fetch report details
        db.collection("reports").document(reportId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentReport = documentSnapshot.toObject(ReportModel.class);
                if (currentReport == null) return;
                tvReason.setText(currentReport.getReasonDisplay() != null ? currentReport.getReasonDisplay() : "");
                tvDate.setText(currentReport.getCreatedAt() != null ? dateFormat.format(currentReport.getCreatedAt().toDate()) : "");
                tvDetails.setText(currentReport.getDescription() != null ? currentReport.getDescription() : "");
                spinnerStatus.setText(currentReport.getStatus() != null ? currentReport.getStatus() : getString(R.string.select_status), false);
                if (tvReportType != null) tvReportType.setText(currentReport.getReportType() != null ? currentReport.getReportType() : "");
                if (tvReportedItemId != null) tvReportedItemId.setText(currentReport.getReportedItemId() != null ? currentReport.getReportedItemId() : "");
                if (tvAdminNotes != null) tvAdminNotes.setText(currentReport.getAdminNotes() != null ? currentReport.getAdminNotes() : "");
                if (tvEvidence != null && currentReport.getEvidence() != null && !currentReport.getEvidence().isEmpty()) {
                    StringBuilder evidenceStr = new StringBuilder();
                    for (String url : currentReport.getEvidence()) {
                        evidenceStr.append(url).append("\n");
                    }
                    tvEvidence.setText(evidenceStr.toString().trim());
                } else if (tvEvidence != null) {
                    tvEvidence.setText("");
                }
                // Fetch reporter and reported user names
                String reporterId = currentReport.getReporterId() != null ? currentReport.getReporterId() : "";
                String reportedUserId = currentReport.getReportedUserId() != null ? currentReport.getReportedUserId() : "";
                db.collection("users").document(reporterId).get().addOnSuccessListener(userDoc -> {
                    User reporter = userDoc.toObject(User.class);
                    tvReporter.setText(reporter != null && reporter.getDisplayName() != null ? reporter.getDisplayName() : reporterId);
                });
                db.collection("users").document(reportedUserId).get().addOnSuccessListener(userDoc -> {
                    User reported = userDoc.toObject(User.class);
                    tvReportedUser.setText(reported != null && reported.getDisplayName() != null ? reported.getDisplayName() : reportedUserId);
                });
                // Show/hide admin action buttons based on report type and reportedType
                String reportType = currentReport.getReportType() != null ? currentReport.getReportType() : "";
                String reportedType = reportType.toLowerCase();
                boolean isProfile = reportType.equalsIgnoreCase("profile");
                boolean isConversation = reportedType.contains("conversation");
                boolean isListing = reportedType.contains("listing") || currentReport.isListingReport();

                // User actions: only for profile/conversation
                if (btnSuspend != null) btnSuspend.setVisibility((isProfile || isConversation) ? View.VISIBLE : View.GONE);
                if (btnWarn != null) btnWarn.setVisibility((isProfile || isConversation) ? View.VISIBLE : View.GONE);
                if (btnDeleteUser != null) btnDeleteUser.setVisibility((isProfile || isConversation) ? View.VISIBLE : View.GONE);

                // Listing actions: only for listing
                if (btnDeleteListing != null) btnDeleteListing.setVisibility(isListing ? View.VISIBLE : View.GONE);
                if (btnSuspendListing != null) btnSuspendListing.setVisibility(isListing ? View.VISIBLE : View.GONE);
                if (btnWarnListing != null) btnWarnListing.setVisibility(isListing ? View.VISIBLE : View.GONE);

                // Delete Report always visible
                if (btnDeleteReport != null) btnDeleteReport.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Report not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnUpdateStatus.setOnClickListener(v -> {
            String newStatus = spinnerStatus.getText().toString();
            if (currentReport != null && newStatus != null && !newStatus.isEmpty()) {
                db.collection("reports").document(reportId)
                        .update("status", newStatus)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, getString(R.string.status) + " updated", Toast.LENGTH_SHORT).show();
                            currentReport.setStatus(newStatus);
                            spinnerStatus.setText(newStatus, false);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, getString(R.string.update_status) + " failed", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, getString(R.string.select_status), Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteReport.setOnClickListener(v -> {
            db.collection("reports").document(reportId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, getString(R.string.delete_report) + " success", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.delete_report) + " failed", Toast.LENGTH_SHORT).show();
                    });
        });

        btnClose.setOnClickListener(v -> finish());
        btnSuspend.setOnClickListener(v -> {
            if (currentReport == null) return;
            String reportedUserId = currentReport.getReportedUserId();
            if (reportedUserId == null || reportedUserId.isEmpty()) {
                Toast.makeText(this, "No reported user ID", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users").document(reportedUserId)
                .update("status", "suspended", "isActive", false, "deactivated", true, "deactivatedAt", com.google.firebase.Timestamp.now())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "User suspended", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to suspend user", Toast.LENGTH_SHORT).show());
        });

        btnWarn.setOnClickListener(v -> {
            if (currentReport == null) return;
            String reportedUserId = currentReport.getReportedUserId();
            if (reportedUserId == null || reportedUserId.isEmpty()) {
                Toast.makeText(this, "No reported user ID", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users").document(reportedUserId)
                .get().addOnSuccessListener(doc -> {
                    Long warningCount = doc.contains("warningCount") ? doc.getLong("warningCount") : 0L;
                    db.collection("users").document(reportedUserId)
                        .update("status", "flagged", "warningCount", (warningCount != null ? warningCount + 1 : 1))
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "User warned", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to warn user", Toast.LENGTH_SHORT).show());
                });
        });

        btnDeleteUser.setOnClickListener(v -> {
            if (currentReport == null) return;
            String reportedUserId = currentReport.getReportedUserId();
            if (reportedUserId == null || reportedUserId.isEmpty()) {
                Toast.makeText(this, "No reported user ID", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users").document(reportedUserId)
                .update("isDeleted", true, "deletedAt", com.google.firebase.Timestamp.now(), "isActive", false, "deactivated", true)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "User account marked as deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show());
        });

        btnDeleteListing.setOnClickListener(v -> {
            if (currentReport == null) return;
            String listingId = currentReport.getReportedItemId();
            if (listingId == null || listingId.isEmpty()) {
                Toast.makeText(this, "No listing ID", Toast.LENGTH_SHORT).show();
                return;
            }
            // Fetch the listing to check transaction status and get itemId
            db.collection("listings").document(listingId).get().addOnSuccessListener(listingDoc -> {
                if (!listingDoc.exists()) {
                    Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Defensive: handle missing isBanned field
                Boolean isBanned = listingDoc.contains("isBanned") ? listingDoc.getBoolean("isBanned") : false;
                // Now you can use isBanned safely
                String transactionStatus = listingDoc.contains("transactionStatus") ? listingDoc.getString("transactionStatus") : null;
                if (transactionStatus != null && (transactionStatus.equalsIgnoreCase("pending") || transactionStatus.equalsIgnoreCase("sold"))) {
                    // If already has transaction, just ban
                    db.collection("listings").document(listingId)
                        .update("isBanned", true, "isActive", false, "status", "suspended")
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Listing banned (cannot delete due to transaction)", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to ban listing", Toast.LENGTH_SHORT).show());
                    return;
                }
                String itemId = listingDoc.contains("itemId") ? listingDoc.getString("itemId") : null;
                // Delete listing
                db.collection("listings").document(listingId).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Delete item if exists
                        if (itemId != null && !itemId.isEmpty()) {
                            db.collection("items").document(itemId).delete()
                                .addOnSuccessListener(aVoid2 -> Toast.makeText(this, "Listing and item deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Listing deleted but failed to delete item", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Listing deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete listing", Toast.LENGTH_SHORT).show());
            });
        });
        // Suspend listing (set isActive=false, update status only)
        btnSuspend.setOnLongClickListener(v -> {
            if (currentReport == null) return false;
            String listingId = currentReport.getReportedItemId();
            if (listingId == null || listingId.isEmpty()) {
                Toast.makeText(this, "No listing ID", Toast.LENGTH_SHORT).show();
                return false;
            }
            db.collection("listings").document(listingId)
                .get().addOnSuccessListener(doc -> {
                    db.collection("listings").document(listingId)
                        .update("isActive", false, "status", "suspended")
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Listing suspended.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to suspend listing", Toast.LENGTH_SHORT).show());
                });
            return true;
        });
        // Warn listing (set status=flagged, do not update warningCount)
        btnWarn.setOnLongClickListener(v -> {
            if (currentReport == null) return false;
            final String listingId = currentReport.getReportedItemId();
            if (listingId == null || listingId.isEmpty()) {
                Toast.makeText(this, "No listing ID", Toast.LENGTH_SHORT).show();
                return false;
            }
            db.collection("listings").document(listingId)
                .get().addOnSuccessListener(doc -> {
                    db.collection("listings").document(listingId)
                        .update("status", "flagged")
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Listing warned.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to warn listing", Toast.LENGTH_SHORT).show());
                });
            return true;
        });
        // Listing suspend/warn admin actions
        if (btnSuspendListing != null) btnSuspendListing.setOnClickListener(v -> {
            if (currentReport == null) return;
            String listingId = currentReport.getReportedItemId();
            if (listingId == null || listingId.isEmpty()) {
                Toast.makeText(this, "No listing ID", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("listings").document(listingId)
                .get().addOnSuccessListener(doc -> {
                    db.collection("listings").document(listingId)
                        .update("isActive", false, "status", "suspended")
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Listing suspended", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to suspend listing", Toast.LENGTH_SHORT).show());
                });
        });
        if (btnWarnListing != null) btnWarnListing.setOnClickListener(v -> {
            if (currentReport == null) return;
            String listingId = currentReport.getReportedItemId();
            if (listingId == null || listingId.isEmpty()) {
                Toast.makeText(this, "No listing ID", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("listings").document(listingId)
                .get().addOnSuccessListener(doc -> {
                    Long warningCount = doc.contains("warningCount") ? doc.getLong("warningCount") : 0L;
                    if (warningCount == null) warningCount = 0L;
                    db.collection("listings").document(listingId)
                        .update("status", "flagged", "warningCount", warningCount + 1)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Listing warned", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to warn listing", Toast.LENGTH_SHORT).show());
                });
        });
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }
}
