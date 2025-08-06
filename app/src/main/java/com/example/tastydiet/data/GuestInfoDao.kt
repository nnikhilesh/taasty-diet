package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.GuestInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface GuestInfoDao {
    @Query("SELECT * FROM GuestInfo WHERE date = :date")
    suspend fun getGuestCountsForDate(date: String): List<GuestInfo>

    @Query("SELECT * FROM GuestInfo")
    suspend fun getAll(): List<GuestInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(guestInfo: GuestInfo): Long

    @Update
    suspend fun update(guestInfo: GuestInfo)

    @Delete
    suspend fun delete(guestInfo: GuestInfo)

    @Query("DELETE FROM GuestInfo WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM GuestInfo")
    suspend fun clearAll()

    // Insert or update guest count for a specific date and meal time
    @Transaction
    suspend fun insertOrUpdateGuestCount(date: String, mealTime: String, guestCount: Int): Long {
        val existing = getGuestCountsForDate(date).find { it.mealTime == mealTime }
        return if (existing != null) {
            val updated = existing.copy(guestCount = guestCount)
            update(updated)
            existing.id.toLong() // Return the existing ID as Long
        } else {
            val newGuestInfo = GuestInfo(
                guestCount = guestCount,
                date = date,
                mealTime = mealTime
            )
            insert(newGuestInfo)
        }
    }
} 