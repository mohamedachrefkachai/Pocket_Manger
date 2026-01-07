package com.example.recyclersleam.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.recyclersleam.Entity.Expense;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insertExpense(Expense expense);

    @Query("SELECT * FROM expense")
    List<Expense> getAllExpenses();

    // Ajout: Récupérer les dépenses d'un utilisateur
    @Query("SELECT * FROM expense WHERE userId = :userId")
    List<Expense> getAllExpensesByUser(int userId);

    @Query("SELECT * FROM expense WHERE id = :id")
    Expense getExpenseById(int id);

    @androidx.room.Update
    void updateExpense(Expense expense);

    @androidx.room.Delete
    void deleteExpense(Expense expense);
}
