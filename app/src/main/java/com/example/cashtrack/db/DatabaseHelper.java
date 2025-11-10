package com.example.cashtrack.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "expenses.db";
    public static final int DB_VERSION = 2;

    // Tables
    public static final String TABLE_USER = "user_profile";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_TRIPS = "trips";
    public static final String TABLE_PARTICIPANTS = "participants";
    public static final String TABLE_TRIP_EXPENSES = "trip_expenses";
    public static final String TABLE_EXPENSE_PAYERS = "expense_payers";
    public static final String TABLE_EXPENSE_SHARES = "expense_shares";


    // User columns
    public static final String COL_USER_ID = "id";
    public static final String COL_MONTHLY_INCOME = "monthly_income";

    // Expense columns
    public static final String COL_EXPENSE_ID = "id";
    public static final String COL_CATEGORY = "category";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_DATE = "date";
    public static final String COL_NOTE = "note";

    // Trip columns
    public static final String COL_TRIP_ID = "id";
    public static final String COL_TRIP_NAME = "name";

    // Participant columns
    public static final String COL_PARTICIPANT_ID = "id";
    public static final String COL_PARTICIPANT_TRIP_ID = "trip_id";
    public static final String COL_PARTICIPANT_NAME = "name";

    // Trip Expense columns
    public static final String COL_TRIP_EXPENSE_ID = "id";
    public static final String COL_TRIP_EXPENSE_TRIP_ID = "trip_id";
    public static final String COL_TRIP_EXPENSE_DESCRIPTION = "description";
    public static final String COL_TRIP_EXPENSE_AMOUNT = "amount";
    public static final String COL_TRIP_EXPENSE_DATE = "date";

    // Expense Payer columns
    public static final String COL_PAYER_ID = "id";
    public static final String COL_PAYER_EXPENSE_ID = "expense_id";
    public static final String COL_PAYER_PARTICIPANT_ID = "participant_id";
    public static final String COL_AMOUNT_PAID = "amount_paid";

    // Expense Share columns
    public static final String COL_SHARE_ID = "id";
    public static final String COL_SHARE_EXPENSE_ID = "expense_id";
    public static final String COL_SHARE_PARTICIPANT_ID = "participant_id";


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE " + TABLE_USER + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY CHECK(" + COL_USER_ID + "=1), " +
                COL_MONTHLY_INCOME + " REAL NOT NULL DEFAULT 0)";
        db.execSQL(createUserTable);

        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COL_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CATEGORY + " TEXT NOT NULL, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_DATE + " TEXT NOT NULL, " +
                COL_NOTE + " TEXT)";
        db.execSQL(createExpensesTable);

        db.execSQL("INSERT INTO " + TABLE_USER + " VALUES (1, 0)");

        // Trip Manager Tables
        String createTripsTable = "CREATE TABLE " + TABLE_TRIPS + " (" +
                COL_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRIP_NAME + " TEXT NOT NULL)";
        db.execSQL(createTripsTable);

        String createParticipantsTable = "CREATE TABLE " + TABLE_PARTICIPANTS + " (" +
                COL_PARTICIPANT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PARTICIPANT_TRIP_ID + " INTEGER NOT NULL, " +
                COL_PARTICIPANT_NAME + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_PARTICIPANT_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COL_TRIP_ID + "))";
        db.execSQL(createParticipantsTable);

        String createTripExpensesTable = "CREATE TABLE " + TABLE_TRIP_EXPENSES + " (" +
                COL_TRIP_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRIP_EXPENSE_TRIP_ID + " INTEGER NOT NULL, " +
                COL_TRIP_EXPENSE_DESCRIPTION + " TEXT NOT NULL, " +
                COL_TRIP_EXPENSE_AMOUNT + " REAL NOT NULL, " +
                COL_TRIP_EXPENSE_DATE + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_TRIP_EXPENSE_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COL_TRIP_ID + "))";
        db.execSQL(createTripExpensesTable);

        String createExpensePayersTable = "CREATE TABLE " + TABLE_EXPENSE_PAYERS + " (" +
                COL_PAYER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PAYER_EXPENSE_ID + " INTEGER NOT NULL, " +
                COL_PAYER_PARTICIPANT_ID + " INTEGER NOT NULL, " +
                COL_AMOUNT_PAID + " REAL NOT NULL, " +
                "FOREIGN KEY(" + COL_PAYER_EXPENSE_ID + ") REFERENCES " + TABLE_TRIP_EXPENSES + "(" + COL_TRIP_EXPENSE_ID + "), " +
                "FOREIGN KEY(" + COL_PAYER_PARTICIPANT_ID + ") REFERENCES " + TABLE_PARTICIPANTS + "(" + COL_PARTICIPANT_ID + "))";
        db.execSQL(createExpensePayersTable);

        String createExpenseSharesTable = "CREATE TABLE " + TABLE_EXPENSE_SHARES + " (" +
                COL_SHARE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SHARE_EXPENSE_ID + " INTEGER NOT NULL, " +
                COL_SHARE_PARTICIPANT_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_SHARE_EXPENSE_ID + ") REFERENCES " + TABLE_TRIP_EXPENSES + "(" + COL_TRIP_EXPENSE_ID + "), " +
                "FOREIGN KEY(" + COL_SHARE_PARTICIPANT_ID + ") REFERENCES " + TABLE_PARTICIPANTS + "(" + COL_PARTICIPANT_ID + "))";
        db.execSQL(createExpenseSharesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_SHARES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_PAYERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTICIPANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }
}
