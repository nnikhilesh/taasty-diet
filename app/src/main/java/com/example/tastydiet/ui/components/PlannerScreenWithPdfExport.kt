package com.example.tastydiet.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tastydiet.utils.PdfExportUtil
import com.example.tastydiet.data.models.MealPlan
import com.example.tastydiet.data.models.ShoppingListItem
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.Meal

@Composable
fun PlannerScreenWithPdfExport(
    mealPlans: List<MealPlan>,
    shoppingList: List<ShoppingListItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var lastExportPath by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // ... existing planner UI ...
        Button(onClick = {
            val pdfExportUtil = PdfExportUtil(context)
            val shoppingListFile = pdfExportUtil.exportShoppingList(shoppingList, "shopping_list")
            lastExportPath = shoppingListFile?.absolutePath ?: "Export failed"
            Toast.makeText(context, "PDF exported to: $lastExportPath", Toast.LENGTH_LONG).show()
        }) {
            Text("Export to PDF")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // ... rest of planner UI ...
    }
} 