package com.example.cashtrack.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cashtrack.R;
import com.example.cashtrack.model.Expense;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Expense> expenseList;

    public TransactionAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.tvTransactionName.setText(expense.getNote());
        holder.tvTransactionCategory.setText(expense.getCategory());
        holder.tvTransactionAmount.setText(String.format(Locale.getDefault(), "-₹%.2f", expense.getAmount()));
        holder.tvTransactionDate.setText(expense.getDate());
        // TODO: Set category icon based on category
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvTransactionName;
        TextView tvTransactionCategory;
        TextView tvTransactionAmount;
        TextView tvTransactionDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTransactionName = itemView.findViewById(R.id.tvTransactionName);
            tvTransactionCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
        }
    }
}
