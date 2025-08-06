package com.example.tastydiet.ui

import com.example.tastydiet.ui.ResultCardType

/**
 * Represents a result/status message to be shown in the UI, e.g. for snackbars, toasts, banners, etc.
 * @param text The message to display.
 * @param type The type/category of the result (success, error, info, etc.).
 * @param undoAction Optional undo action (e.g. for inventory add/remove).
 * @param timestamp When this result was created (default: now).
 */
data class ResultCard(
    val text: String,
    val type: ResultCardType,
    val undoAction: (() -> Unit)? = null,
    val timestamp: Long = System.currentTimeMillis()
)
