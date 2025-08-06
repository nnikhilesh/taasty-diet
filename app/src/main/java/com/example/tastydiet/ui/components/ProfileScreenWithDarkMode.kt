package com.example.tastydiet.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ProfileScreenWithDarkMode(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile & Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        DarkModeToggle(
            isDarkMode = isDarkMode,
            onThemeChange = onThemeChange
        )
        Spacer(modifier = Modifier.height(24.dp))
        ProfileBackupRestoreSection()
        // ... rest of profile UI ...
    }
} 