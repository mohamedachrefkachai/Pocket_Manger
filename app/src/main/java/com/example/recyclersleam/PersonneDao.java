package com.example.recyclersleam;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recyclersleam.Entity.Personne;

import java.util.List;

@Dao
interface PersonneDao {
    @Insert
    void insertPerson(Personne p);

    @Delete
    void deletePerson(Personne p);

    @Update
    void updatePerson(Personne p);

    @Query("select * from Personne")
    List<Personne> getAllPersons();

    @Query("select * from Personne where id=:id")
    Personne getOnePersons(int id);

}
