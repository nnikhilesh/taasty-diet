package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastydiet.viewmodel.GroceryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryScreen(
    groceryViewModel: GroceryViewModel = viewModel()
) {
    val groceryItems by groceryViewModel.groceryItems.collectAsState()
    val categories by groceryViewModel.categories.collectAsState()
    val isLoading by groceryViewModel.isLoading.collectAsState()
    val errorMessage by groceryViewModel.errorMessage.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grocery Items (${groceryItems.size})",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { groceryViewModel.reloadGroceryItems() },
                enabled = !isLoading
            ) {
                Text("Reload")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Status messages
        if (isLoading) {
            Text(
                text = "Loading grocery items...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                if (it.isNotEmpty()) {
                    groceryViewModel.searchGroceryItems(it)
                } else {
                    groceryViewModel.loadGroceryItems()
                }
            },
            label = { Text("Search grocery items...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category filter
        if (categories.isNotEmpty()) {
            Text(
                text = "Categories:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = {
                            selectedCategory = null
                            groceryViewModel.loadGroceryItems()
                        },
                        label = { Text("All") }
                    )
                }
                
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            groceryViewModel.getGroceryItemsByCategory(category)
                        },
                        label = { Text(category) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Grocery items list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groceryItems) { groceryItem ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = groceryItem.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Category: ${groceryItem.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Unit: ${groceryItem.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
} 