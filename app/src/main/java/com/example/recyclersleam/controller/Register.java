package com.example.recyclersleam.controller;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;

public class Register extends AppCompatActivity {

    EditText username, email, password, confirmPassword;
    Button registerBtn, backLoginBtn;
    Switch biometricSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Champs
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);

        biometricSwitch = findViewById(R.id.switch_biometric);

        registerBtn = findViewById(R.id.register_btn);
        backLoginBtn = findViewById(R.id.back_login_btn);

        registerBtn.setOnClickListener(v -> registerUser());
        backLoginBtn.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String nom = username.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pwd = password.getText().toString().trim();
        String confirmPwd = confirmPassword.getText().toString().trim();

        boolean biometricEnabled = biometricSwitch.isChecked();

        if (nom.isEmpty() || mail.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pwd.equals(confirmPwd)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            User existing = MyDataBase.getAppDataBase(this)
                    .UserDao()
                    .findByEmail(mail);

            if (existing != null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Email déjà utilisé", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            User user = new User(nom, mail, pwd, "USER");
            user.setBiometricEnabled(biometricEnabled); // 🔐 ASSOCIATION EMAIL ↔ BIOMETRIC

            MyDataBase.getAppDataBase(this)
                    .UserDao()
                    .insert(user);

            runOnUiThread(() -> {
                Toast.makeText(this,
                        biometricEnabled
                                ? "Compte créé avec empreinte activée"
                                : "Compte créé sans empreinte",
                        Toast.LENGTH_SHORT).show();
                finish();
            });

        }).start();
    }
}
