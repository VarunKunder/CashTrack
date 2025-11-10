package com.example.cashtrack.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cashtrack.R;
import com.example.cashtrack.model.Settlement;
import java.util.List;
import java.util.Locale;

public class SettlementAdapter extends RecyclerView.Adapter<SettlementAdapter.ViewHolder> {

    private final List<Settlement> settlementList;

    public SettlementAdapter(List<Settlement> settlementList) {
        this.settlementList = settlementList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settlement_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Settlement settlement = settlementList.get(position);
        holder.tvSettlement.setText(String.format(Locale.getDefault(), "%s owes %s ₹%,.0f", settlement.getFrom(), settlement.getTo(), settlement.getAmount()));
    }

    @Override
    public int getItemCount() {
        return settlementList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSettlement;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSettlement = itemView.findViewById(R.id.tvSettlement);
        }
    }
}
