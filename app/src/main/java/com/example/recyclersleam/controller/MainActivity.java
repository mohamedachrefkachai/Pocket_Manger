package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;


import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.recyclersleam.R;
import com.google.android.material.navigation.NavigationView;




public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Toggle pour ouvrir/fermer Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_budget) {
                startActivity(new Intent(this, Login.class));
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, Login.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, Login.class));
            } else if (id == R.id.nav_logout) {
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // Clic sur les cartes
        CardView budgetCard = findViewById(R.id.budgetCard);
        CardView historyCard = findViewById(R.id.historyCard);

        budgetCard.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));

        historyCard.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));
    }
}
