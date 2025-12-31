package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.recyclersleam.R;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView title = findViewById(R.id.title);
        Animation slideAnim = AnimationUtils.loadAnimation(this, R.anim.slide_down_fade);
        title.startAnimation(slideAnim);

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
