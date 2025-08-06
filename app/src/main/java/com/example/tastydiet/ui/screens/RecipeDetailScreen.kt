package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.Recipe
import android.content.Intent
import android.net.Uri

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Function to open YouTube with recipe search
    fun openYouTubeSearch(recipeName: String) {
        val searchQuery = Uri.encode("$recipeName recipe cooking")
        val youtubeUrl = "https://www.youtube.com/results?search_query=$searchQuery"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Recipe Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(recipe.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Cuisine: ${recipe.cuisine}")
        Text("Category: ${recipe.category}")
        Spacer(modifier = Modifier.height(12.dp))
        Text("Macros:")
        Text("Calories: ${recipe.caloriesPer100g}")
        Text("Protein: ${recipe.proteinPer100g}g")
        Text("Carbs: ${recipe.carbsPer100g}g")
        Text("Fat: ${recipe.fatPer100g}g")
        Text("Fiber: ${recipe.fiberPer100g}g")
        Spacer(modifier = Modifier.height(12.dp))
        Spacer(modifier = Modifier.height(12.dp))
        
        // Ingredients section
        if (recipe.ingredients.isNotBlank()) {
            Text("Ingredients:", fontWeight = FontWeight.Bold)
            Text(recipe.ingredients)
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Text("Instructions:", fontWeight = FontWeight.Bold)
        Text(recipe.instructions)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // YouTube search button
        Button(
            onClick = { openYouTubeSearch(recipe.name) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "YouTube")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search on YouTube")
        }
        
        if (!recipe.youtubeUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("YouTube: ${recipe.youtubeUrl}", color = MaterialTheme.colorScheme.primary)
        }
    }
}
