package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.recyclersleam.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    // Infos utilisateur
    private String userName;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupérer les extras envoyés depuis Login
        userName = getIntent().getStringExtra("name");
        userEmail = getIntent().getStringExtra("email");

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_nav);

        // Fragment par défaut
        loadFragment(new HomeFragment());
        toolbar.setTitle("Accueil");

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.nav_home) {
                fragment = new HomeFragment();
                toolbar.setTitle("Accueil");

            } else if (item.getItemId() == R.id.nav_profile) {
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setUserData(userName, userEmail); // PASSER LES INFOS
                fragment = profileFragment;
                toolbar.setTitle("Profil");

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
}
