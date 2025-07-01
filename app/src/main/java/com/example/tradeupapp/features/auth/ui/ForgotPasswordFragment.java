package com.example.tradeupapp.features.auth.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    private TextInputEditText etEmail;
    private MaterialButton btnResetPassword;
    private MaterialButton btnBack;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        initViews(view);
        setupTextWatchers();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        btnResetPassword = view.findViewById(R.id.btn_reset_password);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void setupTextWatchers() {
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputFields();
            }
        });
    }

    private void validateInputFields() {
        String email = etEmail.getText().toString().trim();
        // Enable reset button only if email is valid
        btnResetPassword.setEnabled(!email.isEmpty() && isValidEmail(email));
    }

    private void setupClickListeners(View view) {
        btnResetPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (validateInput(email)) {
                sendResetPasswordEmail(email);
            }
        });

        btnBack.setOnClickListener(v -> {
            // Navigate back using fragment manager instead of navigation component
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                // Fallback: finish the activity to return to previous screen
                requireActivity().finish();
            }
        });
    }

    private boolean validateInput(String email) {
        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            etEmail.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Địa chỉ email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendResetPasswordEmail(String email) {
        // Disable button during request
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Đang gửi...");

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("Gửi Email Đặt Lại");

                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(),
                                "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn",
                                Toast.LENGTH_LONG).show();

                        // Navigate back to login using fragment manager
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        } else {
                            // Fallback: finish the activity to return to previous screen
                            requireActivity().finish();
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                getLocalizedErrorMessage(task.getException().getMessage()) :
                                "Không thể gửi email đặt lại mật khẩu";
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getLocalizedErrorMessage(String firebaseError) {
        if (firebaseError == null) return "Có lỗi xảy ra";

        if (firebaseError.contains("user-not-found")) {
            return "Không tìm thấy tài khoản với email này";
        } else if (firebaseError.contains("invalid-email")) {
            return "Địa chỉ email không hợp lệ";
        } else if (firebaseError.contains("too-many-requests")) {
            return "Quá nhiều yêu cầu. Vui lòng thử lại sau";
        } else {
            return "Không thể gửi email đặt lại mật khẩu";
        }
    }
}
