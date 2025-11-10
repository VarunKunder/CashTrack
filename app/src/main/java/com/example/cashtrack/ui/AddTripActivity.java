package com.example.cashtrack.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cashtrack.R;
import com.example.cashtrack.db.DbRepo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class AddTripActivity extends AppCompatActivity {

    private DbRepo repo;
    private TextInputEditText etTripName;
    private LinearLayout llParticipants;
    private Button btnAddParticipant, btnSaveTrip;
    private List<EditText> participantEditTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        repo = new DbRepo(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTripName = findViewById(R.id.etTripName);
        llParticipants = findViewById(R.id.llParticipants);
        btnAddParticipant = findViewById(R.id.btnAddParticipant);
        btnSaveTrip = findViewById(R.id.btnSaveTrip);

        btnAddParticipant.setOnClickListener(v -> addParticipantEditText());
        btnSaveTrip.setOnClickListener(v -> saveTrip());

        // Add one participant field by default
        addParticipantEditText();
    }

    private void addParticipantEditText() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View participantView = inflater.inflate(R.layout.participant_edit_text, llParticipants, false);
        EditText editText = participantView.findViewById(R.id.etParticipantName);
        llParticipants.addView(participantView);
        participantEditTexts.add(editText);
    }

    private void saveTrip() {
        String tripName = etTripName.getText().toString().trim();
        if (tripName.isEmpty()) {
            Toast.makeText(this, "Enter a trip name", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> participantNames = new ArrayList<>();
        for (EditText editText : participantEditTexts) {
            String name = editText.getText().toString().trim();
            if (!name.isEmpty()) {
                participantNames.add(name);
            }
        }

        if (participantNames.size() < 2) {
            Toast.makeText(this, "Add at least two participants", Toast.LENGTH_SHORT).show();
            return;
        }

        long tripId = repo.addTrip(tripName);
        if (tripId != -1) {
            for (String name : participantNames) {
                repo.addParticipant(tripId, name);
            }
            Toast.makeText(this, "Trip created successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error creating trip", Toast.LENGTH_SHORT).show();
        }
    }
}
