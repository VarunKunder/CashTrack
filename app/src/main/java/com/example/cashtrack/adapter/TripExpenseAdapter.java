package com.example.cashtrack.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cashtrack.R;
import com.example.cashtrack.model.Participant;
import com.example.cashtrack.model.TripExpense;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TripExpenseAdapter extends RecyclerView.Adapter<TripExpenseAdapter.ViewHolder> {

    private final List<TripExpense> expenseList;
    private OnTripExpenseClickListener listener;

    public interface OnTripExpenseClickListener {
        void onTripExpenseLongClick(TripExpense expense);
    }

    public TripExpenseAdapter(List<TripExpense> expenseList, OnTripExpenseClickListener listener) {
        this.expenseList = expenseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_expense_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripExpense expense = expenseList.get(position);
        holder.tvDescription.setText(expense.getDescription());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%,.0f", expense.getAmount()));
        holder.tvPaidBy.setText("Paid by " + expense.getPaidBy().getName());

        // Populate tvSharedWith
        if (expense.getSharedWith() != null && !expense.getSharedWith().isEmpty()) {
            String sharedWithNames = expense.getSharedWith().stream()
                                            .map(Participant::getName)
                                            .collect(Collectors.joining(", "));
            holder.tvSharedWith.setText("Shared with: " + sharedWithNames);
            holder.tvSharedWith.setVisibility(View.VISIBLE);
        } else {
            holder.tvSharedWith.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTripExpenseLongClick(expense);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvPaidBy, tvSharedWith;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPaidBy = itemView.findViewById(R.id.tvPaidBy);
            tvSharedWith = itemView.findViewById(R.id.tvSharedWith);
        }
    }
}
