package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.ui.components.DailyMacroSummaryCard
import com.example.tastydiet.ui.components.MealSuggestionCard
import com.example.tastydiet.viewmodel.SmartMealPlannerViewModel
import com.example.tastydiet.viewmodel.SmartMealPlannerUiState

@Composable
fun SmartMealPlannerScreen(
    viewModel: SmartMealPlannerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        SmartMealPlannerHeader(
            selectedMember = selectedMember,
            currentDate = currentDate,
            onDateChange = { viewModel.setDate(it) },
            onMemberChange = { viewModel.selectFamilyMember(it) }
        )

        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error message
                uiState.error?.let { error ->
                    item {
                        ErrorCard(error = error, onDismiss = { viewModel.clearError() })
                    }
                }

                // Macro summary
                uiState.macroSummary?.let { macroSummary ->
                    item {
                        DailyMacroSummaryCard(macroSummary = macroSummary)
                    }
                }

                // Meal suggestions
                items(uiState.mealSuggestions) { mealSuggestion ->
                    MealSuggestionCard(
                        mealSuggestion = mealSuggestion,
                        onAcceptMeal = { /* Individual meal acceptance handled by accept plan */ },
                        onRegenerateMeal = { 
                            viewModel.regenerateMealSuggestion(mealSuggestion.mealType)
                        }
                    )
                }

                // Accept plan button
                if (uiState.mealSuggestions.isNotEmpty()) {
                    item {
                        AcceptMealPlanButton(
                            isAccepted = uiState.currentMealPlan?.isAccepted == true,
                            onAcceptPlan = { viewModel.acceptMealPlan() }
                        )
                    }
                }

                // Generate new plan button
                item {
                    GenerateNewPlanButton(
                        onGeneratePlan = { viewModel.generateMealPlan() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartMealPlannerHeader(
    selectedMember: com.example.tastydiet.data.models.FamilyMember?,
    currentDate: String,
    onDateChange: (String) -> Unit,
    onMemberChange: (com.example.tastydiet.data.models.FamilyMember) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Smart Meal Planner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Date: $currentDate",
                style = MaterialTheme.typography.bodyMedium
            )
            
            selectedMember?.let { member ->
                Text(
                    text = "Member: ${member.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun AcceptMealPlanButton(
    isAccepted: Boolean,
    onAcceptPlan: () -> Unit
) {
    Button(
        onClick = onAcceptPlan,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isAccepted
    ) {
        Text(
            text = if (isAccepted) "Meal Plan Accepted" else "Accept Complete Meal Plan"
        )
    }
}

@Composable
private fun GenerateNewPlanButton(
    onGeneratePlan: () -> Unit
) {
    OutlinedButton(
        onClick = onGeneratePlan,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Generate New Meal Plan")
    }
} 