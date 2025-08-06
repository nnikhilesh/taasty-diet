package com.example.tastydiet.utils

import android.content.Context
import com.example.tastydiet.AppDatabase

object DatabaseResetHelper {
    
    fun resetDatabase(context: Context) {
        try {
            // Clear the database instance
            AppDatabase.clearDatabase(context)
            
            // Force recreation of database
            AppDatabase.getInstance(context)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun isDatabaseCorrupted(context: Context): Boolean {
        return try {
            AppDatabase.getInstance(context)
            false
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }
} 