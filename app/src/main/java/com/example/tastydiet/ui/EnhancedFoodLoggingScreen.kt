package com.example.tastydiet.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastydiet.data.models.FoodLog
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.viewmodel.EnhancedFoodLogViewModel
import com.example.tastydiet.viewmodel.ProfileViewModel
import com.example.tastydiet.viewmodel.RecipeViewModel
import com.example.tastydiet.ui.components.EnhancedMacroSummaryCard
import com.example.tastydiet.ui.components.SmartMealSuggestionCard
import com.example.tastydiet.ui.components.FoodLogItem
import kotlinx.coroutines.delay

enum class NotificationType {
    SUCCESS, ERROR, INFO, WARNING
}

@Composable
fun ElegantNotification(
    message: String,
    type: NotificationType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        NotificationType.SUCCESS -> Color(0xFF4CAF50)
        NotificationType.ERROR -> Color(0xFFF44336)
        NotificationType.INFO -> Color(0xFF2196F3)
        NotificationType.WARNING -> Color(0xFFFF9800)
    }
    
    val icon = when (type) {
        NotificationType.SUCCESS -> Icons.Default.CheckCircle
        NotificationType.ERROR -> Icons.Default.Error
        NotificationType.INFO -> Icons.Default.Info
        NotificationType.WARNING -> Icons.Default.Warning
    }
    
    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 300),
        label = "notification_offset"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(1000f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .offset(y = offsetY.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedFoodLoggingScreen(
    foodLogViewModel: EnhancedFoodLogViewModel,
    profileViewModel: ProfileViewModel,
    recipeViewModel: RecipeViewModel? = null,
    modifier: Modifier = Modifier
) {
    val profiles by profileViewModel.profiles.collectAsStateWithLifecycle()
    val selectedProfileId by foodLogViewModel.selectedProfileId.collectAsStateWithLifecycle()
    var selectedProfile = profiles.find { it.id == selectedProfileId }
    
    val recipes by recipeViewModel?.recipes?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val currentFoodLogs by foodLogViewModel.currentFoodLogs.collectAsStateWithLifecycle()
    val currentTotals by foodLogViewModel.currentTotals.collectAsStateWithLifecycle()
    val isLoading by foodLogViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by foodLogViewModel.errorMessage.collectAsStateWithLifecycle()
    val foodSuggestions by foodLogViewModel.foodSuggestions.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    
    // Elegant Auto-dismissing Notification System
    var notificationMessage by remember { mutableStateOf("") }
    var showNotification by remember { mutableStateOf(false) }
    var notificationType by remember { mutableStateOf(NotificationType.SUCCESS) }
    
    // Auto-dismiss notification after 3 seconds
    LaunchedEffect(showNotification) {
        if (showNotification) {
            delay(3000)
            showNotification = false
        }
    }
    
    // Handle snackbar messages with elegant notification
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotEmpty()) {
            notificationMessage = snackbarMessage
            notificationType = NotificationType.SUCCESS
            showNotification = true
            snackbarMessage = ""
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Elegant Notification at the top
        if (showNotification) {
            ElegantNotification(
                message = notificationMessage,
                type = notificationType,
                onDismiss = { showNotification = false }
            )
        }
        
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Food Logging",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Food")
                }
            }
        )
        
        // Main Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Selection
            if (profiles.isNotEmpty() && selectedProfile == null) {
                item {
                    ProfileSelectionCard(
                        profiles = profiles,
                        selectedProfile = selectedProfile,
                        onProfileSelected = { profile ->
                            foodLogViewModel.setSelectedProfile(profile.id)
                        }
                    )
                }
            }
            
            // Animated Macro Summary Card
            item {
                selectedProfile?.let { profile ->
                    EnhancedMacroSummaryCard(
                        todayTotals = currentTotals,
                        profile = profile
                    )
                }
            }
            
            // Smart Meal Suggestions
            item {
                selectedProfile?.let { profile ->
                    SmartMealSuggestionCard(
                        profile = profile,
                        inventory = emptyList(), // TODO: Get inventory
                        recipes = recipes,
                        onMealSelected = { recipe ->
                            // TODO: Handle meal selection
                        }
                    )
                }
            }
            
            // Food logs list
            currentFoodLogs.forEach { foodLog ->
                item {
                    FoodLogItem(
                        foodLog = foodLog,
                        onDelete = { 
                            foodLogViewModel.deleteFoodLog(foodLog)
                            snackbarMessage = "Deleted ${foodLog.foodName}"
                            showSnackbar = true
                        }
                    )
                }
            }
            
            // Empty state
            if (currentFoodLogs.isEmpty()) {
                item {
                    EmptyFoodLogCard()
                }
            }
        }
    }
    
    // Add Food Dialog
    if (showAddDialog) {
        EnhancedAddFoodDialog(
            onDismiss = { showAddDialog = false },
            onAddFood = { name, quantity, calories, protein, carbs, fat ->
                selectedProfile?.let { profile ->
                    val foodLog = FoodLog(
                        profileId = profile.id,
                        foodName = name,
                        mealType = "Meal",
                        quantity = quantity,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat
                    )
                    foodLogViewModel.addFoodLog(foodLog)
                    snackbarMessage = "Added $name to your food log"
                    showSnackbar = true
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProfileSelectionCard(
    profiles: List<Profile>,
    selectedProfile: Profile?,
    onProfileSelected: (Profile) -> Unit
) {
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
            Text(
                "Select Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles) { profile ->
                    ProfileItem(
                        profile = profile,
                        isSelected = profile.id == selectedProfile?.id,
                        onClick = { onProfileSelected(profile) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileItem(
    profile: Profile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    profile.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                val bmi = profile.calculateBMI()
                val bmiCategory = profile.getBMICategory()
                Text(
                    "BMI: ${String.format("%.1f", bmi)} ($bmiCategory)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}



@Composable
fun FoodLogItem(
    foodLog: FoodLog,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                foodLog.foodName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                "${foodLog.quantity} ${foodLog.unit} • ${foodLog.getFormattedTime()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            "${foodLog.getTotalCalories()} kcal",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
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

@Composable
fun EmptyFoodLogCard() {
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
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = "No food logs",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "No food logged today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Start logging your meals to track your nutrition",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAddFoodDialog(
    onDismiss: () -> Unit,
    onAddFood: (String, Float, Float, Float, Float, Float) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var calories by remember { mutableStateOf("100") }
    var protein by remember { mutableStateOf("5") }
    var carbs by remember { mutableStateOf("15") }
    var fat by remember { mutableStateOf("2") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Food")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food Name") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (foodName.isNotEmpty()) {
                            IconButton(
                                onClick = { foodName = "" }
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
                    confirmButton = {
                TextButton(
                    onClick = {
                        onAddFood(
                            foodName,
                            quantity.toFloatOrNull() ?: 1f,
                            calories.toFloatOrNull() ?: 0f,
                            protein.toFloatOrNull() ?: 0f,
                            carbs.toFloatOrNull() ?: 0f,
                            fat.toFloatOrNull() ?: 0f
                        )
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