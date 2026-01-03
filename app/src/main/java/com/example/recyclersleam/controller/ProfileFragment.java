package com.example.recyclersleam.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.recyclersleam.R;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView imgProfile;
    private Button btnLogout, btnUpdate, btnDelete;

    private int userId;
    private String name = "Nom Utilisateur";
    private String email = "email@email.com";

    public ProfileFragment() {}

    public void setUserData(int userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 🔹 VIEWS
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        imgProfile = view.findViewById(R.id.imgProfile);

        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnLogout = view.findViewById(R.id.btnLogout); // ✅ MANQUAIT ICI

        // 🔹 DATA
        tvName.setText(name);
        tvEmail.setText(email);

        // 🔹 LOGOUT
        btnLogout.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        // 🔹 EDIT PROFILE
        btnUpdate.setOnClickListener(v -> {
            EditProfileFragment editFragment = new EditProfileFragment();
            editFragment.setUserId(userId);

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
}
