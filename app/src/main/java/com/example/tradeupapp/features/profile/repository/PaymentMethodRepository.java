package com.example.tradeupapp.features.profile.repository;

import com.example.tradeupapp.features.profile.model.PaymentMethod;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodRepository {
    private final FirebaseFirestore db;
    private final String userId;
    private final CollectionReference paymentMethodsRef;

    public PaymentMethodRepository() {
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        paymentMethodsRef = db.collection("users").document(userId).collection("payment_methods");
    }

    public void addPaymentMethod(PaymentMethod method, PaymentMethodCallback callback) {
        paymentMethodsRef.add(method)
            .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
            .addOnFailureListener(callback::onFailure);
    }

    public void getPaymentMethods(PaymentMethodsCallback callback) {
        paymentMethodsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<PaymentMethod> methods = new ArrayList<>();
                QuerySnapshot snapshot = task.getResult();
                for (QueryDocumentSnapshot doc : snapshot) {
                    PaymentMethod method = doc.toObject(PaymentMethod.class);
                    method.setId(doc.getId());
                    methods.add(method);
                }
                callback.onSuccess(methods);
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    public void deletePaymentMethod(String methodId, PaymentMethodCallback callback) {
        paymentMethodsRef.document(methodId).delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess(methodId))
            .addOnFailureListener(callback::onFailure);
    }

    public void setDefaultPaymentMethod(String methodId, PaymentMethodCallback callback) {
        getPaymentMethods(new PaymentMethodsCallback() {
            @Override
            public void onSuccess(List<PaymentMethod> methods) {
                for (PaymentMethod method : methods) {
                    boolean isDefault = method.getId().equals(methodId);
                    paymentMethodsRef.document(method.getId())
                        .update("isDefault", isDefault);
                }
                callback.onSuccess(methodId);
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public interface PaymentMethodsCallback {
        void onSuccess(List<PaymentMethod> methods);
        void onFailure(Exception e);
    }

    public interface PaymentMethodCallback {
        void onSuccess(String methodId);
        void onFailure(Exception e);
    }
}
