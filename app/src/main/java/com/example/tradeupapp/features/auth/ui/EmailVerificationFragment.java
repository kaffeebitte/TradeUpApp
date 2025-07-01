package com.example.tradeupapp.features.auth.ui;

import android.content.Intent;
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

import com.example.tradeupapp.MainActivity;
import com.example.tradeupapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationFragment extends Fragment {

    private TextView tvEmailSent;
    private TextView tvResendTimer;
    private TextView tvUserEmail;
    private MaterialButton btnResendEmail;
    private MaterialButton btnBackToLogin;
    private CountDownTimer resendTimer;
    private CountDownTimer verificationCheckTimer;
    private FirebaseAuth mAuth;
    private String userEmail;
    private static final int RESEND_DELAY_SECONDS = 60;
    private static final int VERIFICATION_CHECK_INTERVAL = 3000; // 3 seconds

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_email_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        initViews(view);
        setupClickListeners();

        // Get email from bundle if available
        if (getArguments() != null) {
            userEmail = getArguments().getString("email", "");
        }

        // If no email in bundle, get from current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (userEmail.isEmpty() && currentUser != null) {
            userEmail = currentUser.getEmail();
        }

        // Display email
        if (!userEmail.isEmpty()) {
            tvUserEmail.setText(userEmail);
        }

        // Start automatic verification checking
        startVerificationCheckTimer();

        // Start resend timer
        startResendTimer();
    }

    private void initViews(View view) {
        tvEmailSent = view.findViewById(R.id.tv_email_sent);
        tvResendTimer = view.findViewById(R.id.tv_resend_timer);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        btnResendEmail = view.findViewById(R.id.btn_resend_email);
        btnBackToLogin = view.findViewById(R.id.btn_back_to_login);
    }

    private void setupClickListeners() {
        btnResendEmail.setOnClickListener(v -> resendVerificationEmail());

        btnBackToLogin.setOnClickListener(v -> {
            // Sign out current user and go back to login
            mAuth.signOut();
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_emailVerificationFragment_to_loginFragment);
        });
    }

    private void startResendTimer() {
        btnResendEmail.setEnabled(false);

        resendTimer = new CountDownTimer(RESEND_DELAY_SECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvResendTimer.setText("Gửi lại sau " + secondsLeft + " giây");
                tvResendTimer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                btnResendEmail.setEnabled(true);
                tvResendTimer.setVisibility(View.GONE);
            }
        };
        resendTimer.start();
    }

    private void startVerificationCheckTimer() {
        verificationCheckTimer = new CountDownTimer(Long.MAX_VALUE, VERIFICATION_CHECK_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                checkEmailVerificationSilently();
            }

            @Override
            public void onFinish() {
                // Won't be called due to Long.MAX_VALUE
            }
        };
        verificationCheckTimer.start();
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // progressIndicator.setVisibility(View.VISIBLE);

            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        // progressIndicator.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Email xác minh đã được gửi lại",
                                    Toast.LENGTH_SHORT).show();
                            startResendTimer();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Không thể gửi lại email. Vui lòng thử lại",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // progressIndicator.setVisibility(View.VISIBLE);

            // Reload user to get updated verification status
            user.reload().addOnCompleteListener(task -> {
                // progressIndicator.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        // Email verified successfully
                        stopTimers();
                        Toast.makeText(requireContext(),
                                "Email đã được xác minh thành công!",
                                Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        Toast.makeText(requireContext(),
                                "Email chưa được xác minh. Vui lòng kiểm tra hộp thư của bạn",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(requireContext(),
                            "Không thể kiểm tra trạng thái xác minh",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkEmailVerificationSilently() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && user.isEmailVerified()) {
                    // Email verified - automatically navigate
                    stopTimers();
                    Toast.makeText(requireContext(),
                            "Email đã được xác minh thành công!",
                            Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                }
            });
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void stopTimers() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        if (verificationCheckTimer != null) {
            verificationCheckTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimers();
    }
}
