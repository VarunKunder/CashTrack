package com.example.cashtrack.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cashtrack.R;
import com.example.cashtrack.db.DbRepo;
import com.example.cashtrack.model.Trip;
import java.util.List;
import java.util.Locale;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<Trip> tripList;
    private OnTripClickListener listener;
    private DbRepo repo;

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
        void onTripLongClick(Trip trip);
    }

    public TripAdapter(List<Trip> tripList, OnTripClickListener listener, DbRepo repo) {
        this.tripList = tripList;
        this.listener = listener;
        this.repo = repo;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_list_item, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = tripList.get(position);
        holder.tvTripName.setText(trip.getName());
        // TODO: Set trip details like number of participants

        // Set total spent
        double totalSpent = repo.getTotalSpentForTrip(trip.getId());
        holder.tvTotalSpent.setText(String.format(Locale.getDefault(), "Total Spent: ₹%,.0f", totalSpent));

        holder.itemView.setOnClickListener(v -> listener.onTripClick(trip));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onTripLongClick(trip);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripName;
        TextView tvTripDetails;
        TextView tvTotalSpent;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripName = itemView.findViewById(R.id.tvTripName);
            tvTripDetails = itemView.findViewById(R.id.tvTripDetails);
            tvTotalSpent = itemView.findViewById(R.id.tvTotalSpent);
        }
    }
}
