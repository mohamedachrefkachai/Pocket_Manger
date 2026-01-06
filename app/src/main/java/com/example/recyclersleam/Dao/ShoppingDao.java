package com.example.recyclersleam.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recyclersleam.Entity.ShoppingItem;

import java.util.List;

@Dao
public interface ShoppingDao {

    @Insert
    void insert(ShoppingItem item);

    @Delete
    void delete(ShoppingItem item);

    @Update
    void update(ShoppingItem item);

    @Query("SELECT * FROM shopping_items WHERE userId = :userId ORDER BY id DESC")
    List<ShoppingItem> getAllForUser(int userId);
}
