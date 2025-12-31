package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.example.recyclersleam.R;

public class Register extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // ton layout d'inscription

        // Récupérer le bouton Retour
        Button backLoginBtn = findViewById(R.id.back_login_btn);

        // Fermer RegisterActivity pour revenir au Login
        backLoginBtn.setOnClickListener(v -> finish());

    }


}