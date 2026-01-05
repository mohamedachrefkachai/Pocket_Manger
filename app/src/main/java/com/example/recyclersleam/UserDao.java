package com.example.recyclersleam.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import androidx.room.Delete;

import com.example.recyclersleam.Entity.User;

@Dao
public interface UserDao {

    // ➕ Inscription
    @Insert
    long insert(User user);

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

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User findById(int id);



    // 📊 Statistiques Admin : Répartition par genre
    // Retourne une liste de "GenderCount" (il faudra une classe POJO ou utiliser
    // Cursor/Map, mais ici on va faire simple)
    // Ici on peut juste récupérer tous les genres et compter en Java, ou utiliser
    // un return type spécifique.
    // Simplifions : on récupère tout et on compte dans l'activité Admin.

    // Pour l'âge, on récupère toutes les dates de naissance
    @Query("SELECT birthDate FROM users")
    java.util.List<String> getAllBirthDates();

    @Query("SELECT gender FROM users")
    java.util.List<String> getAllGenders();
}
