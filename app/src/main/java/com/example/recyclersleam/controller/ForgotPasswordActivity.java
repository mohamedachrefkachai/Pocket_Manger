package com.example.recyclersleam.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.GMailSender;
import com.example.recyclersleam.Util.MyDataBase;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText emailReset, codeVerify, newPwd, confPwd;
    Button btnReset;

    private String generatedCode;
    private User foundUser;
    private int step = 1; // 1: Send Code, 2: Verify Code, 3: Reset Password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailReset = findViewById(R.id.emailReset);
        codeVerify = findViewById(R.id.codeVerify);
        newPwd = findViewById(R.id.newPwd);
        confPwd = findViewById(R.id.confPwd);
        btnReset = findViewById(R.id.btnReset);

        btnReset.setOnClickListener(v -> handleButtonClick());
    }

    private void handleButtonClick() {
        if (step == 1) {
            sendVerificationCode();
        } else if (step == 2) {
            verifyCode();
        } else if (step == 3) {
            resetPassword();
        }
    }

    private void sendVerificationCode() {
        String email = emailReset.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            MyDataBase db = MyDataBase.getAppDataBase(this);
            foundUser = db.UserDao().findByEmail(email);

            runOnUiThread(() -> {
                if (foundUser == null) {
                    Toast.makeText(this, "Email introuvable", Toast.LENGTH_SHORT).show();
                } else {
                    // Generate Code
                    generatedCode = String.format("%06d", new Random().nextInt(999999));

                    // Send Email
                    sendEmail(email, generatedCode);
                }
            });
        }).start();
    }

    private void sendEmail(String email, String code) {
        new Thread(() -> {
            try {
                GMailSender.sendEmail(email, "Code de Réinitialisation", "Votre code est : " + code);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Code envoyé ! Vérifiez votre email", Toast.LENGTH_SHORT).show();

                    // Update UI for next step
                    step = 2;
                    emailReset.setEnabled(false);
                    codeVerify.setVisibility(View.VISIBLE);
                    btnReset.setText("VÉRIFIER CODE");
                });
            } catch (Exception e) {
                Log.e("ForgotPwd", "Error sending email", e);
                runOnUiThread(() -> Toast
                        .makeText(this, "Erreur d'envoi. Vérifiez votre connexion ou config SMTP.", Toast.LENGTH_LONG)
                        .show());
            }
        }).start();
    }

    private void verifyCode() {
        String inputCode = codeVerify.getText().toString().trim();
        if (inputCode.equals(generatedCode)) {
            Toast.makeText(this, "Code validé !", Toast.LENGTH_SHORT).show();
            step = 3;
            codeVerify.setEnabled(false);
            newPwd.setVisibility(View.VISIBLE);
            confPwd.setVisibility(View.VISIBLE);
            btnReset.setText("RÉINITIALISER");
        } else {
            Toast.makeText(this, "Code incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPassword() {
        String p1 = newPwd.getText().toString().trim();
        String p2 = confPwd.getText().toString().trim();

        if (p1.isEmpty() || p2.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir les mots de passe", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!p1.equals(p2)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            foundUser.setPassword(p1);
            MyDataBase.getAppDataBase(this).UserDao().update(foundUser);

            runOnUiThread(() -> {
                Toast.makeText(this, "Mot de passe modifié avec succès", Toast.LENGTH_SHORT).show();
                finish(); // Return to Login
            });
        }).start();
    }
}
