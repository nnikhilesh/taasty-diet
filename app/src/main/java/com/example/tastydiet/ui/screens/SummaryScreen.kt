package com.example.tastydiet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastydiet.viewmodel.AnalyticsViewModel
import com.example.tastydiet.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    analyticsViewModel: AnalyticsViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val profiles by profileViewModel.profiles.collectAsStateWithLifecycle()
    val progressSummary by analyticsViewModel.progressSummary.collectAsStateWithLifecycle()
    val isLoading by analyticsViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by analyticsViewModel.errorMessage.collectAsStateWithLifecycle()
    
    var selectedProfile by remember { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(Unit) {
        if (profiles.isNotEmpty() && selectedProfile == null) {
            selectedProfile = profiles.first().id
            analyticsViewModel.setSelectedProfile(profiles.first().id)
        }
    }
    
    LaunchedEffect(selectedProfile) {
        selectedProfile?.let { analyticsViewModel.setSelectedProfile(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Summary") },
                actions = {
                    IconButton(onClick = { /* Export summary */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Selector
            ProfileSelector(
                profiles = profiles,
                selectedProfile = selectedProfile,
                onProfileSelected = { profileId ->
                    selectedProfile = profileId
                    analyticsViewModel.setSelectedProfile(profileId)
                }
            )
            
            // Error Message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { analyticsViewModel.clearErrorMessage() }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Loading Indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Progress Summary Content
                progressSummary?.let { summary ->
                    ProgressSummaryContent(summary = summary)
                } ?: run {
                    NoDataMessage()
                }
            }
        }
    }
}

@Composable
fun ProfileSelector(
    profiles: List<com.example.tastydiet.data.models.Profile>,
    selectedProfile: Int?,
    onProfileSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            profiles.forEach { profile ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedProfile == profile.id,
                        onClick = { onProfileSelected(profile.id) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressSummaryContent(summary: com.example.tastydiet.data.models.ProgressSummary) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // BMI and Weight Trend Section
        BMISection(summary = summary)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Top Foods Section
        TopFoodsSection(topFoods = summary.topFoods)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skipped Meals Section
        SkippedMealsSection(skippedMeals = summary.skippedMeals)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Overall Stats Section
        OverallStatsSection(summary = summary)
    }
}

@Composable
fun BMISection(summary: com.example.tastydiet.data.models.ProgressSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "BMI & Weight Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current BMI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${summary.currentBMI}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = summary.bmiCategory,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (summary.weightTrend.lowercase()) {
                                "gaining" -> Icons.Filled.TrendingUp
        "losing" -> Icons.Filled.TrendingDown
        else -> Icons.Filled.TrendingFlat
                    },
                    contentDescription = null,
                    tint = when (summary.weightTrend.lowercase()) {
                        "gaining" -> Color(0xFF4CAF50)
                        "losing" -> Color(0xFFFF9800)
                        else -> Color(0xFF2196F3)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weight Trend: ${summary.weightTrend}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TopFoodsSection(topFoods: List<com.example.tastydiet.data.models.TopFood>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Top 5 Foods Eaten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            topFoods.forEachIndexed { index, food ->
                TopFoodRow(
                    rank = index + 1,
                    food = food
                )
                if (index < topFoods.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TopFoodRow(
    rank: Int,
    food: com.example.tastydiet.data.models.TopFood
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when (rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Color(0xFFCD7F32) // Bronze
                        else -> MaterialTheme.colorScheme.primaryContainer
                    },
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Food Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = food.foodName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${food.frequency} times • ${food.totalCalories.toInt()} kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SkippedMealsSection(skippedMeals: List<com.example.tastydiet.data.models.SkippedMeal>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Skipped Meals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (skippedMeals.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Great! No meals skipped recently.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
            } else {
                skippedMeals.forEach { skippedMeal ->
                    SkippedMealRow(skippedMeal = skippedMeal)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SkippedMealRow(skippedMeal: com.example.tastydiet.data.models.SkippedMeal) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = null,
            tint = Color(0xFFFF9800)
        )
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = skippedMeal.mealType,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${skippedMeal.frequency} times on ${skippedMeal.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OverallStatsSection(summary: com.example.tastydiet.data.models.ProgressSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Overall Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = "Avg Calories/Day",
                    value = "${summary.averageCaloriesPerDay.toInt()}",
                    unit = "kcal"
                )
                StatCard(
                    title = "Days Logged",
                    value = "${summary.daysLogged}",
                    unit = "days"
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NoDataMessage() {
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
                Icons.Filled.Assessment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Progress Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start logging your meals and tracking your progress to see detailed analytics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
} 