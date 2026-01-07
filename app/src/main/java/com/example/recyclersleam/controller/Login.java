package com.example.recyclersleam.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import com.example.recyclersleam.sensors.movement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;

import java.util.concurrent.Executor;

public class Login extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView registerBtn, forgotPassword;
    ImageButton fingerprintBtn;

    Executor executor;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    // Sensor vars
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private movement shakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        createAdminIfNotExists();

        // Animation titre
        TextView title = findViewById(R.id.title);
        Animation slideAnim = AnimationUtils.loadAnimation(this, R.anim.slide_down_fade);
        title.startAnimation(slideAnim);

        // Champs
        email = findViewById(R.id.username);
        password = findViewById(R.id.pwd);

        loginBtn = findViewById(R.id.login);
        registerBtn = findViewById(R.id.register);
        fingerprintBtn = findViewById(R.id.btnFingerprint);
        forgotPassword = findViewById(R.id.forgotPassword);

        forgotPassword.setOnClickListener(v -> startActivity(new Intent(Login.this, ForgotPasswordActivity.class)));

        loginBtn.setOnClickListener(v -> loginWithPassword());

        registerBtn.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));

        setupFingerprint();
        setupShakeDetector();
    }

    private void setupShakeDetector() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            shakeDetector = new movement();
            shakeDetector.setOnShakeListener(count -> {
                runOnUiThread(() -> {
                    if (email != null && password != null) {
                        if (email.getText().length() > 0 || password.getText().length() > 0) {
                            email.setText("");
                            password.setText("");
                            Toast.makeText(Login.this, "Champs réinitialisés", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }

    // Créer admin si non existant
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

    // LOGIN classique mot de passe
    private void loginWithPassword() {
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
                    saveUserForBiometric(user);
                    redirectUser(user);
                } else {
                    Toast.makeText(this,
                            "Email ou mot de passe incorrect",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    // Configurer l'empreinte
    private void setupFingerprint() {
        executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);

                        // Récupérer userId stocké
                        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
                        int userId = prefs.getInt("userId", -1);

                        if (userId != -1) {
                            new Thread(() -> {
                                User user = MyDataBase.getAppDataBase(Login.this)
                                        .UserDao()
                                        .findById(userId);

                                if (user != null) {
                                    runOnUiThread(() -> redirectUser(user));
                                }
                            }).start();
                        } else {
                            Toast.makeText(Login.this,
                                    "Veuillez vous connecter d'abord avec votre mot de passe",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(Login.this,
                                "Empreinte non reconnue", Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Connexion par empreinte")
                .setSubtitle("Utilisez votre empreinte digitale ou Face ID")
                .setNegativeButtonText("Annuler")
                .build();

        // Vérifier si le device supporte la biométrie
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            fingerprintBtn.setOnClickListener(v -> {
                // Vérifier si email est renseigné et biométrie activée
                String e = email.getText().toString().trim();
                if (e.isEmpty()) {
                    Toast.makeText(this, "Veuillez entrer votre email pour l’empreinte", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(() -> {
                    User user = MyDataBase.getAppDataBase(Login.this)
                            .UserDao()
                            .findByEmail(e);

                    runOnUiThread(() -> {
                        if (user != null && user.isBiometricEnabled()) {
                            // Stocker userId pour la connexion biométrique
                            saveUserForBiometric(user);
                            biometricPrompt.authenticate(promptInfo);
                        } else {
                            Toast.makeText(Login.this, "Empreinte non activée pour cet email", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                }).start();
            });
        } else {
            fingerprintBtn.setEnabled(false);
        }
    }

    private void saveUserForBiometric(User user) {
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        prefs.edit().putInt("userId", user.getId()).apply();
    }

    // REDIRECTION selon rôle
    private void redirectUser(User user) {
        Toast.makeText(this,
                "Bienvenue " + user.getNom(),
                Toast.LENGTH_SHORT).show();

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
    }
}
