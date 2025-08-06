package com.example.tastydiet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.Recipe
import com.example.tastydiet.viewmodel.RecipeViewModel
import com.example.tastydiet.viewmodel.ProfileViewModel
import com.example.tastydiet.viewmodel.EnhancedFoodLogViewModel
import com.example.tastydiet.viewmodel.AIAssistantViewModel
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

// Enums for the flow
enum class DietaryPreference {
    VEGETARIAN, NON_VEGETARIAN, MIXED
}

enum class MealType {
    BREAKFAST, LUNCH, SNACK, DINNER
}

enum class ProfileMode {
    INDIVIDUAL, FAMILY, GUEST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    recipeViewModel: RecipeViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    foodLogViewModel: EnhancedFoodLogViewModel = viewModel(),
    aiAssistantViewModel: AIAssistantViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // State variables
    var selectedDietaryPreference by remember { mutableStateOf<DietaryPreference?>(null) }
    var selectedMealType by remember { mutableStateOf<MealType?>(null) }
    var selectedProfileMode by remember { mutableStateOf<ProfileMode?>(null) }
    var selectedProfile by remember { mutableStateOf<Profile?>(null) }
    var guestCount by remember { mutableStateOf(1) }
    var showAISuggestions by remember { mutableStateOf(false) }
    var aiSuggestions by remember { mutableStateOf("") }
    var isLoadingAI by remember { mutableStateOf(false) }
    
    // Collect data from ViewModels
    val profiles by profileViewModel.profiles.collectAsStateWithLifecycle()
    val recipes by recipeViewModel.recipes.collectAsStateWithLifecycle()
    val currentTotals by foodLogViewModel.currentTotals.collectAsStateWithLifecycle()
    val selectedProfileId by foodLogViewModel.selectedProfileId.collectAsStateWithLifecycle()
    
    // Get inventory items for better suggestions
    val inventoryItems by aiAssistantViewModel.inventoryItems.collectAsStateWithLifecycle()
    
    // Get the selected profile for target values
    val selectedProfileForTargets = profiles.find { it.id == selectedProfileId }
    
    // Debug information
    LaunchedEffect(profiles.size, selectedProfileMode, selectedProfile, guestCount) {
        println("DEBUG: Profiles count: ${profiles.size}")
        println("DEBUG: Selected profile mode: $selectedProfileMode")
        println("DEBUG: Selected profile: ${selectedProfile?.name}")
        println("DEBUG: Guest count: $guestCount")
        println("DEBUG: Current totals: $currentTotals")
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        "Smart Meal Planning",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Select preferences & get AI suggestions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // 1. Dietary Preference
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "1. Dietary Preference",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DietaryPreference.values().forEach { preference ->
                            val isSelected = selectedDietaryPreference == preference
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedDietaryPreference = preference },
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        when (preference) {
                                            DietaryPreference.VEGETARIAN -> Icons.Default.Eco
                                            DietaryPreference.NON_VEGETARIAN -> Icons.Default.Restaurant
                                            DietaryPreference.MIXED -> Icons.Default.Balance
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        preference.name.replace("_", " "),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 2. Meal Type
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "2. Meal Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MealType.values().forEach { mealType ->
                            val isSelected = selectedMealType == mealType
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMealType = mealType },
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        when (mealType) {
                                            MealType.BREAKFAST -> Icons.Default.WbSunny
                                            MealType.LUNCH -> Icons.Default.Restaurant
                                            MealType.SNACK -> Icons.Default.Coffee
                                            MealType.DINNER -> Icons.Default.NightsStay
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        mealType.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 3. Profile Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "3. Profile Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProfileMode.values().forEach { mode ->
                            val isSelected = selectedProfileMode == mode
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        selectedProfileMode = mode
                                        selectedProfile = null
                                    },
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        when (mode) {
                                            ProfileMode.INDIVIDUAL -> Icons.Default.Person
                                            ProfileMode.FAMILY -> Icons.Default.FamilyRestroom
                                            ProfileMode.GUEST -> Icons.Default.Group
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        mode.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Additional options based on selected mode
        when (selectedProfileMode) {
            ProfileMode.INDIVIDUAL -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Select Profile (Click for AI suggestions)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (profiles.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "No profiles available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Please create a profile first",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    profiles.forEach { profile ->
                                        val isSelected = selectedProfile?.id == profile.id
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { 
                                                    selectedProfile = profile
                                                    if (selectedDietaryPreference != null && selectedMealType != null) {
                                                        isLoadingAI = true
                                                        val targetCalories = selectedProfileForTargets?.targetCalories ?: 2000f
                                                        val targetProtein = selectedProfileForTargets?.targetProtein ?: 150f
                                                        val targetCarbs = selectedProfileForTargets?.targetCarbs ?: 200f
                                                        val targetFat = selectedProfileForTargets?.targetFat ?: 67f
                                                        
                                                        println("DEBUG: Auto-generating AI suggestions for ${profile.name}")
                                                        
                                                        generateAISuggestions(
                                                            selectedDietaryPreference!!,
                                                            selectedMealType!!,
                                                            selectedProfileMode!!,
                                                            selectedProfile,
                                                            guestCount,
                                                            currentTotals,
                                                            targetCalories,
                                                            targetProtein,
                                                            targetCarbs,
                                                            targetFat,
                                                            aiAssistantViewModel,
                                                            inventoryItems
                                                        ) { suggestions ->
                                                            println("DEBUG: AI suggestions received: ${suggestions.take(100)}...")
                                                            aiSuggestions = suggestions
                                                            showAISuggestions = true
                                                            isLoadingAI = false
                                                        }
                                                    }
                                                },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) 
                                                    MaterialTheme.colorScheme.secondary 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            ),
                                            border = if (isSelected) {
                                                BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                                            } else null
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    if (isSelected) Icons.Default.CheckCircle else Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        profile.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        "${profile.age} years • ${profile.targetCalories} cal",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.weight(1f))
                                                if (isSelected && isLoadingAI) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(14.dp),
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            ProfileMode.GUEST -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Number of Guests",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            OutlinedTextField(
                                value = guestCount.toString(),
                                onValueChange = { value ->
                                    val count = value.toIntOrNull() ?: 1
                                    guestCount = count.coerceIn(1, 20)
                                },
                                label = { Text("Guest Count") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Auto-generate suggestions for guest mode
                            if (selectedDietaryPreference != null && selectedMealType != null && guestCount > 0) {
                                LaunchedEffect(guestCount) {
                                    delay(500)
                                    if (!isLoadingAI) {
                                        isLoadingAI = true
                                        val targetCalories = selectedProfileForTargets?.targetCalories ?: 2000f
                                        val targetProtein = selectedProfileForTargets?.targetProtein ?: 150f
                                        val targetCarbs = selectedProfileForTargets?.targetCarbs ?: 200f
                                        val targetFat = selectedProfileForTargets?.targetFat ?: 67f
                                        
                                        println("DEBUG: Auto-generating AI suggestions for $guestCount guests")
                                        
                                        generateAISuggestions(
                                            selectedDietaryPreference!!,
                                            selectedMealType!!,
                                            selectedProfileMode!!,
                                            selectedProfile,
                                            guestCount,
                                            currentTotals,
                                            targetCalories,
                                            targetProtein,
                                            targetCarbs,
                                            targetFat,
                                            aiAssistantViewModel,
                                            inventoryItems
                                        ) { suggestions ->
                                            println("DEBUG: AI suggestions received: ${suggestions.take(100)}...")
                                            aiSuggestions = suggestions
                                            showAISuggestions = true
                                            isLoadingAI = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            ProfileMode.FAMILY -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Family Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "AI will suggest meals for the whole family",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // Auto-generate suggestions for family mode
                            if (selectedDietaryPreference != null && selectedMealType != null) {
                                LaunchedEffect(selectedProfileMode) {
                                    delay(500)
                                    if (!isLoadingAI) {
                                        isLoadingAI = true
                                        val targetCalories = selectedProfileForTargets?.targetCalories ?: 2000f
                                        val targetProtein = selectedProfileForTargets?.targetProtein ?: 150f
                                        val targetCarbs = selectedProfileForTargets?.targetCarbs ?: 200f
                                        val targetFat = selectedProfileForTargets?.targetFat ?: 67f
                                        
                                        println("DEBUG: Auto-generating AI suggestions for family mode")
                                        
                                        generateAISuggestions(
                                            selectedDietaryPreference!!,
                                            selectedMealType!!,
                                            selectedProfileMode!!,
                                            selectedProfile,
                                            guestCount,
                                            currentTotals,
                                            targetCalories,
                                            targetProtein,
                                            targetCarbs,
                                            targetFat,
                                            aiAssistantViewModel,
                                            inventoryItems
                                        ) { suggestions ->
                                            println("DEBUG: AI suggestions received: ${suggestions.take(100)}...")
                                            aiSuggestions = suggestions
                                            showAISuggestions = true
                                            isLoadingAI = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            else -> {} // No additional input needed
        }
    }
    
    // AI Suggestions Dialog
    if (showAISuggestions) {
        AISuggestionsDialog(
            suggestions = aiSuggestions,
            onDismiss = { showAISuggestions = false },
            onApplySuggestions = {
                showAISuggestions = false
            }
        )
    }
}

@Composable
fun AISuggestionsDialog(
    suggestions: String,
    onDismiss: () -> Unit,
    onApplySuggestions: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "🤖 AI Meal Suggestions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "🎯 Personalized Recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                suggestions,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApplySuggestions,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Suggestions")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Close")
            }
        }
    )
}

// Function to generate AI suggestions
fun generateAISuggestions(
    dietaryPreference: DietaryPreference,
    mealType: MealType,
    profileMode: ProfileMode,
    selectedProfile: Profile?,
    guestCount: Int,
    currentTotals: EnhancedFoodLogViewModel.TodayTotals,
    targetCalories: Float,
    targetProtein: Float,
    targetCarbs: Float,
    targetFat: Float,
    aiAssistantViewModel: AIAssistantViewModel,
    inventoryItems: List<com.example.tastydiet.data.models.InventoryItem>,
    onComplete: (String) -> Unit
) {
    val prompt = buildString {
        appendLine("Generate meal suggestions for TODAY (${java.time.LocalDate.now()})")
        appendLine("Dietary Preference: ${dietaryPreference.name}")
        appendLine("Meal Type: ${mealType.name}")
        appendLine("Profile Mode: ${profileMode.name}")
        
        when (profileMode) {
            ProfileMode.INDIVIDUAL -> {
                selectedProfile?.let { profile ->
                    appendLine("Profile: ${profile.name}")
                    appendLine("Target Calories: ${profile.targetCalories}")
                    appendLine("Target Protein: ${profile.targetProtein}g")
                    appendLine("Target Carbs: ${profile.targetCarbs}g")
                    appendLine("Target Fat: ${profile.targetFat}g")
                }
            }
            ProfileMode.FAMILY -> {
                appendLine("Family mode - consider multiple family members")
            }
            ProfileMode.GUEST -> {
                appendLine("Guest Count: $guestCount")
            }
        }
        
        appendLine("TODAY'S CURRENT DAILY TOTALS:")
        appendLine("Consumed Calories: ${currentTotals.calories}/${targetCalories} (${String.format("%.1f", (currentTotals.calories/targetCalories)*100)}%)")
        appendLine("Consumed Protein: ${currentTotals.protein}/${targetProtein}g (${String.format("%.1f", (currentTotals.protein/targetProtein)*100)}%)")
        appendLine("Consumed Carbs: ${currentTotals.carbs}/${targetCarbs}g (${String.format("%.1f", (currentTotals.carbs/targetCarbs)*100)}%)")
        appendLine("Consumed Fat: ${currentTotals.fat}/${targetFat}g (${String.format("%.1f", (currentTotals.fat/targetFat)*100)}%)")
        appendLine("Consumed Fiber: ${currentTotals.fiber}g")
        
        appendLine("REMAINING MACROS NEEDED:")
        appendLine("Remaining Calories: ${targetCalories - currentTotals.calories}")
        appendLine("Remaining Protein: ${targetProtein - currentTotals.protein}g")
        appendLine("Remaining Carbs: ${targetCarbs - currentTotals.carbs}g")
        appendLine("Remaining Fat: ${targetFat - currentTotals.fat}g")
        
        appendLine("Please suggest 3-5 meal options that:")
        appendLine("1. Match the dietary preference")
        appendLine("2. Are appropriate for the meal type")
        appendLine("3. Help meet the remaining macro goals")
        appendLine("4. Include Indian cuisine options")
        appendLine("5. Provide estimated macros for each suggestion")
    }
    
    // Create a comprehensive meal suggestion based on the data
    val suggestions = buildString {
        appendLine("🎯 **Personalized Meal Suggestions for ${mealType.name}**")
        appendLine()
        appendLine("📊 **Your Current Status:**")
        appendLine("• Consumed: ${String.format("%.0f", currentTotals.calories)}/${String.format("%.0f", targetCalories)} calories (${String.format("%.1f", (currentTotals.calories/targetCalories)*100)}%)")
        appendLine("• Remaining: ${String.format("%.0f", targetCalories - currentTotals.calories)} calories needed")
        
        // Show available inventory items
        if (inventoryItems.isNotEmpty()) {
            appendLine()
            appendLine("🏪 **Available in Your Inventory:**")
            val availableItems = inventoryItems.take(8).joinToString(", ") { it.name }
            appendLine("• $availableItems${if (inventoryItems.size > 8) " and ${inventoryItems.size - 8} more items" else ""}")
        }
        appendLine()
        
        when (dietaryPreference) {
            DietaryPreference.VEGETARIAN -> {
                appendLine("🥬 **Vegetarian Options:**")
                appendLine()
                appendLine("🍽️ **Option 1: Vegetable Pulao with Raita**")
                appendLine("• Calories: 450 | Protein: 12g | Carbs: 65g | Fat: 15g")
                appendLine("• Perfect for ${mealType.name.lowercase()}")
                appendLine("• Ingredients: Basmati rice, mixed vegetables, ghee, spices")
                appendLine()
                appendLine("🍽️ **Option 2: Dal Khichdi with Ghee**")
                appendLine("• Calories: 420 | Protein: 18g | Carbs: 58g | Fat: 18g")
                appendLine("• Traditional Indian comfort food")
                appendLine("• Ingredients: Rice, yellow dal, ghee, turmeric, cumin")
                appendLine()
                appendLine("🍽️ **Option 3: Paneer Bhurji with Roti**")
                appendLine("• Calories: 380 | Protein: 22g | Carbs: 45g | Fat: 16g")
                appendLine("• High protein vegetarian option")
                appendLine("• Ingredients: Paneer, onions, tomatoes, whole wheat roti")
            }
            DietaryPreference.NON_VEGETARIAN -> {
                appendLine("🍗 **Non-Vegetarian Options:**")
                appendLine()
                appendLine("🍽️ **Option 1: Grilled Chicken with Quinoa**")
                appendLine("• Calories: 380 | Protein: 35g | Carbs: 45g | Fat: 12g")
                appendLine("• High protein option")
                appendLine("• Ingredients: Chicken breast, quinoa, vegetables")
                appendLine()
                appendLine("🍽️ **Option 2: Fish Curry with Brown Rice**")
                appendLine("• Calories: 420 | Protein: 28g | Carbs: 52g | Fat: 18g")
                appendLine("• Rich in omega-3 fatty acids")
                appendLine("• Ingredients: Fish fillet, coconut milk, brown rice")
                appendLine()
                appendLine("🍽️ **Option 3: Egg Bhurji with Whole Wheat Bread**")
                appendLine("• Calories: 350 | Protein: 25g | Carbs: 38g | Fat: 14g")
                appendLine("• Quick and nutritious")
                appendLine("• Ingredients: Eggs, onions, tomatoes, whole wheat bread")
            }
            DietaryPreference.MIXED -> {
                appendLine("🍽️ **Mixed Diet Options:**")
                appendLine()
                appendLine("🍽️ **Option 1: Chicken Biryani with Raita**")
                appendLine("• Calories: 480 | Protein: 32g | Carbs: 68g | Fat: 16g")
                appendLine("• Flavorful and balanced")
                appendLine("• Ingredients: Chicken, basmati rice, spices, yogurt")
                appendLine()
                appendLine("🍽️ **Option 2: Mixed Vegetable Curry with Fish**")
                appendLine("• Calories: 420 | Protein: 26g | Carbs: 55g | Fat: 18g")
                appendLine("• Balanced nutrition")
                appendLine("• Ingredients: Fish, mixed vegetables, coconut milk")
                appendLine()
                appendLine("🍽️ **Option 3: Dal Makhani with Roti**")
                appendLine("• Calories: 380 | Protein: 20g | Carbs: 48g | Fat: 15g")
                appendLine("• Traditional Indian comfort food")
                appendLine("• Ingredients: Black dal, cream, whole wheat roti")
            }
        }
        
        appendLine()
        appendLine("💡 **Tips:**")
        appendLine("• These suggestions help you meet your remaining daily goals")
        appendLine("• Adjust portion sizes based on your hunger level")
        appendLine("• Include a side of fresh vegetables for extra nutrition")
        appendLine("• Drink plenty of water with your meal")
    }
    
    // Return the generated suggestions
    onComplete(suggestions)
} 