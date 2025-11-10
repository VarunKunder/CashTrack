package com.example.cashtrack.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cashtrack.MainActivity;
import com.example.cashtrack.R;
import com.example.cashtrack.adapter.TripAdapter;
import com.example.cashtrack.db.DbRepo;
import com.example.cashtrack.model.Trip;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class TripListActivity extends AppCompatActivity implements TripAdapter.OnTripClickListener {

    private RecyclerView rvTrips;
    private TripAdapter tripAdapter;
    private List<Trip> tripList = new ArrayList<>();
    private DbRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        repo = new DbRepo(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvTrips = findViewById(R.id.rvTrips);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupRecyclerView();

        bottomNavigationView.setSelectedItemId(R.id.navigation_trips);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (itemId == R.id.navigation_trips) {
                // Already on this screen
            } else if (itemId == R.id.navigation_add) {
                startActivity(new Intent(this, AddTripActivity.class));
            } else if (itemId == R.id.navigation_transactions) {
                startActivity(new Intent(this, AllTransactionsActivity.class));
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrips();
    }

    private void setupRecyclerView() {
        tripAdapter = new TripAdapter(tripList, this, repo);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        rvTrips.setAdapter(tripAdapter);
    }

    private void loadTrips() {
        tripList.clear();
        tripList.addAll(repo.getAllTrips());
        tripAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTripClick(Trip trip) {
        // TODO: Launch TripDetailActivity
    }

    @Override
    public void onTripLongClick(Trip trip) {
        // Do nothing
    }
}
