package com.example.recyclersleam.controller;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;

public class Register extends AppCompatActivity {

    EditText username, email, password, confirmPassword;
    Button registerBtn, backLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 🔹 Champs
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);

        registerBtn = findViewById(R.id.register_btn);
        backLoginBtn = findViewById(R.id.back_login_btn);

        // ➕ Bouton S'inscrire
        registerBtn.setOnClickListener(v -> registerUser());

        // 🔙 Bouton Retour Login
        backLoginBtn.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String nom = username.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pwd = password.getText().toString().trim();
        String confirmPwd = confirmPassword.getText().toString().trim();

        // ✅ Vérifications simples
        if (nom.isEmpty() || mail.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pwd.equals(confirmPwd)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⛔ Vérifier si email déjà utilisé
        new Thread(() -> {
            User existingUser = MyDataBase.getAppDataBase(this)
                    .UserDao()
                    .findByEmail(mail);

            runOnUiThread(() -> {
                if (existingUser != null) {
                    Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🆕 Créer un nouvel utilisateur
                User user = new User(nom, mail, pwd, "USER"); // rôle automatique USER

                new Thread(() -> {
                    MyDataBase.getAppDataBase(this).UserDao().insert(user);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();
                        finish(); // retour au login
                    });
                }).start();
            });
        }).start();
    }
}
