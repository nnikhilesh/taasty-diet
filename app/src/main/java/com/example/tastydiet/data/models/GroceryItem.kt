package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val unit: String,
    val quantity: Float = 0f,
    val isAvailable: Boolean = false
) 