package com.example.tradeupapp.features.profile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tradeupapp.R;
import com.example.tradeupapp.features.auth.ui.AuthActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class DeleteAccountFragment extends Fragment {

    private MaterialButton btnBack;
    private TextInputEditText etReason;
    private TextInputEditText etConfirmDelete;
    private MaterialButton btnDeleteAccount;

    private static final String DELETE_CONFIRMATION = "DELETE";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delete_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        etReason = view.findViewById(R.id.et_reason);
        etConfirmDelete = view.findViewById(R.id.et_confirm_delete);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
    }

    private void setupListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        // Nút xóa tài khoản
        btnDeleteAccount.setOnClickListener(v -> {
            String confirmText = etConfirmDelete.getText().toString();
            if (!DELETE_CONFIRMATION.equals(confirmText)) {
                etConfirmDelete.setError("Vui lòng nhập chính xác từ DELETE");
                return;
            }

            // N���u nhập đúng DELETE, hiện dialog xác nhận
            showDeleteConfirmationDialog();
        });
    }

    private void showDeleteConfirmationDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete_account, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Xử lý các nút trong dialog
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirmDelete = dialogView.findViewById(R.id.btn_confirm_delete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirmDelete.setOnClickListener(v -> {
            dialog.dismiss();
            deleteAccount();
        });
    }

    private void deleteAccount() {
        String reason = etReason.getText().toString().trim();
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.auth.FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "No user signed in!", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = user.getUid();
        db.collection("users").document(uid)
            .update(
                "isDeleted", true,
                "isActive", false,
                "deletedAt", com.google.firebase.Timestamp.now()
            )
            .addOnSuccessListener(unused -> {
                Toast.makeText(requireContext(), "Tài khoản đã được đánh dấu xoá thành công", Toast.LENGTH_SHORT).show();
                // Đăng xuất và chuyển về AuthActivity
                auth.signOut();
                Intent intent = new Intent(requireContext(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Cập nhật trạng thái xoá thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
}
