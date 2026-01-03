package com.example.recyclersleam.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileFragment extends Fragment {

    private TextInputEditText etName, etEmail, etPassword;
    private Button btnSave;

    // Données utilisateur à passer depuis ProfileFragment
    private int userId; // <-- utiliser ID pour update
    private User currentUser;

    // Ajouter cette méthode pour recevoir l'ID
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        // Charger les infos actuelles de l'utilisateur par ID
        new Thread(() -> {
            MyDataBase db = MyDataBase.getAppDataBase(getContext());
            currentUser = db.UserDao().findById(userId); // <-- utiliser l'ID
            if (currentUser != null) {
                getActivity().runOnUiThread(() -> {
                    etName.setText(currentUser.getNom());
                    etEmail.setText(currentUser.getEmail());
                    etPassword.setText(currentUser.getPassword());
                });
            }
        }).start();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (currentUser != null) {
                MyDataBase db = MyDataBase.getAppDataBase(getContext());
                currentUser.setNom(name);
                currentUser.setEmail(email);
                currentUser.setPassword(password);
                db.UserDao().update(currentUser);

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show()
                );
                getActivity().getSupportFragmentManager().popBackStack();

            }
        }).start();
    }
}

