package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class WeeklyDietPreference(
    @PrimaryKey val id: Int = 1, // singleton row
    val monday: String = "Veg",
    val tuesday: String = "Veg",
    val wednesday: String = "Veg",
    val thursday: String = "Veg",
    val friday: String = "Veg",
    val saturday: String = "Veg",
    val sunday: String = "Veg"
) 