package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import com.example.tastydiet.data.models.MacroPref

@Entity
@Serializable
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "User",
    val age: Int = 25,
    val gender: String = "Other", // "Male", "Female", or "Other"
    val height: Float = 165f, // in cm
    val weight: Float = 60f, // in kg
    val isVeg: Boolean = true,
    val targetCalories: Float = 2000f, // kcal
    val targetProtein: Float = 50f, // grams
    val targetCarbs: Float = 250f, // grams
    val targetFat: Float = 70f, // grams
    val proteinPref: MacroPref = MacroPref.MEDIUM,
    val carbPref: MacroPref = MacroPref.MEDIUM,
    val fatPref: MacroPref = MacroPref.MEDIUM,
    val fiberPref: MacroPref = MacroPref.MEDIUM
) {
    // Legacy fields for backward compatibility
    val calorieGoal: Int get() = targetCalories.toInt()
    val proteinGoal: Int get() = targetProtein.toInt()
    val carbGoal: Int get() = targetCarbs.toInt()
    val fatGoal: Int get() = targetFat.toInt()
    val fiberGoal: Int get() = 25 // Default fiber goal
}