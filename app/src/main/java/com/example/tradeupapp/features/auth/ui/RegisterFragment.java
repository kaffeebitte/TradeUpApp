package com.example.tradeupapp.features.auth.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends Fragment {

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnRegister;
    private MaterialButton btnGoogleRegister;
    private TextView tvLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupTextWatchers();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnRegister = view.findViewById(R.id.btn_register);
        btnGoogleRegister = view.findViewById(R.id.btn_google_register);
        tvLogin = view.findViewById(R.id.tv_login);
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        };

        etName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);
    }

    private void setupClickListeners(View view) {
        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                performRegistration();
            }
        });

        btnGoogleRegister.setOnClickListener(v -> {
            performGoogleSignUp();
        });

        tvLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_loginFragment);
        });
    }

    private void validateForm() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        boolean isValid = !name.isEmpty() && !email.isEmpty() &&
                          !password.isEmpty() && !confirmPassword.isEmpty() &&
                          password.equals(confirmPassword);

        btnRegister.setEnabled(isValid);
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (name.isEmpty()) {
            etName.setError("Tên không được để trống");
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            return false;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Email không hợp lệ");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Mật khẩu không được để trống");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void performRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // TODO: Implement user registration
        Toast.makeText(requireContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();

        // Navigate to email verification screen
        Navigation.findNavController(requireView()).navigate(
            R.id.action_registerFragment_to_emailVerificationFragment
        );
    }

    private void performGoogleSignUp() {
        // TODO: Implement Google Sign-Up
        Toast.makeText(requireContext(), "Đăng ký bằng Google", Toast.LENGTH_SHORT).show();
    }
}
