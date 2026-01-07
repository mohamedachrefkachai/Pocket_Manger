package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.recyclersleam.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import com.example.recyclersleam.sensors.movement;
import com.example.recyclersleam.Util.MyDataBase;
import com.example.recyclersleam.Entity.Revenue;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    // Infos utilisateur
    private String userName;
    private String userEmail;
    private int userId;

    // Global Sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private movement shakeDetector;
    private MyDataBase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupérer les extras envoyés depuis Login
        userName = getIntent().getStringExtra("name");
        userEmail = getIntent().getStringExtra("email");
        userId = getIntent().getIntExtra("userId", -1);
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_nav);

        // Init database for global add
        db = MyDataBase.getAppDataBase(this);
        setupGlobalSensor();
        // Fragment par défaut
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setUserId(userId);
        loadFragment(homeFragment);
        toolbar.setTitle("Accueil");

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.nav_home) {
                HomeFragment hFragment = new HomeFragment();
                hFragment.setUserId(userId);
                fragment = hFragment;
                toolbar.setTitle("Accueil");

            } else if (item.getItemId() == R.id.nav_profile) {
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setUserData(userId, userName, userEmail);

                fragment = profileFragment;
                toolbar.setTitle("Profil");

            } else if (item.getItemId() == R.id.nav_shopping) {
                ShoppingListFragment shoppingFragment = new ShoppingListFragment();
                shoppingFragment.setUserId(userId);
                fragment = shoppingFragment;
                toolbar.setTitle("Courses");
            } else if (item.getItemId() == R.id.nav_expenses) {
                Intent intent = new Intent(MainActivity.this, ExpenseListActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_logout) {
                // 🔹 Déconnexion
                Intent intent = new Intent(MainActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true; // on retourne true pour dire que l'action est gérée
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }

            return false;
        });

    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setupGlobalSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            shakeDetector = new movement();
            shakeDetector.setOnShakeListener(count -> {
                // Check if HomeFragment is active
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof HomeFragment && currentFragment.isVisible()) {
                    // Do nothing, HomeFragment handles its own shake (Tips)
                    return;
                }

                // Else show Quick Add
                showAddRevenueDialog();
            });
        }
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
        }
    }

    private void showAddRevenueDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajout Rapide Revenu");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_revenue, null);

        final EditText etSource = view.findViewById(R.id.etSource);
        final EditText etAmount = view.findViewById(R.id.etAmount);
        final EditText etDate = view.findViewById(R.id.etDate);

        // Pre-fill date
        etDate.setText(new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                .format(new Date()));

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
                Revenue newRevenue = new Revenue(userId, source, amount, date);
                db.RevenueDao().insert(newRevenue);
                runOnUiThread(() -> Toast.makeText(this, "Revenu ajouté !", Toast.LENGTH_SHORT).show());
            }).start();
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

}
