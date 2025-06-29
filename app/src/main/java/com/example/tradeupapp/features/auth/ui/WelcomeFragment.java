package com.example.tradeupapp.features.auth.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tradeupapp.R;

public class WelcomeFragment extends Fragment {

    private Button btnLogin;
    private Button btnRegister;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogin = view.findViewById(R.id.btnLogin);
        btnRegister = view.findViewById(R.id.btnRegister);

        setupClickListeners();
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(view -> {
            // Navigate to Login Fragment
            Navigation.findNavController(view)
                    .navigate(R.id.action_welcomeFragment_to_loginFragment);
        });

        btnRegister.setOnClickListener(view -> {
            // Navigate to Register Fragment
            Navigation.findNavController(view)
                    .navigate(R.id.action_welcomeFragment_to_registerFragment);
        });
    }
}
