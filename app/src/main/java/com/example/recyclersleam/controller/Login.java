package com.example.recyclersleam.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;

public class Login extends AppCompatActivity {

    EditText email, password;
    Button loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        createAdminIfNotExists();

        // 🎬 Animation du titre
        TextView title = findViewById(R.id.title);
        Animation slideAnim = AnimationUtils.loadAnimation(this, R.anim.slide_down_fade);
        title.startAnimation(slideAnim);

        // Champs
        email = findViewById(R.id.username);
        password = findViewById(R.id.pwd);

        loginBtn = findViewById(R.id.login);
        registerBtn = findViewById(R.id.register);

        // 🔐 LOGIN
        loginBtn.setOnClickListener(v -> login());

        // ➕ REGISTER
        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(Login.this, Register.class))
        );
    }
    private void createAdminIfNotExists() {
        new Thread(() -> {
            MyDataBase db = MyDataBase.getAppDataBase(this);
            User existing = db.UserDao().findByEmail("ADMIN@gmail.com");
            if (existing == null) {
                User u = new User("Achref", "ADMIN@gmail.com", "123", "ADMIN");
                db.UserDao().insert(u);
            }
        }).start();
    }

    private void login() {
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();

        if (e.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            User user = MyDataBase.getAppDataBase(this)
                    .UserDao()
                    .login(e, p);

            runOnUiThread(() -> {
                if (user != null) {
                    Toast.makeText(this,
                            "Bienvenue " + user.getNom(),
                            Toast.LENGTH_SHORT).show();

                    // Redirection selon rôle
                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                        startActivity(new Intent(this, Admin.class));
                    } else {
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.putExtra("name", user.getNom());
                        intent.putExtra("email", user.getEmail());
                        intent.putExtra("userId", user.getId());
                        startActivity(intent);
                    }
                    finish();


                    finish();
                } else {
                    Toast.makeText(this,
                            "Email ou mot de passe incorrect",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

}
