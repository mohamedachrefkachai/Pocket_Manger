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
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import com.example.recyclersleam.sensors.movement;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class RevenueListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RevenueAdapter adapter;
    private FloatingActionButton fabAdd;
    private int userId;
    private MyDataBase db;
    private List<Revenue> revenueList = new ArrayList<>();

    // Undo / Sensor vars
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private movement shakeDetector;
    private Revenue lastDeletedRevenue;
    private long lastDeletedTime;
    private static final int UNDO_WINDOW_MS = 3000;

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

        setupShakeDetector();
        loadRevenues();
    }

    private void setupShakeDetector() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            shakeDetector = new movement();
            shakeDetector.setOnShakeListener(count -> {
                long now = System.currentTimeMillis();
                if (lastDeletedRevenue != null && (now - lastDeletedTime) < UNDO_WINDOW_MS) {
                    runOnUiThread(() -> restoreDeletedRevenue());
                }
            });
        }
    }

    private void restoreDeletedRevenue() {
        if (lastDeletedRevenue == null)
            return;

        final Revenue toRestore = lastDeletedRevenue; // capture
        lastDeletedRevenue = null; // Prevent double restore

        new Thread(() -> {
            // Need to insert it back. Since ID is auto-generated usually, we might get a
            // new ID.
            // But we want to restore data.
            // If we want exact ID, we might need to change logic, but for "undo",
            // re-inserting with same data is usually fine.
            // However, Room might ignore ID if it is 0. If we want to restore potentially
            // same ID we should check if we can.
            // For now let's just re-insert the object. If ID is set, Room might try to use
            // it or might fail if conflict mechanism not set.
            // Let's assume re-insertion is fine.
            // Make sure ID is 0 if we want it auto-generated, or keep ID if we want to try
            // to force it.
            // Often "Undo" just adds it back.
            // If the ID was auto-generated, we should probably set it to 0 to let it
            // generate a new one, OR try to insert with same ID.
            // Let's try to insert as is.
            db.RevenueDao().insert(toRestore);
            loadRevenues();
            runOnUiThread(() -> Toast.makeText(RevenueListActivity.this, "Revenu restauré", Toast.LENGTH_SHORT).show());
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeDetector);
            // We could clear undo here if we want to be strict, but keeping it is fine too.
        }
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
                    // Save for Undo
                    lastDeletedRevenue = revenue;
                    lastDeletedTime = System.currentTimeMillis();

                    new Thread(() -> {
                        db.RevenueDao().delete(revenue);
                        loadRevenues();
                        runOnUiThread(() -> Toast
                                .makeText(this, "Revenu supprimé. Secouez pour annuler (3s)", Toast.LENGTH_LONG)
                                .show());
                    }).start();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}
