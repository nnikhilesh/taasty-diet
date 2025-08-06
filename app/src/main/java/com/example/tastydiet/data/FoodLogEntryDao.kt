package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.FoodLogEntry

@Dao
interface FoodLogEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodLogEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<FoodLogEntry>)

    @Update
    suspend fun update(entry: FoodLogEntry)

    @Delete
    suspend fun delete(entry: FoodLogEntry)

    @Query("SELECT * FROM FoodLogEntry WHERE date(timestamp/1000, 'unixepoch', 'localtime') = date(:date/1000, 'unixepoch', 'localtime') ORDER BY timestamp DESC")
    suspend fun getByDate(date: Long): List<FoodLogEntry>

    @Query("SELECT * FROM FoodLogEntry WHERE date(timestamp/1000, 'unixepoch', 'localtime') = date(:date/1000, 'unixepoch', 'localtime') AND memberId = :memberId ORDER BY timestamp DESC")
    suspend fun getByDateAndMember(date: Long, memberId: Int): List<FoodLogEntry>

    @Query("SELECT * FROM FoodLogEntry ORDER BY timestamp DESC")
    suspend fun getAll(): List<FoodLogEntry>
    
    @Query("SELECT * FROM FoodLogEntry ORDER BY timestamp DESC")
    fun getAllFoodLogEntries(): kotlinx.coroutines.flow.Flow<List<FoodLogEntry>>
    
    suspend fun insertAllFoodLogEntries(entries: List<FoodLogEntry>) {
        insertAll(entries)
    }
}
