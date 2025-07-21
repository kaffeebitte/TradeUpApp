package com.example.tradeupapp.features.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeupapp.R;
import com.example.tradeupapp.features.profile.model.PaymentMethod;
import com.example.tradeupapp.features.profile.repository.PaymentMethodRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

public class PaymentMethodsFragment extends Fragment {
    private static final String TAG = "PaymentMethodsFragment";
    private PaymentMethodRepository repository;
    private RecyclerView recyclerView;
    private PaymentMethodsAdapter adapter;
    private MaterialButton btnAddPaymentMethod;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_methods, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        repository = new PaymentMethodRepository();
        recyclerView = view.findViewById(R.id.rv_payment_methods);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PaymentMethodsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnPaymentMethodSelectedListener(method -> {
            Log.d(TAG, "Payment method selected: " + method.getDisplayName());
            Toast.makeText(getContext(), "Selected: " + method.getDisplayName(), Toast.LENGTH_SHORT).show();
        });
        btnAddPaymentMethod = view.findViewById(R.id.btn_add_payment_method);
        // Fix: Explicit cast to MaterialButton for Material3 compatibility
        if (!(btnAddPaymentMethod instanceof com.google.android.material.button.MaterialButton)) {
            btnAddPaymentMethod = (com.google.android.material.button.MaterialButton) view.findViewById(R.id.btn_add_payment_method);
        }
        btnAddPaymentMethod.setOnClickListener(v -> {
            Log.d(TAG, "Add Payment Method button clicked");
            showAddPaymentMethodDialog();
        });
        loadPaymentMethods();
    }

    private void loadPaymentMethods() {
        repository.getPaymentMethods(new PaymentMethodRepository.PaymentMethodsCallback() {
            @Override
            public void onSuccess(List<PaymentMethod> methods) {
                adapter.setPaymentMethods(methods);
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to load payment methods", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add Payment Method Dialog with Material 3 UI
    private void showAddPaymentMethodDialog() {
        Log.d(TAG, "showAddPaymentMethodDialog called");
        // Inflate custom layout for Material 3 dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_payment_method, null);
        Log.d(TAG, "Dialog layout inflated");
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setView(dialogView)
            .create();
        Log.d(TAG, "AlertDialog created");

        // Top App Bar
        View appBar = dialogView.findViewById(R.id.top_app_bar);
        if (appBar != null) {
            Log.d(TAG, "Top App Bar found");
            if (appBar instanceof com.google.android.material.appbar.MaterialToolbar) {
                ((com.google.android.material.appbar.MaterialToolbar) appBar).setNavigationOnClickListener(v -> dialog.dismiss());
            } else {
                Log.e(TAG, "top_app_bar is not a MaterialToolbar");
            }
        } else {
            Log.e(TAG, "Top App Bar NOT found");
        }

        // Payment Method Cards
        View cardOption = dialogView.findViewById(R.id.card_option);
        View walletOption = dialogView.findViewById(R.id.wallet_option);
        if (cardOption != null && walletOption != null) {
            Log.d(TAG, "Payment method options found");
            cardOption.setOnClickListener(v -> selectPaymentType(dialogView, true));
            walletOption.setOnClickListener(v -> selectPaymentType(dialogView, false));
        } else {
            Log.e(TAG, "Payment method options NOT found");
        }

        // Card Input Form
        com.google.android.material.textfield.TextInputLayout cardNumberLayout = dialogView.findViewById(R.id.til_card_number);
        com.google.android.material.textfield.TextInputEditText cardNumberInput = dialogView.findViewById(R.id.et_card_number);
        com.google.android.material.textfield.TextInputLayout cardNameLayout = dialogView.findViewById(R.id.til_card_name);
        com.google.android.material.textfield.TextInputEditText cardNameInput = dialogView.findViewById(R.id.et_card_name);
        com.google.android.material.textfield.TextInputLayout expiryLayout = dialogView.findViewById(R.id.til_expiry);
        com.google.android.material.textfield.TextInputEditText expiryInput = dialogView.findViewById(R.id.et_expiry);
        com.google.android.material.textfield.TextInputLayout cvvLayout = dialogView.findViewById(R.id.til_cvv);
        com.google.android.material.textfield.TextInputEditText cvvInput = dialogView.findViewById(R.id.et_cvv);
        com.google.android.material.button.MaterialButton saveCardToggle = dialogView.findViewById(R.id.btn_save_card_toggle);

        // UPI/Wallet Selection
        View walletSheet = dialogView.findViewById(R.id.wallet_bottom_sheet);
        if (walletSheet != null) {
            Log.d(TAG, "Wallet bottom sheet found");
            walletSheet.findViewById(R.id.handle_bar);
        } else {
            Log.e(TAG, "Wallet bottom sheet NOT found");
        }
        // ...setup wallet list and selection logic...

        // Bottom Action Area
        com.google.android.material.button.MaterialButton btnAdd = dialogView.findViewById(R.id.btn_add_payment_method);
        if (btnAdd != null) {
            Log.d(TAG, "Add Payment Method button in dialog found");
            // Logic: Only enable if card info is valid or wallet selected
            btnAdd.setEnabled(true); // For demo, always enabled
            btnAdd.setOnClickListener(v -> {
                Log.d(TAG, "Dialog Add button clicked");
                // Validate form before showing message
                boolean valid = false;
                if (isCardSelected(dialogView)) {
                    String cardNumber = cardNumberInput.getText() != null ? cardNumberInput.getText().toString().replace(" ", "") : "";
                    String cardName = cardNameInput.getText() != null ? cardNameInput.getText().toString().trim() : "";
                    String expiry = expiryInput.getText() != null ? expiryInput.getText().toString().trim() : "";
                    String cvv = cvvInput.getText() != null ? cvvInput.getText().toString().trim() : "";
                    valid = cardNumber.length() == 16 && !cardName.isEmpty() && !expiry.isEmpty() && cvv.length() == 3;
                } else {
                    // For wallet, just require selection (can add more logic)
                    valid = true;
                }
                if (!valid) {
                    Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(), "Feature not available yet. Please try again later.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        } else {
            Log.e(TAG, "Add Payment Method button in dialog NOT found");
        }
        if (saveCardToggle != null) {
            saveCardToggle.setEnabled(true); // Always enabled for demo
            saveCardToggle.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Feature not available yet. Please try again later.", Toast.LENGTH_SHORT).show();
            });
        }
        // Show the dialog
        Log.d(TAG, "Showing dialog");
        dialog.show();
    }

    // Helper: select payment type
    private void selectPaymentType(View dialogView, boolean isCard) {
        dialogView.findViewById(R.id.card_option).setSelected(isCard);
        dialogView.findViewById(R.id.wallet_option).setSelected(!isCard);
        dialogView.findViewById(R.id.card_form).setVisibility(isCard ? View.VISIBLE : View.GONE);
        dialogView.findViewById(R.id.wallet_bottom_sheet).setVisibility(isCard ? View.GONE : View.VISIBLE);
    }
    private boolean isCardSelected(View dialogView) {
        return dialogView.findViewById(R.id.card_option).isSelected();
    }
}
