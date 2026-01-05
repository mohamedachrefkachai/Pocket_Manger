package com.example.recyclersleam.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;

public class UpdateProfileFormFragment extends Fragment {

    private EditText etName, etEmail, etPassword, etBirthDate;
    private android.widget.RadioGroup radioGroupGender;
    private Button btnSave;
    private int userId;
    private User currentUser;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_profile_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etBirthDate = view.findViewById(R.id.etBirthDate);
        etBirthDate.setFocusable(false);
        etBirthDate.setClickable(true);
        etBirthDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            int year = c.get(java.util.Calendar.YEAR);
            int month = c.get(java.util.Calendar.MONTH);
            int day = c.get(java.util.Calendar.DAY_OF_MONTH);

            new android.app.DatePickerDialog(getContext(), (picker, year1, month1, dayOfMonth) -> {
                String date = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1,
                        dayOfMonth);
                etBirthDate.setText(date);
            }, year, month, day).show();
        });

        radioGroupGender = view.findViewById(R.id.radioGroupGender);
        btnSave = view.findViewById(R.id.btnSave);

        // Load Data
        new Thread(() -> {
            MyDataBase db = MyDataBase.getAppDataBase(getContext());
            currentUser = db.UserDao().findById(userId);
            if (currentUser != null) {
                getActivity().runOnUiThread(() -> {
                    etName.setText(currentUser.getNom());
                    etEmail.setText(currentUser.getEmail());
                    etPassword.setText(currentUser.getPassword());
                    etBirthDate.setText(currentUser.getBirthDate());

                    if ("Homme".equals(currentUser.getGender())) {
                        radioGroupGender.check(R.id.radioMale);
                    } else if ("Femme".equals(currentUser.getGender())) {
                        radioGroupGender.check(R.id.radioFemale);
                    }
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
                currentUser.setBirthDate(etBirthDate.getText().toString().trim());

                int selectedId = radioGroupGender.getCheckedRadioButtonId();
                if (selectedId == R.id.radioMale)
                    currentUser.setGender("Homme");
                else if (selectedId == R.id.radioFemale)
                    currentUser.setGender("Femme");
                else
                    currentUser.setGender("Non précisé");
                db.UserDao().update(currentUser);

                getActivity().runOnUiThread(
                        () -> Toast.makeText(getContext(), "Profil mis à jour", Toast.LENGTH_SHORT).show());
                getParentFragmentManager().popBackStack(); // Go back to Hub
            }
        }).start();
    }
}
