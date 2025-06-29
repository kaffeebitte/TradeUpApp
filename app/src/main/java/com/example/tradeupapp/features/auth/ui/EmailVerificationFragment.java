package com.example.tradeupapp.features.auth.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tradeupapp.R;
import com.google.android.material.button.MaterialButton;

public class EmailVerificationFragment extends Fragment {

    private TextView tvEmailSent;
    private TextView tvResendTimer;
    private MaterialButton btnResendEmail;
    private MaterialButton btnContinue;
    private CountDownTimer resendTimer;
    private boolean isEmailVerified = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_email_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupCountdownTimer();
        setupClickListeners(view);

        // Display user email (would normally come from registration)
        String userEmail = "user@example.com"; // Replace with actual email from registration
        tvEmailSent.setText(getString(R.string.email_verification_sent, userEmail));
    }

    private void initViews(View view) {
        tvEmailSent = view.findViewById(R.id.tv_email_sent);
        tvResendTimer = view.findViewById(R.id.tv_resend_timer);
        btnResendEmail = view.findViewById(R.id.btn_resend_email);
        btnContinue = view.findViewById(R.id.btn_continue);
    }

    private void setupCountdownTimer() {
        btnResendEmail.setEnabled(false);

        resendTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResendTimer.setText(getString(R.string.resend_timer, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tvResendTimer.setVisibility(View.GONE);
                btnResendEmail.setEnabled(true);
            }
        }.start();
    }

    private void setupClickListeners(View view) {
        btnResendEmail.setOnClickListener(v -> {
            resendVerificationEmail();
            setupCountdownTimer();
        });

        btnContinue.setOnClickListener(v -> {
            // In a real app, you would check if the email is verified
            checkEmailVerification();

            if (isEmailVerified) {
                // Navigate to login or main screen
                navigateToLogin();
            } else {
                Toast.makeText(requireContext(),
                    "Vui lòng xác nhận email trước khi tiếp tục",
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendVerificationEmail() {
        // TODO: Implement resend verification email logic
        Toast.makeText(requireContext(), "Email xác nhận đã được gửi lại", Toast.LENGTH_SHORT).show();
    }

    private void checkEmailVerification() {
        // TODO: Implement actual email verification check
        // For demo purposes, we'll simulate verification
        isEmailVerified = true;
    }

    private void navigateToLogin() {
        // Navigate to login screen
        Navigation.findNavController(requireView()).navigate(
            R.id.action_emailVerificationFragment_to_loginFragment
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}
