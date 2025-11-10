package com.example.cashtrack.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cashtrack.R;
import com.example.cashtrack.db.DbRepo;
import com.example.cashtrack.model.Participant;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTripExpenseActivity extends AppCompatActivity {

    private long tripId;
    private DbRepo repo;
    private TextInputEditText etDescription, etAmount;
    private Spinner spPaidBy;
    private Button btnSharedWith, btnSave;
    private List<Participant> participants;
    private List<Long> sharedWithParticipantIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip_expense);

        repo = new DbRepo(this);
        tripId = getIntent().getLongExtra("TRIP_ID", -1);
        participants = repo.getTripParticipants(tripId);

        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        spPaidBy = findViewById(R.id.spPaidBy);
        btnSharedWith = findViewById(R.id.btnSharedWith);
        btnSave = findViewById(R.id.btnSave);

        setupPaidBySpinner();
        setupSharedWithButton();

        btnSave.setOnClickListener(v -> saveTripExpense());
    }

    private void setupPaidBySpinner() {
        List<String> participantNames = new ArrayList<>();
        for (Participant p : participants) {
            participantNames.add(p.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, participantNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaidBy.setAdapter(adapter);
    }

    private void setupSharedWithButton() {
        btnSharedWith.setOnClickListener(v -> {
            showSharedWithDialog();
        });
    }

    private void showSharedWithDialog() {
        String[] participantNames = new String[participants.size()];
        boolean[] checkedItems = new boolean[participants.size()];
        for (int i = 0; i < participants.size(); i++) {
            participantNames[i] = participants.get(i).getName();
            checkedItems[i] = sharedWithParticipantIds.contains(participants.get(i).getId());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shared With")
                .setMultiChoiceItems(participantNames, checkedItems, (dialog, which, isChecked) -> {
                    long participantId = participants.get(which).getId();
                    if (isChecked) {
                        sharedWithParticipantIds.add(participantId);
                    } else {
                        sharedWithParticipantIds.remove(participantId);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    // Update button text
                    btnSharedWith.setText("Shared with " + sharedWithParticipantIds.size() + " people");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveTripExpense() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        if (description.isEmpty() || amountStr.isEmpty() || sharedWithParticipantIds.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select participants", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        long paidByParticipantId = participants.get(spPaidBy.getSelectedItemPosition()).getId();

        repo.addTripExpense(tripId, description, amount, date, paidByParticipantId, sharedWithParticipantIds);
        Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
        finish();
    }
}