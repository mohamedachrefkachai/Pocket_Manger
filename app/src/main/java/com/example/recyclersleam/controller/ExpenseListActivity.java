package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclersleam.Adapter.ExpenseAdapter;
import com.example.recyclersleam.Entity.Expense;
import com.example.recyclersleam.Location.LocationStats;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class ExpenseListActivity extends AppCompatActivity {

    private RecyclerView rvExpenses;
    private FloatingActionButton fabAddExpense;
    private FloatingActionButton fabShowMap;
    private TextView tvTotalAmount;
    private TextView tvTransactionCount;
    private TextView tvTopLocations;
    private MyDataBase db;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        // Get User ID from Intent
        userId = getIntent().getIntExtra("userId", -1);

        rvExpenses = findViewById(R.id.rvExpenses);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        fabShowMap = findViewById(R.id.fabShowMap);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvTransactionCount = findViewById(R.id.tvTransactionCount);
        tvTopLocations = findViewById(R.id.tvTopLocations);

        db = MyDataBase.getAppDataBase(this);

        rvExpenses.setLayoutManager(new LinearLayoutManager(this));

        loadExpenses();

        // Ouvrir l'écran d'ajout
        fabAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseListActivity.this, AddExpenseActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        // OUVRIR LA CARTE GLOBALE
        fabShowMap.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseListActivity.this, GlobalMapActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void loadExpenses() {
        new Thread(() -> {
            // 1. Load Expenses List (Filtered by User)
            List<Expense> expenses;
            if (userId != -1) {
                expenses = db.ExpenseDao().getAllExpensesByUser(userId);
            } else {
                expenses = db.ExpenseDao().getAllExpenses(); // Fallback if no user
            }

            // 2. Calculate Global Stats
            double totalAmount = 0;
            for (Expense e : expenses) {
                totalAmount += e.getAmount();
            }
            int count = expenses.size();
            final double finalTotal = totalAmount;

            // 3. Load Location Stats
            List<LocationStats> topLocations = db.LocationDao().getTopSpendingLocations(); // Global stats for now

            runOnUiThread(() -> {
                ExpenseAdapter adapter = new ExpenseAdapter(expenses, db.LocationDao());
                rvExpenses.setAdapter(adapter);

                tvTotalAmount.setText(String.format(Locale.getDefault(), "%.2f DT", finalTotal));
                tvTransactionCount.setText(String.valueOf(count));

                if (topLocations != null && !topLocations.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (LocationStats stat : topLocations) {
                        if (stat.address != null && !stat.address.isEmpty()) {
                            sb.append("📍 ").append(stat.address)
                                    .append(": ")
                                    .append(String.format(Locale.getDefault(), "%.1f DT", stat.totalAmount))
                                    .append("\n");
                        }
                    }
                    tvTopLocations.setText(sb.toString().trim());
                } else {
                    tvTopLocations.setText("No location data recorded yet.");
                }
            });
        }).start();
    }
}
