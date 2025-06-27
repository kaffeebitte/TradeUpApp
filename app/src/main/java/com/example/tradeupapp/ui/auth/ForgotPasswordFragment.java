package com.example.tradeupapp.ui.auth;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordFragment extends Fragment {

    private TextInputEditText etEmail;
    private MaterialButton btnResetPassword;
    private MaterialButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners(view);
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        btnResetPassword = view.findViewById(R.id.btn_reset_password);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void setupClickListeners(View view) {
        btnResetPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (validateInput(email)) {
                sendResetPasswordEmail(email);
            }
        });

        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });
    }

    private boolean validateInput(String email) {
        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            return false;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Email không hợp lệ");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendResetPasswordEmail(String email) {
        // TODO: Implement password reset email logic
        Toast.makeText(requireContext(), "Email đặt lại mật khẩu đã được gửi đến " + email, Toast.LENGTH_LONG).show();

        // Navigate back to login screen after sending email
        Navigation.findNavController(requireView()).navigateUp();
    }
}
