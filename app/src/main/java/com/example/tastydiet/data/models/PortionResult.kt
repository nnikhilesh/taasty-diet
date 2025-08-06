package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PortionResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mealId: Int,
    val memberIdOrGuest: String, // memberId or guestX
    val gramsServed: Float,
    val caloriesServed: Float
) 