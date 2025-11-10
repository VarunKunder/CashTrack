package com.example.cashtrack.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.cashtrack.MainActivity;
import com.example.cashtrack.R;
import com.example.cashtrack.adapter.TransactionAdapter;
import com.example.cashtrack.db.DbRepo;
import com.example.cashtrack.model.Expense;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllTransactionsActivity extends AppCompatActivity {

    private RecyclerView rvAllTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Expense> expenseList = new ArrayList<>();
    private DbRepo repo;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);

        repo = new DbRepo(this);
        rvAllTransactions = findViewById(R.id.rvAllTransactions);
        pieChart = findViewById(R.id.pieChartAll);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupRecyclerView();
        loadAllTransactions();
        setupPieChart();

        bottomNavigationView.setSelectedItemId(R.id.navigation_transactions);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (itemId == R.id.navigation_trips) {
                startActivity(new Intent(this, TripListActivity.class));
            } else if (itemId == R.id.navigation_add) {
                new AddExpenseDialogFragment().show(getSupportFragmentManager(), "AddExpense");
                return false;
            } else if (itemId == R.id.navigation_transactions) {
                // Already on this screen
            }
            return true;
        });
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(expenseList);
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvAllTransactions.setAdapter(transactionAdapter);
    }

    private void loadAllTransactions() {
        expenseList.clear();
        expenseList.addAll(repo.getAllExpenses());
        transactionAdapter.notifyDataSetChanged();
    }

    private void setupPieChart() {
        Map<String, Double> categoryData = repo.getAllTimeCategoryBreakdown();

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "All Time Expenses");
        dataSet.setColors(getResources().getColor(R.color.home_expense), getResources().getColor(R.color.charity_expense), getResources().getColor(R.color.food_expense), getResources().getColor(R.color.other_expense), getResources().getColor(R.color.accent_pink));
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setTextColor(Color.WHITE);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setDrawHoleEnabled(false);
        pieChart.invalidate();
    }
}
