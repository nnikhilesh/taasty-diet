package com.example.tastydiet.data.models

import android.util.Log
import androidx.room.TypeConverter

private const val TAG = "MacroPrefConverter"

object MacroPrefConverter {
    @TypeConverter
    @JvmStatic
    fun fromMacroPref(value: MacroPref?): String? {
        Log.d(TAG, "Converting MacroPref to String: $value")
        return value?.name
    }
    
    @TypeConverter
    @JvmStatic
    fun toMacroPref(value: String?): MacroPref? {
        Log.d(TAG, "Converting String to MacroPref: $value")
        return value?.let {
            try {
                MacroPref.valueOf(it)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Error converting $it to MacroPref, defaulting to MEDIUM", e)
                MacroPref.MEDIUM // Default value if conversion fails
            }
        }
    }
}