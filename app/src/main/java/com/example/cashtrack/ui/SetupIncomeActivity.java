package com.example.cashtrack.ui;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cashtrack.R;
import com.example.cashtrack.db.DbRepo;

public class SetupIncomeActivity extends AppCompatActivity {

    private EditText etIncome;
    private Button btnSave;
    private DbRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_income);

        repo = new DbRepo(this);
        etIncome = findViewById(R.id.etIncome);
        btnSave = findViewById(R.id.btnSaveIncome);

        double currentIncome = repo.getMonthlyIncome();
        if (currentIncome > 0) etIncome.setText(String.valueOf(currentIncome));

        btnSave.setOnClickListener(v -> {
            String text = etIncome.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Enter monthly income", Toast.LENGTH_SHORT).show();
                return;
            }
            double income = Double.parseDouble(text);
            repo.setMonthlyIncome(income);
            Toast.makeText(this, "Income saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
