package com.example.recyclersleam.Location;

public class HotSpot {
    public double latitude;
    public double longitude;
    public double totalAmount; // Montant total dépensé
    public int count; // Nombre de dépenses
    public int color; // Couleur basée sur l'intensité

    public HotSpot(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalAmount = 0;
        this.count = 0;
    }

    public void addExpense(double amount) {
        this.totalAmount += amount;
        this.count++;
    }

    public double getAverageExpense() {
        return count > 0 ? totalAmount / count : 0;
    }

    public String getLabel() {
        return String.format("%.2f DT (%d dépenses)", totalAmount, count);
    }
}
