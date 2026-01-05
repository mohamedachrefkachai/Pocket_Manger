package com.example.recyclersleam.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recyclersleam.Entity.Revenue;

import java.util.List;

@Dao
public interface RevenueDao {

    @Insert
    void insert(Revenue revenue);

    @Update
    void update(Revenue revenue);

    @Delete
    void delete(Revenue revenue);

    @Query("SELECT * FROM revenues WHERE userId = :userId")
    List<Revenue> getAllByUser(int userId);

    @Query("SELECT SUM(amount) FROM revenues WHERE userId = :userId")
    double getTotalRevenueByUser(int userId);
}
