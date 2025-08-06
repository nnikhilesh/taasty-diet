package com.example.tastydiet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import androidx.compose.animation.core.RepeatMode

/**
 * Floating bubble that overlays all screens and can be dragged around
 * @param onBubbleClick Callback when bubble is tapped
 * @param modifier Additional modifier for the bubble
 */
@Composable
fun FloatingBubble(
    onBubbleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // Screen dimensions
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    
    // Bubble position state
    var bubblePosition by remember { 
        mutableStateOf(
            BubblePosition(
                x = screenWidth * 0.8f, // Start on right side
                y = screenHeight * 0.7f  // Start in lower area
            )
        ) 
    }
    
    // Animation for bubble bounce effect
    val bounceAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = repeatable(
            iterations = Int.MAX_VALUE, // Infinite repetition
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    // Bubble size with bounce effect
    val bubbleSize = (56 * bounceAnimation).dp
    
    // Dragging state
    var isDragging by remember { mutableStateOf(false) }
    
    // Snap to edges animation
    val snapAnimation by animateFloatAsState(
        targetValue = if (isDragging) 0f else 1f,
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "snap"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(9999f) // Ensure it's always on top, above navigation bar
    ) {
        // Floating bubble
        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { bubblePosition.x.toDp() },
                    y = with(density) { bubblePosition.y.toDp() }
                )
                .size(bubbleSize)
                .shadow(
                    elevation = if (isDragging) 16.dp else 8.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(
                    color = if (isDragging) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable { onBubbleClick() }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { 
                            isDragging = false
                            // Snap to nearest edge
                            bubblePosition = snapToEdge(
                                bubblePosition, 
                                screenWidth, 
                                screenHeight,
                                with(density) { bubbleSize.toPx() }
                            )
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newX = (bubblePosition.x + dragAmount.x).coerceIn(
                                0f, 
                                screenWidth - with(density) { bubbleSize.toPx() }
                            )
                            val newY = (bubblePosition.y + dragAmount.y).coerceIn(
                                0f, 
                                screenHeight - with(density) { bubbleSize.toPx() }
                            )
                            bubblePosition = BubblePosition(newX, newY)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Bubble content - using a food emoji as temporary icon
            Text(
                text = "🥗",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDragging) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onPrimary
            )
            
            // Pulse animation when not dragging
            if (!isDragging) {
                val pulseAnimation by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = repeatable(
                        iterations = Int.MAX_VALUE, // Infinite repetition
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )
                
                Box(
                    modifier = Modifier
                        .size(bubbleSize)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f * pulseAnimation)
                        )
                )
            }
        }
    }
}

/**
 * Data class to hold bubble position
 */
data class BubblePosition(
    val x: Float,
    val y: Float
)

/**
 * Snap bubble to nearest screen edge
 */
private fun snapToEdge(
    position: BubblePosition,
    screenWidth: Float,
    screenHeight: Float,
    bubbleSize: Float
): BubblePosition {
    val centerX = screenWidth / 2
    val centerY = screenHeight / 2
    
    return when {
        // Snap to left edge
        position.x < centerX -> BubblePosition(
            x = 16f,
            y = position.y.coerceIn(16f, screenHeight - bubbleSize - 16f)
        )
        // Snap to right edge
        else -> BubblePosition(
            x = screenWidth - bubbleSize - 16f,
            y = position.y.coerceIn(16f, screenHeight - bubbleSize - 16f)
        )
    }
} 