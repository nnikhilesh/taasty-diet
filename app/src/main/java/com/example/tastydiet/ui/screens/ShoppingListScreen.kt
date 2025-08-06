package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastydiet.viewmodel.ShoppingListViewModel

import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    shoppingListViewModel: ShoppingListViewModel = viewModel()
) {
    val context = LocalContext.current
    val shoppingListItems by shoppingListViewModel.shoppingListItems.collectAsState()
    val uncheckedItems by shoppingListViewModel.uncheckedItems.collectAsState()
    val categories by shoppingListViewModel.categories.collectAsState()
    val summary by shoppingListViewModel.summary.collectAsState()
    val isLoading by shoppingListViewModel.isLoading.collectAsState()
    val errorMessage by shoppingListViewModel.errorMessage.collectAsState()
    val lowInventorySuggestions by shoppingListViewModel.lowInventorySuggestions.collectAsState()
    
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf(false) }
    var showLowInventoryDialog by remember { mutableStateOf(false) }
    var showAddToInventoryDialog by remember { mutableStateOf(false) }
    var selectedItemForEdit by remember { mutableStateOf<com.example.tastydiet.data.models.ShoppingListItem?>(null) }
    
    // Dialog state variables
    var dialogName by remember { mutableStateOf("") }
    var dialogQuantity by remember { mutableStateOf("1") }
    var dialogUnit by remember { mutableStateOf("pieces") }
    var dialogCategory by remember { mutableStateOf("Other") }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            kotlinx.coroutines.delay(3000)
            shoppingListViewModel.clearErrorMessage()
        }
    }
    
    // Refresh low inventory suggestions when screen becomes visible
    LaunchedEffect(Unit) {
        shoppingListViewModel.refreshLowInventorySuggestions()
    }
    
    var expandShopping by remember { mutableStateOf(true) }
    var expandSuggestions by remember { mutableStateOf(false) }
    var expandShare by remember { mutableStateOf(false) }
    
    // Track expanded categories
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    val groupedItems = shoppingListItems
        .distinctBy { it.name.lowercase().trim() }
        .groupBy { it.category }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            
            // 🛒 Card 1: Shopping List
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expandShopping = !expandShopping },
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛒 Shopping List", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(
                            onClick = { 
                                shoppingListViewModel.refreshShoppingList()
                                shoppingListViewModel.refreshLowInventorySuggestions()
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        Button(
                            onClick = { showAddItemDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item", fontSize = 12.sp)
                        }
                    }
                }
                AnimatedVisibility(visible = expandShopping) {
                    Column {
                        // Debug info
                        Text(
                            "Debug: ${shoppingListItems.size} items, Loading: $isLoading",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        // Help text
                        Text(
                            "💡 Tip: Check items as you buy them, then 'Add to Inventory' to automatically add them to your inventory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        if (shoppingListItems.isEmpty()) {
                            Column {
                                Text(
                                    "Shopping list is empty",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                
                                // Test button to add sample item
                                Button(
                                    onClick = {
                                        shoppingListViewModel.addItem("Test Item", 1f, "pieces", "Other")
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Add Test Item")
                                }
                            }
                        } else {
                            // Action buttons row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Expand/Collapse buttons
                                Row {
                                    TextButton(
                                        onClick = { 
                                            expandedCategories = groupedItems.keys.toSet()
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
                                
                                // Add to Inventory button
                                if (shoppingListItems.any { it.isChecked }) {
                                    TextButton(
                                        onClick = { showAddToInventoryDialog = true }
                                    ) {
                                        Icon(Icons.Default.Store, contentDescription = "Add to inventory", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add to Inventory (${shoppingListItems.count { it.isChecked }})")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Scrollable shopping list with expandable categories
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 400.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                groupedItems.forEach { (category, items) ->
                                    val isExpanded = expandedCategories.contains(category)
                                    
                                    item {
                                        // Expandable category header
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
                                                "📂 $category (${items.size} items)", 
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
                                    }
                                    
                                    if (isExpanded) {
                                        items(items) { item ->
                                            ShoppingListItemCard(
                                                item = item,
                                                onCheckedChange = { isChecked ->
                                                    shoppingListViewModel.toggleItemChecked(item.id, isChecked)
                                                },
                                                onEdit = { 
                                                    selectedItemForEdit = item
                                                    showEditItemDialog = true 
                                                },
                                                onDelete = { shoppingListViewModel.deleteItem(item) }
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

        Spacer(modifier = Modifier.height(16.dp))

        // 📉 Card 2: Suggestions from Low Inventory
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expandSuggestions = !expandSuggestions },
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📉 Suggestions from Low Inventory", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${lowInventorySuggestions.size} items", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AnimatedVisibility(visible = expandSuggestions) {
                    Column {
                        if (lowInventorySuggestions.isEmpty()) {
                            Column {
                                Text(
                                    "No low inventory items found. Add items to your inventory first.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                
                                // Add sample data button
                                Button(
                                    onClick = { shoppingListViewModel.addSampleInventoryData() },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add sample data", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Sample Inventory")
                                }
                            }
                        } else {
                            // Group suggestions by category
                            val groupedSuggestions = lowInventorySuggestions
                                .filter { suggestion -> 
                                    shoppingListItems.none { it.name.equals(suggestion.inventoryItem.name, ignoreCase = true) }
                                }
                                .groupBy { it.inventoryItem.category }
                            
                            // Add selected items button
                            val selectedCount = lowInventorySuggestions.count { it.isSelected }
                            if (selectedCount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Selected: $selectedCount items",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Button(
                                        onClick = { shoppingListViewModel.addSelectedLowInventoryItemsToShoppingList() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("Add Selected", fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            // Scrollable suggestions list
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 400.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                groupedSuggestions.forEach { (category, suggestions) ->
                                    // Category header
                                    item {
                                        Text(
                                            "📂 $category",
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    // Category items
                                    items(suggestions) { suggestion ->
                                        LowInventorySuggestionItem(
                                            suggestion = suggestion,
                                            onToggleSelection = { shoppingListViewModel.toggleLowInventorySelection(suggestion.inventoryItem.id) },
                                            onSnooze = { days -> shoppingListViewModel.snoozeLowInventoryItem(suggestion.inventoryItem.id, days) },
                                            onAddToShoppingList = { shoppingListViewModel.addLowInventoryItemToShoppingList(suggestion.inventoryItem) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 📤 Card 3: Share in WhatsApp
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expandShare = !expandShare },
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📤 Share in WhatsApp", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${shoppingListItems.size} items", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AnimatedVisibility(visible = expandShare) {
                    Column {
                        if (shoppingListItems.isEmpty()) {
                            Text(
                                "Add items to your shopping list first to share.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val shoppingListText = shoppingListViewModel.formatShoppingListForWhatsApp()
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shoppingListText)
                                        setPackage("com.whatsapp")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // WhatsApp not installed, show toast
                                        android.widget.Toast.makeText(
                                            context,
                                            "WhatsApp not installed. Copying to clipboard instead.",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        // Copy to clipboard as fallback
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Shopping List", shoppingListText)
                                        clipboard.setPrimaryClip(clip)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366) // WhatsApp green
                                )
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share via WhatsApp",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share Now")
                            }
                        }
                    }
                }
            }
        }
        
        // Add Item Dialog
        if (showAddItemDialog) {
            val nameFocusRequester = remember { FocusRequester() }
            var nameField by remember { mutableStateOf(TextFieldValue(dialogName)) }
            var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
            var showSuggestions by remember { mutableStateOf(false) }
            
            // Auto-category and unit assignment based on item name
            LaunchedEffect(nameField.text) {
                val currentText = nameField.text ?: ""
                if (currentText.isNotBlank()) {
                    val (suggestedCategory, suggestedUnit) = shoppingListViewModel.getAutoCategoryAndUnit(currentText, context)
                    
                    // Only auto-assign if user hasn't manually set them
                    if (dialogCategory == "Other") {
                        dialogCategory = suggestedCategory
                    }
                    
                    if (dialogUnit == "pieces") {
                        dialogUnit = suggestedUnit
                    }
                }
            }
            
            LaunchedEffect(Unit) {
                nameFocusRequester.requestFocus()
            }
            
            AlertDialog(
                onDismissRequest = { showAddItemDialog = false },
                title = { Text("Add Shopping Item") },
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
                                    nameField = it
                                    dialogName = it.text ?: ""
                                    
                                    // Get suggestions as user types
                                    if (it.text.isNotBlank()) {
                                        suggestions = shoppingListViewModel.getItemSuggestions(it.text, context, 8)
                                        showSuggestions = true
                                    } else {
                                        suggestions = emptyList()
                                        showSuggestions = false
                                    }
                                },
                                label = { Text("Item Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(nameFocusRequester)
                                    .menuAnchor(),
                                singleLine = true,
                                trailingIcon = {
                                    Row {
                                        // Clear button
                                        if (nameField.text.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    nameField = TextFieldValue("")
                                                    dialogName = ""
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
                                            dialogName = suggestion
                                            suggestions = emptyList()
                                            showSuggestions = false
                                            
                                            // Trigger auto-assignment when suggestion is selected
                                            val (suggestedCategory, suggestedUnit) = shoppingListViewModel.getAutoCategoryAndUnit(suggestion, context)
                                            if (dialogCategory == "Other") {
                                                dialogCategory = suggestedCategory
                                            }
                                            if (dialogUnit == "pieces") {
                                                dialogUnit = suggestedUnit
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row {
                            OutlinedTextField(
                                value = dialogQuantity,
                                onValueChange = { dialogQuantity = it },
                                label = { Text("Quantity") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            var unitDropdownExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = unitDropdownExpanded,
                                onExpandedChange = { unitDropdownExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = dialogUnit,
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
                                                dialogUnit = unitOption
                                                unitDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var categoryExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = dialogCategory,
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
                                            dialogCategory = categoryOption
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val nameText = dialogName.trim()
                            val quantityValue = dialogQuantity.toFloatOrNull() ?: 1f
                            if (nameText.isNotEmpty()) {
                                shoppingListViewModel.addItem(nameText, quantityValue, dialogUnit, dialogCategory)
                                showAddItemDialog = false
                                // Reset dialog state
                                dialogName = ""
                                dialogQuantity = "1"
                                dialogUnit = "pieces"
                                dialogCategory = "Other"
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddItemDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Add to Inventory Confirmation Dialog
        if (showAddToInventoryDialog) {
            val completedItems = shoppingListItems.filter { it.isChecked }
            AlertDialog(
                onDismissRequest = { showAddToInventoryDialog = false },
                title = { Text("Add to Inventory") },
                text = {
                    Column {
                        Text(
                            "This will add ${completedItems.size} checked items to your inventory and remove them from the shopping list.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Items to be added:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        completedItems.forEach { item ->
                            Text(
                                "• ${item.quantity} ${item.unit} ${item.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            shoppingListViewModel.clearCompletedItems()
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
        
        // Edit Item Dialog
        if (showEditItemDialog && selectedItemForEdit != null) {
            selectedItemForEdit?.let { item ->
                var editName by remember { mutableStateOf(item.name) }
                var editQuantity by remember { mutableStateOf(item.quantity.toString()) }
                var editUnit by remember { mutableStateOf(item.unit) }
                var editCategory by remember { mutableStateOf(item.category) }
                var editNotes by remember { mutableStateOf(item.notes) }
                
                AlertDialog(
                    onDismissRequest = { 
                        showEditItemDialog = false 
                        selectedItemForEdit = null
                    },
                    title = { Text("Edit Shopping Item") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Item Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row {
                                OutlinedTextField(
                                    value = editQuantity,
                                    onValueChange = { editQuantity = it },
                                    label = { Text("Quantity") },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                var unitDropdownExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = unitDropdownExpanded,
                                    onExpandedChange = { unitDropdownExpanded = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = editUnit,
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
                                                    editUnit = unitOption
                                                    unitDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            var categoryExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = editCategory,
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
                                                editCategory = categoryOption
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = editNotes,
                                onValueChange = { editNotes = it },
                                label = { Text("Notes (Optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val nameText = editName.trim()
                                val quantityValue = editQuantity.toFloatOrNull() ?: 1f
                                if (nameText.isNotEmpty()) {
                                    val updatedItem = item.copy(
                                        name = nameText,
                                        quantity = quantityValue,
                                        unit = editUnit,
                                        category = editCategory,
                                        notes = editNotes
                                    )
                                    shoppingListViewModel.updateItem(updatedItem)
                                    showEditItemDialog = false
                                    selectedItemForEdit = null
                                }
                            }
                        ) {
                            Text("Update")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            showEditItemDialog = false 
                            selectedItemForEdit = null
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
    }
}

@Composable
fun ShoppingListSummaryCard(summary: com.example.tastydiet.data.models.ShoppingListSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                label = "Total Items",
                value = "${summary.totalItems}",
                icon = Icons.Filled.ShoppingCart
            )
            SummaryItem(
                label = "Checked",
                value = "${summary.checkedItems}",
                icon = Icons.Filled.CheckCircle
            )
            SummaryItem(
                label = "Estimated Cost",
                value = "₹${summary.totalEstimatedPrice.toInt()}",
                icon = Icons.Filled.AttachMoney
            )
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyShoppingList() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Shopping List is Empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add items manually or generate from your meal plan to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun ShoppingListContent(
    shoppingListItems: List<com.example.tastydiet.data.models.ShoppingListItem>,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onItemChecked: (Int, Boolean) -> Unit,
    onItemDeleted: (com.example.tastydiet.data.models.ShoppingListItem) -> Unit
) {
    Column {
        // Category Filter
        if (categories.isNotEmpty()) {
            CategoryFilter(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }
        
        // Shopping List Items
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filteredItems = if (selectedCategory != null) {
                shoppingListItems.filter { it.category == selectedCategory }
            } else {
                shoppingListItems
            }
            
            items(filteredItems) { item ->
                ShoppingListItemCard(
                    item = item,
                    onCheckedChange = { isChecked ->
                        onItemChecked(item.id, isChecked)
                    },
                    onEdit = { /* Edit functionality not available in this context */ },
                    onDelete = {
                        onItemDeleted(item)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilter(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filter by Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text("All") }
                    )
                }
                
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListItemCard(
    item: com.example.tastydiet.data.models.ShoppingListItem,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange
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
                    
                    if (item.estimatedPrice > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ₹${item.estimatedPrice}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${item.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (item.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    shoppingListViewModel: ShoppingListViewModel,
    onDismiss: () -> Unit,
    onItemAdded: (String, Float, String, String, Int, Float, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    val unitState = remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(1) }
    var estimatedPrice by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Autocomplete state
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
                        var unitDropdownExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var nameField by remember { mutableStateOf(TextFieldValue("")) }
    
    val categories = listOf("Vegetables", "Non-Veg", "Eggs", "Dry Fruits", "Grains", "Dairy", "Spices", "Oils", "Beverages", "Snacks", "Fruits")
    val units = listOf("g", "kg", "ml", "L", "pcs", "cups", "tbsp", "tsp")
    
    val scope = rememberCoroutineScope()
    
    // Auto-fill category and unit when name changes
    LaunchedEffect(nameField.text) {
        if (nameField.text.length >= 2) {
            scope.launch {
                val suggestedCategory = shoppingListViewModel.getCategorySuggestion(nameField.text)
                if (suggestedCategory != "Other") {
                    category = suggestedCategory
                }
                // Auto-assign unit based on category and name
                val suggestedUnit = shoppingListViewModel.getUnitSuggestion(category, nameField.text)
                unitState.value = suggestedUnit
            }
        }
    }
    
    // Get suggestions when name changes
    LaunchedEffect(nameField.text) {
        if (nameField.text.length >= 2) {
            scope.launch {
                suggestions = shoppingListViewModel.getItemNameSuggestions(nameField.text)
                showSuggestions = suggestions.isNotEmpty()
            }
        } else {
            suggestions = emptyList()
            showSuggestions = false
        }
    }
    
    // Update unit when category changes
    LaunchedEffect(category) {
        if (nameField.text.isNotEmpty()) {
            scope.launch {
                val suggestedUnit = shoppingListViewModel.getUnitSuggestion(category, nameField.text)
                unitState.value = suggestedUnit
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Shopping Item") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = showSuggestions,
                    onExpandedChange = { showSuggestions = !showSuggestions },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = nameField,
                        onValueChange = {
                            nameField = it
                            name = it.text
                            // Reset category/unit if name changes
                            if (!showSuggestions) {
                                category = "Other"
                                unitState.value = "pieces"
                            }
                        },
                        label = { Text("Item Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSuggestions) },
                        isError = nameField.text.isBlank()
                    )

                    ExposedDropdownMenu(
                        expanded = showSuggestions,
                        onDismissRequest = { showSuggestions = false }
                    ) {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    nameField = TextFieldValue(suggestion)
                                    name = suggestion
                                    showSuggestions = false
                                    // Auto-fill category and unit
                                    val suggestionName = suggestion
                                    scope.launch {
                                        val info = shoppingListViewModel.getNutritionalInfoByName(suggestionName)
                                        if (info != null) {
                                            category = info.category
                                            val unitValue = info.unit
                                            unitState.value = unitValue
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    val currentUnit = unitState.value
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity (${currentUnit})") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unitState.value,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            units.forEach { unitOption ->
                                DropdownMenuItem(
                                    text = { Text(unitOption) },
                                    onClick = {
                                        unitState.value = unitOption
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { categoryOption ->
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = estimatedPrice,
                    onValueChange = { estimatedPrice = it },
                    label = { Text("Estimated Price (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nameField.text.isNotBlank() && quantity.isNotEmpty() && unitState.value.isNotEmpty() && category.isNotEmpty()) {
                        onItemAdded(
                            nameField.text.trim(),
                            quantity.toFloatOrNull() ?: 0f,
                            unitState.value,
                            category,
                            priority,
                            estimatedPrice.toFloatOrNull() ?: 0f,
                            notes
                        )
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

@Composable
fun GenerateFromMealPlanDialog(
    onDismiss: () -> Unit,
    onGenerate: (List<String>) -> Unit
) {
    var mealPlan by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate from Meal Plan") },
        text = {
            Column {
                Text(
                    text = "Enter your meal plan (one meal per line) to automatically generate a shopping list:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mealPlan,
                    onValueChange = { mealPlan = it },
                    label = { Text("Meal Plan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (mealPlan.isNotEmpty()) {
                        val meals = mealPlan.split("\n").filter { it.isNotEmpty() }
                        onGenerate(meals)
                    }
                }
            ) {
                Text("Generate")
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
fun LowInventorySuggestionsSection(
    suggestions: List<com.example.tastydiet.data.models.Ingredient>,
    onAddToShoppingList: (com.example.tastydiet.data.models.Ingredient) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Suggested Items (Low Inventory)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "These items are running low in your inventory:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { ingredient ->
                    LowInventoryItemCard(
                        ingredient = ingredient,
                        onAddToShoppingList = onAddToShoppingList
                    )
                }
            }
        }
    }
}

@Composable
fun LowInventoryItemCard(
    ingredient: com.example.tastydiet.data.models.Ingredient,
    onAddToShoppingList: (com.example.tastydiet.data.models.Ingredient) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Current: ${ingredient.getFormattedQuantity()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Category: ${ingredient.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = { onAddToShoppingList(ingredient) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
} 

@Composable
fun LowInventorySuggestionItem(
    suggestion: com.example.tastydiet.viewmodel.LowInventorySuggestion,
    onToggleSelection: () -> Unit,
    onSnooze: (Int) -> Unit,
    onAddToShoppingList: () -> Unit
) {
    var showSnoozeMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = suggestion.isSelected,
                onCheckedChange = { onToggleSelection() },
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Item info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = suggestion.inventoryItem.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Current: ${suggestion.inventoryItem.quantity} ${suggestion.inventoryItem.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (suggestion.snoozeUntil != null) {
                    Text(
                        text = "Snoozed for ${suggestion.snoozeDays} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            // Action buttons
            Row {
                // Snooze button
                IconButton(
                    onClick = { showSnoozeMenu = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Snooze",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                
                // Add button
                IconButton(
                    onClick = onAddToShoppingList,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add to shopping list",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    // Snooze dropdown menu
    DropdownMenu(
        expanded = showSnoozeMenu,
        onDismissRequest = { showSnoozeMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("Snooze for 15 days") },
            onClick = {
                onSnooze(15)
                showSnoozeMenu = false
            }
        )
        DropdownMenuItem(
            text = { Text("Snooze for 30 days") },
            onClick = {
                onSnooze(30)
                showSnoozeMenu = false
            }
        )
    }
}

@Composable
fun LowInventoryChip(
    item: com.example.tastydiet.data.models.InventoryItem,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = "${item.name} (${item.quantity} ${item.unit})",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Low inventory",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer
        )
    )
} 