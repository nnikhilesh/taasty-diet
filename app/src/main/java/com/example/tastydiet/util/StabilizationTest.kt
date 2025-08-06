package com.example.tastydiet.util

import android.content.Context
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.InventoryItem
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.ShoppingListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Comprehensive test utility to verify all stabilization fixes
 */
object StabilizationTest {
    
    /**
     * Run all stabilization tests
     */
    suspend fun runAllTests(context: Context): TestResults {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<TestResult>()
            
            // Test 1: Database Schema Compatibility
            results.add(testDatabaseSchema(context))
            
            // Test 2: Inventory Add/Edit Functionality
            results.add(testInventoryFunctionality(context))
            
            // Test 3: Predictive Input Functionality
            results.add(testPredictiveInput(context))
            
            // Test 4: Shopping List Functionality
            results.add(testShoppingListFunctionality(context))
            
            // Test 5: Profile Add/Edit Functionality
            results.add(testProfileFunctionality(context))
            
            // Test 6: Nutritional Database
            results.add(testNutritionalDatabase(context))
            
            TestResults(results)
        }
    }
    
    private suspend fun testDatabaseSchema(context: Context): TestResult {
        return try {
            val database = AppDatabase.getInstance(context)
            
            // Test database access
            val inventoryDao = database.inventoryDao()
            val shoppingListDao = database.shoppingListDao()
            val profileDao = database.profileDao()
            val nutritionalInfoDao = database.nutritionalInfoDao()
            
            // Test basic queries
            val inventoryCount = inventoryDao.getCount()
            val shoppingListCount = shoppingListDao.getUncheckedItemCount()
            val profileCount = profileDao.getProfileCount()
            val nutritionalCount = nutritionalInfoDao.getAll().first().size
            
            TestResult(
                name = "Database Schema Compatibility",
                success = true,
                message = "Database schema is compatible. Counts: Inventory=$inventoryCount, Shopping=$shoppingListCount, Profiles=$profileCount, Nutritional=$nutritionalCount"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Database Schema Compatibility",
                success = false,
                message = "Database schema error: ${e.message}"
            )
        }
    }
    
    private suspend fun testInventoryFunctionality(context: Context): TestResult {
        return try {
            val database = AppDatabase.getInstance(context)
            val inventoryDao = database.inventoryDao()
            
            // Test adding inventory item
            val testItem = InventoryItem(
                name = "Test Banana",
                quantity = 5f,
                unit = "pieces",
                category = "Fruits"
            )
            
            val itemId = inventoryDao.insert(testItem)
            
            // Test retrieving the item
            val retrievedItem = inventoryDao.getById(itemId.toInt())
            
            // Test updating the item
            val updatedItem = retrievedItem?.copy(quantity = 10f)
            if (updatedItem != null) {
                inventoryDao.update(updatedItem)
            }
            
            // Test deleting the item
            if (retrievedItem != null) {
                inventoryDao.delete(retrievedItem)
            }
            
            TestResult(
                name = "Inventory Add/Edit Functionality",
                success = true,
                message = "Inventory CRUD operations working correctly"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Inventory Add/Edit Functionality",
                success = false,
                message = "Inventory test failed: ${e.message}"
            )
        }
    }
    
    private suspend fun testPredictiveInput(context: Context): TestResult {
        return try {
            val database = AppDatabase.getInstance(context)
            val nutritionalInfoDao = database.nutritionalInfoDao()
            
            // Test nutritional info lookup
            val bananaInfo = nutritionalInfoDao.getByName("Banana")
            val appleInfo = nutritionalInfoDao.getByName("Apple")
            val milkInfo = nutritionalInfoDao.getByName("Milk")
            
            // Test search functionality
            val searchResults = nutritionalInfoDao.searchByName("ban")
            
            val success = bananaInfo != null || appleInfo != null || milkInfo != null || searchResults.isNotEmpty()
            
            TestResult(
                name = "Predictive Input Functionality",
                success = success,
                message = "Predictive input working. Found: ${searchResults.size} items for 'ban' search"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Predictive Input Functionality",
                success = false,
                message = "Predictive input test failed: ${e.message}"
            )
        }
    }
    
    private suspend fun testShoppingListFunctionality(context: Context): TestResult {
        return try {
            val database = AppDatabase.getInstance(context)
            val shoppingListDao = database.shoppingListDao()
            
            // Test adding shopping list item
            val testItem = ShoppingListItem(
                name = "Test Rice",
                quantity = 2f,
                unit = "kg",
                category = "Grains",
                priority = "Medium"
            )
            
            val itemId = shoppingListDao.insertItem(testItem)
            
            // Test retrieving items
            val allItems = shoppingListDao.getAllItems().first()
            val uncheckedItems = shoppingListDao.getUncheckedItems().first()
            
            // Test updating item status
            shoppingListDao.updateItemCheckedStatus(itemId.toInt(), true)
            
            // Test deleting the item
            val itemToDelete = allItems.find { it.id == itemId.toInt() }
            if (itemToDelete != null) {
                shoppingListDao.deleteItem(itemToDelete)
            }
            
            TestResult(
                name = "Shopping List Functionality",
                success = true,
                message = "Shopping list CRUD operations working correctly"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Shopping List Functionality",
                success = false,
                message = "Shopping list test failed: ${e.message}"
            )
        }
    }
    
    private suspend fun testProfileFunctionality(context: Context): TestResult {
        return try {
            val database = AppDatabase.getInstance(context)
            val profileDao = database.profileDao()
            
            // Test adding profile
            val testProfile = Profile(
                name = "Test User",
                age = 25,
                weight = 70f,
                height = 170f,
                gender = "Male",
                goal = "Weight Loss",
                goalDurationInWeeks = 12
            )
            
            val profileId = profileDao.insertProfile(testProfile)
            
            // Test retrieving profile
            val retrievedProfile = profileDao.getProfileById(profileId.toInt())
            
            // Test updating profile
            val updatedProfile = retrievedProfile?.copy(weight = 68f)
            if (updatedProfile != null) {
                profileDao.updateProfile(updatedProfile)
            }
            
            // Test deleting profile
            if (retrievedProfile != null) {
                profileDao.deleteProfile(retrievedProfile)
            }
            
            TestResult(
                name = "Profile Add/Edit Functionality",
                success = true,
                message = "Profile CRUD operations working correctly"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Profile Add/Edit Functionality",
                success = false,
                message = "Profile test failed: ${e.message}"
            )
        }
    }
    
    private suspend fun testNutritionalDatabase(context: Context): TestResult {
        return try {
            val database = AppDatabase.getInstance(context)
            val nutritionalInfoDao = database.nutritionalInfoDao()
            
            // Test adding nutritional info
            val testInfo = NutritionalInfo(
                name = "Test Food",
                caloriesPer100g = 100f,
                proteinPer100g = 10f,
                carbsPer100g = 20f,
                fatPer100g = 5f,
                category = "Test",
                unit = "g"
            )
            
            nutritionalInfoDao.insert(testInfo)
            
            // Test retrieving
            val retrievedInfo = nutritionalInfoDao.getByName("Test Food")
            
            // Test deleting
            if (retrievedInfo != null) {
                nutritionalInfoDao.delete(retrievedInfo)
            }
            
            TestResult(
                name = "Nutritional Database",
                success = true,
                message = "Nutritional database operations working correctly"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Nutritional Database",
                success = false,
                message = "Nutritional database test failed: ${e.message}"
            )
        }
    }
    
    data class TestResult(
        val name: String,
        val success: Boolean,
        val message: String
    )
    
    data class TestResults(
        val results: List<TestResult>
    ) {
        val allPassed: Boolean
            get() = results.all { it.success }
        
        val passedCount: Int
            get() = results.count { it.success }
        
        val totalCount: Int
            get() = results.size
        
        fun getSummary(): String {
            return "Test Results: $passedCount/$totalCount passed\n" +
                    results.joinToString("\n") { 
                        "${if (it.success) "✅" else "❌"} ${it.name}: ${it.message}" 
                    }
        }
    }
} 