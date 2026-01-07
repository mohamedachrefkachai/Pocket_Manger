package com.example.recyclersleam.Location; // Keeping it in Location package as per source structure, or move to Entity? Plan said Entity package. I will move to Location package to avoid circular deps if Dao uses it? No, usually Entity package. Wait. The source had it in Location package. My plan said "Port LocationEntity.java". The source: com.example.pocketmanager.Location.LocationEntity. But existing project has `com.example.recyclersleam.Entity`. Mixing packages might be messy. The plan said "Create new package com.example.recyclersleam.Location. Import LocationEntity.java". BUT plan also said "Register Expense and LocationEntity in MyDataBase".

// Let's stick to the structure: Expense in Entity, LocationEntity in Location (since it's very specific).
// Wait, `LocationEntity` references `Expense`. If Expense is in Entity package, we need import.

import androidx.room.ColumnInfo;
import com.example.recyclersleam.Entity.Expense;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "location", foreignKeys = @ForeignKey(entity = Expense.class, parentColumns = "id", childColumns = "expense_id", onDelete = CASCADE))
public class LocationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "expense_id", index = true)
    public int expenseId;

    public double latitude;
    public double longitude;
    public String adresse;
}
