package com.example.tradeupapp.features.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeupapp.R;
import com.example.tradeupapp.features.profile.model.PaymentMethod;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.PaymentMethodViewHolder> {
    private List<PaymentMethod> paymentMethods = new ArrayList<>();
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnPaymentMethodSelectedListener listener;

    public void setPaymentMethods(List<PaymentMethod> methods) {
        this.paymentMethods = methods;
        notifyDataSetChanged();
    }

    public void setOnPaymentMethodSelectedListener(OnPaymentMethodSelectedListener listener) {
        this.listener = listener;
    }

    public PaymentMethod getSelectedPaymentMethod() {
        if (selectedPosition >= 0 && selectedPosition < paymentMethods.size()) {
            return paymentMethods.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_method, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethod method = paymentMethods.get(position);
        holder.tvType.setText(method.getType());
        holder.tvDisplayName.setText(method.getDisplayName());
        holder.tvDetails.setText(method.getDetails());
        holder.tvDefault.setVisibility(method.isDefault() ? View.VISIBLE : View.GONE);
        holder.itemView.setSelected(selectedPosition == position);
        // Use a built-in Material color for highlight, fallback to a light gray
        holder.itemView.setBackgroundColor(selectedPosition == position ? holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray) : android.graphics.Color.TRANSPARENT);
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onSelected(method);
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    static class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDisplayName, tvDetails, tvDefault;
        public PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_payment_type);
            tvDisplayName = itemView.findViewById(R.id.tv_payment_display_name);
            tvDetails = itemView.findViewById(R.id.tv_payment_details);
            tvDefault = itemView.findViewById(R.id.tv_payment_default);
        }
    }

    public interface OnPaymentMethodSelectedListener {
        void onSelected(PaymentMethod method);
    }
}
