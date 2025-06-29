package com.example.tradeupapp.features.listing.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.OfferModel;
import com.google.android.material.button.MaterialButton;;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.UUID;

/**
 * Dialog fragment for making an offer on an item
 */
public class MakeOfferDialogFragment extends DialogFragment {

    private ItemModel item;
    private EditText etOfferAmount;
    private EditText etMessage;
    private MaterialButton btnSubmitOffer;
    private MaterialButton btnCancel;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get item from arguments
        if (getArguments() != null) {
            item = getArguments().getParcelable("item");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_offer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        TextView tvItemTitle = view.findViewById(R.id.tv_item_title);
        TextView tvItemPrice = view.findViewById(R.id.tv_item_price);
        etOfferAmount = view.findViewById(R.id.et_offer_amount);
        etMessage = view.findViewById(R.id.et_message);
        btnSubmitOffer = view.findViewById(R.id.btn_submit_offer);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Set initial values
        if (item != null) {
            tvItemTitle.setText(item.getTitle());
            tvItemPrice.setText("$" + item.getPrice());
            // Set initial offer amount to 90% of the item price as a suggestion
            double suggestedOffer = item.getPrice() * 0.9;
            etOfferAmount.setText(String.format("%.2f", suggestedOffer));
        }

        // Set click listeners
        btnSubmitOffer.setOnClickListener(v -> submitOffer());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void submitOffer() {
        if (item == null || auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Error: Unable to make offer", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        String offerAmountStr = etOfferAmount.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        // Validate offer amount
        if (offerAmountStr.isEmpty()) {
            etOfferAmount.setError("Please enter an offer amount");
            return;
        }

        try {
            double offerAmount = Double.parseDouble(offerAmountStr);

            // Create offer object
            OfferModel offer = new OfferModel();
            offer.setOfferId(UUID.randomUUID().toString());
            offer.setItemId(item.getId());
            offer.setSellerId(item.getUserId()); // Get sellerId from the item
            offer.setBuyerId(auth.getCurrentUser().getUid());
            offer.setOfferAmount(offerAmount);
            offer.setMessage(message);
            offer.setStatus("pending");
            offer.setTimestamp(new Date());
            offer.setItemTitle(item.getTitle());
            offer.setItemPrice(item.getPrice());

            // Disable the button to prevent multiple submissions
            btnSubmitOffer.setEnabled(false);

            // Save offer to Firestore
            db.collection("offers")
                    .document(offer.getOfferId())
                    .set(offer)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Offer sent successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSubmitOffer.setEnabled(true);
                    });

        } catch (NumberFormatException e) {
            etOfferAmount.setError("Please enter a valid amount");
        }
    }
}
