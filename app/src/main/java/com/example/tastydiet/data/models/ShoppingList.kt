package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "shopping_list_items")
@Serializable
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Float = 1f,
    val unit: String = "",
    val category: String = "", // "Vegetables", "Proteins", "Grains", "Dairy", "Spices", etc.
    val isChecked: Boolean = false,
    val priority: String = "Medium", // "Low", "Medium", "High"
    val estimatedPrice: Float = 0f,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val neededForDate: String? = null // e.g., YYYY-MM-DD or null for general
)

data class ShoppingList(
    val id: Int = 0,
    val name: String,
    val items: List<ShoppingListItem>,
    val totalEstimatedPrice: Float,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

data class ShoppingListSummary(
    val totalItems: Int,
    val checkedItems: Int,
    val totalEstimatedPrice: Float,
    val categories: Map<String, Int>
) 