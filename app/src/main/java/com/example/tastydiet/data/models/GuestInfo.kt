package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "GuestInfo")
@Serializable
data class GuestInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val guestCount: Int,
    @androidx.room.ColumnInfo(name = "date")
    val date: String, // e.g., YYYY-MM-DD (renamed from activeForDate for DAO consistency)
    val mealTime: String // Breakfast, Lunch, Dinner
)