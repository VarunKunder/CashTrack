package com.example.cashtrack.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cashtrack.R;
import com.example.cashtrack.adapter.TransactionAdapter;
import com.example.cashtrack.db.DbRepo;
import com.example.cashtrack.model.Expense;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.*;
import com.example.cashtrack.ui.AddExpenseDialogFragment;

public class DashboardFragment extends Fragment implements AddExpenseDialogFragment.OnExpenseAddedListener {

    private DbRepo repo;
    private TextView tvIncome, tvExpense, tvBalance;
    private BarChart barChart;
    private PieChart pieChart;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Expense> expenseList = new ArrayList<>();
    private boolean isCategoryBreakdown = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        repo = new DbRepo(getContext());

        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvBalance = view.findViewById(R.id.tvBalance);
        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        FloatingActionButton fabAddExpense = view.findViewById(R.id.fabAddExpense);

        setupRecyclerView();
        setupPieChartListener();

        fabAddExpense.setOnClickListener(v -> {
            AddExpenseDialogFragment dialog = new AddExpenseDialogFragment();
            dialog.setOnExpenseAddedListener(this);
            dialog.show(getParentFragmentManager(), "AddExpense");
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDashboard();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(expenseList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void refreshDashboard() {
        isCategoryBreakdown = false;
        String yyyyMM = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        double income = repo.getMonthlyIncome();
        if (income == 0) {
            // If income is not set, launch SetupIncomeActivity
            startActivity(new Intent(getContext(), SetupIncomeActivity.class));
            return; // Stop further execution
        }
        double spent = repo.getTotalExpensesForMonth(yyyyMM);
        double balance = Math.max(income - spent, 0);

        tvIncome.setText("₹" + String.format(Locale.getDefault(), "%,.0f", income));
        tvExpense.setText("₹" + String.format(Locale.getDefault(), "%,.0f", spent));
        tvBalance.setText("₹" + String.format(Locale.getDefault(), "%,.0f", balance));


        setupBarChart();
        setupIncomeExpensePieChart(income, spent);
        loadRecentTransactions();
    }

    private void setupBarChart() {
        Map<String, Double> dailyTotals = repo.getExpensesForLast7Days();
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue().floatValue()));
            labels.add(entry.getKey().substring(5)); // "MM-dd"
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Expenses");
        dataSet.setColor(getResources().getColor(R.color.accent_yellow));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);

        barChart.invalidate();
    }

    private void setupPieChartListener() {
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (isCategoryBreakdown) {
                    isCategoryBreakdown = false;
                    refreshDashboard(); // Revert to original chart
                } else {
                    PieEntry pieEntry = (PieEntry) e;
                    if (pieEntry.getLabel().equals("Spent")) {
                        isCategoryBreakdown = true;
                        setupCategoryBreakdownPieChart();
                    }
                }
            }

            @Override
            public void onNothingSelected() {
                if (isCategoryBreakdown) {
                    isCategoryBreakdown = false;
                    refreshDashboard();
                }
            }
        });
    }

    private void setupIncomeExpensePieChart(double income, double spent) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) spent, "Spent"));
        entries.add(new PieEntry((float) (income - spent), "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getResources().getColor(R.color.expenses), getResources().getColor(R.color.income));
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(70f);
        if (income > 0) {
            pieChart.setCenterText(String.format(Locale.getDefault(), "%.0f%% Spent", (spent / income) * 100));
        } else {
            pieChart.setCenterText("0% Spent");
        }
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(18f);
        pieChart.invalidate();
    }

    private void setupCategoryBreakdownPieChart() {
        String yyyyMM = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        Map<String, Double> categoryData = repo.getCategoryBreakdownForMonth(yyyyMM);

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getResources().getColor(R.color.home_expense), getResources().getColor(R.color.charity_expense), getResources().getColor(R.color.food_expense), getResources().getColor(R.color.other_expense));
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setCenterText("Categories");
        pieChart.invalidate();
    }


    private void loadRecentTransactions() {
        expenseList.clear();
        expenseList.addAll(repo.getRecentExpenses(5)); // Limit to 5 for a cleaner look
        transactionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onExpenseAdded() {
        refreshDashboard();
    }
}
