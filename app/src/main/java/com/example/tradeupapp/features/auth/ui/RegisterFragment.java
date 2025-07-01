package com.example.tradeupapp.features.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tradeupapp.MainActivity;
import com.example.tradeupapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

@SuppressWarnings("deprecation")
public class RegisterFragment extends Fragment {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnRegister;
    private MaterialButton btnGoogleRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

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

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        setupGoogleSignIn();

        // Initialize the Activity Result Launcher for Google Sign-In
        setupGoogleSignInLauncher();
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnRegister = view.findViewById(R.id.btn_register);
        btnGoogleRegister = view.findViewById(R.id.btn_google_register); // Fixed: was btn_google_signin
        tvLogin = view.findViewById(R.id.tv_login);
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputFields();
            }
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);
    }

    private void validateInputFields() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Enable register button only if all fields are filled and valid
        boolean isValid = !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty() &&
                isValidEmail(email) && isValidPassword(password) && password.equals(confirmPassword);

        btnRegister.setEnabled(isValid);
    }

    private void setupClickListeners(View view) {
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInput(email, password, confirmPassword)) {
                performRegistration(email, password);
            }
        });

        btnGoogleRegister.setOnClickListener(v -> {
            performGoogleSignIn();
        });

        tvLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_loginFragment);
        });
    }

    private boolean validateInput(String email, String password, String confirmPassword) {
        // Email validation
        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            etEmail.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Định dạng email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        // Password validation
        if (password.isEmpty()) {
            etPassword.setError("Mật khẩu không được để trống");
            etPassword.requestFocus();
            return false;
        }

        if (!isValidPassword(password)) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Xác nhận mật khẩu không được để trống");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        // Password must be at least 6 characters
        return password.length() >= 6;
    }

    private void performRegistration(String email, String password) {
        // Disable register button during registration
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnRegister.setEnabled(true); // Re-enable button

                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Send email verification
                            sendEmailVerification(user, email);
                        }
                    } else {
                        // Registration failed
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Đăng ký thất bại";
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user, String email) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(),
                                "Đăng ký thành công! Vui lòng kiểm tra email để xác minh tài khoản",
                                Toast.LENGTH_LONG).show();

                        // Navigate to email verification screen
                        Bundle args = new Bundle();
                        args.putString("email", email);
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_registerFragment_to_emailVerificationFragment, args);
                    } else {
                        Toast.makeText(requireContext(),
                                "Không thể gửi email xác minh. Vui lòng thử lại",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupGoogleSignIn() {
        // Configure Google Sign-In to request the user's ID, email address, and basic profile
        // Using the web client ID from google-services.json for com.example.tradeupapp
        String webClientId = "966394665760-d73tf9klj68snprd3dukqmgbbo08hiun.apps.googleusercontent.com";

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK) {
                        Intent data = result.getData();
                        handleGoogleSignInResult(data);
                    } else {
                        // User cancelled the sign-in
                        Toast.makeText(requireContext(), "Đăng ký Google bị hủy", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @SuppressWarnings("deprecation")
    private void handleGoogleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                Toast.makeText(requireContext(), "Không thể lấy thông tin từ Google", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            // Google Sign In failed
            String errorMessage = "Đăng ký Google thất bại";
            if (e.getStatusCode() == 12501) {
                errorMessage = "Người dùng hủy đăng ký";
            } else if (e.getStatusCode() == 7) {
                errorMessage = "Lỗi kết nối mạng";
            }
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void performGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success - Google accounts are automatically verified
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(requireContext(), "Đăng ký Google thành công", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        }
                    } else {
                        // Sign in failed
                        Toast.makeText(requireContext(), "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
