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
                R.string.navigation_drawer_close
        );
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

        loadUsers();
    }

    private void loadUsers() {
        new Thread(() -> {
            List<User> users = MyDataBase.getAppDataBase(this)
                    .UserDao()
                    .getAllUsers();

            runOnUiThread(() -> {
                if (users != null && !users.isEmpty()) {
                    userAdapter = new UserAdapter(users);
                    recyclerView.setAdapter(userAdapter);
                } else {
                    Toast.makeText(this, "Aucun utilisateur trouvé", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
