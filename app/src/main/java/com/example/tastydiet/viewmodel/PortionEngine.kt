package com.example.tastydiet.viewmodel

import com.example.tastydiet.data.models.FamilyMember

class PortionEngine {
    data class Portion(val memberIdOrGuest: String, val grams: Float, val calories: Float)

    fun calculatePortions(
        familyMembers: List<FamilyMember>,
        guestCount: Int,
        guestCalorieGoal: Int,
        totalCalories: Float,
        totalWeight: Float
    ): List<Portion> {
        val all = familyMembers.map { it.id.toString() to it.calorieGoal.toFloat() } +
                (1..guestCount).map { "guest$it" to guestCalorieGoal.toFloat() }
        val totalGoal = all.sumOf { it.second.toDouble() }.toFloat()
        return all.map { (id, goal) ->
            val portionGrams = if (totalCalories > 0) (goal / totalCalories) * totalWeight else 0f
            val portionCalories = if (totalCalories > 0) (goal / totalCalories) * totalCalories else 0f
            Portion(id, portionGrams, portionCalories)
        }
    }
} 