package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastydiet.viewmodel.AnalyticsViewModel
import com.example.tastydiet.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    analyticsViewModel: AnalyticsViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val profiles by profileViewModel.profiles.collectAsStateWithLifecycle()
    val selectedProfileId by analyticsViewModel.selectedProfileId.collectAsStateWithLifecycle()
    val dateRange by analyticsViewModel.dateRange.collectAsStateWithLifecycle()
    val weeklyAnalytics by analyticsViewModel.weeklyAnalytics.collectAsStateWithLifecycle()
    val monthlyAnalytics by analyticsViewModel.monthlyAnalytics.collectAsStateWithLifecycle()
    val isLoading by analyticsViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by analyticsViewModel.errorMessage.collectAsStateWithLifecycle()
    
    var selectedProfile by remember { mutableStateOf<Int?>(null) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    
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
                title = { Text("Analytics & Trends") },
                actions = {
                    IconButton(onClick = { showDateRangeDialog = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Date Range")
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
            AnalyticsProfileSelector(
                profiles = profiles,
                selectedProfile = selectedProfile,
                onProfileSelected = { profileId ->
                    selectedProfile = profileId
                    analyticsViewModel.setSelectedProfile(profileId)
                }
            )
            
            // Date Range Selector
            DateRangeSelector(
                currentRange = dateRange,
                onRangeSelected = { range ->
                    analyticsViewModel.setDateRange(range)
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
                // Analytics Content
                when (dateRange) {
                    "week" -> weeklyAnalytics?.let { analytics ->
                        WeeklyAnalyticsContent(analytics = analytics)
                    }
                    "month" -> monthlyAnalytics?.let { analytics ->
                        MonthlyAnalyticsContent(analytics = analytics)
                    }
                    "custom" -> weeklyAnalytics?.let { analytics ->
                        WeeklyAnalyticsContent(analytics = analytics)
                    }
                }
                
                // No Data Message
                if (weeklyAnalytics == null && monthlyAnalytics == null && !isLoading) {
                    AnalyticsNoDataMessage()
                }
            }
        }
    }
    
    // Date Range Dialog
    if (showDateRangeDialog) {
        DateRangeDialog(
            onDismiss = { showDateRangeDialog = false },
            onCustomRangeSelected = { startDate, endDate ->
                analyticsViewModel.setCustomDateRange(startDate, endDate)
                showDateRangeDialog = false
            }
        )
    }
}

@Composable
fun AnalyticsProfileSelector(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(
    currentRange: String,
    onRangeSelected: (String) -> Unit
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
                text = "Date Range",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = currentRange == "week",
                    onClick = { onRangeSelected("week") },
                    label = { Text("Week") }
                )
                FilterChip(
                    selected = currentRange == "month",
                    onClick = { onRangeSelected("month") },
                    label = { Text("Month") }
                )
                FilterChip(
                    selected = currentRange == "custom",
                    onClick = { onRangeSelected("custom") },
                    label = { Text("Custom") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyAnalyticsContent(analytics: com.example.tastydiet.data.models.WeeklyAnalytics) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Avg Calories",
                value = "${analytics.averageCalories.toInt()}",
                unit = "kcal",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Avg Protein",
                value = "${analytics.averageProtein.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Avg Carbs",
                value = "${analytics.averageCarbs.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Avg Fat",
                value = "${analytics.averageFat.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Daily Breakdown
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Daily Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                analytics.dailyData.forEach { dailyData ->
                    DailyDataRow(dailyData = dailyData)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyAnalyticsContent(analytics: com.example.tastydiet.data.models.MonthlyAnalytics) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Total Calories",
                value = "${analytics.totalCalories.toInt()}",
                unit = "kcal",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Avg/Day",
                value = "${analytics.averageCaloriesPerDay.toInt()}",
                unit = "kcal",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Total Protein",
                value = "${analytics.totalProtein.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Total Carbs",
                value = "${analytics.totalCarbs.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weekly Breakdown
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Weekly Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                analytics.weeklyData.forEach { weeklyData ->
                    WeeklyDataRow(weeklyData = weeklyData)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
fun DailyDataRow(dailyData: com.example.tastydiet.data.models.AnalyticsData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dailyData.date,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${dailyData.totalCalories.toInt()} kcal",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "${dailyData.mealCount} meals",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeeklyDataRow(weeklyData: com.example.tastydiet.data.models.WeeklyAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "${weeklyData.weekStart} - ${weeklyData.weekEnd}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Avg: ${weeklyData.averageCalories.toInt()} kcal, ${weeklyData.totalMeals} meals",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AnalyticsNoDataMessage() {
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
                Icons.Filled.Analytics,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Analytics Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start logging your meals to see nutrition trends and analytics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun DateRangeDialog(
    onDismiss: () -> Unit,
    onCustomRangeSelected: (String, String) -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Date Range") },
        text = {
            Column {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                        onCustomRangeSelected(startDate, endDate)
                    }
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 