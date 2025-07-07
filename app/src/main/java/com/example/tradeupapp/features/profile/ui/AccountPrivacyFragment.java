package com.example.tradeupapp.features.profile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.User;
import com.example.tradeupapp.shared.auth.UserManager;

public class AccountPrivacyFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_privacy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button deactivateBtn = view.findViewById(R.id.btn_deactivate_account);
        Button deleteBtn = view.findViewById(R.id.btn_delete_account);

        deactivateBtn.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Deactivation")
                .setMessage("Are you sure you want to deactivate your account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Thực hiện deactivate trước, sau đó mới mở dialog xác nhận
                    com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
                    com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                    String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
                    if (uid == null) {
                        android.widget.Toast.makeText(requireContext(), "User not found!", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    db.collection("users").document(uid)
                        .update(
                            "deactivated", true,
                            "isActive", false,
                            "deactivatedAt", com.google.firebase.Timestamp.now()
                        )
                        .addOnSuccessListener(unused -> {
                            new AccountDeactivatedDialogFragment().show(getParentFragmentManager(), "deactivate_dialog");
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("Deactivate", "Firestore update failed: " + e.getMessage());
                            android.widget.Toast.makeText(requireContext(), "Failed to deactivate account!", android.widget.Toast.LENGTH_LONG).show();
                        });
                })
                .setNegativeButton("No", null)
                .show();
        });
        deleteBtn.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.deleteAccountFragment);
        });
    }
}
