package com.example.tastydiet.viewmodel

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.FoodItemDao
import com.example.tastydiet.data.FoodLogDao
import com.example.tastydiet.data.InventoryDao
import com.example.tastydiet.data.ShoppingListDao
import com.example.tastydiet.data.models.FoodItem
import com.example.tastydiet.data.models.FoodLog
import com.example.tastydiet.data.models.InventoryItem
import com.example.tastydiet.data.models.ShoppingListItem
import com.example.tastydiet.ui.components.ChatMessage
import com.example.tastydiet.llm.LlamaManager
import com.example.tastydiet.voice.VoskManager
import com.example.tastydiet.utils.RecipeVideoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for AI Assistant functionality
 * Handles chat messages, voice interactions, and database operations
 */
class AIAssistantViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getInstance(application)
    private val nutritionalInfoDao = database.nutritionalInfoDao()
    private val foodLogDao = database.foodLogDao()
    private val inventoryDao = database.inventoryDao()
    private val shoppingListDao = database.shoppingListDao()
    
    // State flows
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    // Vosk state flows
    private val _showLanguagePicker = MutableStateFlow(false)
    val showLanguagePicker: StateFlow<Boolean> = _showLanguagePicker.asStateFlow()
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()
    
    private val _isVoskListening = MutableStateFlow(false)
    val isVoskListening: StateFlow<Boolean> = _isVoskListening.asStateFlow()
    
    private val _currentLanguage = MutableStateFlow(VoskManager.LANGUAGE_ENGLISH)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
    
    // Inventory items for meal suggestions
    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()
    
    // Recipe video player state
    private val _showVideoPlayer = MutableStateFlow(false)
    val showVideoPlayer: StateFlow<Boolean> = _showVideoPlayer.asStateFlow()
    
    private val _currentVideoRecipe = MutableStateFlow("")
    val currentVideoRecipe: StateFlow<String> = _currentVideoRecipe.asStateFlow()
    
    private val _currentVideoId = MutableStateFlow("")
    val currentVideoId: StateFlow<String> = _currentVideoId.asStateFlow()
    
    // Text-to-Speech
    private var textToSpeech: TextToSpeech? = null
    
    // Local LLM Manager
    private val llamaManager = LlamaManager(application)
    
    // Vosk Manager for speech recognition
    private val voskManager = VoskManager(application)
    
    init {
        initializeTTS()
        addWelcomeMessage()
        initializeLLM()
        initializeVosk()
        loadInventory()
    }
    
    /**
     * Initialize Text-to-Speech
     */
    private fun initializeTTS() {
        textToSpeech = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {}
                    override fun onError(utteranceId: String?) {}
                })
            }
        }
    }
    
    /**
     * Load inventory items for meal suggestions
     */
    private fun loadInventory() {
        viewModelScope.launch {
            try {
                inventoryDao.getAllInventoryItems().collect { items ->
                    _inventoryItems.value = items
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading inventory: ${e.message}")
            }
        }
    }
    
    /**
     * Initialize the local LLM
     */
    private fun initializeLLM() {
        viewModelScope.launch {
            try {
                val modelStatus = llamaManager.getModelStatus()
                
                if (modelStatus.isAvailable) {
                    // Try to initialize the model
                    val success = llamaManager.initializeModel()
                    if (success) {
                        addMessage(ChatMessage(
                            text = modelStatus.userMessage,
                            isUser = false,
                            timestamp = getCurrentTimestamp()
                        ))
                    } else {
                        addMessage(ChatMessage(
                            text = "⚠️ Model found but failed to initialize. Using fallback responses.",
                            isUser = false,
                            timestamp = getCurrentTimestamp()
                        ))
                    }
                } else {
                    // Model not available - show detailed message with copyable paths
                    val detailedMessage = buildString {
                        appendLine(modelStatus.userMessage)
                        appendLine()
                        appendLine("📋 **Copy these paths to clipboard:**")
                        appendLine()
                        
                        val recommendedPaths = llamaManager.getRecommendedModelPaths()
                        recommendedPaths.forEachIndexed { index, path ->
                            appendLine("${index + 1}. `$path`")
                        }
                        appendLine()
                        appendLine("💡 **Instructions:**")
                        appendLine("1. Copy one of the paths above")
                        appendLine("2. Create the directory if it doesn't exist")
                        appendLine("3. Place the model file there")
                        appendLine("4. Restart the app")
                    }
                    
                    addMessage(ChatMessage(
                        text = detailedMessage,
                        isUser = false,
                        timestamp = getCurrentTimestamp()
                    ))
                }
            } catch (e: Exception) {
                addMessage(ChatMessage(
                    text = "⚠️ Error loading local AI model: ${e.message}",
                    isUser = false,
                    timestamp = getCurrentTimestamp()
                ))
            }
        }
    }
    
    /**
     * Initialize Vosk speech recognition
     */
    private fun initializeVosk() {
        viewModelScope.launch {
            try {
                // Check if models are available
                val englishSuccess = voskManager.isModelDownloaded(VoskManager.LANGUAGE_ENGLISH)
                val teluguSuccess = voskManager.isModelDownloaded(VoskManager.LANGUAGE_TELUGU)
                
                if (englishSuccess || teluguSuccess) {
                    // Initialize with default language (English)
                    val defaultLanguage = getDefaultLanguage()
                    val initSuccess = voskManager.initializeModel(defaultLanguage)
                    
                    if (initSuccess) {
                        addMessage(ChatMessage(
                            text = "🎤 Voice recognition ready! Tap the microphone to start speaking.",
                            isUser = false,
                            timestamp = getCurrentTimestamp()
                        ))
                    } else {
                        addMessage(ChatMessage(
                            text = "⚠️ Voice recognition not available. You can still type your messages.",
                            isUser = false,
                            timestamp = getCurrentTimestamp()
                        ))
                    }
                } else {
                    addMessage(ChatMessage(
                        text = "⚠️ Voice recognition models not found. You can still type your messages.",
                        isUser = false,
                        timestamp = getCurrentTimestamp()
                    ))
                }
            } catch (e: Exception) {
                addMessage(ChatMessage(
                    text = "⚠️ Error initializing voice recognition: ${e.message}",
                    isUser = false,
                    timestamp = getCurrentTimestamp()
                ))
            }
        }
    }
    
    /**
     * Add welcome message
     */
    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            text = "Hello! I'm your Tasty Diet AI assistant. I can help you with:\n\n" +
                    "• Logging food items\n" +
                    "• Checking nutrition information\n" +
                    "• Managing inventory\n" +
                    "• Adding items to shopping list\n" +
                    "• Recipe suggestions\n\n" +
                    "Just ask me anything about your diet!",
            isUser = false,
            timestamp = getCurrentTimestamp()
        )
        addMessage(welcomeMessage)
    }
    
    /**
     * Send a message and process AI response
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        // Add user message
        val userMessage = ChatMessage(
            text = message,
            isUser = true,
            timestamp = getCurrentTimestamp()
        )
        addMessage(userMessage)
        
        // Process AI response
        processAIResponse(message)
    }
    
    /**
     * Process AI response based on user input
     */
    private fun processAIResponse(userInput: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Check model status first
                val modelStatus = llamaManager.getModelStatus()
                
                // Try to use local LLM first
                val response = if (modelStatus.isAvailable && llamaManager.isModelReady()) {
                    try {
                        // Send just the user input to LlamaManager
                        llamaManager.generateResponse(userInput)
                    } catch (e: Exception) {
                        // Fallback to rule-based responses
                        Log.w("AIAssistantViewModel", "LLM failed, using fallback: ${e.message}")
                        generateFallbackResponse(userInput.lowercase())
                    }
                } else {
                    // Use fallback responses if LLM not available
                    Log.w("AIAssistantViewModel", "LLM not available, using fallback")
                    generateFallbackResponse(userInput.lowercase())
                }
                
                val aiMessage = ChatMessage(
                    text = response,
                    isUser = false,
                    timestamp = getCurrentTimestamp()
                )
                
                addMessage(aiMessage)
                
                // Speak the response
                speakText(response)
                
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = "❌ Sorry, I encountered an error. Please try again.\n\nError: ${e.message}",
                    isUser = false,
                    timestamp = getCurrentTimestamp()
                )
                addMessage(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get user context for LLM responses
     */
    private suspend fun getUserContext(): String {
        return try {
            val profiles = database.profileDao().getAllProfiles().first()
            val today = java.time.LocalDate.now()
            val startOfDay = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayFoodLogs = foodLogDao.getFoodLogsByDateRange(1, startOfDay, endOfDay).first() // Using profileId 1 as default
            val inventoryItems = inventoryDao.getAllInventoryItems().first()
            
            val context = StringBuilder()
            context.append("Current User Context:\n")
            
            if (profiles.isNotEmpty()) {
                val profile = profiles.first()
                val bmi = profile.calculateBMI()
                val goal = when {
                    bmi < 18.5 -> "Weight Gain"
                    bmi < 25 -> "Maintenance"
                    bmi < 30 -> "Weight Loss"
                    else -> "Weight Loss"
                }
                
                context.append("- Profile: ${profile.name}, Age: ${profile.age}, Goal: $goal\n")
                context.append("- Daily Goals: ${profile.targetCalories} cal, ${profile.targetProtein}g protein, ${profile.targetCarbs}g carbs, ${profile.targetFat}g fat\n")
                
                // Calculate today's progress
                var consumedCalories = 0f
                var consumedProtein = 0f
                var consumedCarbs = 0f
                var consumedFat = 0f
                
                todayFoodLogs.forEach { foodLog ->
                    consumedCalories += foodLog.calories
                    consumedProtein += foodLog.protein
                    consumedCarbs += foodLog.carbs
                    consumedFat += foodLog.fat
                }
                
                val remainingCalories = profile.targetCalories - consumedCalories
                val remainingProtein = profile.targetProtein - consumedProtein
                val remainingCarbs = profile.targetCarbs - consumedCarbs
                val remainingFat = profile.targetFat - consumedFat
                
                context.append("- Today's Progress: ${String.format("%.0f", consumedCalories)}/${profile.targetCalories} cal consumed\n")
                context.append("- Remaining: ${String.format("%.0f", remainingCalories)} cal, ${String.format("%.1f", remainingProtein)}g protein, ${String.format("%.1f", remainingCarbs)}g carbs, ${String.format("%.1f", remainingFat)}g fat\n")
            }
            
            if (inventoryItems.isNotEmpty()) {
                val itemNames = inventoryItems.take(10).map { it.name }
                context.append("- Available ingredients: ${itemNames.joinToString(", ")}${if (inventoryItems.size > 10) " and ${inventoryItems.size - 10} more" else ""}\n")
            }
            
            if (todayFoodLogs.isNotEmpty()) {
                val foodNames = todayFoodLogs.map { it.foodName }
                context.append("- Today's food: ${foodNames.joinToString(", ")}\n")
            }
            
            context.toString()
        } catch (e: Exception) {
            "Current User Context: Unable to retrieve user data"
        }
    }
    
    /**
     * Generate fallback response based on user input (when LLM is not available)
     */
    private suspend fun generateFallbackResponse(userInput: String): String {
        return when {
            // Greetings
            userInput.contains("hi") || userInput.contains("hello") || userInput.contains("hey") || 
            userInput.contains("namaste") || userInput.contains("namaskaram") -> {
                "Hello! 👋 I'm your Tasty Diet AI assistant. I can help you with:\n\n" +
                "• Logging your meals and tracking nutrition\n" +
                "• Managing your food inventory\n" +
                "• Creating shopping lists\n" +
                "• Suggesting healthy recipes\n" +
                "• Checking your daily macro goals\n\n" +
                "What would you like to do today?"
            }
            
            // Profile and macro queries
            userInput.contains("profile") || userInput.contains("macro") || userInput.contains("goal") -> {
                handleProfileQuery(userInput)
            }
            userInput.contains("what") && (userInput.contains("eat") || userInput.contains("remaining")) -> {
                handleRemainingMacrosQuery(userInput)
            }
            
            // Food logging commands
            userInput.contains("log") && userInput.contains("food") -> {
                handleFoodLogging(userInput)
            }
            userInput.contains("ate") || userInput.contains("consumed") -> {
                handleFoodLogging(userInput)
            }
            
            // Inventory commands
            userInput.contains("inventory") || userInput.contains("stock") -> {
                handleInventoryQuery(userInput)
            }
            userInput.contains("add") && userInput.contains("inventory") -> {
                handleInventoryAdd(userInput)
            }
            
            // Shopping list commands
            userInput.contains("shopping") || userInput.contains("buy") -> {
                handleShoppingListQuery(userInput)
            }
            userInput.contains("add") && userInput.contains("shopping") -> {
                handleShoppingListAdd(userInput)
            }
            
            // Nutrition queries
            userInput.contains("calories") || userInput.contains("nutrition") -> {
                handleNutritionQuery(userInput)
            }
            userInput.contains("protein") || userInput.contains("carbs") || userInput.contains("fat") -> {
                handleNutritionQuery(userInput)
            }
            
            // Recipe suggestions
            userInput.contains("recipe") || userInput.contains("cook") -> {
                handleRecipeSuggestion(userInput)
            }
            
            // Help
            userInput.contains("help") || userInput.contains("what can you do") -> {
                "I can help you with:\n\n" +
                "• Log food: \"I ate rice and dal\" or \"Log food: apple\"\n" +
                "• Check inventory: \"What's in my inventory?\"\n" +
                "• Add to inventory: \"Add tomatoes to inventory\"\n" +
                "• Shopping list: \"Add milk to shopping list\"\n" +
                "• Nutrition info: \"How many calories in apple?\"\n" +
                "• Recipe ideas: \"Suggest a healthy recipe\"\n\n" +
                "Just ask me naturally!"
            }
            
            // Default response
            else -> {
                "I'm not sure how to help with that. Try asking me to log food, check inventory, or get nutrition information. Type 'help' for more options."
            }
        }
    }
    
    /**
     * Handle profile and macro queries
     */
    private suspend fun handleProfileQuery(userInput: String): String {
        return try {
            val profiles = database.profileDao().getAllProfiles().first()
            if (profiles.isEmpty()) {
                "No profiles found. Please create a profile first."
            } else {
                val profile = profiles.first() // Get first profile for now
                val bmi = profile.calculateBMI()
                val goal = when {
                    bmi < 18.5 -> "Weight Gain"
                    bmi < 25 -> "Maintenance"
                    bmi < 30 -> "Weight Loss"
                    else -> "Weight Loss"
                }
                
                "📊 **Profile Summary**\n\n" +
                "Name: ${profile.name}\n" +
                "Age: ${profile.age} years\n" +
                "Weight: ${profile.weight} kg\n" +
                "Height: ${profile.height} cm\n" +
                "BMI: ${String.format("%.1f", bmi)}\n" +
                "Goal: $goal\n\n" +
                "**Daily Macro Goals:**\n" +
                "• Calories: ${profile.targetCalories} kcal\n" +
                "• Protein: ${profile.targetProtein}g\n" +
                "• Carbs: ${profile.targetCarbs}g\n" +
                "• Fat: ${profile.targetFat}g"
            }
        } catch (e: Exception) {
            "Error retrieving profile information: ${e.message}"
        }
    }
    
    /**
     * Handle remaining macros queries
     */
    private suspend fun handleRemainingMacrosQuery(userInput: String): String {
        return try {
            val profiles = database.profileDao().getAllProfiles().first()
            if (profiles.isEmpty()) {
                "No profiles found. Please create a profile first."
            } else {
                val profile = profiles.first()
                val today = java.time.LocalDate.now()
                val startOfDay = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val todayFoodLogs = foodLogDao.getFoodLogsByDateRange(profile.id, startOfDay, endOfDay).first()
                
                // Calculate consumed macros
                var consumedCalories = 0f
                var consumedProtein = 0f
                var consumedCarbs = 0f
                var consumedFat = 0f
                
                todayFoodLogs.forEach { foodLog ->
                    consumedCalories += foodLog.calories
                    consumedProtein += foodLog.protein
                    consumedCarbs += foodLog.carbs
                    consumedFat += foodLog.fat
                }
                
                // Calculate remaining macros
                val remainingCalories = profile.targetCalories - consumedCalories
                val remainingProtein = profile.targetProtein - consumedProtein
                val remainingCarbs = profile.targetCarbs - consumedCarbs
                val remainingFat = profile.targetFat - consumedFat
                
                "🍽️ **Today's Progress**\n\n" +
                "**Consumed:**\n" +
                "• Calories: ${String.format("%.0f", consumedCalories)} / ${profile.targetCalories} kcal\n" +
                "• Protein: ${String.format("%.1f", consumedProtein)} / ${profile.targetProtein}g\n" +
                "• Carbs: ${String.format("%.1f", consumedCarbs)} / ${profile.targetCarbs}g\n" +
                "• Fat: ${String.format("%.1f", consumedFat)} / ${profile.targetFat}g\n\n" +
                "**Remaining:**\n" +
                "• Calories: ${String.format("%.0f", remainingCalories)} kcal\n" +
                "• Protein: ${String.format("%.1f", remainingProtein)}g\n" +
                "• Carbs: ${String.format("%.1f", remainingCarbs)}g\n" +
                "• Fat: ${String.format("%.1f", remainingFat)}g"
            }
        } catch (e: Exception) {
            "Error calculating remaining macros: ${e.message}"
        }
    }
    
    /**
     * Handle food logging requests
     */
    private suspend fun handleFoodLogging(userInput: String): String {
        // Extract food items from input
        val foodItems = extractFoodItems(userInput)
        
        if (foodItems.isEmpty()) {
            return "I couldn't identify any food items. Please specify what you ate, like 'I ate rice and dal' or 'Log food: apple'"
        }
        
        val loggedItems = mutableListOf<String>()
        
        for (foodName in foodItems) {
            try {
                // Find food in database
                val foodItem = nutritionalInfoDao.getByName(foodName)
                
                if (foodItem != null) {
                    // Log the food
                    val foodLog = FoodLog(
                        profileId = 1, // Default profile
                        foodName = foodItem.name,
                        mealType = "Snack", // Default meal type
                        quantity = 1.0f,
                        unit = "serving",
                        calories = foodItem.caloriesPer100g,
                        protein = foodItem.proteinPer100g,
                        carbs = foodItem.carbsPer100g,
                        fat = foodItem.fatPer100g,
                        fiber = foodItem.fiberPer100g,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    foodLogDao.insertFoodLog(foodLog)
                    loggedItems.add("${foodItem.name} (${foodItem.caloriesPer100g} cal)")
                } else {
                    loggedItems.add("$foodName (not found in database)")
                }
            } catch (e: Exception) {
                loggedItems.add("$foodName (error logging)")
            }
        }
        
        return "Logged: ${loggedItems.joinToString(", ")}"
    }
    
    /**
     * Handle inventory queries
     */
    private suspend fun handleInventoryQuery(userInput: String): String {
        return try {
            val inventoryItems = inventoryDao.getAllInventoryItems().first()
            
            if (inventoryItems.isEmpty()) {
                "Your inventory is empty. Add some items to get started!"
            } else {
                val itemList = inventoryItems.take(10).joinToString(", ") { it.name }
                "Your inventory contains: $itemList${if (inventoryItems.size > 10) " and ${inventoryItems.size - 10} more items" else ""}"
            }
        } catch (e: Exception) {
            "Sorry, I couldn't access your inventory right now."
        }
    }
    
    /**
     * Handle adding items to inventory
     */
    private suspend fun handleInventoryAdd(userInput: String): String {
        val items = extractFoodItems(userInput)
        
        if (items.isEmpty()) {
            return "Please specify what to add to inventory, like 'Add tomatoes to inventory'"
        }
        
        val addedItems = mutableListOf<String>()
        
        for (itemName in items) {
            try {
                val inventoryItem = InventoryItem(
                    name = itemName,
                    quantity = 1.0f,
                    unit = "pieces",
                    category = "General"
                )
                
                inventoryDao.insert(inventoryItem)
                addedItems.add(itemName)
            } catch (e: Exception) {
                // Item might already exist
            }
        }
        
        return if (addedItems.isNotEmpty()) {
            "Added to inventory: ${addedItems.joinToString(", ")}"
        } else {
            "Couldn't add items to inventory. They might already exist."
        }
    }
    
    /**
     * Handle shopping list queries
     */
    private suspend fun handleShoppingListQuery(userInput: String): String {
        return try {
            val shoppingItems = shoppingListDao.getAllItems().first()
            
            if (shoppingItems.isEmpty()) {
                "Your shopping list is empty. Add some items to get started!"
            } else {
                val itemList = shoppingItems.take(10).joinToString(", ") { it.name }
                "Your shopping list: $itemList${if (shoppingItems.size > 10) " and ${shoppingItems.size - 10} more items" else ""}"
            }
        } catch (e: Exception) {
            "Sorry, I couldn't access your shopping list right now."
        }
    }
    
    /**
     * Handle adding items to shopping list
     */
    private suspend fun handleShoppingListAdd(userInput: String): String {
        val items = extractFoodItems(userInput)
        
        if (items.isEmpty()) {
            return "Please specify what to add to shopping list, like 'Add milk to shopping list'"
        }
        
        val addedItems = mutableListOf<String>()
        
        for (itemName in items) {
            try {
                val shoppingItem = ShoppingListItem(
                    name = itemName,
                    quantity = 1.0f,
                    unit = "pieces",
                    category = "General",
                    isChecked = false,
                    priority = "Medium"
                )
                
                shoppingListDao.insertItem(shoppingItem)
                addedItems.add(itemName)
            } catch (e: Exception) {
                // Item might already exist
            }
        }
        
        return if (addedItems.isNotEmpty()) {
            "Added to shopping list: ${addedItems.joinToString(", ")}"
        } else {
            "Couldn't add items to shopping list. They might already exist."
        }
    }
    
    /**
     * Handle nutrition queries
     */
    private suspend fun handleNutritionQuery(userInput: String): String {
        val foodItems = extractFoodItems(userInput)
        
        if (foodItems.isEmpty()) {
            return "Please specify a food item to get nutrition information, like 'How many calories in apple?'"
        }
        
        val nutritionInfo = mutableListOf<String>()
        
        for (foodName in foodItems) {
            try {
                val foodItem = nutritionalInfoDao.getByName(foodName)
                
                if (foodItem != null) {
                    nutritionInfo.add("${foodItem.name}: ${foodItem.caloriesPer100g} cal, ${foodItem.proteinPer100g}g protein, ${foodItem.carbsPer100g}g carbs, ${foodItem.fatPer100g}g fat")
                } else {
                    nutritionInfo.add("$foodName: Not found in database")
                }
            } catch (e: Exception) {
                nutritionInfo.add("$foodName: Error retrieving nutrition info")
            }
        }
        
        return nutritionInfo.joinToString("\n")
    }
    
    /**
     * Handle recipe suggestions
     */
    private suspend fun handleRecipeSuggestion(userInput: String): String {
        return "Here are some healthy recipe suggestions:\n\n" +
                "🥗 **Vegetable Stir-Fry with Brown Rice**\n" +
                "📺 [Watch Recipe Video](youtube://vegetable-stir-fry-brown-rice)\n\n" +
                "🍗 **Grilled Chicken with Quinoa Salad**\n" +
                "📺 [Watch Recipe Video](youtube://grilled-chicken-quinoa-salad)\n\n" +
                "🥣 **Lentil Soup with Whole Grain Bread**\n" +
                "📺 [Watch Recipe Video](youtube://lentil-soup-whole-grain-bread)\n\n" +
                "🥛 **Greek Yogurt with Berries and Nuts**\n" +
                "📺 [Watch Recipe Video](youtube://greek-yogurt-berries-nuts)\n\n" +
                "🐟 **Baked Salmon with Roasted Vegetables**\n" +
                "📺 [Watch Recipe Video](youtube://baked-salmon-roasted-vegetables)\n\n" +
                "Would you like me to help you log any of these ingredients or suggest more recipes?"
    }
    
    /**
     * Extract food items from user input
     */
    private fun extractFoodItems(userInput: String): List<String> {
        // Simple extraction - can be improved with NLP
        val commonFoods = listOf(
            "rice", "dal", "apple", "banana", "milk", "bread", "egg", "chicken", 
            "fish", "tomato", "onion", "potato", "carrot", "spinach", "yogurt",
            "cheese", "butter", "oil", "sugar", "salt", "pepper", "garlic",
            "ginger", "turmeric", "cumin", "coriander", "chili", "lemon",
            "orange", "grape", "strawberry", "blueberry", "almond", "walnut",
            "peanut", "cashew", "pistachio", "raisin", "dates", "honey",
            "jaggery", "ghee", "curd", "paneer", "tofu", "soybean", "lentil",
            "chickpea", "kidney bean", "black bean", "quinoa", "oats", "wheat",
            "maize", "corn", "cucumber", "lettuce", "cabbage", "cauliflower",
            "broccoli", "bell pepper", "capsicum", "mushroom", "eggplant",
            "brinjal", "okra", "lady finger", "bitter gourd", "pumpkin",
            "sweet potato", "beetroot", "radish", "turnip", "parsnip"
        )
        
        return commonFoods.filter { food ->
            userInput.contains(food, ignoreCase = true)
        }
    }
    
    /**
     * Toggle voice recording
     */
    fun toggleVoiceRecording() {
        if (_isVoskListening.value) {
            // Stop recording
            stopVoiceRecognition()
        } else {
            // Show language picker if not already listening
            _showLanguagePicker.value = true
        }
    }
    
    /**
     * Start voice recognition with selected language
     */
    fun startVoiceRecognition(language: String) {
        viewModelScope.launch {
            try {
                _isRecording.value = true
                _isVoskListening.value = true
                _currentLanguage.value = language
                _recognizedText.value = ""
                
                // Initialize Vosk with selected language
                val success = voskManager.initializeModel(language)
                if (success) {
                    // Save language preference
                    saveDefaultLanguage(language)
                    
                    // Start recognition
                    voskManager.startRecognition()
                    
                    // Monitor recognition results
                    monitorRecognitionResults()
                } else {
                    _isRecording.value = false
                    _isVoskListening.value = false
                    addMessage(ChatMessage(
                        text = "⚠️ Failed to initialize voice recognition for $language",
                        isUser = false,
                        timestamp = getCurrentTimestamp()
                    ))
                }
            } catch (e: Exception) {
                _isRecording.value = false
                _isVoskListening.value = false
                addMessage(ChatMessage(
                    text = "⚠️ Error starting voice recognition: ${e.message}",
                    isUser = false,
                    timestamp = getCurrentTimestamp()
                ))
            }
        }
    }
    
    /**
     * Stop voice recognition
     */
    fun stopVoiceRecognition() {
        _isRecording.value = false
        _isVoskListening.value = false
        voskManager.stopRecognition()
        
        // Get final recognized text
        val finalText = voskManager.getRecognizedText()
        if (finalText.isNotEmpty()) {
            sendMessage(finalText)
        }
        
        _recognizedText.value = ""
    }
    
    /**
     * Monitor recognition results
     */
    private fun monitorRecognitionResults() {
        viewModelScope.launch {
            while (_isVoskListening.value) {
                val text = voskManager.getRecognizedText()
                if (text.isNotEmpty() && text != _recognizedText.value) {
                    _recognizedText.value = text
                }
                delay(100) // Check every 100ms
            }
        }
    }
    
    /**
     * Show language picker
     */
    fun showLanguagePicker() {
        _showLanguagePicker.value = true
    }
    
    /**
     * Hide language picker
     */
    fun hideLanguagePicker() {
        _showLanguagePicker.value = false
    }
    
    /**
     * Get available languages
     */
    fun getAvailableLanguages(): List<String> {
        return voskManager.getAvailableLanguages()
    }
    
    /**
     * Get default language from SharedPreferences
     */
    private fun getDefaultLanguage(): String {
        val prefs = getApplication<Application>().getSharedPreferences("vosk_prefs", Context.MODE_PRIVATE)
        return prefs.getString("default_language", VoskManager.LANGUAGE_ENGLISH) ?: VoskManager.LANGUAGE_ENGLISH
    }
    
    /**
     * Save default language to SharedPreferences
     */
    private fun saveDefaultLanguage(language: String) {
        val prefs = getApplication<Application>().getSharedPreferences("vosk_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("default_language", language).apply()
    }
    
    /**
     * Speak text using TTS
     */
    private fun speakText(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
    }
    
    /**
     * Add message to chat
     */
    private fun addMessage(message: ChatMessage) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages
    }
    
    /**
     * Get current timestamp
     */
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
    
    /**
     * Show recipe video player
     */
    fun showRecipeVideo(recipeKey: String) {
        val videoId = RecipeVideoManager.getVideoId(recipeKey)
        val recipeName = RecipeVideoManager.getRecipeName(recipeKey)
        
        if (videoId != null) {
            _currentVideoRecipe.value = recipeName
            _currentVideoId.value = videoId
            _showVideoPlayer.value = true
        }
    }
    
    /**
     * Hide recipe video player
     */
    fun hideRecipeVideo() {
        _showVideoPlayer.value = false
        _currentVideoRecipe.value = ""
        _currentVideoId.value = ""
    }
    
    /**
     * Handle YouTube video link click
     */
    fun handleVideoLinkClick(url: String) {
        val recipeKey = RecipeVideoManager.extractRecipeKeyFromUrl(url)
        if (recipeKey != null) {
            showRecipeVideo(recipeKey)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        textToSpeech?.shutdown()
        llamaManager.cleanupResources()
        voskManager.dispose()
    }
} 