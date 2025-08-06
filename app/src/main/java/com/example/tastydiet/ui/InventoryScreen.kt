package com.example.tastydiet.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.tastydiet.data.models.InventoryItem
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.InventoryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // val scope = rememberCoroutineScope() // Unused
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<InventoryItem?>(null) }
    
    // Track expanded categories
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearErrorMessage()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Inventory Management",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${inventoryItems.size} item${if (inventoryItems.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add Item Button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Item")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Test Data Button (for testing purposes)
            OutlinedButton(
                onClick = { viewModel.populateInventoryWithTestData() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DataUsage, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Test Ingredients")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Inventory List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (inventoryItems.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No inventory items yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add items to your inventory to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val groupedItems = inventoryItems.groupBy { it.category }
                    val sortedCategories = groupedItems.keys.sorted()
                    
                    // Expand/Collapse buttons
                    if (sortedCategories.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Inventory Items",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Row {
                                    TextButton(
                                        onClick = { 
                                            expandedCategories = sortedCategories.toSet()
                                        }
                                    ) {
                                        Icon(Icons.Default.ExpandMore, contentDescription = "Expand all", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Expand All")
                                    }
                                    TextButton(
                                        onClick = { expandedCategories = emptySet() }
                                    ) {
                                        Icon(Icons.Default.ExpandLess, contentDescription = "Collapse all", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Collapse All")
                                    }
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    items(sortedCategories) { category ->
                        val isExpanded = expandedCategories.contains(category)
                        val categoryItems = groupedItems[category] ?: emptyList()
                        
                        // Category header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    expandedCategories = if (isExpanded) {
                                        expandedCategories - category
                                    } else {
                                        expandedCategories + category
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📂 $category (${categoryItems.size} items)", 
                                fontWeight = FontWeight.SemiBold, 
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Category items
                        if (isExpanded) {
                            categoryItems.forEach { item ->
                                CompactInventoryItemCard(
                                    item = item,
                                    onEdit = {
                                        itemToEdit = item
                                        showEditDialog = true
                                    },
                                    onDelete = { viewModel.deleteItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Error Snackbar
        errorMessage?.let { message ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearErrorMessage() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(message)
            }
        }
        
        // Add Dialog
        if (showAddDialog) {
            AddInventoryItemDialog(
                viewModel = viewModel,
                onDismiss = { showAddDialog = false },
                onAdd = { item ->
                    viewModel.addItem(item)
                    showAddDialog = false
                }
            )
        }
        
        // Edit Dialog
        if (showEditDialog && itemToEdit != null) {
            EditInventoryItemDialog(
                viewModel = viewModel,
                item = itemToEdit!!,
                onDismiss = { showEditDialog = false },
                onUpdate = { item ->
                    viewModel.updateItem(item)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryItemDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit,
    onAdd: (InventoryItem) -> Unit
) {
    // val scope = rememberCoroutineScope() // Unused
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("g") }
    var category by remember { mutableStateOf("General") }
    
    // var nameError by remember { mutableStateOf(false) } // Unused
    var quantityError by remember { mutableStateOf(false) }
    
    // Autocomplete state
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var nameField by remember { mutableStateOf(TextFieldValue("")) }
    
    // Focus management
    val nameFocusRequester = remember { FocusRequester() }
    
    // Auto-category and unit assignment based on item name
    val context = LocalContext.current
    LaunchedEffect(nameField.text) {
        val currentText = nameField.text ?: ""
        if (currentText.isNotBlank()) {
            Log.d("DEBUG", "Auto-assigning category/unit for: '$currentText'")
            val (suggestedCategory, suggestedUnit) = viewModel.getAutoCategoryAndUnit(currentText, context)
            
            // Only auto-assign if user hasn't manually set them
            if (category == "General" || category == "Other") {
                category = suggestedCategory
                Log.d("DEBUG", "Auto-assigned category: $suggestedCategory")
            }
            
            // Auto-assign unit if it's still at default value
            if (unit == "g" || unit == "pieces") {
                unit = suggestedUnit
                Log.d("DEBUG", "Auto-assigned unit: $suggestedUnit")
            }
        }
    }
    
    // Auto-focus the name field when dialog opens
    LaunchedEffect(Unit) {
        nameFocusRequester.requestFocus()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Inventory Item") },
        text = {
            Column {
                // Name input with autocomplete suggestions
                ExposedDropdownMenuBox(
                    expanded = showSuggestions && suggestions.isNotEmpty(),
                    onExpandedChange = { showSuggestions = it }
                ) {
                    OutlinedTextField(
                        value = nameField,
                        onValueChange = {
                            Log.d("DEBUG", "TextField onValueChange: '${it.text}'")
                            nameField = it
                            name = it.text ?: ""
                            
                            // Get suggestions as user types
                            if (it.text.isNotBlank()) {
                                suggestions = viewModel.getItemSuggestions(it.text, context, 8)
                                showSuggestions = true
                            } else {
                                suggestions = emptyList()
                                showSuggestions = false
                            }
                        },
                        label = { Text("Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocusRequester)
                            .menuAnchor(),
                        singleLine = true,
                        isError = name.isBlank(),
                        trailingIcon = {
                            Row {
                                // Clear button
                                if (nameField.text.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            nameField = TextFieldValue("")
                                            name = ""
                                            suggestions = emptyList()
                                            showSuggestions = false
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                // Dropdown arrow
                                if (suggestions.isNotEmpty()) {
                                    Icon(
                                        imageVector = if (showSuggestions) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Show suggestions"
                                    )
                                }
                            }
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showSuggestions && suggestions.isNotEmpty(),
                        onDismissRequest = { showSuggestions = false }
                    ) {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = suggestion.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    nameField = TextFieldValue(suggestion)
                                    name = suggestion
                                    suggestions = emptyList()
                                    showSuggestions = false
                                    
                                    // Trigger auto-assignment when suggestion is selected
                                    val (suggestedCategory, suggestedUnit) = viewModel.getAutoCategoryAndUnit(suggestion, context)
                                    if (category == "General" || category == "Other") {
                                        category = suggestedCategory
                                    }
                                    if (unit == "g" || unit == "pieces") {
                                        unit = suggestedUnit
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quantity and unit row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { 
                            quantity = it
                            quantityError = false
                        },
                        label = { Text("Quantity (${unit})") },
                        isError = quantityError,
                        modifier = Modifier.weight(1f)
                    )
                    
                    var unitDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = it },
                        modifier = Modifier.weight(0.5f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            listOf("g", "kg", "ml", "L", "pcs", "cups", "tbsp", "tsp").forEach { unitOption ->
                                DropdownMenuItem(
                                    text = { Text(unitOption) },
                                    onClick = {
                                        unit = unitOption
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Enhanced category dropdown with updated categories
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        listOf(
                            "Vegetables", "Non-Veg", "Eggs", "Dry Fruits", "Grains", 
                            "Dairy", "Spices", "Oils", "Beverages", "Snacks", "Fruits", "Other"
                        ).forEach { categoryOption ->
                            DropdownMenuItem(
                                text = { Text(categoryOption) },
                                onClick = {
                                    category = categoryOption
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            // Validation
            val isNameValid = name.isNotBlank()
            val isQuantityValid = quantity.toFloatOrNull()?.let { it > 0 } == true
            
            Button(
                onClick = {
                    if (isNameValid && isQuantityValid) {
                        val newItem = InventoryItem(
                            name = name.trim(),
                            quantity = quantity.toFloat(),
                            unit = unit,
                            category = category
                        )
                        onAdd(newItem)
                    }
                },
                enabled = isNameValid && isQuantityValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInventoryItemDialog(
    viewModel: InventoryViewModel,
    item: InventoryItem,
    onDismiss: () -> Unit,
    onUpdate: (InventoryItem) -> Unit
) {
    // val scope = rememberCoroutineScope() // Unused
    var name by remember { mutableStateOf(item.name) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var unit by remember { mutableStateOf(item.unit) }
    var category by remember { mutableStateOf(item.category) }
    
    // var nameError by remember { mutableStateOf(false) } // Unused
    var quantityError by remember { mutableStateOf(false) }
    
    // Autocomplete state
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var nameField by remember { mutableStateOf(TextFieldValue(item.name)) }
    
    // Focus management
    val nameFocusRequester = remember { FocusRequester() }
    
    // Auto-category and unit assignment based on item name
    val context = LocalContext.current
    LaunchedEffect(nameField.text) {
        val currentText = nameField.text ?: ""
        if (currentText.isNotBlank()) {
            Log.d("DEBUG", "Edit: Auto-assigning category/unit for: '$currentText'")
            val (suggestedCategory, suggestedUnit) = viewModel.getAutoCategoryAndUnit(currentText, context)
            
            // Only auto-assign if user hasn't manually set them
            if (category == "General" || category == "Other") {
                category = suggestedCategory
                Log.d("DEBUG", "Edit: Auto-assigned category: $suggestedCategory")
            }
            
            // Auto-assign unit if it's still at default value
            if (unit == "g" || unit == "pieces") {
                unit = suggestedUnit
                Log.d("DEBUG", "Edit: Auto-assigned unit: $suggestedUnit")
            }
        }
    }
    
    // Auto-focus the name field when dialog opens
    LaunchedEffect(Unit) {
        nameFocusRequester.requestFocus()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Inventory Item") },
        text = {
            Column {
                // Name input with autocomplete suggestions
                ExposedDropdownMenuBox(
                    expanded = showSuggestions && suggestions.isNotEmpty(),
                    onExpandedChange = { showSuggestions = it }
                ) {
                    OutlinedTextField(
                        value = nameField,
                        onValueChange = {
                            Log.d("DEBUG", "TextField onValueChange: '${it.text}'")
                            nameField = it
                            name = it.text ?: ""
                            
                            // Get suggestions as user types
                            if (it.text.isNotBlank()) {
                                suggestions = viewModel.getItemSuggestions(it.text, context, 8)
                                showSuggestions = true
                            } else {
                                suggestions = emptyList()
                                showSuggestions = false
                            }
                        },
                        label = { Text("Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocusRequester)
                            .menuAnchor(),
                        singleLine = true,
                        isError = name.isBlank(),
                        trailingIcon = {
                            Row {
                                // Clear button
                                if (nameField.text.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            nameField = TextFieldValue("")
                                            name = ""
                                            suggestions = emptyList()
                                            showSuggestions = false
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                // Dropdown arrow
                                if (suggestions.isNotEmpty()) {
                                    Icon(
                                        imageVector = if (showSuggestions) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Show suggestions"
                                    )
                                }
                            }
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showSuggestions && suggestions.isNotEmpty(),
                        onDismissRequest = { showSuggestions = false }
                    ) {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = suggestion.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    nameField = TextFieldValue(suggestion)
                                    name = suggestion
                                    suggestions = emptyList()
                                    showSuggestions = false
                                    
                                    // Trigger auto-assignment when suggestion is selected
                                    val (suggestedCategory, suggestedUnit) = viewModel.getAutoCategoryAndUnit(suggestion, context)
                                    if (category == "General" || category == "Other") {
                                        category = suggestedCategory
                                    }
                                    if (unit == "g" || unit == "pieces") {
                                        unit = suggestedUnit
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quantity and unit row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { 
                            quantity = it
                            quantityError = false
                        },
                        label = { Text("Quantity (${unit})") },
                        isError = quantityError,
                        modifier = Modifier.weight(1f)
                    )
                    
                    var unitDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = it },
                        modifier = Modifier.weight(0.5f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            listOf("g", "kg", "ml", "L", "pcs", "cups", "tbsp", "tsp").forEach { unitOption ->
                                DropdownMenuItem(
                                    text = { Text(unitOption) },
                                    onClick = {
                                        unit = unitOption
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Enhanced category dropdown with updated categories
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        listOf(
                            "Vegetables", "Non-Veg", "Eggs", "Dry Fruits", "Grains", 
                            "Dairy", "Spices", "Oils", "Beverages", "Snacks", "Fruits", "Other"
                        ).forEach { categoryOption ->
                            DropdownMenuItem(
                                text = { Text(categoryOption) },
                                onClick = {
                                    category = categoryOption
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            // Validation
            val isNameValid = name.isNotBlank()
            val isQuantityValid = quantity.toFloatOrNull()?.let { it > 0 } == true
            
            Button(
                onClick = {
                    if (isNameValid && isQuantityValid) {
                        val updatedItem = InventoryItem(
                            id = item.id,
                            name = name.trim(),
                            quantity = quantity.toFloat(),
                            unit = unit,
                            category = category
                        )
                        onUpdate(updatedItem)
                    }
                },
                enabled = isNameValid && isQuantityValid
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CompactInventoryItemCard(
    item: InventoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator based on quantity
            val statusColor = when {
                item.quantity <= 0.5f -> Color(0xFFF44336) // Red for low stock
                item.quantity <= 1f -> Color(0xFFFF9800)   // Orange for medium stock
                else -> Color(0xFF4CAF50)                  // Green for good stock
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = statusColor,
                        shape = RoundedCornerShape(6.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${item.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Show expiry info if available
                if (item.isExpired()) {
                    Text(
                        text = "⚠️ Expired",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (item.isExpiringSoon()) {
                    val daysLeft = item.daysToExpiry()
                    Text(
                        text = "⚠️ Expires in $daysLeft days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}