package com.example.tastydiet.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import com.example.tastydiet.voice.VoskManager

/**
 * AI Assistant popup that appears when floating bubble is tapped
 * @param isVisible Whether the popup is visible
 * @param onDismiss Callback to dismiss the popup
 * @param messages List of chat messages
 * @param onSendMessage Callback when user sends a message
 * @param onVoiceRecord Callback for voice recording
 * @param isRecording Whether currently recording voice
 * @param isLoading Whether AI is processing
 */
@Composable
fun AIAssistantPopup(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onVoiceRecord: () -> Unit,
    isRecording: Boolean = false,
    isLoading: Boolean = false,
    // Vosk-related parameters
    showLanguagePicker: Boolean = false,
    onLanguageSelected: (String) -> Unit = {},
    onHideLanguagePicker: () -> Unit = {},
    availableLanguages: List<String> = emptyList(),
    currentLanguage: String = VoskManager.LANGUAGE_ENGLISH,
    recognizedText: String = "",
    isVoskListening: Boolean = false,
    // Video player parameters
    showVideoPlayer: Boolean = false,
    currentVideoRecipe: String = "",
    currentVideoId: String = "",
    onVideoDismiss: () -> Unit = {},
    onVideoLinkClick: (String) -> Unit = {}
) {
    if (!isVisible) return
    
    var userInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // AI Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🥗",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Tasty Diet AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Your nutrition assistant",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Messages area
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageItem(
                            message = message,
                            onVideoLinkClick = onVideoLinkClick
                        )
                    }
                    
                    // Loading indicator
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "AI is thinking...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Voice recognition feedback
                if (isVoskListening && recognizedText.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Listening indicator
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                            
                            Text(
                                text = "Listening...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Text(
                                text = recognizedText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 2
                            )
                        }
                    }
                }
                
                // Input area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Voice record button
                    IconButton(
                        onClick = onVoiceRecord,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isVoskListening) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                    ) {
                        Icon(
                            imageVector = if (isVoskListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isVoskListening) "Stop Recording" else "Start Recording",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Text input
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Ask about nutrition, recipes, or log food...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        maxLines = 3
                    )
                    
                    // Send button
                    IconButton(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                onSendMessage(userInput)
                                userInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (userInput.isNotBlank()) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (userInput.isNotBlank()) 
                                Color.White 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        // Language picker dialog
        LanguagePickerDialog(
            isVisible = showLanguagePicker,
            onDismiss = onHideLanguagePicker,
            onLanguageSelected = onLanguageSelected,
            availableLanguages = availableLanguages,
            currentLanguage = currentLanguage
        )
        
        // Recipe video player dialog
        if (showVideoPlayer) {
            RecipeVideoPlayer(
                recipeName = currentVideoRecipe,
                videoId = currentVideoId,
                onDismiss = onVideoDismiss
            )
        }
    }
}

/**
 * Individual chat message item
 */
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onVideoLinkClick: (String) -> Unit = {}
) {
    val isUser = message.isUser
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Parse message text for YouTube links
                val lines = message.text.split("\n")
                lines.forEach { line ->
                    if (line.contains("youtube://")) {
                        // Extract YouTube link
                        val linkMatch = Regex("\\[Watch Recipe Video\\]\\(youtube://([^)]+)\\)").find(line)
                        if (linkMatch != null) {
                            val recipeKey = linkMatch.groupValues[1]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onVideoLinkClick("youtube://$recipeKey") }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Watch Video",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "📺 Watch Recipe Video",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = if (isUser) TextAlign.End else TextAlign.Start
                            )
                        }
                    } else {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUser) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = if (isUser) TextAlign.End else TextAlign.Start
                        )
                    }
                }
                
                if (message.timestamp != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isUser) 
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = if (isUser) TextAlign.End else TextAlign.Start
                    )
                }
            }
        }
    }
}

/**
 * Data class for chat messages
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: String? = null
) 