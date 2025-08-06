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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.ui.components.RemainingMacroCard
import com.example.tastydiet.util.DinnerSuggestion
import com.example.tastydiet.util.RemainingMacros
import com.example.tastydiet.viewmodel.RemainingMacroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemainingMacroScreen(
    profile: Profile,
    viewModel: RemainingMacroViewModel,
    onDinnerSelected: (DinnerSuggestion) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val remainingMacros by viewModel.remainingMacros.collectAsState()
    val dinnerSuggestions by viewModel.dinnerSuggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val macroSummary by viewModel.macroSummary.collectAsState()
    
    // Calculate remaining macros when screen loads
    LaunchedEffect(profile) {
        viewModel.calculateRemainingMacrosAndSuggestions(profile)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Daily Macro Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshCalculations(profile) }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Info Card
            ProfileInfoCard(profile = profile)
            
            // Progress Summary Card
            ProgressSummaryCard(
                macroSummary = macroSummary,
                recommendationMessage = viewModel.getRecommendationMessage()
            )
            
            // Remaining Macros and Dinner Suggestions
            if (remainingMacros != null) {
                RemainingMacroCard(
                    profile = profile,
                    remainingMacros = remainingMacros!!,
                    dinnerSuggestions = dinnerSuggestions,
                    onDinnerSelected = onDinnerSelected
                )
            }
            
            // Loading State
            if (isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Calculating remaining macros...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Error State
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { viewModel.clearErrorMessage() }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Tips Section
            TipsSection()
        }
    }
}

@Composable
fun ProfileInfoCard(profile: Profile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    "Daily Targets: ${profile.targetCalories.toInt()} cal, ${profile.targetProtein.toInt()}g protein",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ProgressSummaryCard(
    macroSummary: Map<String, Any>,
    recommendationMessage: String
) {
    val progress = macroSummary["progress"] as? Map<String, Float> ?: emptyMap()
    val consumed = macroSummary["consumed"] as? Map<String, Float> ?: emptyMap()
    val mealsLogged = macroSummary["mealsLogged"] as? Int ?: 0
    
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
                "Today's Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bars
            ProgressBar(
                label = "Calories",
                current = consumed["calories"] ?: 0f,
                target = 2000f, // This should come from profile
                progress = progress["calories"] ?: 0f,
                color = Color(0xFFFF5722)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ProgressBar(
                label = "Protein",
                current = consumed["protein"] ?: 0f,
                target = 150f, // This should come from profile
                progress = progress["protein"] ?: 0f,
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ProgressBar(
                label = "Carbs",
                current = consumed["carbs"] ?: 0f,
                target = 250f, // This should come from profile
                progress = progress["carbs"] ?: 0f,
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ProgressBar(
                label = "Fat",
                current = consumed["fat"] ?: 0f,
                target = 70f, // This should come from profile
                progress = progress["fat"] ?: 0f,
                color = Color(0xFF9C27B0)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Meals logged
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = "Meals",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    "$mealsLogged meals logged today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Recommendation
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "Tip",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        recommendationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressBar(
    label: String,
    current: Float,
    target: Float,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                "${current.toInt()}/${target.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = (progress / 100f).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun TipsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Tips for Meeting Your Daily Targets",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TipItem(
                icon = Icons.Default.Schedule,
                title = "Log Meals Regularly",
                description = "Log your breakfast and lunch to get personalized dinner suggestions."
            )
            
            TipItem(
                icon = Icons.Default.Scale,
                title = "Watch Portion Sizes",
                description = "The app suggests optimal portion sizes to meet your remaining macros."
            )
            
            TipItem(
                icon = Icons.Default.Restaurant,
                title = "Choose High-Protein Options",
                description = "Prioritize protein-rich foods if you're falling short on protein goals."
            )
            
            TipItem(
                icon = Icons.Default.Eco,
                title = "Include Fiber-Rich Foods",
                description = "Add vegetables and whole grains to meet your fiber targets."
            )
        }
    }
}

@Composable
fun TipItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
} 