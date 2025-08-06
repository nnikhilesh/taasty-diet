package com.example.tastydiet.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Undo
// Note: Do not use MaterialTheme.colorScheme.primary here, as it is only available in a composable context.
// If you want to use theme colors, pass them at usage time, not as a sealed object property initializer.

sealed class ResultCardType(val icon: ImageVector, val accent: Color) {
    object Default : ResultCardType(Icons.Filled.Info, Color(0xFF2196F3)) // Use static blue as fallback
    object Inventory : ResultCardType(Icons.Filled.Inventory, Color(0xFF43A047))
    object Success : ResultCardType(Icons.Filled.CheckCircle, Color(0xFF388E3C))
    object Error : ResultCardType(Icons.Filled.Error, Color(0xFFD32F2F))
    object Warning : ResultCardType(Icons.Filled.Warning, Color(0xFFFBC02D))
    object Undo : ResultCardType(Icons.Filled.Undo, Color(0xFF1976D2))
    object Info : ResultCardType(Icons.Filled.Info, Color(0xFF2196F3)) // Use static blue as fallback
}
