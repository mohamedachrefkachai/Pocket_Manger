package com.example.recyclersleam.controller;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class UserListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    UserAdapter userAdapter;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        // 🔹 Toolbar
        toolbar = findViewById(R.id.toolbar_admin);
        setSupportActionBar(toolbar);

        // 🔹 DrawerLayout et NavigationView
        drawerLayout = findViewById(R.id.drawer_layout_admin);
        navigationView = findViewById(R.id.nav_view_admin);

        // 🔹 Toggle pour ouvrir/fermer le Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 🔹 Gestion des clics dans le menu avec if/else
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_budget) {
                // Ouvrir activité Budget Admin si besoin

            } else if (id == R.id.nav_settings) {
                // Ouvrir activité Paramètres Admin si besoin
            } else if (id == R.id.nav_logout) {
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAddUser = findViewById(R.id.fabAddUser);
        fabAddUser.setOnClickListener(v -> showAddUserDialog());

        loadUsers();
        setupCharts();
    }

    private void setupCharts() {
        com.github.mikephil.charting.charts.PieChart genderChart = findViewById(R.id.genderChart);
        com.github.mikephil.charting.charts.BarChart ageChart = findViewById(R.id.ageChart);

        new Thread(() -> {
            com.example.recyclersleam.Util.MyDataBase db = MyDataBase.getAppDataBase(this);
            List<User> users = db.UserDao().getAllUsers();

            // 1. Gender Stats
            int male = 0, female = 0, other = 0;
            if (users != null) {
                for (User u : users) {
                    if ("Homme".equals(u.getGender()))
                        male++;
                    else if ("Femme".equals(u.getGender()))
                        female++;
                    else
                        other++;
                }
            }

            // 2. Age Stats
            int[] ageGroups = new int[5]; // 0: <18, 1: 18-25, 2: 26-35, 3: 36-50, 4: >50
            java.util.Calendar today = java.util.Calendar.getInstance();
            int currentYear = today.get(java.util.Calendar.YEAR);

            if (users != null) {
                for (User u : users) {
                    if (u.getBirthDate() != null && !u.getBirthDate().isEmpty()) {
                        try {
                            String[] parts = u.getBirthDate().split("-");
                            int year = Integer.parseInt(parts[0]);
                            int age = currentYear - year;

                            if (age < 18)
                                ageGroups[0]++;
                            else if (age <= 25)
                                ageGroups[1]++;
                            else if (age <= 35)
                                ageGroups[2]++;
                            else if (age <= 50)
                                ageGroups[3]++;
                            else
                                ageGroups[4]++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // Prepare final variables for UI thread
            final int finalMale = male;
            final int finalFemale = female;
            final int finalOther = other;
            final int[] finalAgeGroups = ageGroups;

            runOnUiThread(() -> {
                // UPDATE GENDER CHART
                java.util.List<com.github.mikephil.charting.data.PieEntry> genderEntries = new java.util.ArrayList<>();
                if (finalMale > 0)
                    genderEntries.add(new com.github.mikephil.charting.data.PieEntry(finalMale, "Homme"));
                if (finalFemale > 0)
                    genderEntries.add(new com.github.mikephil.charting.data.PieEntry(finalFemale, "Femme"));
                if (finalOther > 0)
                    genderEntries.add(new com.github.mikephil.charting.data.PieEntry(finalOther, "Autre"));

                com.github.mikephil.charting.data.PieDataSet genderDataSet = new com.github.mikephil.charting.data.PieDataSet(
                        genderEntries, "");
                genderDataSet.setColors(
                        android.graphics.Color.parseColor("#1976D2"), // Blue
                        android.graphics.Color.parseColor("#E91E63"), // Pink
                        android.graphics.Color.GRAY);
                genderDataSet.setValueTextColor(android.graphics.Color.WHITE);
                genderDataSet.setValueTextSize(12f);

                com.github.mikephil.charting.data.PieData genderData = new com.github.mikephil.charting.data.PieData(
                        genderDataSet);
                genderChart.setData(genderData);
                genderChart.setDescription(null);
                genderChart.setCenterTextColor(android.graphics.Color.WHITE);
                genderChart.setHoleColor(android.graphics.Color.TRANSPARENT);
                genderChart.getLegend().setTextColor(android.graphics.Color.WHITE);
                genderChart.invalidate();

                // UPDATE AGE CHART
                java.util.List<com.github.mikephil.charting.data.BarEntry> ageEntries = new java.util.ArrayList<>();
                ageEntries.add(new com.github.mikephil.charting.data.BarEntry(0, finalAgeGroups[0]));
                ageEntries.add(new com.github.mikephil.charting.data.BarEntry(1, finalAgeGroups[1]));
                ageEntries.add(new com.github.mikephil.charting.data.BarEntry(2, finalAgeGroups[2]));
                ageEntries.add(new com.github.mikephil.charting.data.BarEntry(3, finalAgeGroups[3]));
                ageEntries.add(new com.github.mikephil.charting.data.BarEntry(4, finalAgeGroups[4]));

                com.github.mikephil.charting.data.BarDataSet ageDataSet = new com.github.mikephil.charting.data.BarDataSet(
                        ageEntries, "Tranches d'âge");
                ageDataSet.setColor(android.graphics.Color.parseColor("#4CAF50")); // Green
                ageDataSet.setValueTextColor(android.graphics.Color.WHITE);

                com.github.mikephil.charting.data.BarData ageData = new com.github.mikephil.charting.data.BarData(
                        ageDataSet);
                ageChart.setData(ageData);

                com.github.mikephil.charting.components.XAxis xAxis = ageChart.getXAxis();
                xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                        new String[] { "<18", "18-25", "26-35", "36-50", "50+" }));
                xAxis.setTextColor(android.graphics.Color.WHITE);
                xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);

                ageChart.getAxisLeft().setTextColor(android.graphics.Color.WHITE);
                ageChart.getAxisRight().setEnabled(false);
                ageChart.getLegend().setTextColor(android.graphics.Color.WHITE);
                ageChart.getDescription().setEnabled(false);
                ageChart.invalidate();
            });

        }).start();
    }

    private void loadUsers() {
        new Thread(() -> {
            // 1. Fetch the data
            List<User> fetchedUsers = MyDataBase.getAppDataBase(this)
                    .UserDao()
                    .getAllUsers(); // 2. Ensure we have a non-null list for the UI thread
            // We create a NEW variable that is effectively final because we won't change
            // it.
            List<User> finalUsers = (fetchedUsers != null) ? fetchedUsers : new java.util.ArrayList<>();

            runOnUiThread(() -> {
                // 3. Use finalUsers here instead of the original variable
                if (!finalUsers.isEmpty()) {
                    userAdapter = new UserAdapter(finalUsers, new UserAdapter.OnUserActionListener() {
                        @Override
                        public void onEdit(User user) {
                            showEditUserDialog(user);
                        }

                        @Override
                        public void onDelete(User user) {
                            showDeleteConfirmation(user);
                        }
                    });
                    recyclerView.setAdapter(userAdapter);
                } else {
                    Toast.makeText(this, "Aucun utilisateur trouvé", Toast.LENGTH_SHORT).show();

                    // Set empty adapter using the final list
                    userAdapter = new UserAdapter(finalUsers, new UserAdapter.OnUserActionListener() {
                        @Override
                        public void onEdit(User user) {
                        }

                        @Override
                        public void onDelete(User user) {
                        }
                    });
                    recyclerView.setAdapter(userAdapter);
                }
            });
        }).start();
    }

    private void showAddUserDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Ajouter Utilisateur");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        final android.widget.EditText etName = new android.widget.EditText(this);
        etName.setHint("Nom");
        layout.addView(etName);

        final android.widget.EditText etEmail = new android.widget.EditText(this);
        etEmail.setHint("Email");
        layout.addView(etEmail);

        final android.widget.EditText etPassword = new android.widget.EditText(this);
        etPassword.setHint("Mot de passe");
        etPassword.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPassword);

        // 🔹 Gender
        final android.widget.TextView tvGender = new android.widget.TextView(this);
        tvGender.setText("Genre");
        tvGender.setPadding(0, 16, 0, 8);
        layout.addView(tvGender);

        final android.widget.RadioGroup rgGender = new android.widget.RadioGroup(this);
        rgGender.setOrientation(android.widget.RadioGroup.HORIZONTAL);

        final android.widget.RadioButton rbMale = new android.widget.RadioButton(this);
        rbMale.setText("Homme");
        rgGender.addView(rbMale);

        final android.widget.RadioButton rbFemale = new android.widget.RadioButton(this);
        rbFemale.setText("Femme");
        rgGender.addView(rbFemale);
        layout.addView(rgGender);

        // 🔹 Birth Date
        final android.widget.EditText etBirthDate = new android.widget.EditText(this);
        etBirthDate.setHint("Date de naissance (YYYY-MM-DD)");
        etBirthDate.setFocusable(false);
        etBirthDate.setClickable(true);
        etBirthDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            int year = c.get(java.util.Calendar.YEAR);
            int month = c.get(java.util.Calendar.MONTH);
            int day = c.get(java.util.Calendar.DAY_OF_MONTH);

            new android.app.DatePickerDialog(this, (picker, year1, month1, dayOfMonth) -> {
                String date = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1,
                        dayOfMonth);
                etBirthDate.setText(date);
            }, year, month, day).show();
        });
        layout.addView(etBirthDate);

        builder.setView(layout);
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            String birthDate = etBirthDate.getText().toString();

            String gender = "Non précisé";
            int selectedId = rgGender.getCheckedRadioButtonId();
            if (selectedId == rbMale.getId())
                gender = "Homme";
            else if (selectedId == rbFemale.getId())
                gender = "Femme";

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Champs vides", Toast.LENGTH_SHORT).show();
                return;
            }

            // Capture final variables for thread (if needed explicitly, though string
            // content is already captured)
            String finalGender = gender;

            new Thread(() -> {
                User newUser = new User();
                newUser.setNom(name);
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.setRole("USER"); // Default role
                newUser.setSolde(0.0);
                newUser.setGender(finalGender);
                newUser.setBirthDate(birthDate);

                MyDataBase.getAppDataBase(this).UserDao().insert(newUser);
                // Note: insert returns long/void depending on version.
                // We don't need ID here immediately unless we want to refresh list efficiently.
                loadUsers();

                // Refresh charts on UI thread (since data changed)
                runOnUiThread(this::setupCharts);
            }).start();
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showEditUserDialog(User user) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Modifier Utilisateur");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        final android.widget.EditText etName = new android.widget.EditText(this);
        etName.setHint("Nom");
        etName.setText(user.getNom());
        layout.addView(etName);

        final android.widget.EditText etEmail = new android.widget.EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(user.getEmail());
        layout.addView(etEmail);

        // Password? Usually admin doesn't see password but can reset it.
        // Let's leave password out for edit unless requested specifically.

        builder.setView(layout);
        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Champs vides", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                user.setNom(name);
                user.setEmail(email);
                MyDataBase.getAppDataBase(this).UserDao().update(user);
                loadUsers();
            }).start();
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showDeleteConfirmation(User user) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Supprimer ?")
                .setMessage("Supprimer utilisateur " + user.getNom() + " ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    new Thread(() -> {
                        MyDataBase.getAppDataBase(this).UserDao().delete(user);
                        loadUsers();
                    }).start();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}
