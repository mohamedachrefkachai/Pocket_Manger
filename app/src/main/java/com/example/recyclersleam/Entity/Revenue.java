package com.example.recyclersleam.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "revenues", foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId", onDelete = ForeignKey.CASCADE))
public class Revenue {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(index = true)
    private int userId;

    @ColumnInfo
    private String source;

    @ColumnInfo
    private double amount;

    @ColumnInfo
    private String date; // Format: DD/MM/YYYY

    // Constructeur vide
    public Revenue() {
    }

    // Constructeur avec paramètres
    public Revenue(int userId, String source, double amount, String date) {
        this.userId = userId;
        this.source = source;
        this.amount = amount;
        this.date = date;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
