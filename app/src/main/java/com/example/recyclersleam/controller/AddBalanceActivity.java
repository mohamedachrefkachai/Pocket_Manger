package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;

public class AddBalanceActivity extends AppCompatActivity {

    private EditText etInitialBalance;
    private Button btnConfirmBalance;
    private TextView tvSkip;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_balance);

        etInitialBalance = findViewById(R.id.etInitialBalance);
        btnConfirmBalance = findViewById(R.id.btnConfirmBalance);
        tvSkip = findViewById(R.id.tvSkip);

        // Get User ID passed from Register Activity
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Erreur utilisateur incomplet", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        btnConfirmBalance.setOnClickListener(v -> saveBalanceAndProceed());
        tvSkip.setOnClickListener(v -> navigateToLogin());
    }

    private void saveBalanceAndProceed() {
        String balanceStr = etInitialBalance.getText().toString().trim();

        if (balanceStr.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un montant", Toast.LENGTH_SHORT).show();
            return;
        }

        double balance = Double.parseDouble(balanceStr);

        new Thread(() -> {
            MyDataBase db = MyDataBase.getAppDataBase(getApplicationContext());
            User user = db.UserDao().findById(userId);

            if (user != null) {
                user.setSolde(balance);
                db.UserDao().update(user);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Solde ajouté avec succès", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur utilisateur introuvable", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            }
        }).start();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(AddBalanceActivity.this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
