package com.example.tradeupapp.ui.auth;

import android.content.Intent;
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

import com.example.tradeupapp.MainActivity;
import com.example.tradeupapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnGoogleLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupTextWatchers();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        btnGoogleLogin = view.findViewById(R.id.btn_google_signin);
        tvRegister = view.findViewById(R.id.tv_signup);
        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
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
                validateInputFields();
            }
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
    }

    private void validateInputFields() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Enable the login button only if both fields are not empty
        btnLogin.setEnabled(!email.isEmpty() && !password.isEmpty());
    }

    private void setupClickListeners(View view) {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                performLogin(email, password);
            }
        });

        btnGoogleLogin.setOnClickListener(v -> {
            performGoogleSignIn();
        });

        tvRegister.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Mật khẩu không được để trống");
            return false;
        }

        return true;
    }

    private void performLogin(String email, String password) {
        // TODO: Implement authentication logic
        Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

        // After successful login, navigate to main activity
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void performGoogleSignIn() {
        // TODO: Implement Google Sign-In
        Toast.makeText(requireContext(), "Đăng nhập bằng Google", Toast.LENGTH_SHORT).show();
    }
}
