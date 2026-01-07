package com.example.recyclersleam.Dao; // CHANGED to Dao package

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.recyclersleam.Location.LocationEntity;
import com.example.recyclersleam.Location.LocationStats; // Import definition

import java.util.List;

@Dao
public interface LocationDao {

    @Insert
    void insertLocation(LocationEntity location);

    @Query("SELECT * FROM location WHERE expense_id = :expenseId")
    LocationEntity getLocationForExpense(int expenseId);

    @Query("SELECT * FROM location")
    List<LocationEntity> getAllLocations();

    // Statistiques par lieu (Top 3)
    // Note: This query joins location and expense. Expense is in Entity package.
    // Table names are "location" and "expense".
    @Query("SELECT l.adresse as address, SUM(e.amount) as totalAmount " +
            "FROM location l " +
            "JOIN expense e ON l.expense_id = e.id " +
            "WHERE l.adresse IS NOT NULL AND l.adresse != '' " +
            "GROUP BY l.adresse " +
            "ORDER BY totalAmount DESC " +
            "LIMIT 3")
    List<LocationStats> getTopSpendingLocations();

    // Ajout: Filtrer par user via join si besoin, mais pour l'instant global map is
    // simpler.
    // Ideally we join expense and filter by userId.
    @Query("SELECT l.adresse as address, SUM(e.amount) as totalAmount " +
            "FROM location l " +
            "JOIN expense e ON l.expense_id = e.id " +
            "WHERE l.adresse IS NOT NULL AND l.adresse != '' AND e.userId = :userId " +
            "GROUP BY l.adresse " +
            "ORDER BY totalAmount DESC " +
            "LIMIT 3")
    List<LocationStats> getTopSpendingLocationsByUser(int userId);
}
