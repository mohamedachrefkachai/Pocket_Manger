package com.example.recyclersleam.controller;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclersleam.Entity.Revenue;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RevenueListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RevenueAdapter adapter;
    private FloatingActionButton fabAdd;
    private int userId;
    private MyDataBase db;
    private List<Revenue> revenueList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_list);

        userId = getIntent().getIntExtra("USER_ID", -1);
        db = MyDataBase.getAppDataBase(this);

        if (userId == -1) {
            Toast.makeText(this, "Erreur utilisateur", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewRevenues);
        fabAdd = findViewById(R.id.fabAddRevenue);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        loadRevenues();
    }

    private void loadRevenues() {
        new Thread(() -> {
            revenueList = db.RevenueDao().getAllByUser(userId);
            runOnUiThread(() -> {
                adapter = new RevenueAdapter(revenueList, new RevenueAdapter.OnRevenueClickListener() {
                    @Override
                    public void onDeleteClick(Revenue revenue) {
                        showDeleteConfirmation(revenue);
                    }

                    @Override
                    public void onItemClick(Revenue revenue) {
                        showAddEditDialog(revenue);
                    }
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void showAddEditDialog(Revenue revenue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(revenue == null ? "Ajouter Revenu" : "Modifier Revenu");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_revenue, null);

        final EditText etSource = view.findViewById(R.id.etSource);
        final EditText etAmount = view.findViewById(R.id.etAmount);
        final EditText etDate = view.findViewById(R.id.etDate);

        // Pre-fill date
        etDate.setText(new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                .format(new java.util.Date()));

        if (revenue != null) {
            etSource.setText(revenue.getSource());
            etAmount.setText(String.valueOf(revenue.getAmount()));
            etDate.setText(revenue.getDate());
        }

        builder.setView(view);

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String source = etSource.getText().toString();
            String amountStr = etAmount.getText().toString();
            String date = etDate.getText().toString();

            if (TextUtils.isEmpty(source) || TextUtils.isEmpty(amountStr)) {
                Toast.makeText(this, "Champs requis", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            new Thread(() -> {
                if (revenue == null) {
                    Revenue newRevenue = new Revenue(userId, source, amount, date);
                    db.RevenueDao().insert(newRevenue);
                } else {
                    revenue.setSource(source);
                    revenue.setAmount(amount);
                    revenue.setDate(date);
                    db.RevenueDao().update(revenue);
                }
                loadRevenues();
            }).start();
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showDeleteConfirmation(Revenue revenue) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer ?")
                .setMessage("Voulez-vous supprimer ce revenu ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    new Thread(() -> {
                        db.RevenueDao().delete(revenue);
                        loadRevenues();
                    }).start();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}
