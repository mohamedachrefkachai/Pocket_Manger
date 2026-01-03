package com.example.recyclersleam.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recyclersleam.Entity.User;

@Dao
public interface UserDao {

    // ➕ Inscription
    @Insert
    void insert(User user);

    // 🔐 Login (email + password)
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    // 🔎 Vérifier si email existe déjà
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    // 👥 Récupérer tous les users (admin)
    @Query("SELECT * FROM users")
    java.util.List<User> getAllUsers();

    // ❌ Supprimer tous les users (tests)
    @Query("DELETE FROM users")
    void deleteAll();

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User findById(int id);
}
