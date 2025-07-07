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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnGoogleLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

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
        btnLogin = view.findViewById(R.id.btn_login);
        btnGoogleLogin = view.findViewById(R.id.btn_google_signin);
        tvRegister = view.findViewById(R.id.tv_register);
        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
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
    }

    private void validateInputFields() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Enable the login button only if:
        // 1. Email is not empty and has valid format
        // 2. Password has at least 6 characters
        boolean isEmailValid = !email.isEmpty() && isValidEmail(email);
        boolean isPasswordValid = password.length() >= 6;

        btnLogin.setEnabled(isEmailValid && isPasswordValid);
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

    @SuppressWarnings("deprecation")
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
                        Toast.makeText(requireContext(), "Đăng nhập Google bị hủy", Toast.LENGTH_SHORT).show();
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
            String errorMessage = "Đăng nhập Google thất bại";
            if (e.getStatusCode() == 12501) {
                errorMessage = "Người dùng hủy đăng nhập";
            } else if (e.getStatusCode() == 7) {
                errorMessage = "Lỗi kết nối mạng";
            }
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String email, String password) {
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

        if (password.isEmpty()) {
            etPassword.setError("Mật khẩu không được để trống");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void performLogin(String email, String password) {
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if the email is verified
                            if (user.isEmailVerified()) {
                                // Check if user is deleted or deactivated in Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(user.getUid()).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                Boolean isDeleted = documentSnapshot.getBoolean("isDeleted");
                                                if (Boolean.TRUE.equals(isDeleted)) {
                                                    Toast.makeText(requireContext(), "Tài khoản của bạn đã bị xoá và không thể đăng nhập.", Toast.LENGTH_LONG).show();
                                                    mAuth.signOut();
                                                    return;
                                                }
                                                Boolean deactivated = documentSnapshot.getBoolean("deactivated");
                                                Boolean isActive = documentSnapshot.getBoolean("isActive");
                                                if (Boolean.TRUE.equals(deactivated) || Boolean.FALSE.equals(isActive)) {
                                                    // Reactivate account: set deactivated=false, isActive=true, deactivatedAt=null
                                                    db.collection("users").document(user.getUid())
                                                            .update(
                                                                "deactivated", false,
                                                                "isActive", true,
                                                                "deactivatedAt", null
                                                            )
                                                            .addOnSuccessListener(unused -> {
                                                                Toast.makeText(requireContext(), "Tài khoản đã được kích hoạt lại", Toast.LENGTH_SHORT).show();
                                                                navigateToMainActivity();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(requireContext(), "Không thể kích hoạt lại tài khoản: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                                mAuth.signOut();
                                                            });
                                                } else {
                                                    Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                                    navigateToMainActivity();
                                                }
                                            } else {
                                                Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(requireContext(),
                                        "Vui lòng xác minh email của bạn trước khi tiếp tục",
                                        Toast.LENGTH_LONG).show();
                                Bundle args = new Bundle();
                                args.putString("email", email);
                                Navigation.findNavController(requireView())
                                        .navigate(R.id.action_loginFragment_to_emailVerificationFragment, args);
                            }
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Đăng nhập thất bại";
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
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
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if user is deleted or deactivated in Firestore (Google login)
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(user.getUid()).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            Boolean isDeleted = documentSnapshot.getBoolean("isDeleted");
                                            if (Boolean.TRUE.equals(isDeleted)) {
                                                Toast.makeText(requireContext(), "Tài khoản của bạn đã bị xoá và không thể đăng nhập.", Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                                return;
                                            }
                                            Boolean deactivated = documentSnapshot.getBoolean("deactivated");
                                            Boolean isActive = documentSnapshot.getBoolean("isActive");
                                            if (Boolean.TRUE.equals(deactivated) || Boolean.FALSE.equals(isActive)) {
                                                db.collection("users").document(user.getUid())
                                                        .update(
                                                            "deactivated", false,
                                                            "isActive", true,
                                                            "deactivatedAt", null
                                                        )
                                                        .addOnSuccessListener(unused -> {
                                                            Toast.makeText(requireContext(), "Tài khoản đã được kích hoạt lại", Toast.LENGTH_SHORT).show();
                                                            navigateToMainActivity();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(requireContext(), "Không thể kích hoạt lại tài khoản: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                            mAuth.signOut();
                                                        });
                                            } else {
                                                Toast.makeText(requireContext(), "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show();
                                                navigateToMainActivity();
                                            }
                                        } else {
                                            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                                        }
                                    });
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
