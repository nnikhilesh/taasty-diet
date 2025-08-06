package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.MealPlan

@Composable
fun PlannerScreenWithPdfExport(
    mealPlanList: List<MealPlan>,
    onExportPdf: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onExportPdf) {
                Text("Export PDF")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(mealPlanList.size) { idx ->
                val plan = mealPlanList[idx]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Date: ${plan.date}", style = MaterialTheme.typography.titleMedium)
                        Text("Meal Time: ${plan.mealTime}")
                        Text("Recipe IDs: ${plan.recipeIds}")
                    }
                }
            }
        }
    }
}
