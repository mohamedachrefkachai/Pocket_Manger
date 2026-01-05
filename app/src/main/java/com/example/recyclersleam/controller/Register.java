package com.example.recyclersleam.controller;

import android.content.Intent;
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

    EditText username, email, password, confirmPassword, etBirthDate;
    Button registerBtn, backLoginBtn;
    Switch biometricSwitch;
    android.widget.RadioGroup radioGroupGender;

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
        radioGroupGender = findViewById(R.id.radioGroupGender);
        etBirthDate = findViewById(R.id.etBirthDate);
        etBirthDate.setFocusable(false);
        etBirthDate.setClickable(true);
        etBirthDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            int year = c.get(java.util.Calendar.YEAR);
            int month = c.get(java.util.Calendar.MONTH);
            int day = c.get(java.util.Calendar.DAY_OF_MONTH);

            new android.app.DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                // Format: YYYY-MM-DD
                String date = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1,
                        dayOfMonth);
                etBirthDate.setText(date);
            }, year, month, day).show();
        });

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
                runOnUiThread(() -> Toast.makeText(this, "Email déjà utilisé", Toast.LENGTH_SHORT).show());
                return;
            }

            User user = new User(nom, mail, pwd, "USER");
            user.setBiometricEnabled(biometricEnabled);

            // Gender
            int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
            if (selectedGenderId == R.id.radioMale) {
                user.setGender("Homme");
            } else if (selectedGenderId == R.id.radioFemale) {
                user.setGender("Femme");
            } else {
                user.setGender("Non précisé");
            }

            // Date
            user.setBirthDate(etBirthDate.getText().toString().trim());
            user.setBiometricEnabled(biometricEnabled); // 🔐 ASSOCIATION EMAIL ↔ BIOMETRIC

            long newUserId = MyDataBase.getAppDataBase(this)
                    .UserDao()
                    .insert(user);

            runOnUiThread(() -> {
                Toast.makeText(this,
                        biometricEnabled
                                ? "Compte créé avec empreinte activée"
                                : "Compte créé sans empreinte",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Register.this, AddBalanceActivity.class);
                intent.putExtra("USER_ID", (int) newUserId);
                startActivity(intent);
                finish();
            });

        }).start();
    }
}
