package com.example.cashtrack.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.cashtrack.model.Expense;
import com.example.cashtrack.model.Participant;
import com.example.cashtrack.model.Trip;

import java.text.SimpleDateFormat;
import java.util.*;

public class DbRepo {
    private final DatabaseHelper dbHelper;

    public DbRepo(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ===== Trip methods =====
    public long addTrip(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_TRIP_NAME, name);
        return db.insert(DatabaseHelper.TABLE_TRIPS, null, cv);
    }

    public long addParticipant(long tripId, String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PARTICIPANT_TRIP_ID, tripId);
        cv.put(DatabaseHelper.COL_PARTICIPANT_NAME, name);
        return db.insert(DatabaseHelper.TABLE_PARTICIPANTS, null, cv);
    }

    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_TRIPS, null, null, null, null, null, null);
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
            String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_NAME));
            trips.add(new Trip(id, name));
        }
        c.close();
        return trips;
    }

    public Trip getTrip(long tripId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_TRIPS, null, DatabaseHelper.COL_TRIP_ID + "=?", new String[]{String.valueOf(tripId)}, null, null, null);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_NAME));
            c.close();
            return new Trip(tripId, name);
        }
        c.close();
        return null;
    }

    public List<Participant> getTripParticipants(long tripId) {
        List<Participant> participants = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_PARTICIPANTS, null, DatabaseHelper.COL_PARTICIPANT_TRIP_ID + "=?", new String[]{String.valueOf(tripId)}, null, null, null);
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PARTICIPANT_ID));
            String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PARTICIPANT_NAME));
            participants.add(new com.example.cashtrack.model.Participant(id, tripId, name));
        }
        c.close();
        return participants;
    }

    public com.example.cashtrack.model.Participant getParticipantByName(long tripId, String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_PARTICIPANTS, null, DatabaseHelper.COL_PARTICIPANT_TRIP_ID + "=? AND " + DatabaseHelper.COL_PARTICIPANT_NAME + "=?", new String[]{String.valueOf(tripId), name}, null, null, null);
        if (c.moveToFirst()) {
            long id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PARTICIPANT_ID));
            c.close();
            return new com.example.cashtrack.model.Participant(id, tripId, name);
        }
        c.close();
        return null;
    }

    public long addTripExpense(long tripId, String description, double amount, String date, long paidByParticipantId, List<Long> sharedWithParticipantIds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues expenseCv = new ContentValues();
            expenseCv.put(DatabaseHelper.COL_TRIP_EXPENSE_TRIP_ID, tripId);
            expenseCv.put(DatabaseHelper.COL_TRIP_EXPENSE_DESCRIPTION, description);
            expenseCv.put(DatabaseHelper.COL_TRIP_EXPENSE_AMOUNT, amount);
            expenseCv.put(DatabaseHelper.COL_TRIP_EXPENSE_DATE, date);
            long expenseId = db.insert(DatabaseHelper.TABLE_TRIP_EXPENSES, null, expenseCv);

            if (expenseId != -1) {
                ContentValues payerCv = new ContentValues();
                payerCv.put(DatabaseHelper.COL_PAYER_EXPENSE_ID, expenseId);
                payerCv.put(DatabaseHelper.COL_PAYER_PARTICIPANT_ID, paidByParticipantId);
                payerCv.put(DatabaseHelper.COL_AMOUNT_PAID, amount);
                db.insert(DatabaseHelper.TABLE_EXPENSE_PAYERS, null, payerCv);

                for (long participantId : sharedWithParticipantIds) {
                    ContentValues shareCv = new ContentValues();
                    shareCv.put(DatabaseHelper.COL_SHARE_EXPENSE_ID, expenseId);
                    shareCv.put(DatabaseHelper.COL_SHARE_PARTICIPANT_ID, participantId);
                    db.insert(DatabaseHelper.TABLE_EXPENSE_SHARES, null, shareCv);
                }
            }
            db.setTransactionSuccessful();
            return expenseId;
        } finally {
            db.endTransaction();
        }
    }

    public List<com.example.cashtrack.model.TripExpense> getTripExpenses(long tripId) {
        List<com.example.cashtrack.model.TripExpense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT te.*, p.name as paid_by_name, p.id as paid_by_id FROM " + DatabaseHelper.TABLE_TRIP_EXPENSES + " te " +
                "INNER JOIN " + DatabaseHelper.TABLE_EXPENSE_PAYERS + " ep ON te." + DatabaseHelper.COL_TRIP_EXPENSE_ID + " = ep." + DatabaseHelper.COL_PAYER_EXPENSE_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_PARTICIPANTS + " p ON ep." + DatabaseHelper.COL_PAYER_PARTICIPANT_ID + " = p." + DatabaseHelper.COL_PARTICIPANT_ID + " " +
                "WHERE te." + DatabaseHelper.COL_TRIP_EXPENSE_TRIP_ID + " = ?";
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(tripId)});

        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_EXPENSE_ID));
            String description = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_EXPENSE_DESCRIPTION));
            double amount = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_EXPENSE_AMOUNT));
            String date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_EXPENSE_DATE));
            long paidById = c.getLong(c.getColumnIndexOrThrow("paid_by_id"));
            String paidByName = c.getString(c.getColumnIndexOrThrow("paid_by_name"));
            com.example.cashtrack.model.Participant paidBy = new com.example.cashtrack.model.Participant(paidById, tripId, paidByName);

            List<com.example.cashtrack.model.Participant> sharedWith = new ArrayList<>();
            Cursor sharesCursor = db.query(DatabaseHelper.TABLE_EXPENSE_SHARES + " es " +
                            "INNER JOIN " + DatabaseHelper.TABLE_PARTICIPANTS + " p ON es." + DatabaseHelper.COL_SHARE_PARTICIPANT_ID + " = p." + DatabaseHelper.COL_PARTICIPANT_ID,
                    new String[]{"p." + DatabaseHelper.COL_PARTICIPANT_ID, "p." + DatabaseHelper.COL_PARTICIPANT_NAME},
                    "es." + DatabaseHelper.COL_SHARE_EXPENSE_ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null);

            while (sharesCursor.moveToNext()) {
                long participantId = sharesCursor.getLong(0);
                String participantName = sharesCursor.getString(1);
                sharedWith.add(new com.example.cashtrack.model.Participant(participantId, tripId, participantName));
            }
            sharesCursor.close();
            expenses.add(new com.example.cashtrack.model.TripExpense(id, tripId, description, amount, date, paidBy, sharedWith));
        }
        c.close();
        return expenses;
    }

    public com.example.cashtrack.model.Participant getParticipantById(long participantId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_PARTICIPANTS, null, DatabaseHelper.COL_PARTICIPANT_ID + "=?", new String[]{String.valueOf(participantId)}, null, null, null);
        if (c.moveToFirst()) {
            long tripId = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PARTICIPANT_TRIP_ID));
            String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PARTICIPANT_NAME));
            c.close();
            return new com.example.cashtrack.model.Participant(participantId, tripId, name);
        }
        c.close();
        return null;
    }

    public void deleteTrip(long tripId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Get all expense IDs for the trip
            List<Long> expenseIds = new ArrayList<>();
            Cursor c = db.query(DatabaseHelper.TABLE_TRIP_EXPENSES, new String[]{DatabaseHelper.COL_TRIP_EXPENSE_ID}, DatabaseHelper.COL_TRIP_EXPENSE_TRIP_ID + "=?", new String[]{String.valueOf(tripId)}, null, null, null);
            while (c.moveToNext()) {
                expenseIds.add(c.getLong(0));
            }
            c.close();

            // Delete from expense_shares and expense_payers for each expense
            for (long expenseId : expenseIds) {
                db.delete(DatabaseHelper.TABLE_EXPENSE_SHARES, DatabaseHelper.COL_SHARE_EXPENSE_ID + "=?", new String[]{String.valueOf(expenseId)});
                db.delete(DatabaseHelper.TABLE_EXPENSE_PAYERS, DatabaseHelper.COL_PAYER_EXPENSE_ID + "=?", new String[]{String.valueOf(expenseId)});
            }

            // Delete from trip_expenses
            db.delete(DatabaseHelper.TABLE_TRIP_EXPENSES, DatabaseHelper.COL_TRIP_EXPENSE_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});

            // Delete from participants
            db.delete(DatabaseHelper.TABLE_PARTICIPANTS, DatabaseHelper.COL_PARTICIPANT_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});

            // Delete from trips
            db.delete(DatabaseHelper.TABLE_TRIPS, DatabaseHelper.COL_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteTripExpense(long expenseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete from expense_shares
            db.delete(DatabaseHelper.TABLE_EXPENSE_SHARES, DatabaseHelper.COL_SHARE_EXPENSE_ID + "=?", new String[]{String.valueOf(expenseId)});

            // Delete from expense_payers
            db.delete(DatabaseHelper.TABLE_EXPENSE_PAYERS, DatabaseHelper.COL_PAYER_EXPENSE_ID + "=?", new String[]{String.valueOf(expenseId)});

            // Delete from trip_expenses
            db.delete(DatabaseHelper.TABLE_TRIP_EXPENSES, DatabaseHelper.COL_TRIP_EXPENSE_ID + "=?", new String[]{String.valueOf(expenseId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public double getTotalSpentForTrip(long tripId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + DatabaseHelper.COL_TRIP_EXPENSE_AMOUNT + ") FROM " +
                DatabaseHelper.TABLE_TRIP_EXPENSES + " WHERE " + DatabaseHelper.COL_TRIP_EXPENSE_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        return total;
    }



    // ===== Income methods =====
    public void setMonthlyIncome(double income) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_MONTHLY_INCOME, income);
        db.update(DatabaseHelper.TABLE_USER, cv, DatabaseHelper.COL_USER_ID + "=?", new String[]{"1"});
    }

    public double getMonthlyIncome() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + DatabaseHelper.COL_MONTHLY_INCOME +
                " FROM " + DatabaseHelper.TABLE_USER + " WHERE " + DatabaseHelper.COL_USER_ID + "=1", null);
        double income = 0;
        if (c.moveToFirst()) income = c.getDouble(0);
        c.close();
        return income;
    }

    // ===== Expense methods =====
    public long addExpense(String category, double amount, String date, String note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_CATEGORY, category);
        cv.put(DatabaseHelper.COL_AMOUNT, amount);
        cv.put(DatabaseHelper.COL_DATE, date);
        cv.put(DatabaseHelper.COL_NOTE, note);
        return db.insert(DatabaseHelper.TABLE_EXPENSES, null, cv);
    }

    public void addDummyExpenses() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_EXPENSES, null, null); // Clear existing expenses

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // Add expenses for the last 7 days
        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -i);
            addExpense("Food", 300 - (i * 20), sdf.format(cal.getTime()), "Lunch");
            addExpense("Transport", 200 - (i * 15), sdf.format(cal.getTime()), "Bus ticket");
        }
        // Add a larger expense for today
        addExpense("Shopping", 500, sdf.format(new Date()), "New shoes");
    }

    public List<Expense> getRecentExpenses(int limit) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_EXPENSES, null, null, null, null, null, DatabaseHelper.COL_EXPENSE_ID + " DESC", String.valueOf(limit));
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_ID));
            String category = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY));
            double amount = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT));
            String date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));
            String note = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOTE));
            expenses.add(new Expense(id, category, amount, date, note));
        }
        c.close();
        return expenses;
    }

    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_EXPENSES, null, null, null, null, null, DatabaseHelper.COL_DATE + " DESC");
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_ID));
            String category = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY));
            double amount = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT));
            String date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));
            String note = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOTE));
            expenses.add(new Expense(id, category, amount, date, note));
        }
        c.close();
        return expenses;
    }

    public Map<String, Double> getExpensesForLast7Days() {
        Map<String, Double> dailyTotals = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -i);
            dailyTotals.put(sdf.format(cal.getTime()), 0.0);
        }

        cal.setTime(new Date());
        cal.add(Calendar.DATE, -6);
        String sevenDaysAgo = sdf.format(cal.getTime());

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + DatabaseHelper.COL_DATE + ", SUM(" + DatabaseHelper.COL_AMOUNT + ") " +
                        "FROM " + DatabaseHelper.TABLE_EXPENSES +
                        " WHERE date(" + DatabaseHelper.COL_DATE + ") >= date(?) " +
                        "GROUP BY " + DatabaseHelper.COL_DATE,
                new String[]{sevenDaysAgo});

        while (c.moveToNext()) {
            dailyTotals.put(c.getString(0), c.getDouble(1));
        }
        c.close();
        return dailyTotals;
    }

    public double getTotalExpensesForMonth(String yyyyMM) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + DatabaseHelper.COL_AMOUNT + ") FROM " +
                DatabaseHelper.TABLE_EXPENSES + " WHERE substr(" + DatabaseHelper.COL_DATE + ",1,7)=?", new String[]{yyyyMM});
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        return total;
    }

    public Map<String, Double> getCategoryBreakdownForMonth(String yyyyMM) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + DatabaseHelper.COL_CATEGORY + ", SUM(" + DatabaseHelper.COL_AMOUNT + ") " +
                        "FROM " + DatabaseHelper.TABLE_EXPENSES +
                        " WHERE substr(" + DatabaseHelper.COL_DATE + ",1,7)=? GROUP BY " + DatabaseHelper.COL_CATEGORY,
                new String[]{yyyyMM});
        Map<String, Double> map = new HashMap<>();
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getDouble(1));
        }
        c.close();
        return map;
    }

    public Map<String, Double> getAllTimeCategoryBreakdown() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + DatabaseHelper.COL_CATEGORY + ", SUM(" + DatabaseHelper.COL_AMOUNT + ") " +
                        "FROM " + DatabaseHelper.TABLE_EXPENSES +
                        " GROUP BY " + DatabaseHelper.COL_CATEGORY,
                null);
        Map<String, Double> map = new HashMap<>();
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getDouble(1));
        }
        c.close();
        return map;
    }

    public List<double[]> getMonthlyTrendLast6() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT substr(" + DatabaseHelper.COL_DATE + ",1,7) m, SUM(" + DatabaseHelper.COL_AMOUNT + ") " +
                "FROM " + DatabaseHelper.TABLE_EXPENSES +
                " GROUP BY m ORDER BY m DESC LIMIT 6", null);

        List<String> monthsDesc = new ArrayList<>();
        List<Double> totalsDesc = new ArrayList<>();
        while (c.moveToNext()) {
            monthsDesc.add(0, c.getString(0));
            totalsDesc.add(0, c.getDouble(1));
        }
        c.close();

        List<double[]> result = new ArrayList<>();
        for (int i = 0; i < totalsDesc.size(); i++) {
            result.add(new double[]{i + 1, totalsDesc.get(i)});
        }
        return result;
    }
}
