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
    private Button btnLogout;

    private String name = "Nom Utilisateur";
    private String email = "email@email.com";

    public ProfileFragment() {}

    public void setUserData(String name, String email) {
        this.name = name;
        this.email = email;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        imgProfile = view.findViewById(R.id.imgProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        tvName.setText(name);
        tvEmail.setText(email);

        btnLogout.setOnClickListener(v -> {
            // Retourner au login
            if(getActivity() != null){
                getActivity().finish();
            }
        });

        return view;
    }
}
