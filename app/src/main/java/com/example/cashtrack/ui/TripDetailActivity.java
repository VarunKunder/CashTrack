package com.example.cashtrack.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashtrack.R;
import com.example.cashtrack.adapter.TripExpenseAdapter;
import com.example.cashtrack.adapter.SettlementAdapter;
import com.example.cashtrack.db.DbRepo;
import com.example.cashtrack.model.TripExpense;
import com.example.cashtrack.model.Participant;
import com.example.cashtrack.model.Settlement;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripDetailActivity extends AppCompatActivity implements TripExpenseAdapter.OnTripExpenseClickListener {

    private long tripId;
    private DbRepo repo;
    private RecyclerView rvTripExpenses, rvSettlements;
    private TripExpenseAdapter tripExpenseAdapter;
    private SettlementAdapter settlementAdapter;
    private List<TripExpense> tripExpenses = new ArrayList<>();
    private List<Settlement> settlements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        repo = new DbRepo(this);
        tripId = getIntent().getLongExtra("TRIP_ID", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        com.example.cashtrack.model.Trip trip = repo.getTrip(tripId);
        if (trip != null) {
            getSupportActionBar().setTitle(trip.getName());
        }

        rvTripExpenses = findViewById(R.id.rvTripExpenses);
        rvSettlements = findViewById(R.id.rvSettlements);
        FloatingActionButton fabAddTripExpense = findViewById(R.id.fabAddTripExpense);

        setupRecyclerViews();

        fabAddTripExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTripExpenseActivity.class);
            intent.putExtra("TRIP_ID", tripId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTripExpenses();
        calculateSettlements();
    }

    private void setupRecyclerViews() {
        tripExpenseAdapter = new TripExpenseAdapter(tripExpenses, this);
        rvTripExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvTripExpenses.setAdapter(tripExpenseAdapter);

        settlementAdapter = new SettlementAdapter(settlements);
        rvSettlements.setLayoutManager(new LinearLayoutManager(this));
        rvSettlements.setAdapter(settlementAdapter);
    }

    private void loadTripExpenses() {
        tripExpenses.clear();
        tripExpenses.addAll(repo.getTripExpenses(tripId));
        tripExpenseAdapter.notifyDataSetChanged();
    }

    /**
     * ✅ Fixed Settlement Logic — handles correct pairwise debts
     */
    private void calculateSettlements() {
        settlements.clear();

        List<Participant> participants = repo.getTripParticipants(tripId);
        if (participants.isEmpty()) return;

        // Map<FromId, Map<ToId, AmountOwed>>
        Map<Long, Map<Long, Double>> pairwiseBalances = new HashMap<>();

        for (TripExpense expense : tripExpenses) {
            long paidBy = expense.getPaidBy().getId();
            double amount = expense.getAmount();
            List<Participant> sharedWith = expense.getSharedWith();

            if (sharedWith.isEmpty()) continue;

            double share = amount / sharedWith.size();

            for (Participant p : sharedWith) {
                if (p.getId() != paidBy) {
                    pairwiseBalances
                            .computeIfAbsent(p.getId(), k -> new HashMap<>())
                            .merge(paidBy, share, Double::sum);
                }
            }
        }

        // ✅ Cancel out opposite debts (A owes B, B owes A)
        for (Long a : new ArrayList<>(pairwiseBalances.keySet())) {
            for (Long b : new ArrayList<>(pairwiseBalances.get(a).keySet())) {
                if (pairwiseBalances.containsKey(b) &&
                        pairwiseBalances.get(b).containsKey(a)) {

                    double ab = pairwiseBalances.get(a).get(b);
                    double ba = pairwiseBalances.get(b).get(a);

                    if (ab > ba) {
                        pairwiseBalances.get(a).put(b, ab - ba);
                        pairwiseBalances.get(b).remove(a);
                    } else if (ba > ab) {
                        pairwiseBalances.get(b).put(a, ba - ab);
                        pairwiseBalances.get(a).remove(b);
                    } else {
                        pairwiseBalances.get(a).remove(b);
                        pairwiseBalances.get(b).remove(a);
                    }
                }
            }
        }

        // ✅ Build settlements
        for (Map.Entry<Long, Map<Long, Double>> e1 : pairwiseBalances.entrySet()) {
            long fromId = e1.getKey();
            for (Map.Entry<Long, Double> e2 : e1.getValue().entrySet()) {
                long toId = e2.getKey();
                double amount = e2.getValue();

                if (amount > 0.01) { // ignore tiny floating differences
                    settlements.add(new Settlement(
                            repo.getParticipantById(fromId).getName(),
                            repo.getParticipantById(toId).getName(),
                            amount
                    ));
                }
            }
        }

        settlementAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTripExpenseLongClick(TripExpense expense) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repo.deleteTripExpense(expense.getId());
                    loadTripExpenses();
                    calculateSettlements();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
