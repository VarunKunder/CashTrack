package com.example.cashtrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cashtrack.R;
import com.example.cashtrack.adapter.TripAdapter;
import com.example.cashtrack.db.DbRepo;
import com.example.cashtrack.model.Trip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class TripsFragment extends Fragment implements TripAdapter.OnTripClickListener {

    private RecyclerView rvTrips;
    private TripAdapter tripAdapter;
    private List<Trip> tripList = new ArrayList<>();
    private DbRepo repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips, container, false);

        repo = new DbRepo(getContext());

        rvTrips = view.findViewById(R.id.rvTrips);
        FloatingActionButton fabAddTrip = view.findViewById(R.id.fabAddTrip);

        setupRecyclerView();

        fabAddTrip.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddTripActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTrips();
    }

    private void setupRecyclerView() {
        tripAdapter = new TripAdapter(tripList, this, repo);
        rvTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTrips.setAdapter(tripAdapter);
    }

    private void loadTrips() {
        tripList.clear();
        tripList.addAll(repo.getAllTrips());
        tripAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTripClick(Trip trip) {
        Intent intent = new Intent(getContext(), TripDetailActivity.class);
        intent.putExtra("TRIP_ID", trip.getId());
        startActivity(intent);
    }

    @Override
    public void onTripLongClick(Trip trip) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Trip")
                .setMessage("Are you sure you want to delete this trip?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repo.deleteTrip(trip.getId());
                    loadTrips();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
