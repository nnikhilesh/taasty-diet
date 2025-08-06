package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "inventory_items")
@Serializable
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Float,
    val unit: String,
    val category: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val shelfLifeDays: Int = 7 // default, override per item
) {
    fun daysToExpiry(): Int {
        val daysSinceAdded = (System.currentTimeMillis() - dateAdded) / (1000 * 60 * 60 * 24)
        return shelfLifeDays - daysSinceAdded.toInt()
    }
    
    fun isExpired(): Boolean = daysToExpiry() < 0
    
    fun isExpiringSoon(): Boolean {
        val daysLeft = daysToExpiry()
        return daysLeft <= 3 && daysLeft >= 0
    }
} 