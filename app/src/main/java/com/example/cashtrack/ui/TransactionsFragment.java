package com.example.cashtrack.ui;

import android.graphics.Color;
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
import com.example.cashtrack.adapter.TransactionAdapter;
import com.example.cashtrack.db.DbRepo;
import com.example.cashtrack.model.Expense;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionsFragment extends Fragment {

    private RecyclerView rvAllTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Expense> expenseList = new ArrayList<>();
    private DbRepo repo;
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        repo = new DbRepo(getContext());
        rvAllTransactions = view.findViewById(R.id.rvAllTransactions);
        pieChart = view.findViewById(R.id.pieChartAll);

        setupRecyclerView();
        loadAllTransactions();
        setupPieChart();

        return view;
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(expenseList);
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
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
