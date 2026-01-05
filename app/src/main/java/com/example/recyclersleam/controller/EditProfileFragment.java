package com.example.recyclersleam.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;

public class EditProfileFragment extends Fragment {

    private View cardUpdate, cardDelete;
    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Card clicks
        cardUpdate = view.findViewById(R.id.cardUpdate);
        cardDelete = view.findViewById(R.id.cardDelete);

        // Click Update -> Open Form
        cardUpdate.setOnClickListener(v -> {
            UpdateProfileFormFragment formFragment = new UpdateProfileFormFragment();
            formFragment.setUserId(userId);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment) // Assuming parent container ID
                    .addToBackStack(null)
                    .commit();
        });

        // Click Delete -> Show Dialog
        cardDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Supprimer le compte ?")
                .setMessage("Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteAccount())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteAccount() {
        new Thread(() -> {
            MyDataBase db = MyDataBase.getAppDataBase(getContext());
            User u = db.UserDao().findById(userId);
            if (u != null) {
                db.UserDao().delete(u);

                // Clear prefs
                SharedPreferences prefs = getActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Compte supprimé", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getActivity(), Login.class));
                    getActivity().finish();
                });
            }
        }).start();
    }
}
