package com.example.recyclersleam.Util;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.recyclersleam.Dao.RevenueDao;
import com.example.recyclersleam.Dao.UserDao;
import com.example.recyclersleam.Entity.Revenue;
import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.Entity.Expense;
import com.example.recyclersleam.Location.LocationEntity;

@Database(entities = { User.class, Revenue.class,
        com.example.recyclersleam.Entity.ShoppingItem.class,
        Expense.class,
        LocationEntity.class }, version = 7, exportSchema = false)
public abstract class MyDataBase extends RoomDatabase {

    private static MyDataBase instance;

    // ✅ DAO
    public abstract UserDao UserDao();

    public abstract RevenueDao RevenueDao(); // 💰 Revenus

    public abstract com.example.recyclersleam.Dao.ShoppingDao ShoppingDao(); // 🛒 Liste de courses
    
    public abstract com.example.recyclersleam.Dao.ExpenseDao ExpenseDao(); // 💸 Dépenses
    
    public abstract com.example.recyclersleam.Dao.LocationDao LocationDao(); // 📍 Localisation

    public static MyDataBase getAppDataBase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MyDataBase.class,
                    "room_db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
