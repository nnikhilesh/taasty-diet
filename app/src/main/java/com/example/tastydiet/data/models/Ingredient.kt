package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: Float, // in grams
    val unit: String = "g",
    val category: String = "General", // e.g., "Vegetables", "Non-Veg", "Eggs", "Dry Fruits", "Grains", "Dairy", "Spices", "Oils", "Beverages", "Snacks", "Fruits"
    val addedDate: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null
) {
    fun getFormattedQuantity(): String {
        return when (unit.lowercase()) {
            "kg" -> "${quantity} kg"
            "g" -> "${quantity.toInt()} g"
            "ml" -> "${quantity.toInt()} ml"
            "l" -> "${quantity} L"
            "pcs", "pieces" -> "${quantity.toInt()} pcs"
            "cups" -> "${quantity} cups"
            "tbsp" -> "${quantity} tbsp"
            "tsp" -> "${quantity} tsp"
            else -> "${quantity} $unit"
        }
    }
    
    fun isExpired(): Boolean {
        return expiryDate != null && System.currentTimeMillis() > expiryDate
    }
    
    fun getDaysUntilExpiry(): Int? {
        return expiryDate?.let { expiry ->
            val days = (expiry - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
            days.toInt()
        }
    }
    
    companion object {
        /**
         * Auto-assign unit based on category and item name
         */
        fun getDefaultUnit(category: String, itemName: String): String {
            val normalizedName = itemName.lowercase()
            val normalizedCategory = category.lowercase()
            
            // Category-based rules
            return when {
                // Grains, Vegetables, Spices → "g"
                normalizedCategory in listOf("grains", "vegetables", "spices", "dry fruits") -> "g"
                
                // Fruits, Eggs, Bread → "pcs"
                normalizedCategory in listOf("fruits", "eggs") || 
                normalizedName.contains("banana") || 
                normalizedName.contains("apple") || 
                normalizedName.contains("orange") || 
                normalizedName.contains("bread") || 
                normalizedName.contains("egg") -> "pcs"
                
                // Dairy, Oils, Beverages → "ml"
                normalizedCategory in listOf("dairy", "oils", "beverages") || 
                normalizedName.contains("milk") || 
                normalizedName.contains("curd") || 
                normalizedName.contains("oil") || 
                normalizedName.contains("juice") -> "ml"
                
                // Non-Veg items → "g"
                normalizedCategory == "non-veg" -> "g"
                
                // Default to "g"
                else -> "g"
            }
        }
    }
} 