package com.example.recyclersleam.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Personne {
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo
    private String nom;
    @ColumnInfo
    private String prenom;
    @ColumnInfo
    private int image;

    public Personne() {
    }

    public Personne(String nom, String prenom, int image) {
        this.nom = nom;
        this.prenom = prenom;
        this.image = image;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Personne{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", image=" + image +
                '}';
    }
}
