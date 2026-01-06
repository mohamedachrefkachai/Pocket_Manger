package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recyclersleam.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 secondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animation fade-in pour le logo
        ImageView logo = findViewById(R.id.splash_logo);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(2000);
        logo.startAnimation(fadeIn);

        // Passage au Login après SPLASH_DURATION
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(SplashActivity.this, Login.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("SplashActivity", "Erreur lors du lancement de Login");
                }
                finish();
            }
        }, SPLASH_DURATION);
    }
}
