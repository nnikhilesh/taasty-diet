package com.example.tastydiet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tastydiet.voice.VoskManager

/**
 * Language picker dialog for voice recognition
 * @param isVisible Whether the dialog is visible
 * @param onDismiss Callback to dismiss the dialog
 * @param onLanguageSelected Callback when a language is selected
 * @param availableLanguages List of available languages
 * @param currentLanguage Currently selected language
 */
@Composable
fun LanguagePickerDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    availableLanguages: List<String>,
    currentLanguage: String = VoskManager.LANGUAGE_ENGLISH
) {
    if (!isVisible) return
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = "Choose Language",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Select a language for voice recognition:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Language options
                availableLanguages.forEach { language ->
                    LanguageOption(
                        language = language,
                        isSelected = language == currentLanguage,
                        onLanguageSelected = {
                            onLanguageSelected(language)
                            onDismiss()
                        }
                    )
                    
                    if (language != availableLanguages.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Individual language option
 */
@Composable
private fun LanguageOption(
    language: String,
    isSelected: Boolean,
    onLanguageSelected: () -> Unit
) {
    val languageInfo = when (language) {
        VoskManager.LANGUAGE_ENGLISH -> LanguageInfo(
            name = "English",
            nativeName = "English",
            flag = "🇺🇸",
            description = "Indian English"
        )
        VoskManager.LANGUAGE_TELUGU -> LanguageInfo(
            name = "Telugu",
            nativeName = "తెలుగు",
            flag = "🇮🇳",
            description = "Telugu language"
        )
        else -> LanguageInfo(
            name = language,
            nativeName = language,
            flag = "🌐",
            description = "Unknown language"
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLanguageSelected() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Flag
                Text(
                    text = languageInfo.flag,
                    fontSize = 24.sp
                )
                
                // Language info
                Column {
                    Text(
                        text = languageInfo.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = languageInfo.nativeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = languageInfo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Data class for language information
 */
private data class LanguageInfo(
    val name: String,
    val nativeName: String,
    val flag: String,
    val description: String
) 