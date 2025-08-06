package com.example.tastydiet.data

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.InventoryItem
import com.example.tastydiet.data.models.ShoppingListItem
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.FamilyMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Database initializer that populates the nutritional database with default data
 * when the app is first launched.
 */
class DatabaseInitializer {
    
    companion object {
        
        /**
         * Set up the database with initial data
         */
        fun getDatabaseCallback(): RoomDatabase.Callback {
            return object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // This will be called when the database is first created
                    // The actual population will be done in the ViewModel
                }
            }
        }
        
        /**
         * Populate the nutritional database with default data
         * This should be called from a ViewModel or Repository
         */
        suspend fun populateNutritionalDatabase(database: AppDatabase) {
            val nutritionalInfoDao = database.nutritionalInfoDao()
            
            // Check if database is already populated
            val existingCount = nutritionalInfoDao.getAll().first().size
            if (existingCount > 0) {
                return // Database already populated
            }
            
            // Get default nutritional data
            val defaultData = NutritionalDatabase.getDefaultNutritionalData()
            
            // Insert all nutritional data
            defaultData.forEach { nutritionalInfo ->
                nutritionalInfoDao.insert(nutritionalInfo)
            }
        }
        
        /**
         * Populate inventory with comprehensive food items for testing
         */
        suspend fun populateInventoryWithSampleData(database: AppDatabase) {
            val inventoryDao = database.inventoryDao()
            val shoppingListDao = database.shoppingListDao()
            val profileDao = database.profileDao()
            val familyMemberDao = database.familyMemberDao()
            
            // Check if inventory is already populated
            val existingCount = inventoryDao.getCount()
            if (existingCount > 0) {
                return // Inventory already populated
            }
            
            // Add sample family profiles
            val sampleProfiles = listOf(
                Profile(
                    name = "John Doe",
                    age = 30,
                    gender = "Male",
                    weight = 75.0f,
                    height = 175.0f,
                    goal = "Maintenance",
                    targetCalories = 2200f,
                    targetProtein = 165f,
                    targetCarbs = 220f,
                    targetFat = 73f,
                    activityLevel = 1.4f
                ),
                Profile(
                    name = "Jane Smith",
                    age = 28,
                    gender = "Female",
                    weight = 60.0f,
                    height = 165.0f,
                    goal = "Weight Loss",
                    targetCalories = 1800f,
                    targetProtein = 135f,
                    targetCarbs = 180f,
                    targetFat = 60f,
                    activityLevel = 1.2f
                ),
                Profile(
                    name = "Mike Johnson",
                    age = 35,
                    gender = "Male",
                    weight = 80.0f,
                    height = 180.0f,
                    goal = "Muscle Gain",
                    targetCalories = 2500f,
                    targetProtein = 200f,
                    targetCarbs = 250f,
                    targetFat = 83f,
                    activityLevel = 1.6f
                )
            )
            
            // Insert sample profiles
            sampleProfiles.forEach { profile ->
                profileDao.insertProfile(profile)
            }
            
            // Add sample family members
            val sampleFamilyMembers = listOf(
                FamilyMember(
                    name = "John Doe",
                    age = 30,
                    gender = "Male",
                    weight = 75.0f,
                    height = 175.0f,
                    isVeg = false,
                    targetCalories = 2200f,
                    targetProtein = 165f,
                    targetCarbs = 220f,
                    targetFat = 73f
                ),
                FamilyMember(
                    name = "Jane Smith",
                    age = 28,
                    gender = "Female",
                    weight = 60.0f,
                    height = 165.0f,
                    isVeg = true,
                    targetCalories = 1800f,
                    targetProtein = 135f,
                    targetCarbs = 180f,
                    targetFat = 60f
                ),
                FamilyMember(
                    name = "Mike Johnson",
                    age = 35,
                    gender = "Male",
                    weight = 80.0f,
                    height = 180.0f,
                    isVeg = false,
                    targetCalories = 2500f,
                    targetProtein = 200f,
                    targetCarbs = 250f,
                    targetFat = 83f
                )
            )
            
            // Insert sample family members
            sampleFamilyMembers.forEach { member ->
                familyMemberDao.insert(member)
            }
            
            // Comprehensive inventory items for testing
            val inventoryItems = listOf(
                // Vegetables
                InventoryItem(name = "Tomatoes", quantity = 2.5f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Onions", quantity = 1.0f, unit = "kg", category = "Vegetables", shelfLifeDays = 14),
                InventoryItem(name = "Potatoes", quantity = 3.0f, unit = "kg", category = "Vegetables", shelfLifeDays = 21),
                InventoryItem(name = "Carrots", quantity = 1.5f, unit = "kg", category = "Vegetables", shelfLifeDays = 10),
                InventoryItem(name = "Spinach", quantity = 0.5f, unit = "kg", category = "Vegetables", shelfLifeDays = 5),
                InventoryItem(name = "Bell Peppers", quantity = 0.8f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Cucumber", quantity = 1.2f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Broccoli", quantity = 0.6f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Cauliflower", quantity = 0.8f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Green Beans", quantity = 0.4f, unit = "kg", category = "Vegetables", shelfLifeDays = 5),
                
                // Fruits
                InventoryItem(name = "Apples", quantity = 2.0f, unit = "kg", category = "Fruits", shelfLifeDays = 14),
                InventoryItem(name = "Bananas", quantity = 1.5f, unit = "kg", category = "Fruits", shelfLifeDays = 7),
                InventoryItem(name = "Oranges", quantity = 1.8f, unit = "kg", category = "Fruits", shelfLifeDays = 10),
                InventoryItem(name = "Grapes", quantity = 0.8f, unit = "kg", category = "Fruits", shelfLifeDays = 7),
                InventoryItem(name = "Strawberries", quantity = 0.5f, unit = "kg", category = "Fruits", shelfLifeDays = 5),
                InventoryItem(name = "Mangoes", quantity = 1.2f, unit = "kg", category = "Fruits", shelfLifeDays = 7),
                InventoryItem(name = "Pineapple", quantity = 1.0f, unit = "pieces", category = "Fruits", shelfLifeDays = 7),
                InventoryItem(name = "Pears", quantity = 0.8f, unit = "kg", category = "Fruits", shelfLifeDays = 10),
                
                // Grains
                InventoryItem(name = "Rice", quantity = 5.0f, unit = "kg", category = "Grains", shelfLifeDays = 365),
                InventoryItem(name = "Wheat Flour", quantity = 3.0f, unit = "kg", category = "Grains", shelfLifeDays = 180),
                InventoryItem(name = "Oats", quantity = 1.5f, unit = "kg", category = "Grains", shelfLifeDays = 365),
                InventoryItem(name = "Quinoa", quantity = 0.8f, unit = "kg", category = "Grains", shelfLifeDays = 365),
                InventoryItem(name = "Bread", quantity = 2.0f, unit = "loaves", category = "Grains", shelfLifeDays = 7),
                InventoryItem(name = "Pasta", quantity = 1.5f, unit = "kg", category = "Grains", shelfLifeDays = 365),
                InventoryItem(name = "Cornmeal", quantity = 1.0f, unit = "kg", category = "Grains", shelfLifeDays = 180),
                
                // Proteins
                InventoryItem(name = "Chicken Breast", quantity = 2.0f, unit = "kg", category = "Proteins", shelfLifeDays = 3),
                InventoryItem(name = "Eggs", quantity = 24.0f, unit = "pieces", category = "Proteins", shelfLifeDays = 21),
                InventoryItem(name = "Fish", quantity = 1.5f, unit = "kg", category = "Proteins", shelfLifeDays = 2),
                InventoryItem(name = "Beef", quantity = 1.0f, unit = "kg", category = "Proteins", shelfLifeDays = 3),
                InventoryItem(name = "Lentils", quantity = 1.0f, unit = "kg", category = "Proteins", shelfLifeDays = 365),
                InventoryItem(name = "Chickpeas", quantity = 0.8f, unit = "kg", category = "Proteins", shelfLifeDays = 365),
                InventoryItem(name = "Kidney Beans", quantity = 0.6f, unit = "kg", category = "Proteins", shelfLifeDays = 365),
                InventoryItem(name = "Tofu", quantity = 0.5f, unit = "kg", category = "Proteins", shelfLifeDays = 7),
                
                // Dairy
                InventoryItem(name = "Milk", quantity = 2.0f, unit = "liters", category = "Dairy", shelfLifeDays = 7),
                InventoryItem(name = "Cheese", quantity = 0.5f, unit = "kg", category = "Dairy", shelfLifeDays = 14),
                InventoryItem(name = "Yogurt", quantity = 1.0f, unit = "kg", category = "Dairy", shelfLifeDays = 7),
                InventoryItem(name = "Butter", quantity = 0.25f, unit = "kg", category = "Dairy", shelfLifeDays = 30),
                InventoryItem(name = "Cream", quantity = 0.5f, unit = "liters", category = "Dairy", shelfLifeDays = 7),
                InventoryItem(name = "Paneer", quantity = 0.3f, unit = "kg", category = "Dairy", shelfLifeDays = 7),
                
                // Spices
                InventoryItem(name = "Salt", quantity = 0.5f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Black Pepper", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Turmeric", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Cumin", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Coriander", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Ginger", quantity = 0.2f, unit = "kg", category = "Spices", shelfLifeDays = 14),
                InventoryItem(name = "Garlic", quantity = 0.3f, unit = "kg", category = "Spices", shelfLifeDays = 14),
                InventoryItem(name = "Chili Powder", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Cardamom", quantity = 0.05f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Cinnamon", quantity = 0.05f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                
                // Others
                InventoryItem(name = "Olive Oil", quantity = 1.0f, unit = "liters", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Sugar", quantity = 1.0f, unit = "kg", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Honey", quantity = 0.5f, unit = "kg", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Vinegar", quantity = 0.5f, unit = "liters", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Soy Sauce", quantity = 0.3f, unit = "liters", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Tomato Paste", quantity = 0.2f, unit = "kg", category = "Others", shelfLifeDays = 180),
                InventoryItem(name = "Coconut Milk", quantity = 0.4f, unit = "liters", category = "Others", shelfLifeDays = 7),
                InventoryItem(name = "Peanut Butter", quantity = 0.3f, unit = "kg", category = "Others", shelfLifeDays = 180),
                InventoryItem(name = "Jam", quantity = 0.2f, unit = "kg", category = "Others", shelfLifeDays = 180),
                InventoryItem(name = "Mustard", quantity = 0.1f, unit = "kg", category = "Others", shelfLifeDays = 180)
            )
            
            // Insert all inventory items
            inventoryDao.insertAll(inventoryItems)
            
            // Add some low stock items to shopping list for testing
            val shoppingListItems = listOf(
                ShoppingListItem(name = "Milk", quantity = 2.0f, unit = "liters", category = "Dairy", isChecked = false),
                ShoppingListItem(name = "Bread", quantity = 2.0f, unit = "loaves", category = "Grains", isChecked = false),
                ShoppingListItem(name = "Eggs", quantity = 12.0f, unit = "pieces", category = "Proteins", isChecked = false),
                ShoppingListItem(name = "Bananas", quantity = 1.0f, unit = "kg", category = "Fruits", isChecked = false),
                ShoppingListItem(name = "Onions", quantity = 0.5f, unit = "kg", category = "Vegetables", isChecked = false),
                ShoppingListItem(name = "Tomatoes", quantity = 1.0f, unit = "kg", category = "Vegetables", isChecked = false),
                ShoppingListItem(name = "Cheese", quantity = 0.25f, unit = "kg", category = "Dairy", isChecked = false),
                ShoppingListItem(name = "Chicken Breast", quantity = 1.0f, unit = "kg", category = "Proteins", isChecked = false),
                ShoppingListItem(name = "Rice", quantity = 2.0f, unit = "kg", category = "Grains", isChecked = false),
                ShoppingListItem(name = "Olive Oil", quantity = 0.5f, unit = "liters", category = "Others", isChecked = false)
            )
            
            // Insert shopping list items
            shoppingListItems.forEach { item ->
                shoppingListDao.insertItem(item)
            }
        }
        
        /**
         * Get comprehensive sample inventory data
         */
        fun getSampleInventoryData(): List<InventoryItem> {
            return listOf(
                // Vegetables
                InventoryItem(name = "Tomatoes", quantity = 2.5f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Onions", quantity = 1.0f, unit = "kg", category = "Vegetables", shelfLifeDays = 14),
                InventoryItem(name = "Potatoes", quantity = 3.0f, unit = "kg", category = "Vegetables", shelfLifeDays = 21),
                InventoryItem(name = "Carrots", quantity = 1.5f, unit = "kg", category = "Vegetables", shelfLifeDays = 10),
                InventoryItem(name = "Spinach", quantity = 0.5f, unit = "kg", category = "Vegetables", shelfLifeDays = 5),
                InventoryItem(name = "Bell Peppers", quantity = 0.8f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Cucumber", quantity = 1.2f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Broccoli", quantity = 0.6f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Cauliflower", quantity = 0.8f, unit = "kg", category = "Vegetables", shelfLifeDays = 7),
                InventoryItem(name = "Green Beans", quantity = 0.4f, unit = "kg", category = "Vegetables", shelfLifeDays = 5),
                
                // Fruits
                InventoryItem(name = "Apples", quantity = 2.0f, unit = "kg", category = "Fruits", shelfLifeDays = 14),
                InventoryItem(name = "Bananas", quantity = 1.5f, unit = "kg", category = "Fruits", shelfLifeDays = 7),
                InventoryItem(name = "Oranges", quantity = 1.8f, unit = "kg", category = "Fruits", shelfLifeDays = 10),
                InventoryItem(name = "Grapes", quantity = 0.8f, unit = "kg", category = "Fruits", shelfLifeDays = 7),
                InventoryItem(name = "Strawberries", quantity = 0.5f, unit = "kg", category = "Fruits", shelfLifeDays = 5),
                InventoryItem(name = "Mangoes", quantity = 1.2f, unit = "kg", category = "Fruits", shelfLifeDays = 7),
                InventoryItem(name = "Pineapple", quantity = 1.0f, unit = "pieces", category = "Fruits", shelfLifeDays = 7),
                InventoryItem(name = "Pears", quantity = 0.8f, unit = "kg", category = "Fruits", shelfLifeDays = 10),
                
                // Grains
                InventoryItem(name = "Rice", quantity = 5.0f, unit = "kg", category = "Grains", shelfLifeDays = 365),
                InventoryItem(name = "Wheat Flour", quantity = 3.0f, unit = "kg", category = "Grains", shelfLifeDays = 180),
                InventoryItem(name = "Oats", quantity = 1.5f, unit = "kg", category = "Grains", shelfLifeDays = 365),
                InventoryItem(name = "Quinoa", quantity = 0.8f, unit = "kg", category = "Grains", shelfLifeDays = 365),
                InventoryItem(name = "Bread", quantity = 2.0f, unit = "loaves", category = "Grains", shelfLifeDays = 7),
                InventoryItem(name = "Pasta", quantity = 1.5f, unit = "kg", category = "Grains", shelfLifeDays = 365),
                InventoryItem(name = "Cornmeal", quantity = 1.0f, unit = "kg", category = "Grains", shelfLifeDays = 180),
                
                // Proteins
                InventoryItem(name = "Chicken Breast", quantity = 2.0f, unit = "kg", category = "Proteins", shelfLifeDays = 3),
                InventoryItem(name = "Eggs", quantity = 24.0f, unit = "pieces", category = "Proteins", shelfLifeDays = 21),
                InventoryItem(name = "Fish", quantity = 1.5f, unit = "kg", category = "Proteins", shelfLifeDays = 2),
                InventoryItem(name = "Beef", quantity = 1.0f, unit = "kg", category = "Proteins", shelfLifeDays = 3),
                InventoryItem(name = "Lentils", quantity = 1.0f, unit = "kg", category = "Proteins", shelfLifeDays = 365),
                InventoryItem(name = "Chickpeas", quantity = 0.8f, unit = "kg", category = "Proteins", shelfLifeDays = 365),
                InventoryItem(name = "Kidney Beans", quantity = 0.6f, unit = "kg", category = "Proteins", shelfLifeDays = 365),
                InventoryItem(name = "Tofu", quantity = 0.5f, unit = "kg", category = "Proteins", shelfLifeDays = 7),
                
                // Dairy
                InventoryItem(name = "Milk", quantity = 2.0f, unit = "liters", category = "Dairy", shelfLifeDays = 7),
                InventoryItem(name = "Cheese", quantity = 0.5f, unit = "kg", category = "Dairy", shelfLifeDays = 14),
                InventoryItem(name = "Yogurt", quantity = 1.0f, unit = "kg", category = "Dairy", shelfLifeDays = 7),
                InventoryItem(name = "Butter", quantity = 0.25f, unit = "kg", category = "Dairy", shelfLifeDays = 30),
                InventoryItem(name = "Cream", quantity = 0.5f, unit = "liters", category = "Dairy", shelfLifeDays = 7),
                InventoryItem(name = "Paneer", quantity = 0.3f, unit = "kg", category = "Dairy", shelfLifeDays = 7),
                
                // Spices
                InventoryItem(name = "Salt", quantity = 0.5f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Black Pepper", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Turmeric", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Cumin", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Coriander", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Ginger", quantity = 0.2f, unit = "kg", category = "Spices", shelfLifeDays = 14),
                InventoryItem(name = "Garlic", quantity = 0.3f, unit = "kg", category = "Spices", shelfLifeDays = 14),
                InventoryItem(name = "Chili Powder", quantity = 0.1f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Cardamom", quantity = 0.05f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                InventoryItem(name = "Cinnamon", quantity = 0.05f, unit = "kg", category = "Spices", shelfLifeDays = 365),
                
                // Others
                InventoryItem(name = "Olive Oil", quantity = 1.0f, unit = "liters", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Sugar", quantity = 1.0f, unit = "kg", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Honey", quantity = 0.5f, unit = "kg", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Vinegar", quantity = 0.5f, unit = "liters", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Soy Sauce", quantity = 0.3f, unit = "liters", category = "Others", shelfLifeDays = 365),
                InventoryItem(name = "Tomato Paste", quantity = 0.2f, unit = "kg", category = "Others", shelfLifeDays = 180),
                InventoryItem(name = "Coconut Milk", quantity = 0.4f, unit = "liters", category = "Others", shelfLifeDays = 7),
                InventoryItem(name = "Peanut Butter", quantity = 0.3f, unit = "kg", category = "Others", shelfLifeDays = 180),
                InventoryItem(name = "Jam", quantity = 0.2f, unit = "kg", category = "Others", shelfLifeDays = 180),
                InventoryItem(name = "Mustard", quantity = 0.1f, unit = "kg", category = "Others", shelfLifeDays = 180)
            )
        }
    }
} 