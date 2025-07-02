package com.example.tradeupapp.features.listing.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.OfferModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Dialog fragment for making an offer on an item
 */
public class MakeOfferDialogFragment extends DialogFragment {

    private ListingModel listing;
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

        // Get listing from arguments
        if (getArguments() != null) {
            listing = (ListingModel) getArguments().getSerializable("listing");
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
        if (listing != null) {
            tvItemTitle.setText(listing.getTitle());
            tvItemPrice.setText("$" + listing.getPrice());
            // Set initial offer amount to 90% of the item price as a suggestion
            double suggestedOffer = listing.getPrice() * 0.9;
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
        if (listing == null || auth.getCurrentUser() == null) {
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

            // Validate offer amount is not higher than item price
            if (offerAmount > listing.getPrice()) {
                etOfferAmount.setError("Offer cannot be higher than the item price");
                return;
            }

            // Create offer object using the proper OfferModel structure
            OfferModel offer = new OfferModel(
                listing.getId(),            // listingId
                auth.getCurrentUser().getUid(), // buyerId
                offerAmount,                // offeredPrice
                message                     // message
            );

            // Disable the button to prevent multiple submissions
            btnSubmitOffer.setEnabled(false);

            // Save offer to Firestore
            db.collection("offers")
                    .add(offer)
                    .addOnSuccessListener(documentReference -> {
                        // Update the offer with the generated ID
                        offer.setId(documentReference.getId());

                        // Create notification for the seller
                        createOfferNotification(listing.getSellerId(), listing.getTitle());

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

    /**
     * Create a notification for the seller about the new offer
     */
    private void createOfferNotification(String sellerId, String itemTitle) {
        if (sellerId == null || auth.getCurrentUser() == null) return;

        // Get buyer's name (you might want to fetch it from Firestore in a real app)
        String buyerName = auth.getCurrentUser().getDisplayName();
        if (buyerName == null || buyerName.isEmpty()) {
            buyerName = "A buyer"; // Fallback name
        }

        // Use the NotificationModel factory method
        db.collection("notifications")
            .add(com.example.tradeupapp.models.NotificationModel.createOfferNotification(
                sellerId,
                buyerName,
                itemTitle,
                listing.getId()
            ))
            .addOnFailureListener(e -> {
                // Silent failure - don't interrupt the user flow
                // Log the error in a real app
            });
    }
}
