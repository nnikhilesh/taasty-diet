package com.example.tastydiet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun getDarkModePref(): Boolean {
    // Use system dark mode for now. If you want to support user preference, inject it here.
    return isSystemInDarkTheme()
}
