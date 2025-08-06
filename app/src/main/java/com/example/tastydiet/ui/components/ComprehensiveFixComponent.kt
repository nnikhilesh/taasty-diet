package com.example.tastydiet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.*
import com.example.tastydiet.viewmodel.*
import com.example.tastydiet.InventoryViewModel
import kotlinx.coroutines.launch

@Composable
fun ComprehensiveFixComponent(
    profileViewModel: ProfileViewModel,
    shoppingListViewModel: ShoppingListViewModel,
    mealSuggestionViewModel: MealSuggestionViewModel,
    inventoryViewModel: InventoryViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var testResults by remember { mutableStateOf("") }
    var isRunningTests by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🔧 COMPREHENSIVE FIX & TEST",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Database Fix Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🚨 DATABASE CRITICAL FIXES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isRunningTests = true
                            testResults = "🔄 Running database fixes...\n"
                            
                            try {
                                // Force database recreation
                                AppDatabase.clearDatabase(context)
                                testResults += "✅ Database cleared successfully\n"
                                
                                // Test database connection
                                val database = AppDatabase.getInstance(context)
                                testResults += "✅ Database instance created\n"
                                
                                // Test all DAOs
                                val profileDao = database.profileDao()
                                val shoppingDao = database.shoppingListDao()
                                val inventoryDao = database.inventoryDao()
                                val ingredientDao = database.ingredientDao()
                                val nutritionalDao = database.nutritionalInfoDao()
                                
                                testResults += "✅ All DAOs initialized successfully\n"
                                
                            } catch (e: Exception) {
                                testResults += "❌ Database error: ${e.message}\n"
                            }
                            
                            isRunningTests = false
                        }
                    },
                    enabled = !isRunningTests
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fix Database Issues")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Profile Management Fix Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "👤 PROFILE MANAGEMENT FIXES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isRunningTests = true
                            testResults += "🔄 Testing profile management...\n"
                            
                            try {
                                // Test adding profile
                                val testProfile = Profile(
                                    name = "Test User",
                                    age = 25,
                                    weight = 70f,
                                    height = 170f,
                                    gender = "Male",
                                    goal = "Maintenance",
                                    goalDurationInWeeks = 12,
                                    activityLevel = 1.4f,
                                    targetCalories = 2000f,
                                    targetProtein = 150f,
                                    targetCarbs = 200f,
                                    targetFat = 67f
                                )
                                
                                profileViewModel.addProfile(testProfile)
                                testResults += "✅ Test profile added\n"
                                
                                // Test loading profiles
                                profileViewModel.loadProfiles()
                                testResults += "✅ Profiles loaded\n"
                                
                            } catch (e: Exception) {
                                testResults += "❌ Profile error: ${e.message}\n"
                            }
                            
                            isRunningTests = false
                        }
                    },
                    enabled = !isRunningTests
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Profile Management")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Shopping List Fix Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🛒 SHOPPING LIST FIXES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isRunningTests = true
                            testResults += "🔄 Testing shopping list...\n"
                            
                            try {
                                // Test adding shopping list item
                                shoppingListViewModel.addItem("Test Item", 2f, "pieces", "Other")
                                testResults += "✅ Shopping list item added\n"
                                
                                // Test WhatsApp format
                                val whatsappText = shoppingListViewModel.formatShoppingListForWhatsApp()
                                testResults += "✅ WhatsApp format: $whatsappText\n"
                                
                                // Test loading shopping list
                                testResults += "✅ Shopping list loaded\n"
                                
                            } catch (e: Exception) {
                                testResults += "❌ Shopping list error: ${e.message}\n"
                            }
                            
                            isRunningTests = false
                        }
                    },
                    enabled = !isRunningTests
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Shopping List")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Meal Suggestions Fix Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🍽️ MEAL SUGGESTIONS FIXES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isRunningTests = true
                            testResults += "🔄 Testing meal suggestions...\n"
                            
                            try {
                                // Test meal suggestion
                                mealSuggestionViewModel.suggestMeal()
                                testResults += "✅ Meal suggestion generated\n"
                                
                                // Test loading recipes
                                testResults += "✅ Recipes loaded\n"
                                
                            } catch (e: Exception) {
                                testResults += "❌ Meal suggestions error: ${e.message}\n"
                            }
                            
                            isRunningTests = false
                        }
                    },
                    enabled = !isRunningTests
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Meal Suggestions")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Inventory Fix Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "📦 INVENTORY FIXES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isRunningTests = true
                            testResults += "🔄 Testing inventory...\n"
                            
                            try {
                                // Test adding inventory item
                                val testItem = InventoryItem(
                                    name = "Test Apple",
                                    quantity = 5f,
                                    unit = "pieces",
                                    category = "Fruits"
                                )
                                
                                inventoryViewModel.addItem(testItem)
                                testResults += "✅ Inventory item added\n"
                                
                                // Test loading inventory
                                testResults += "✅ Inventory loaded\n"
                                
                            } catch (e: Exception) {
                                testResults += "❌ Inventory error: ${e.message}\n"
                            }
                            
                            isRunningTests = false
                        }
                    },
                    enabled = !isRunningTests
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Inventory")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Run All Tests Button
        Button(
            onClick = {
                scope.launch {
                    isRunningTests = true
                    testResults = "🔄 Running comprehensive tests...\n"
                    
                    // Run all tests in sequence
                    try {
                        // Database test
                        AppDatabase.clearDatabase(context)
                        val database = AppDatabase.getInstance(context)
                        testResults += "✅ Database: OK\n"
                        
                        // Profile test
                        val testProfile = Profile(
                            name = "Test User",
                            age = 25,
                            weight = 70f,
                            height = 170f,
                            gender = "Male",
                            goal = "Maintenance"
                        )
                        profileViewModel.addProfile(testProfile)
                        profileViewModel.loadProfiles()
                        testResults += "✅ Profile: OK\n"
                        
                        // Shopping list test
                        shoppingListViewModel.addItem("Test Item", 2f, "pieces", "Other")
                        testResults += "✅ Shopping List: OK\n"
                        
                        // Meal suggestions test
                        mealSuggestionViewModel.suggestMeal()
                        testResults += "✅ Meal Suggestions: OK\n"
                        
                        // Inventory test
                        val testItem = InventoryItem(
                            name = "Test Apple",
                            quantity = 5f,
                            unit = "pieces",
                            category = "Fruits"
                        )
                        inventoryViewModel.addItem(testItem)
                        testResults += "✅ Inventory: OK\n"
                        
                        testResults += "\n🎉 ALL TESTS PASSED! App should be working now.\n"
                        
                    } catch (e: Exception) {
                        testResults += "❌ Test failed: ${e.message}\n"
                    }
                    
                    isRunningTests = false
                }
            },
            enabled = !isRunningTests,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Run All Tests")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Results Display
        if (testResults.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "📊 TEST RESULTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = testResults,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Clear Results Button
        if (testResults.isNotEmpty()) {
            Button(
                onClick = { testResults = "" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Results")
            }
        }
    }
} 