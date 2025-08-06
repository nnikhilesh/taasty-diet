package com.example.tastydiet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.InventoryItem
import com.example.tastydiet.data.models.ShoppingListItem
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedInventoryScreen(
    inventoryItems: List<InventoryItem>,
    shoppingListItems: List<ShoppingListItem>,
    onAddInventoryItem: (String, Float, String) -> Unit,
    onUpdateInventoryQuantity: (Int, Float) -> Unit,
    onDeleteInventoryItem: (Int) -> Unit,
    onAddShoppingItem: (String, Float, String) -> Unit,
    onUpdateShoppingItem: (Int, Boolean) -> Unit,
    onDeleteShoppingItem: (Int) -> Unit,
    onAddToInventoryFromShopping: (List<ShoppingListItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedView by remember { mutableStateOf("Inventory") } // "Inventory" or "Shopping"
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddToInventoryDialog by remember { mutableStateOf(false) }
    
    val categories = listOf("All", "Vegetables", "Fruits", "Grains", "Proteins", "Dairy", "Spices", "Others")
    val context = LocalContext.current
    
    // Filter items based on selected view
    val filteredInventoryItems = inventoryItems.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesSearch = searchQuery.isEmpty() || 
            item.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }
    
    val filteredShoppingItems = shoppingListItems.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesSearch = searchQuery.isEmpty() || 
            item.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }
    
    val checkedShoppingItems = filteredShoppingItems.filter { it.isChecked }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with View Toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // View Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Inventory & Shopping",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row {
                        // View Toggle Buttons
                        Row(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(2.dp)
                        ) {
                            TextButton(
                                onClick = { selectedView = "Inventory" },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (selectedView == "Inventory") 
                                        MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Inventory",
                                    color = if (selectedView == "Inventory") 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            TextButton(
                                onClick = { selectedView = "Shopping" },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (selectedView == "Shopping") 
                                        MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Shopping List",
                                    color = if (selectedView == "Shopping") 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search ${selectedView.lowercase()}...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category Filter
                Text(
                    "Filter by Category:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            leadingIcon = {
                                Icon(
                                    getCategoryIcon(category),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // Quick Actions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Share Button
                InventoryQuickActionButton(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {
                        val text = if (selectedView == "Inventory") {
                            "My Inventory:\n${filteredInventoryItems.joinToString("\n") { "${it.name}: ${it.quantity} ${it.unit}" }}"
                        } else {
                            "My Shopping List:\n${filteredShoppingItems.joinToString("\n") { "${if (it.isChecked) "✅" else "⬜"} ${it.name}: ${it.quantity} ${it.unit}" }}"
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                            setPackage("com.whatsapp")
                        }
                        context.startActivity(intent)
                    }
                )
                
                // Add Item Button
                InventoryQuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Add Item",
                    onClick = { showAddDialog = true }
                )
                
                // Shopping List specific actions
                if (selectedView == "Shopping" && checkedShoppingItems.isNotEmpty()) {
                    InventoryQuickActionButton(
                        icon = Icons.Default.Store,
                        label = "Add to Inventory",
                        onClick = { showAddToInventoryDialog = true }
                    )
                }
                
                // Low Stock Warning
                InventoryQuickActionButton(
                    icon = Icons.Default.Warning,
                    label = "Low Stock",
                    onClick = {
                        // Show low stock items
                        val lowStockItems = inventoryItems.filter { it.quantity < 1 }
                        // This could show a dialog or navigate to low stock view
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Item Count and Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (selectedView == "Inventory") {
                    "${filteredInventoryItems.size} items in ${selectedCategory.lowercase()}"
                } else {
                    "${filteredShoppingItems.size} items (${checkedShoppingItems.size} checked)"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (selectedView == "Shopping" && checkedShoppingItems.isNotEmpty()) {
                TextButton(
                    onClick = { showAddToInventoryDialog = true }
                ) {
                    Text("Add ${checkedShoppingItems.size} to Inventory")
                }
            }
        }
        
        // Items List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedView == "Inventory") {
                items(filteredInventoryItems) { item ->
                    EnhancedInventoryItem(
                        item = item,
                        onUpdateQuantity = onUpdateInventoryQuantity,
                        onDeleteItem = onDeleteInventoryItem,
                        onAddToShopping = { name, quantity, category ->
                            onAddShoppingItem(name, quantity, category)
                        }
                    )
                }
            } else {
                items(filteredShoppingItems) { item ->
                    EnhancedShoppingListItem(
                        item = item,
                        onToggleCheck = { isChecked ->
                            onUpdateShoppingItem(item.id, isChecked)
                        },
                        onDeleteItem = onDeleteShoppingItem
                    )
                }
            }
        }
    }
    
    // Add Item Dialog
    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onAddItem = { name, quantity, category ->
                if (selectedView == "Inventory") {
                    onAddInventoryItem(name, quantity, category)
                } else {
                    onAddShoppingItem(name, quantity, category)
                }
                showAddDialog = false
            },
            title = "Add ${selectedView} Item"
        )
    }
    
    // Add to Inventory Dialog
    if (showAddToInventoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddToInventoryDialog = false },
            title = { Text("Add to Inventory") },
            text = { 
                Text("Add ${checkedShoppingItems.size} checked items to your inventory? This will remove them from your shopping list.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddToInventoryFromShopping(checkedShoppingItems)
                        showAddToInventoryDialog = false
                    }
                ) {
                    Text("Add to Inventory")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddToInventoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedInventoryItem(
    item: InventoryItem,
    onUpdateQuantity: (Int, Float) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onAddToShopping: (String, Float, String) -> Unit
) {
    var showQuantityDialog by remember { mutableStateOf(false) }
    var newQuantity by remember { mutableStateOf(item.quantity.toString()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Icon(
                getCategoryIcon(item.category),
                contentDescription = item.category,
                tint = getCategoryColor(item.category),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Item Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.quantity < 1) Color(0xFFFF5722) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Actions
            Row {
                IconButton(
                    onClick = { showQuantityDialog = true }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Quantity",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = { onAddToShopping(item.name, 1f, item.category) }
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Add to Shopping List",
                        tint = Color(0xFF4CAF50)
                    )
                }
                
                IconButton(
                    onClick = { onDeleteItem(item.id) }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5722)
                    )
                }
            }
        }
    }
    
    // Quantity Update Dialog
    if (showQuantityDialog) {
        AlertDialog(
            onDismissRequest = { showQuantityDialog = false },
            title = { Text("Update Quantity") },
            text = {
                OutlinedTextField(
                    value = newQuantity,
                    onValueChange = { newQuantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val quantity = newQuantity.toFloatOrNull() ?: item.quantity
                        onUpdateQuantity(item.id, quantity)
                        showQuantityDialog = false
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuantityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedShoppingListItem(
    item: ShoppingListItem,
    onToggleCheck: (Boolean) -> Unit,
    onDeleteItem: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) 
                MaterialTheme.colorScheme.surfaceVariant 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onToggleCheck,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Category Icon
            Icon(
                getCategoryIcon(item.category),
                contentDescription = item.category,
                tint = getCategoryColor(item.category),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Item Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (item.isChecked) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Delete Action
            IconButton(
                onClick = { onDeleteItem(item.id) }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFFF5722)
                )
            }
        }
    }
}

@Composable
fun InventoryQuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onAddItem: (String, Float, String) -> Unit,
    title: String
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Vegetables") }
    
    val categories = listOf("Vegetables", "Fruits", "Grains", "Proteins", "Dairy", "Spices", "Others")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Category:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toFloatOrNull() ?: 0f
                    if (name.isNotEmpty() && qty > 0) {
                        onAddItem(name, qty, selectedCategory)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getCategoryIcon(category: String) = when (category) {
    "Vegetables" -> Icons.Default.Eco
    "Fruits" -> Icons.Default.LocalFlorist
    "Grains" -> Icons.Default.Grain
    "Proteins" -> Icons.Default.FitnessCenter
    "Dairy" -> Icons.Default.LocalDrink
    "Spices" -> Icons.Default.Restaurant
    "Others" -> Icons.Default.Category
    else -> Icons.Default.Inventory
}

@Composable
private fun getCategoryColor(category: String) = when (category) {
    "Vegetables" -> Color(0xFF4CAF50)
    "Fruits" -> Color(0xFFFF9800)
    "Grains" -> Color(0xFF795548)
    "Proteins" -> Color(0xFF2196F3)
    "Dairy" -> Color(0xFF9C27B0)
    "Spices" -> Color(0xFFFF5722)
    "Others" -> Color(0xFF607D8B)
    else -> MaterialTheme.colorScheme.primary
} 