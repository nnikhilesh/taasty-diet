package com.example.tastydiet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.FoodLog
import com.example.tastydiet.viewmodel.SimpleFoodLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleFoodLogDisplay(
    viewModel: SimpleFoodLogViewModel,
    modifier: Modifier = Modifier
) {
    val foodLogs by viewModel.foodLogs.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Food Logs - ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (foodLogs.isEmpty()) {
                Text(
                    "No food logs for this date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(foodLogs) { foodLog ->
                        FoodLogItem(
                            foodLog = foodLog,
                            onEdit = { viewModel.updateFoodLog(foodLog) },
                            onDelete = { viewModel.deleteFoodLog(foodLog) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogItem(
    foodLog: FoodLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editedQuantity by remember { mutableStateOf(foodLog.quantity.toString()) }
    var editedUnit by remember { mutableStateOf(foodLog.unit) }
    var editedMealType by remember { mutableStateOf(foodLog.mealType) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    foodLog.foodName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${foodLog.quantity} ${foodLog.unit} • ${foodLog.mealType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${foodLog.getTotalCalories().toInt()} cal • ${foodLog.getTotalProtein().toInt()}g protein",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit ${foodLog.foodName}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedQuantity,
                        onValueChange = { editedQuantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = editedUnit,
                        onValueChange = { editedUnit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = editedMealType,
                        onValueChange = { editedMealType = it },
                        label = { Text("Meal Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newQuantity = editedQuantity.toFloatOrNull() ?: foodLog.quantity
                        val updatedFoodLog = foodLog.copy(
                            quantity = newQuantity,
                            unit = editedUnit,
                            mealType = editedMealType
                        )
                        onEdit()
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 