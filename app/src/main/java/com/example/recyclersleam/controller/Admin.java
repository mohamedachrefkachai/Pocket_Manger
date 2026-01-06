package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.recyclersleam.R;
import com.google.android.material.navigation.NavigationView;

public class Admin extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

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

        // 🔹 Carte Budget
        CardView budgetCard = findViewById(R.id.budgetCard_admin);
        budgetCard.setOnClickListener(v -> {
            // Ouvrir activité Budget Admin
        });

        // 🔹 Carte Voir Utilisateurs
        CardView usersCard = findViewById(R.id.usersCard_admin);
        usersCard.setOnClickListener(v -> startActivity(new Intent(Admin.this, UserListActivity.class)));
    }
}
