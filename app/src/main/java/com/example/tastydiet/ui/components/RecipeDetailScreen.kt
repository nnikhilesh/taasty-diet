package com.example.tastydiet.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.Recipe

@Composable
fun RecipeDetailScreen(recipe: Recipe) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(recipe.name)
        // ... other recipe details ...
        if (!recipe.youtubeUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recipe.youtubeUrl))
                context.startActivity(intent)
            }) {
                Text("Watch Recipe Video")
            }
        }
    }
} 