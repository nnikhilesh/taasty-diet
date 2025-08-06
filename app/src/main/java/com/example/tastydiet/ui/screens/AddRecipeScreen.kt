package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.RecipeManager
import com.example.tastydiet.data.models.Recipe
import kotlinx.coroutines.launch

// Data class for ingredient parsing
data class IngredientInfo(
    val weight: Float,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float
)

// Function to parse ingredient line and get nutritional info
fun parseIngredientLine(line: String): IngredientInfo? {
    val trimmedLine = line.trim()
    
    // Common ingredient database (simplified)
    val ingredientDatabase = mapOf(
        "rice" to IngredientInfo(100f, 130, 2.7f, 28f, 0.3f, 0.4f),
        "chicken" to IngredientInfo(100f, 165, 31f, 0f, 3.6f, 0f),
        "vegetables" to IngredientInfo(100f, 25, 2f, 5f, 0.2f, 2f),
        "potato" to IngredientInfo(100f, 77, 2f, 17f, 0.1f, 2.2f),
        "tomato" to IngredientInfo(100f, 18, 0.9f, 3.9f, 0.2f, 1.2f),
        "onion" to IngredientInfo(100f, 40, 1.1f, 9.3f, 0.1f, 1.7f),
        "garlic" to IngredientInfo(100f, 149, 6.4f, 33f, 0.5f, 2.1f),
        "ginger" to IngredientInfo(100f, 80, 1.8f, 18f, 0.8f, 2f),
        "oil" to IngredientInfo(100f, 884, 0f, 0f, 100f, 0f),
        "butter" to IngredientInfo(100f, 717, 0.9f, 0.1f, 81f, 0f),
        "milk" to IngredientInfo(100f, 42, 3.4f, 5f, 1f, 0f),
        "egg" to IngredientInfo(100f, 155, 13f, 1.1f, 11f, 0f),
        "bread" to IngredientInfo(100f, 265, 9f, 49f, 3.2f, 2.7f),
        "dal" to IngredientInfo(100f, 116, 9f, 20f, 0.4f, 7.9f),
        "paneer" to IngredientInfo(100f, 321, 18f, 3.6f, 27f, 0f),
        "curd" to IngredientInfo(100f, 59, 10f, 3.6f, 0.4f, 0f),
        "flour" to IngredientInfo(100f, 364, 10f, 76f, 1f, 2.7f),
        "sugar" to IngredientInfo(100f, 387, 0f, 100f, 0f, 0f),
        "salt" to IngredientInfo(100f, 0, 0f, 0f, 0f, 0f),
        "pepper" to IngredientInfo(100f, 251, 10f, 64f, 3.3f, 25f)
    )
    
    // Try to extract weight and ingredient name
    val weightPattern = Regex("""(\d+(?:\.\d+)?)\s*(g|gram|grams|kg|kilo|kilos)?\s*(.+)""", RegexOption.IGNORE_CASE)
    val match = weightPattern.find(trimmedLine)
    
    if (match != null) {
        val weightStr = match.groupValues[1]
        val unit = match.groupValues[2].lowercase()
        val ingredientName = match.groupValues[3].trim().lowercase()
        
        var weight = weightStr.toFloatOrNull() ?: return null
        
        // Convert to grams if needed
        if (unit.contains("kg") || unit.contains("kilo")) {
            weight *= 1000
        }
        
        // Find matching ingredient in database
        val matchingIngredient = ingredientDatabase.entries.find { (key, _) ->
            ingredientName.contains(key)
        }
        
        if (matchingIngredient != null) {
            val baseInfo = matchingIngredient.value
            val ratio = weight / 100f
            
            return IngredientInfo(
                weight = weight,
                calories = (baseInfo.calories * ratio).toInt(),
                protein = baseInfo.protein * ratio,
                carbs = baseInfo.carbs * ratio,
                fat = baseInfo.fat * ratio,
                fiber = baseInfo.fiber * ratio
            )
        }
    }
    
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    recipeManager: RecipeManager,
    onRecipeAdded: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    
    // Function to calculate macros from ingredients
    fun calculateMacrosFromIngredients(ingredientsText: String) {
        if (ingredientsText.isBlank()) return
        
        val lines = ingredientsText.split("\n").filter { it.isNotBlank() }
        var totalCalories = 0
        var totalProtein = 0f
        var totalCarbs = 0f
        var totalFat = 0f
        var totalFiber = 0f
        var totalWeight = 0f
        
        // Debug: Print each ingredient being processed
        lines.forEach { line ->
            val ingredient = parseIngredientLine(line)
            if (ingredient != null) {
                println("DEBUG: ${ingredient.weight}g ingredient = ${ingredient.calories} calories")
                totalCalories += ingredient.calories
                totalProtein += ingredient.protein
                totalCarbs += ingredient.carbs
                totalFat += ingredient.fat
                totalFiber += ingredient.fiber
                totalWeight += ingredient.weight
            }
        }
        
        println("DEBUG: Total calories = $totalCalories, Total weight = $totalWeight")
        
        // Calculate both total and per 100g values
        if (totalWeight > 0) {
            // Calculate per 100g values for storage
            val caloriesPer100g = (totalCalories * 100 / totalWeight).toInt()
            val proteinPer100g = (totalProtein * 100 / totalWeight)
            val carbsPer100g = (totalCarbs * 100 / totalWeight)
            val fatPer100g = (totalFat * 100 / totalWeight)
            val fiberPer100g = (totalFiber * 100 / totalWeight)
            
            // Show total values in UI (what user sees while creating)
            println("DEBUG: Total calories for recipe = $totalCalories")
            println("DEBUG: Calories per 100g = $caloriesPer100g")
            calories = totalCalories.toString()
            protein = String.format("%.1f", totalProtein)
            carbs = String.format("%.1f", totalCarbs)
            fat = String.format("%.1f", totalFat)
            fiber = String.format("%.1f", totalFiber)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Add New Recipe",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error/Success messages
        errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        successMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Recipe Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Recipe Name *") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Category and Cuisine Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category *") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
            )
            
            OutlinedTextField(
                value = cuisine,
                onValueChange = { cuisine = it },
                label = { Text("Cuisine *") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Nutritional Information
        Text(
            text = "Nutritional Information (Total Recipe Values)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Shows total values for your recipe. Will be stored as per 100g in database.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Calories *") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null) }
            )
            
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("Protein (g)") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it },
                label = { Text("Carbs (g)") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Grain, contentDescription = null) }
            )
            
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("Fat (g)") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Opacity, contentDescription = null) }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = fiber,
                onValueChange = { fiber = it },
                label = { Text("Fiber (g)") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Park, contentDescription = null) }
            )
            
            OutlinedTextField(
                value = servings,
                onValueChange = { servings = it },
                label = { Text("Servings") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.People, contentDescription = null) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ingredients
        OutlinedTextField(
            value = ingredients,
            onValueChange = { 
                ingredients = it
                // Auto-calculate macros when ingredients change
                calculateMacrosFromIngredients(it)
            },
            label = { Text("Ingredients (one per line) *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            leadingIcon = { Icon(Icons.Default.List, contentDescription = null) },
            placeholder = { Text("e.g.,\n100g rice\n50g chicken\n30g vegetables") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Instructions
        OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Cooking Instructions *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 8,
            leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Auto-calculate button
        OutlinedButton(
            onClick = { calculateMacrosFromIngredients(ingredients) },
            modifier = Modifier.fillMaxWidth(),
            enabled = ingredients.isNotBlank()
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Auto-Calculate Macros from Ingredients")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Save Button
        Button(
            onClick = {
                isSaving = true
                errorMessage = null
                successMessage = null
                
                                 // Create recipe object
                 // Recalculate per 100g values for storage
                 val totalWeight = ingredients.split("\n")
                     .filter { it.isNotBlank() }
                     .sumOf { line ->
                         val ingredient = parseIngredientLine(line)
                         ingredient?.weight?.toDouble() ?: 0.0
                     }.toFloat()
                 
                 val totalCalories = calories.toIntOrNull() ?: 0
                 val totalProtein = protein.toFloatOrNull() ?: 0f
                 val totalCarbs = carbs.toFloatOrNull() ?: 0f
                 val totalFat = fat.toFloatOrNull() ?: 0f
                 val totalFiber = fiber.toFloatOrNull() ?: 0f
                 
                 val caloriesPer100g = if (totalWeight > 0) (totalCalories * 100 / totalWeight).toInt() else 0
                 val proteinPer100g = if (totalWeight > 0) (totalProtein * 100 / totalWeight) else 0f
                 val carbsPer100g = if (totalWeight > 0) (totalCarbs * 100 / totalWeight) else 0f
                 val fatPer100g = if (totalWeight > 0) (totalFat * 100 / totalWeight) else 0f
                 val fiberPer100g = if (totalWeight > 0) (totalFiber * 100 / totalWeight) else 0f
                 
                 val recipe = Recipe(
                     id = 0, // Will be set by RecipeManager
                     name = name.trim(),
                     category = category.trim(),
                     cuisine = cuisine.trim(),
                     caloriesPer100g = caloriesPer100g,
                     proteinPer100g = proteinPer100g,
                     carbsPer100g = carbsPer100g,
                     fatPer100g = fatPer100g,
                     fiberPer100g = fiberPer100g,
                     instructions = instructions.trim(),
                     ingredients = ingredients.trim(),
                     liked = null,
                     youtubeUrl = null
                 )
                
                // Validate recipe
                val validation = recipeManager.validateRecipe(recipe)
                when (validation) {
                    is RecipeManager.RecipeValidationResult.Success -> {
                        // Save recipe using coroutines
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            try {
                                val success = recipeManager.addCustomRecipe(recipe)
                                if (success) {
                                    successMessage = "Recipe added successfully!"
                                    onRecipeAdded()
                                } else {
                                    errorMessage = "Failed to save recipe. Please try again."
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error saving recipe: ${e.message}"
                            }
                        }
                    }
                    is RecipeManager.RecipeValidationResult.Error -> {
                        errorMessage = validation.message
                    }
                }
                isSaving = false
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && name.isNotBlank() && category.isNotBlank() && 
                     cuisine.isNotBlank() && calories.isNotBlank() && instructions.isNotBlank() && ingredients.isNotBlank()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isSaving) "Saving..." else "Save Recipe")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Add Templates
        Text(
            text = "Quick Add Templates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                         OutlinedButton(
                 onClick = {
                     name = "My Custom Recipe"
                     category = "Dinner"
                     cuisine = "Andhra Pradesh"
                     calories = "212" // Total calories: 130+82.5+7.5+88.4+12.6 = 321
                     protein = "35.7" // Total protein: 2.7+15.5+0.6+0+0.5 = 19.3
                     carbs = "28.4" // Total carbs: 28+0+1.5+0+3.2 = 32.7
                     fat = "8.9" // Total fat: 0.3+1.8+0.06+10+0.17 = 12.33
                     fiber = "0.4" // Total fiber: 0.4+0+0.6+0+1.25 = 2.25
                     servings = "4"
                     ingredients = "100g rice\n50g chicken\n30g vegetables\n10g oil\n5g spices"
                     instructions = "1. Prepare ingredients\n2. Cook according to your method\n3. Serve hot"
                 },
                 modifier = Modifier.weight(1f)
             ) {
                 Text("Andhra Template")
             }
            
                         OutlinedButton(
                 onClick = {
                     name = "My Custom Recipe"
                     category = "Dinner"
                     cuisine = "North Indian"
                     calories = "364" // Total calories: 364+160.5+7.5+132.6+12.6 = 677.2
                     protein = "18.9" // Total protein: 10+9+0.6+0+0.5 = 20.1
                     carbs = "76.3" // Total carbs: 76+1.8+1.5+0+3.2 = 82.5
                     fat = "27.2" // Total fat: 1+13.5+0.06+15+0.17 = 29.73
                     fiber = "2.7" // Total fiber: 2.7+0+0.6+0+1.25 = 4.55
                     servings = "4"
                     ingredients = "100g flour\n50g paneer\n30g vegetables\n15g oil\n5g spices"
                     instructions = "1. Prepare ingredients\n2. Cook according to your method\n3. Serve hot"
                 },
                 modifier = Modifier.weight(1f)
             ) {
                 Text("North Indian Template")
             }
        }
    }
} 