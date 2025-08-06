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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastydiet.viewmodel.SettingsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToEditProfile: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val dailyReminderEnabled by settingsViewModel.dailyReminderEnabled.collectAsState()
    
    Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Section
                SettingsSection(
                    title = "Profile",
                    icon = Icons.Default.Person
                ) {
                    SettingsItem(
                        title = "Edit Profile",
                        subtitle = "Manage your personal information and preferences",
                        icon = Icons.Default.Edit,
                        onClick = onNavigateToEditProfile
                    )
                }
                
                // Appearance Section
                SettingsSection(
                    title = "Appearance",
                    icon = Icons.Default.Palette
                ) {
                    // Theme Selection
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Theme Options
                            Column {
                                ThemeOption(
                                    title = "Light Theme",
                                    selected = themeMode == ThemeMode.LIGHT,
                                    onClick = { settingsViewModel.setThemeMode(ThemeMode.LIGHT) }
                                )
                                ThemeOption(
                                    title = "Dark Theme",
                                    selected = themeMode == ThemeMode.DARK,
                                    onClick = { settingsViewModel.setThemeMode(ThemeMode.DARK) }
                                )
                                ThemeOption(
                                    title = "System Default",
                                    selected = themeMode == ThemeMode.SYSTEM,
                                    onClick = { settingsViewModel.setThemeMode(ThemeMode.SYSTEM) }
                                )
                            }
                        }
                    }
                }
                
                // Notifications Section
                SettingsSection(
                    title = "Notifications",
                    icon = Icons.Default.Notifications
                ) {
                    SettingsItem(
                        title = "Daily Reminders",
                        subtitle = "Get reminded to log your meals",
                        icon = Icons.Default.Schedule,
                        trailing = {
                            Switch(
                                checked = dailyReminderEnabled,
                                onCheckedChange = { settingsViewModel.setDailyReminderEnabled(it) }
                            )
                        }
                    )
                }
                

                
                // Data & Privacy Section
                SettingsSection(
                    title = "Data & Privacy",
                    icon = Icons.Default.Security
                ) {
                    SettingsItem(
                        title = "Export Data",
                        subtitle = "Export your data as backup",
                        icon = Icons.Default.Download,
                        onClick = { /* TODO: Implement data export */ }
                    )
                    SettingsItem(
                        title = "Clear All Data",
                        subtitle = "Permanently delete all your data",
                        icon = Icons.Default.DeleteForever,
                        onClick = { /* TODO: Implement data deletion */ }
                    )
                }
                
                // About Section
                SettingsSection(
                    title = "About",
                    icon = Icons.Default.Info
                ) {
                    SettingsItem(
                        title = "App Version",
                        subtitle = "Tasty Diet v1.0.0",
                        icon = Icons.Default.Apps,
                        onClick = { /* TODO: Show version info */ }
                    )
                    SettingsItem(
                        title = "Privacy Policy",
                        subtitle = "Read our privacy policy",
                        icon = Icons.Default.Security,
                        onClick = { /* TODO: Open privacy policy */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Section Content
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            trailing?.invoke()
            
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
} 