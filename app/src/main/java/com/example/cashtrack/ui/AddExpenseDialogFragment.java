package com.example.cashtrack.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.cashtrack.R;
import com.example.cashtrack.db.DbRepo;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseDialogFragment extends DialogFragment {

    private AutoCompleteTextView spCategory;
    private TextInputEditText etAmount, etNote;
    private Button btnSave;
    private DbRepo repo;
    private OnExpenseAddedListener listener;

    public interface OnExpenseAddedListener {
        void onExpenseAdded();
    }

    public void setOnExpenseAddedListener(OnExpenseAddedListener listener) {
        this.listener = listener;
    }

    private final String[] categories = {"Home", "Food", "Transport", "Bills", "Shopping", "Other"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        repo = new DbRepo(getContext());

        spCategory = view.findViewById(R.id.spCategory);
        etAmount = view.findViewById(R.id.etAmount);
        etNote = view.findViewById(R.id.etNote);
        btnSave = view.findViewById(R.id.btnSaveExpense);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories);
        spCategory.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            String cat = spCategory.getText().toString();
            String amtStr = etAmount.getText().toString().trim();

            if (cat.isEmpty()) {
                Toast.makeText(getContext(), "Select a category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amtStr.isEmpty()) {
                Toast.makeText(getContext(), "Enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amtStr);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String note = etNote.getText().toString();
            repo.addExpense(cat, amount, date, note);
            Toast.makeText(getContext(), "Expense saved", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onExpenseAdded();
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
    }
}
