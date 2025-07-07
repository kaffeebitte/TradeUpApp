package com.example.tradeupapp.features.profile.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.tradeupapp.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class AccountDeactivatedDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Account Deactivated")
                .setMessage("Your account has been deactivated. You can reactivate it by logging in again.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Đăng xuất và chuyển về AuthActivity
                    FirebaseAuth.getInstance().signOut();
                    android.app.Activity activity = getActivity();
                    if (activity != null) {
                        Intent intent = new Intent(activity, com.example.tradeupapp.features.auth.ui.AuthActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        activity.finishAffinity();
                    }
                })
                .create();
    }
}
