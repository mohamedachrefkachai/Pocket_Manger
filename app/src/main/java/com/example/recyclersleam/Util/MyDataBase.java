package com.example.recyclersleam.Util;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.recyclersleam.Entity.Personne;

@Database(
        entities = {Personne.class}, // ✅ MANQUANT
        version = 1,
        exportSchema = false
)
public abstract class MyDataBase  extends RoomDatabase {
    private static MyDataBase instance;
   // public abstract UserDAO UserDAO();

    public static MyDataBase getAppDataBase(Context context){
        if (instance==null){
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MyDataBase.class,
                            "room_db")
                    .allowMainThreadQueries()
                    .build();
        }

        return instance;
    }


}
