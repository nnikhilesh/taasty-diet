package com.example.tastydiet.utils

import android.content.Context
import android.os.Environment
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import com.example.tastydiet.data.models.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExportUtil(private val context: Context) {
    
    fun exportMealPlan(
        mealPlan: List<Meal>,
        profile: Profile,
        fileName: String? = null
    ): File? {
        return try {
            val pdfDocument = PrintedPdfDocument(
                context,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            )
            
            val page = pdfDocument.startPage(0)
            val canvas = page.canvas
            
            val pageWidth = page.info.pageWidth
            val pageHeight = page.info.pageHeight
            val margin = 50f
            var yPosition = margin + 50f
            
            // Title
            val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }
            canvas.drawText("Meal Plan Report", pageWidth / 2f, yPosition, titlePaint)
            yPosition += 60f
            
            // Profile Information
            val subtitlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 16f
                isFakeBoldText = true
            }
            canvas.drawText("Profile: ${profile.name}", margin, yPosition, subtitlePaint)
            yPosition += 30f
            
            val infoPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 12f
            }
            canvas.drawText("Age: ${profile.age} | Gender: ${profile.gender}", margin, yPosition, infoPaint)
            yPosition += 20f
            canvas.drawText("Height: ${profile.height}cm | Weight: ${profile.weight}kg", margin, yPosition, infoPaint)
            yPosition += 20f
            canvas.drawText("BMI: ${profile.bmi} | Category: ${profile.bmiCategory}", margin, yPosition, infoPaint)
            yPosition += 40f
            
            // Generated Date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            canvas.drawText("Generated on: ${dateFormat.format(Date())}", margin, yPosition, infoPaint)
            yPosition += 40f
            
            // Meal Plan Details
            canvas.drawText("Meal Plan Details", margin, yPosition, subtitlePaint)
            yPosition += 30f
            
            mealPlan.forEach { meal ->
                if (yPosition > pageHeight - 200) {
                    // Start new page
                    pdfDocument.finishPage(page)
                    val newPage = pdfDocument.startPage(1)
                    val newCanvas = newPage.canvas
                    yPosition = margin + 50f
                    
                    // Meal details on new page
                    newCanvas.drawText("${meal.mealType}: ${meal.name}", margin, yPosition, subtitlePaint)
                    yPosition += 25f
                    newCanvas.drawText("Calories: ${meal.calories} kcal", margin + 20, yPosition, infoPaint)
                    yPosition += 20f
                    newCanvas.drawText("Macros: ${meal.macros}", margin + 20, yPosition, infoPaint)
                    yPosition += 30f
                    
                    pdfDocument.finishPage(newPage)
                } else {
                    canvas.drawText("${meal.mealType}: ${meal.name}", margin, yPosition, subtitlePaint)
                    yPosition += 25f
                    canvas.drawText("Calories: ${meal.calories} kcal", margin + 20, yPosition, infoPaint)
                    yPosition += 20f
                    canvas.drawText("Macros: ${meal.macros}", margin + 20, yPosition, infoPaint)
                    yPosition += 30f
                }
            }
            
            pdfDocument.finishPage(page)
            
            // Save file
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName ?: "meal_plan_${System.currentTimeMillis()}.pdf")
            
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun exportDailyLog(
        foodLogs: List<FoodLog>,
        profile: Profile,
        date: String,
        fileName: String? = null
    ): File? {
        return try {
            val pdfDocument = PrintedPdfDocument(
                context,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            )
            
            val page = pdfDocument.startPage(0)
            val canvas = page.canvas
            
            val pageWidth = page.info.pageWidth
            val pageHeight = page.info.pageHeight
            val margin = 50f
            var yPosition = margin + 50f
            
            // Title
            val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }
            canvas.drawText("Daily Food Log Report", pageWidth / 2f, yPosition, titlePaint)
            yPosition += 60f
            
            // Profile Information
            val subtitlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 16f
                isFakeBoldText = true
            }
            canvas.drawText("Profile: ${profile.name}", margin, yPosition, subtitlePaint)
            yPosition += 30f
            
            val infoPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 12f
            }
            canvas.drawText("Date: $date", margin, yPosition, infoPaint)
            yPosition += 20f
            canvas.drawText("Age: ${profile.age} | Gender: ${profile.gender}", margin, yPosition, infoPaint)
            yPosition += 20f
            canvas.drawText("Height: ${profile.height}cm | Weight: ${profile.weight}kg", margin, yPosition, infoPaint)
            yPosition += 40f
            
            // Generated Date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            canvas.drawText("Generated on: ${dateFormat.format(Date())}", margin, yPosition, infoPaint)
            yPosition += 40f
            
            // Daily Summary
            val totalCalories = foodLogs.sumOf { it.getTotalCalories().toDouble() }.toFloat()
            val totalProtein = foodLogs.sumOf { it.getTotalProtein().toDouble() }.toFloat()
            val totalCarbs = foodLogs.sumOf { it.getTotalCarbs().toDouble() }.toFloat()
            val totalFat = foodLogs.sumOf { it.getTotalFat().toDouble() }.toFloat()
            
            canvas.drawText("Daily Summary", margin, yPosition, subtitlePaint)
            yPosition += 30f
            canvas.drawText("Total Calories: ${totalCalories.toInt()} kcal", margin, yPosition, infoPaint)
            yPosition += 20f
            canvas.drawText("Total Protein: ${totalProtein.toInt()}g", margin, yPosition, infoPaint)
            yPosition += 20f
            canvas.drawText("Total Carbs: ${totalCarbs.toInt()}g", margin, yPosition, infoPaint)
            yPosition += 20f
            canvas.drawText("Total Fat: ${totalFat.toInt()}g", margin, yPosition, infoPaint)
            yPosition += 40f
            
            // Meal Details
            canvas.drawText("Meal Details", margin, yPosition, subtitlePaint)
            yPosition += 30f
            
            val mealsByType = foodLogs.groupBy { it.mealType }
            mealsByType.forEach { (mealType, logs) ->
                if (yPosition > pageHeight - 200) {
                    // Start new page
                    pdfDocument.finishPage(page)
                    val newPage = pdfDocument.startPage(1)
                    val newCanvas = newPage.canvas
                    yPosition = margin + 50f
                    
                    newCanvas.drawText("$mealType:", margin, yPosition, subtitlePaint)
                    yPosition += 25f
                    
                    logs.forEach { log ->
                        newCanvas.drawText("• ${log.foodName} - ${log.quantity} ${log.unit}", margin + 20, yPosition, infoPaint)
                        yPosition += 20f
                        newCanvas.drawText("  Calories: ${log.getTotalCalories().toInt()} kcal", margin + 40, yPosition, infoPaint)
                        yPosition += 20f
                    }
                    
                    pdfDocument.finishPage(newPage)
                } else {
                    canvas.drawText("$mealType:", margin, yPosition, subtitlePaint)
                    yPosition += 25f
                    
                    logs.forEach { log ->
                        canvas.drawText("• ${log.foodName} - ${log.quantity} ${log.unit}", margin + 20, yPosition, infoPaint)
                        yPosition += 20f
                        canvas.drawText("  Calories: ${log.getTotalCalories().toInt()} kcal", margin + 40, yPosition, infoPaint)
                        yPosition += 20f
                    }
                }
                yPosition += 20f
            }
            
            pdfDocument.finishPage(page)
            
            // Save file
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName ?: "daily_log_${System.currentTimeMillis()}.pdf")
            
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun exportShoppingList(
        shoppingList: List<ShoppingListItem>,
        fileName: String? = null
    ): File? {
        return try {
            val pdfDocument = PrintedPdfDocument(
                context,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            )
            
            val page = pdfDocument.startPage(0)
            val canvas = page.canvas
            
            val pageWidth = page.info.pageWidth
            val pageHeight = page.info.pageHeight
            val margin = 50f
            var yPosition = margin + 50f
            
            // Title
            val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }
            canvas.drawText("Shopping List", pageWidth / 2f, yPosition, titlePaint)
            yPosition += 60f
            
            // Generated Date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val infoPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 12f
            }
            canvas.drawText("Generated on: ${dateFormat.format(Date())}", margin, yPosition, infoPaint)
            yPosition += 40f
            
            // Shopping List Items
            val subtitlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 16f
                isFakeBoldText = true
            }
            
            val itemsByCategory = shoppingList.groupBy { it.category }
            itemsByCategory.forEach { (category, items) ->
                canvas.drawText(category, margin, yPosition, subtitlePaint)
                yPosition += 25f
                
                items.forEach { item ->
                    if (yPosition > pageHeight - 100) {
                        // Start new page
                        pdfDocument.finishPage(page)
                        val newPage = pdfDocument.startPage(1)
                        val newCanvas = newPage.canvas
                        yPosition = margin + 50f
                        
                        newCanvas.drawText("• ${item.name} - ${item.quantity} ${item.unit}", margin + 20, yPosition, infoPaint)
                        yPosition += 20f
                        if (item.estimatedPrice > 0) {
                            newCanvas.drawText("  Estimated Price: ₹${item.estimatedPrice}", margin + 40, yPosition, infoPaint)
                            yPosition += 20f
                        }
                        
                        pdfDocument.finishPage(newPage)
                    } else {
                        canvas.drawText("• ${item.name} - ${item.quantity} ${item.unit}", margin + 20, yPosition, infoPaint)
                        yPosition += 20f
                        if (item.estimatedPrice > 0) {
                            canvas.drawText("  Estimated Price: ₹${item.estimatedPrice}", margin + 40, yPosition, infoPaint)
                            yPosition += 20f
                        }
                    }
                }
                yPosition += 20f
            }
            
            // Total Estimated Cost
            val totalCost = shoppingList.sumOf { it.estimatedPrice.toDouble() }.toFloat()
            if (totalCost > 0) {
                yPosition += 20f
                canvas.drawText("Total Estimated Cost: ₹${totalCost.toInt()}", margin, yPosition, subtitlePaint)
            }
            
            pdfDocument.finishPage(page)
            
            // Save file
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName ?: "shopping_list_${System.currentTimeMillis()}.pdf")
            
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 