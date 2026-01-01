package com.example.recyclersleam.Util;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.recyclersleam.Dao.UserDao;
import com.example.recyclersleam.Entity.User;

@Database(
        entities = {User.class},
        version = 1,
        exportSchema = false
)
public abstract class MyDataBase extends RoomDatabase {

    private static MyDataBase instance;

    // ✅ DAO
    public abstract UserDao UserDao();

    public static MyDataBase getAppDataBase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MyDataBase.class,
                            "room_db"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
