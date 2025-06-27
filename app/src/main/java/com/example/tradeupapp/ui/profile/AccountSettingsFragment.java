package com.example.tradeupapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tradeupapp.R;
import com.example.tradeupapp.ui.auth.AuthActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AccountSettingsFragment extends Fragment {

    private MaterialButton btnDeactivate;
    private MaterialButton btnDelete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        btnDeactivate = view.findViewById(R.id.btn_deactivate);
        btnDelete = view.findViewById(R.id.btn_delete);
    }

    private void setupListeners() {
        btnDeactivate.setOnClickListener(v -> {
            // Hiển thị dialog xác nhận tạm khóa tài khoản
            showDeactivateConfirmationDialog();
        });

        btnDelete.setOnClickListener(v -> {
            // Chuyển đến màn hình xóa tài khoản
            Navigation.findNavController(requireView()).navigate(R.id.action_accountSettingsFragment_to_deleteAccountFragment);
        });
    }

    private void showDeactivateConfirmationDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_deactivate_account, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Xử lý các nút trong dialog
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirmDeactivate = dialogView.findViewById(R.id.btn_confirm_deactivate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirmDeactivate.setOnClickListener(v -> {
            dialog.dismiss();
            deactivateAccount();
        });
    }

    private void deactivateAccount() {
        // TODO: Implement actual account deactivation (will require API or Firebase Auth, etc.)

        // Show success message
        Toast.makeText(requireContext(), "Account successfully deactivated", Toast.LENGTH_SHORT).show();

        // Navigate back to login screen using Intent
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish(); // Close the current MainActivity
    }
}
