package com.example.recyclersleam.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private String nom;

    @ColumnInfo
    private String email;

    @ColumnInfo
    private String password;

    @ColumnInfo
    private String role; // ADMIN ou USER

    // Constructeur vide (obligatoire pour Room)
    public User() {
    }

    // Constructeur avec paramètres
    public User(String nom, String email, String password, String role) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
