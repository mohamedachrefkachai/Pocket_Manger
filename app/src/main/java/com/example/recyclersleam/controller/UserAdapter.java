package com.example.recyclersleam.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEdit(User user);

        void onDelete(User user);
    }

    public UserAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User u = users.get(position);
        holder.name.setText(u.getNom());
        holder.email.setText(u.getEmail());
        holder.role.setText(u.getRole());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(u));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(u));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, role;
        ImageView btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userNom);
            email = itemView.findViewById(R.id.userEmail);
            role = itemView.findViewById(R.id.userRole);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
