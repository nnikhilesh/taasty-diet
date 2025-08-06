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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.viewmodel.ProfileViewModel
import com.example.tastydiet.ui.screens.AddEditProfileScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<Profile?>(null) }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // Clear error message after 3 seconds
            kotlinx.coroutines.delay(3000)
            viewModel.clearErrorMessage()
        }
    }
    

        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
            // Header
            Text(
                text = "Family Profiles",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Add Profile Button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Profile",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Profile")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profiles List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "No Profiles",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No profiles yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            "Add your first family member profile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profiles) { profile ->
                        ProfileCard(
                            profile = profile,
                            isSelected = selectedProfile?.id == profile.id,
                            onSelect = { viewModel.selectProfile(profile) },
                            onEdit = {
                                profileToEdit = profile
                                showEditDialog = true
                            },
                            onDelete = { viewModel.deleteProfile(profile) },
                            onRecalculateMacros = { viewModel.recalculateMacros(profile) }
                        )
                    }
                }
            }
        }
        
        // Add Profile Dialog
        if (showAddDialog) {
            AddEditProfileScreen(
                profile = null,
                onSave = { profile ->
                    viewModel.addProfile(profile)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }
        
        // Edit Profile Dialog
        if (showEditDialog && profileToEdit != null) {
            AddEditProfileScreen(
                profile = profileToEdit,
                onSave = { profile ->
                    viewModel.updateProfile(profile)
                    showEditDialog = false
                    profileToEdit = null
                },
                onDismiss = {
                    showEditDialog = false
                    profileToEdit = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCard(
    profile: Profile,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRecalculateMacros: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${profile.age} years • ${profile.gender}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${profile.height}cm • ${profile.weight}kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Goal: ${profile.goal}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    val bmi = profile.calculateBMI()
                    Text(
                        "BMI: ${String.format("%.1f", bmi)} (${profile.getBMICategoryWithGoal()})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Macro information
            if (profile.targetCalories > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Daily Nutrition Targets",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroItem("Calories", "${profile.targetCalories.toInt()}", "kcal")
                            MacroItem("Protein", "${profile.targetProtein.toInt()}", "g")
                            MacroItem("Carbs", "${profile.targetCarbs.toInt()}", "g")
                            MacroItem("Fat", "${profile.targetFat.toInt()}", "g")
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Meal-specific macro distribution
                        Text(
                            "Meal Distribution",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Breakfast
                        MealMacroRow(
                            mealName = "Breakfast",
                            calories = profile.getBreakfastCalories(),
                            protein = profile.getBreakfastProtein(),
                            carbs = profile.getBreakfastCarbs(),
                            fat = profile.getBreakfastFat(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Lunch
                        MealMacroRow(
                            mealName = "Lunch",
                            calories = profile.getLunchCalories(),
                            protein = profile.getLunchProtein(),
                            carbs = profile.getLunchCarbs(),
                            fat = profile.getLunchFat(),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Snack
                        MealMacroRow(
                            mealName = "Snack",
                            calories = profile.getSnackCalories(),
                            protein = profile.getSnackProtein(),
                            carbs = profile.getSnackCarbs(),
                            fat = profile.getSnackFat(),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Dinner
                        MealMacroRow(
                            mealName = "Dinner",
                            calories = profile.getDinnerCalories(),
                            protein = profile.getDinnerProtein(),
                            carbs = profile.getDinnerCarbs(),
                            fat = profile.getDinnerFat(),
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRecalculateMacros,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recalculate Macros")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroItem(label: String, value: String, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MealMacroRow(
    mealName: String,
    calories: Float,
    protein: Float,
    carbs: Float,
    fat: Float,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal name
            Text(
                mealName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color,
                modifier = Modifier.weight(1f)
            )
            
            // Macros
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MealMacroChip("${calories.toInt()}", "kcal", color)
                MealMacroChip("${protein.toInt()}", "P", color)
                MealMacroChip("${carbs.toInt()}", "C", color)
                MealMacroChip("${fat.toInt()}", "F", color)
            }
        }
    }
}

@Composable
fun MealMacroChip(value: String, unit: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                unit,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
} 