package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recyclersleam.R;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bouton S'INSCRIRE
        Button registerBtn = findViewById(R.id.register);
        registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

        Button loginBtn = findViewById(R.id.login);
        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, MainActivity.class); // assure-toi que c'est MainActivity
            startActivity(intent);
            finish();
        });

    }
}
