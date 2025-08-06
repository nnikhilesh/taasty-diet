package com.example.tastydiet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tastydiet.data.models.FoodLog
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.viewmodel.SimpleFoodLogViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SimpleFoodLogViewModel,
    modifier: Modifier = Modifier
) {
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val foodLogs by viewModel.foodLogs.collectAsState()
    val totals by viewModel.totals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showFoodLogOptions by remember { mutableStateOf<FoodLog?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopBar(
            selectedDate = selectedDate,
            onDateSelected = { viewModel.setSelectedDate(it) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Selection
            item {
                ProfileSelector(
                    selectedProfile = selectedProfile,
                    onProfileSelected = { viewModel.setSelectedProfile(it) }
                )
            }

            // Calorie Summary Card
            item {
                CalorieSummaryCard(
                    consumed = totals.calories,
                    target = selectedProfile?.targetCalories ?: 2000f
                )
            }

            // Macro Breakdown Card
            item {
                MacroBreakdownCard(
                    totals = totals,
                    profile = selectedProfile
                )
            }

            // Meal Sections
            val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
            mealTypes.forEach { mealType ->
                val mealLogs = foodLogs.filter { it.mealType == mealType }
                val mealCalories = mealLogs.sumOf { it.getTotalCalories().toDouble() }.toFloat()
                
                item {
                    MealSection(
                        mealType = mealType,
                        foodLogs = mealLogs,
                        totalCalories = mealCalories,
                        onAddFood = { showAddFoodDialog = true },
                        onFoodLogClick = { showFoodLogOptions = it }
                    )
                }
            }

            // Add some bottom padding
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Floating Action Button for adding food
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        FloatingActionButton(
            onClick = { showAddFoodDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Food")
        }
    }

    // Add Food Dialog
    if (showAddFoodDialog) {
        AddFoodDialog(
            viewModel = viewModel,
            onDismiss = { showAddFoodDialog = false }
        )
    }

    // Food Log Options Dialog
    showFoodLogOptions?.let { foodLog ->
        AlertDialog(
            onDismissRequest = { showFoodLogOptions = null },
            title = { Text("Food Options") },
            text = { Text("What would you like to do with ${foodLog.foodName}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Implement edit functionality
                        showFoodLogOptions = null
                    }
                ) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFoodLog(foodLog)
                        showFoodLogOptions = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        )
    }

    // Message Snackbar
    message?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
}

@Composable
fun TopBar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Icon(
            Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.clickable { /* TODO: Navigate back */ }
        )

        // Date selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { /* TODO: Show date picker */ }
        ) {
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Select Date",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Settings and more options
        Row {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.clickable { /* TODO: Open settings */ }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More Options",
                modifier = Modifier.clickable { /* TODO: Show more options */ }
            )
        }
    }
}

@Composable
fun ProfileSelector(
    selectedProfile: Profile?,
    onProfileSelected: (Profile) -> Unit
) {
    var showProfileDialog by remember { mutableStateOf(false) }
    var availableProfiles by remember { mutableStateOf<List<Profile>>(emptyList()) }
    
    // Load profiles
    LaunchedEffect(Unit) {
        // This would normally come from a ViewModel, but for now we'll create a default profile
        availableProfiles = listOf(
            Profile(
                id = 1,
                name = "Default Profile",
                age = 30,
                weight = 70f,
                height = 170f,
                gender = "Male",
                goal = "Maintenance",
                targetCalories = 2000f,
                targetProtein = 150f,
                targetCarbs = 200f,
                targetFat = 67f,
                targetFiber = 25f,
                activityLevel = 1.4f,
                goalDurationInWeeks = 12
            )
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { showProfileDialog = true },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedProfile?.name ?: "Select Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Select Profile"
            )
        }
    }
    
    // Profile Selection Dialog
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Select Profile") },
            text = {
                Column {
                    availableProfiles.forEach { profile ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onProfileSelected(profile)
                                    showProfileDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedProfile?.id == profile.id,
                                onClick = {
                                    onProfileSelected(profile)
                                    showProfileDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${profile.targetCalories.toInt()} cal target",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CalorieSummaryCard(
    consumed: Float,
    target: Float
) {
    val percentage = if (target > 0) (consumed / target * 100).toInt() else 0
    val progressColor = when {
        percentage <= 80 -> Color(0xFF4CAF50) // Green
        percentage <= 100 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calorie icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(progressColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = "Calories",
                    modifier = Modifier.size(30.dp),
                    tint = progressColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Calorie info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${consumed.toInt()} of ${target.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cal Eaten",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = (consumed / target).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Percentage
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }
    }
}

@Composable
fun MacroBreakdownCard(
    totals: com.example.tastydiet.viewmodel.MacroTotals,
    profile: Profile?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Macronutrients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macro items
            MacroItem(
                name = "Proteins",
                consumed = totals.protein,
                target = profile?.targetProtein ?: 150f,
                icon = Icons.Default.FitnessCenter,
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MacroItem(
                name = "Carbs",
                consumed = totals.carbs,
                target = profile?.targetCarbs ?: 200f,
                icon = Icons.Default.Grain,
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MacroItem(
                name = "Fats",
                consumed = totals.fat,
                target = profile?.targetFat ?: 67f,
                icon = Icons.Default.WaterDrop,
                color = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MacroItem(
                name = "Fiber",
                consumed = totals.fiber,
                target = profile?.targetFiber ?: 25f,
                icon = Icons.Default.Grass,
                color = Color(0xFF8BC34A)
            )
        }
    }
}

@Composable
fun MacroItem(
    name: String,
    consumed: Float,
    target: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    val percentage = if (target > 0) (consumed / target * 100).toInt() else 0
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            icon,
            contentDescription = name,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Name
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        // Values
        Text(
            text = "${consumed.toInt()}g / ${target.toInt()}g",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Percentage
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MealSection(
    mealType: String,
    foodLogs: List<FoodLog>,
    totalCalories: Float,
    onAddFood: () -> Unit,
    onFoodLogClick: (FoodLog) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Meal header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mealType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${totalCalories.toInt()} Cal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FloatingActionButton(
                        onClick = onAddFood,
                        modifier = Modifier.size(32.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Food",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Food items
            if (foodLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                                 foodLogs.forEach { foodLog ->
                     HomeFoodLogItem(
                         foodLog = foodLog,
                         onClick = { onFoodLogClick(foodLog) }
                     )
                     Spacer(modifier = Modifier.height(8.dp))
                 }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No foods logged yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun HomeFoodLogItem(
    foodLog: FoodLog,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = foodLog.foodName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${foodLog.quantity} ${foodLog.unit} • ${foodLog.getTotalCalories().toInt()} Cal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            Icons.Default.MoreVert,
            contentDescription = "Options",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 