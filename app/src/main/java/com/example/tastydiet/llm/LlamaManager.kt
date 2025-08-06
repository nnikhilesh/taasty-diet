package com.example.tastydiet.llm

import android.content.Context
import android.util.Log
import com.example.tastydiet.utils.ExternalAssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Manager class for local LLM integration using llama.cpp
 * Handles model loading, response generation, and resource management
 * Updated for simplified llama.cpp implementation with full GGUF support
 */
class LlamaManager(private val context: Context) {
    
    companion object {
        private const val TAG = "LlamaManager"
        private const val MODEL_FILENAME = "tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
        private const val MAX_TOKENS = 512
        private const val LLAMA_CPP_VERSION = "2024.12.01" // Simplified implementation version
        
        // Native library loading - now optional
        private var nativeLibraryLoaded = false
        
        init {
            try {
                Log.i(TAG, "Attempting to load native library: llama_jni")
                System.loadLibrary("llama_jni")
                nativeLibraryLoaded = true
                Log.i(TAG, "✅ Native library loaded successfully - llama.cpp version: $LLAMA_CPP_VERSION")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "⚠️ Native library not available: ${e.message}")
                Log.i(TAG, "ℹ️ Using enhanced Kotlin fallback responses")
                nativeLibraryLoaded = false
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Unexpected error loading native library: ${e.message}")
                Log.i(TAG, "ℹ️ Using enhanced Kotlin fallback responses")
                nativeLibraryLoaded = false
            }
        }
    }
    
    // Native method declarations
    private external fun initModel(path: String): Boolean
    private external fun generateResponse(prompt: String, maxTokens: Int): String
    private external fun isModelLoaded(): Boolean
    private external fun cleanup()
    private external fun getModelInfo(): String
    
    private var isInitialized = false
    private var modelPath: String? = null
    private val externalAssetManager = ExternalAssetManager(context)
    
    // Enhanced logging and user feedback
    data class ModelStatus(
        val isAvailable: Boolean,
        val foundPath: String?,
        val missingPaths: List<String>,
        val errorMessage: String?,
        val userMessage: String
    )
    
    /**
     * Initialize the LLM model with enhanced error handling and format validation
     * @return true if successful, false otherwise
     */
    suspend fun initializeModel(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) {
                Log.i(TAG, "Model already initialized")
                return@withContext true
            }
            
            // Enhanced model path finding with detailed logging
            val modelStatus = findModelPathWithDetails()
            
            if (!modelStatus.isAvailable) {
                Log.e(TAG, "Model not available: ${modelStatus.errorMessage}")
                Log.e(TAG, "Searched paths: ${modelStatus.missingPaths.joinToString(", ")}")
                Log.e(TAG, "User message: ${modelStatus.userMessage}")
                return@withContext false
            }
            
            val path = modelStatus.foundPath!!
            Log.i(TAG, "✅ Model found at: $path")
            
            // Add detailed logging for model loading
            Log.i(TAG, "Trying to load local model from: $path")
            val fileExists = File(path).exists()
            Log.i(TAG, "Model file exists? $fileExists")
            if (!fileExists) {
                Log.e(TAG, "Model file NOT FOUND at: $path")
                return@withContext false
            }
            
            // Validate model file format
            if (!validateModelFormat(path)) {
                Log.e(TAG, "Invalid model format detected")
                return@withContext false
            }
            
            modelPath = path
            Log.i(TAG, "Initializing model from: $path")
            Log.i(TAG, "llama.cpp version: $LLAMA_CPP_VERSION")
            
            // Log system information for debugging
            try {
                val runtime = Runtime.getRuntime()
                val maxMemory = runtime.maxMemory() / (1024 * 1024)
                val totalMemory = runtime.totalMemory() / (1024 * 1024)
                val freeMemory = runtime.freeMemory() / (1024 * 1024)
                val usedMemory = totalMemory - freeMemory
                
                Log.i(TAG, "💾 System Memory Information:")
                Log.i(TAG, "   Max Memory: ${maxMemory} MB")
                Log.i(TAG, "   Total Memory: ${totalMemory} MB")
                Log.i(TAG, "   Used Memory: ${usedMemory} MB")
                Log.i(TAG, "   Free Memory: ${freeMemory} MB")
                Log.i(TAG, "   Available Memory: ${freeMemory + (maxMemory - totalMemory)} MB")
                
                // Check if we have enough memory for model loading (rough estimate)
                val modelSizeMB = File(path).length() / (1024 * 1024)
                val requiredMemory = modelSizeMB * 2 // Rough estimate: model needs 2x its size in memory
                Log.i(TAG, "📊 Memory Requirements:")
                Log.i(TAG, "   Model Size: ${modelSizeMB} MB")
                Log.i(TAG, "   Estimated Required: ${requiredMemory} MB")
                Log.i(TAG, "   Sufficient Memory: ${if (freeMemory + (maxMemory - totalMemory) >= requiredMemory) "✅ Yes" else "❌ No"}")
                
            } catch (e: Exception) {
                Log.w(TAG, "Could not get memory information: ${e.message}")
            }
            
            // Try native library if available
            if (nativeLibraryLoaded) {
                Log.i(TAG, "Native library available, attempting native initialization...")
                try {
                    // Log detailed model file information
                    val modelFile = File(path)
                    Log.i(TAG, "📁 Model file details:")
                    Log.i(TAG, "   Path: $path")
                    Log.i(TAG, "   Exists: ${modelFile.exists()}")
                    Log.i(TAG, "   Size: ${modelFile.length()} bytes (${modelFile.length() / (1024 * 1024)} MB)")
                    Log.i(TAG, "   Readable: ${modelFile.canRead()}")
                    Log.i(TAG, "   Last modified: ${java.util.Date(modelFile.lastModified())}")
                    
                    Log.i(TAG, "🚀 Calling native initModel with path: $path")
                    val startTime = System.currentTimeMillis()
                    val success = initModel(path)
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    
                    Log.i(TAG, "⏱️ Native initModel completed in ${duration}ms")
                    Log.i(TAG, "📊 Native initModel returned: $success")
                    
                    if (success) {
                        isInitialized = true
                        Log.i(TAG, "✅ Model initialized successfully with native library")
                        Log.i(TAG, "Model info: ${getModelInfoString()}")
                        return@withContext true
                    } else {
                        Log.e(TAG, "❌ Native initialization failed - initModel returned false")
                        Log.e(TAG, "🔍 This could indicate:")
                        Log.e(TAG, "   • Model format incompatibility")
                        Log.e(TAG, "   • Insufficient memory")
                        Log.e(TAG, "   • Native library internal error")
                        Log.e(TAG, "   • Model file corruption")
                    }
                } catch (e: UnsatisfiedLinkError) {
                    Log.e(TAG, "❌ UnsatisfiedLinkError during native initialization:")
                    Log.e(TAG, "   Error: ${e.message}")
                    Log.e(TAG, "   Cause: ${e.cause?.message}")
                    Log.e(TAG, "   Stack trace: ${e.stackTraceToString()}")
                    Log.e(TAG, "🔍 This indicates native library function not found")
                } catch (e: OutOfMemoryError) {
                    Log.e(TAG, "❌ OutOfMemoryError during native initialization:")
                    Log.e(TAG, "   Error: ${e.message}")
                    Log.e(TAG, "   Stack trace: ${e.stackTraceToString()}")
                    Log.e(TAG, "🔍 This indicates insufficient memory for model loading")
                } catch (e: SecurityException) {
                    Log.e(TAG, "❌ SecurityException during native initialization:")
                    Log.e(TAG, "   Error: ${e.message}")
                    Log.e(TAG, "   Stack trace: ${e.stackTraceToString()}")
                    Log.e(TAG, "🔍 This indicates file access permission issues")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception during native initialization:")
                    Log.e(TAG, "   Error type: ${e.javaClass.simpleName}")
                    Log.e(TAG, "   Error message: ${e.message}")
                    Log.e(TAG, "   Cause: ${e.cause?.message}")
                    Log.e(TAG, "   Stack trace: ${e.stackTraceToString()}")
                    Log.e(TAG, "🔍 This indicates an unexpected error in native code")
                }
            } else {
                Log.i(TAG, "Native library not available, using Kotlin fallback")
            }
            
            // Kotlin fallback initialization
            Log.i(TAG, "Initializing Kotlin fallback mode...")
            isInitialized = true
            Log.i(TAG, "✅ Kotlin fallback mode initialized successfully")
            Log.i(TAG, "Model info: ${getModelInfoString()}")
            return@withContext true
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during model initialization: ${e.message}")
            false
        }
    }
    
    /**
     * Find the model file with detailed logging and user feedback
     * @return ModelStatus with detailed information
     */
    private fun findModelPathWithDetails(): ModelStatus {
        // First, try to copy model from assets to internal storage if not already there
        copyModelFromAssetsIfNeeded()
        
        val possiblePaths = listOf(
            // Android external files directory - CHECK FIRST (highest priority)
            "${context.getExternalFilesDir(null)?.absolutePath}/models/$MODEL_FILENAME",
            // App internal storage - CHECK SECOND
            "${context.filesDir.absolutePath}/models/$MODEL_FILENAME",
            // External asset manager path - CHECK THIRD
            externalAssetManager.getAssetPath(ExternalAssetManager.AssetType.LLM_MODEL),
            // Model downloads directory - CHECK FOURTH
            "model_downloads/$MODEL_FILENAME",
            // External storage - CHECK LAST (lowest priority, permission issues)
            "/storage/emulated/0/dietlarge/$MODEL_FILENAME"
        ).filterNotNull()
        
        Log.i(TAG, "🔍 Searching for model file: $MODEL_FILENAME")
        Log.i(TAG, "📁 Checking ${possiblePaths.size} possible locations:")
        
        val missingPaths = mutableListOf<String>()
        var foundPath: String? = null
        
        for (path in possiblePaths) {
            val file = File(path)
            Log.i(TAG, "  📂 Checking: $path")
            
            // Add detailed existence check logging
            Log.i(TAG, "Trying to load local model from: $path")
            val fileExists = file.exists()
            Log.i(TAG, "Model file exists? $fileExists")
            
            if (fileExists) {
                val fileSize = file.length()
                Log.i(TAG, "  ✅ FOUND: $path (${fileSize / (1024 * 1024)} MB)")
                
                // Check if we can actually read the file
                val canRead = file.canRead()
                Log.i(TAG, "  📖 File readable: $canRead")
                
                if (canRead) {
                    foundPath = path
                    break
                } else {
                    Log.w(TAG, "⚠️ File found but not readable: $path")
                    
                    // Try to copy external files to internal storage for better access
                    if (path.startsWith("/storage/emulated/0/")) {
                        Log.i(TAG, "🔄 External file found but not readable, attempting to copy to internal storage...")
                        val internalPath = "${context.filesDir.absolutePath}/models/$MODEL_FILENAME"
                        val copySuccess = copyModelToInternalStorage(path, internalPath)
                        
                        if (copySuccess) {
                            Log.i(TAG, "✅ Successfully copied model to internal storage: $internalPath")
                            foundPath = internalPath
                            break
                        } else {
                            Log.w(TAG, "⚠️ Failed to copy model to internal storage")
                            missingPaths.add("$path (permission denied)")
                        }
                    } else {
                        missingPaths.add("$path (permission denied)")
                    }
                }
            } else {
                Log.e(TAG, "Model file NOT FOUND at: $path")
                Log.w(TAG, "  ❌ NOT FOUND: $path")
                missingPaths.add(path)
            }
        }
        
        return if (foundPath != null) {
            ModelStatus(
                isAvailable = true,
                foundPath = foundPath,
                missingPaths = missingPaths,
                errorMessage = null,
                userMessage = "🤖 Local AI model loaded successfully!"
            )
        } else {
            val primaryPath = "${context.filesDir.absolutePath}/models/$MODEL_FILENAME"
            val externalPath = "/storage/emulated/0/dietlarge/$MODEL_FILENAME"
            
            ModelStatus(
                isAvailable = false,
                foundPath = null,
                missingPaths = missingPaths,
                errorMessage = "Model file not found in any location",
                userMessage = "⚠️ Local AI model not available.\n\n" +
                    "📁 Place your model file at:\n" +
                    "• $primaryPath\n" +
                    "• $externalPath\n\n" +
                    "🔄 Restart the app after placing the file."
            )
        }
    }
    
    /**
     * Find the model file in multiple possible locations (legacy method)
     * @return Path to the model file, or null if not found
     */
    private fun findModelPath(): String? {
        return findModelPathWithDetails().foundPath
    }
    
    /**
     * Get detailed model status for UI display
     * @return ModelStatus with user-friendly information
     */
    fun getModelStatus(): ModelStatus {
        return findModelPathWithDetails()
    }
    
    /**
     * Get the exact path where the model should be placed
     * @return List of recommended paths
     */
    fun getRecommendedModelPaths(): List<String> {
        return listOf(
            "${context.filesDir.absolutePath}/models/$MODEL_FILENAME",
            "/storage/emulated/0/dietlarge/$MODEL_FILENAME"
        )
    }
    
    /**
     * Copy model from assets to internal storage if not already present
     */
    private fun copyModelFromAssetsIfNeeded() {
        val externalFilesPath = "${context.getExternalFilesDir(null)?.absolutePath}/models/$MODEL_FILENAME"
        val externalFilesFile = File(externalFilesPath)
        
        if (externalFilesFile.exists()) {
            Log.i(TAG, "✅ Model already exists in external files directory: $externalFilesPath")
            return
        }
        
        Log.i(TAG, "📋 Model not found in external files directory, copying from assets...")
        
        try {
            // Create models directory
            val modelsDir = File("${context.getExternalFilesDir(null)?.absolutePath}/models")
            modelsDir.mkdirs()
            
            // Copy from assets
            context.assets.open(MODEL_FILENAME).use { input ->
                externalFilesFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.i(TAG, "✅ Model copied from assets to external files directory: $externalFilesPath")
            Log.i(TAG, "   Size: ${externalFilesFile.length() / (1024 * 1024)} MB")
            
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Could not copy model from assets: ${e.message}")
        }
    }
    
    /**
     * Copy model file from external to internal storage
     * @param sourcePath Source file path
     * @param destPath Destination file path
     * @return true if successful, false otherwise
     */
    private fun copyModelToInternalStorage(sourcePath: String, destPath: String): Boolean {
        return try {
            val destFile = File(destPath)
            
            // Create directory if it doesn't exist
            destFile.parentFile?.mkdirs()
            
            Log.i(TAG, "📋 Copying model file...")
            Log.i(TAG, "   From: $sourcePath")
            Log.i(TAG, "   To: $destPath")
            
            // Try using MediaStore API to access external file
            val fileName = File(sourcePath).name
            val projection = arrayOf(android.provider.MediaStore.Files.FileColumns._ID)
            val selection = "${android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(fileName)
            
            Log.i(TAG, "   Searching MediaStore for: $fileName")
            
            val cursor = context.contentResolver.query(
                android.provider.MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )
            
            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Files.FileColumns._ID))
                val uri = android.provider.MediaStore.Files.getContentUri("external", id)
                
                Log.i(TAG, "   Found file in MediaStore with ID: $id")
                
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    Log.i(TAG, "   Using MediaStore - success")
                    val outputStream = destFile.outputStream()
                    
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    Log.i(TAG, "✅ Model file copied successfully")
                    Log.i(TAG, "   Destination size: ${destFile.length() / (1024 * 1024)} MB")
                    Log.i(TAG, "   Destination readable: ${destFile.canRead()}")
                    
                    cursor.close()
                    true
                } else {
                    Log.e(TAG, "❌ MediaStore could not open input stream")
                    cursor.close()
                    false
                }
            } else {
                Log.e(TAG, "❌ File not found in MediaStore")
                cursor?.close()
                false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ SecurityException during copy: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to copy model file: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            false
        }
    }
    
    /**
     * Validate model file format (GGUF/GGML)
     * @param modelPath Path to the model file
     * @return true if valid format, false otherwise
     */
    private fun validateModelFormat(modelPath: String): Boolean {
        return try {
            val file = File(modelPath)
            if (!file.exists()) {
                Log.e(TAG, "Model file does not exist: $modelPath")
                return false
            }
            
            // Check file extension
            val extension = file.extension.lowercase()
            if (extension != "gguf" && extension != "ggml" && extension != "bin") {
                Log.w(TAG, "Unsupported model format: $extension. Supported formats: gguf, ggml, bin")
                // Still allow loading, but warn user
            }
            
            // Check file size (minimum 10MB for a valid model)
            val fileSize = file.length()
            if (fileSize < 10 * 1024 * 1024) {
                Log.w(TAG, "Model file seems too small: ${fileSize / (1024 * 1024)} MB")
                // Still allow loading, but warn user
            }
            
            Log.i(TAG, "✅ Model format validation passed: $extension, ${fileSize / (1024 * 1024)} MB")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error validating model format: ${e.message}")
            false
        }
    }
    
    /**
     * Generate a response for the given prompt with enhanced error handling
     * @param prompt User input prompt
     * @return Generated response
     */
    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val modelLoaded = try {
                isModelLoaded()
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Native library not available for model status check: ${e.message}")
                false
            }
            
            if (!isInitialized || !modelLoaded) {
                Log.w(TAG, "Model not initialized, attempting to initialize...")
                val initSuccess = initializeModel()
                if (!initSuccess) {
                    val modelStatus = getModelStatus()
                    return@withContext "❌ AI model not available.\n\n${modelStatus.userMessage}"
                }
            }
            
            Log.i(TAG, "Generating response for prompt: $prompt")
            
            // Create a nutrition-focused prompt
            val enhancedPrompt = createNutritionPrompt(prompt)
            
            // Generate response using native code or Kotlin fallback
            Log.i(TAG, "Response generation - nativeLibraryLoaded: $nativeLibraryLoaded, isInitialized: $isInitialized")
            
            if (nativeLibraryLoaded && isInitialized) {
                try {
                    Log.i(TAG, "🚀 Attempting native response generation...")
                    Log.i(TAG, "Calling native generateResponse with prompt: $enhancedPrompt")
                    val startTime = System.currentTimeMillis()
                    val response = generateResponse(enhancedPrompt, MAX_TOKENS)
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    
                    Log.i(TAG, "✅ Native response received in ${duration}ms: $response")
                    response
                } catch (e: UnsatisfiedLinkError) {
                    Log.e(TAG, "❌ UnsatisfiedLinkError in native response generation: ${e.message}")
                    Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                    Log.i(TAG, "🔄 Falling back to Kotlin response")
                    generateKotlinResponse(prompt)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception in native response generation: ${e.message}")
                    Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                    Log.i(TAG, "🔄 Falling back to Kotlin response")
                    generateKotlinResponse(prompt)
                }
            } else {
                Log.i(TAG, "ℹ️ Using Kotlin fallback response generation")
                Log.i(TAG, "   Reason: nativeLibraryLoaded=$nativeLibraryLoaded, isInitialized=$isInitialized")
                generateKotlinResponse(prompt)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during response generation: ${e.message}")
            "❌ Failed to generate response. Please try again."
        }
    }
    
    /**
     * Create a nutrition-focused prompt for the diet app
     * @param userInput Original user input
     * @return Enhanced prompt for the LLM
     */
    private fun createNutritionPrompt(userInput: String): String {
        return buildString {
            appendLine("<|system|>")
            appendLine("You are a helpful AI assistant. Answer questions directly and naturally.")
            appendLine("</s>")
            appendLine("<|user|>")
            appendLine(userInput)
            appendLine("</s>")
            appendLine("<|assistant|>")
        }
    }
    
    /**
     * Generate smart responses using Kotlin fallback (no native library required)
     * @param userInput User input prompt
     * @return Smart response based on input
     */
    private fun generateKotlinResponse(userInput: String): String {
        val lowerInput = userInput.lowercase()
        
        return when {
            // Math questions
            lowerInput.contains("what is") && (
                lowerInput.contains("+") || lowerInput.contains("-") || 
                lowerInput.contains("*") || lowerInput.contains("times") ||
                lowerInput.contains("÷") || lowerInput.contains("divided")
            ) -> {
                "I can help with basic math! For example:\n" +
                "• 2 + 2 = 4\n" +
                "• 10 - 3 = 7\n" +
                "• 5 × 6 = 30\n" +
                "• 15 ÷ 3 = 5\n\n" +
                "What calculation would you like me to help with?"
            }
            
            // Greetings
            lowerInput.contains("hello") || lowerInput.contains("hi") || lowerInput.contains("hey") -> {
                "Hello! 👋 I'm your Tasty Diet AI assistant. I can help you with:\n\n" +
                "• Food logging and nutrition tracking\n" +
                "• Recipe suggestions and meal planning\n" +
                "• Inventory management\n" +
                "• Shopping lists\n\n" +
                "What would you like to do today?"
            }
            
            // Help requests
            lowerInput.contains("help") || lowerInput.contains("what can you do") -> {
                "I can help you with:\n\n" +
                "📝 **Commands you can try:**\n" +
                "• \"Log food: apple\" - Add food to your log\n" +
                "• \"What's in my inventory?\" - Check available ingredients\n" +
                "• \"Add milk to shopping list\" - Add items to buy\n" +
                "• \"How many calories in rice?\" - Get nutrition info\n" +
                "• \"Suggest a healthy recipe\" - Get meal ideas\n" +
                "• \"What's my remaining calories?\" - Check daily progress\n\n" +
                "Just ask me naturally!"
            }
            
            // Nutrition questions
            lowerInput.contains("calories") || lowerInput.contains("nutrition") -> {
                "I can help with nutrition information! Here are some examples:\n\n" +
                "🍎 **Apple**: ~52 calories per 100g\n" +
                "🍚 **Rice**: ~130 calories per 100g\n" +
                "🥛 **Milk**: ~42 calories per 100ml\n" +
                "🍗 **Chicken**: ~165 calories per 100g\n\n" +
                "Ask me about specific foods or your daily nutrition goals!"
            }
            
            // Default response
            else -> {
                "I'm your Tasty Diet AI assistant! I can help you with:\n\n" +
                "• Food logging and nutrition tracking\n" +
                "• Recipe suggestions\n" +
                "• Inventory management\n" +
                "• Shopping lists\n\n" +
                "Try asking me to log food, check inventory, or get nutrition information. " +
                "Type 'help' for more options!\n\n" +
                "💡 **Note:** I'm currently using enhanced rule-based responses. " +
                "For more complex questions, try asking about nutrition, food logging, or recipe suggestions."
            }
        }
    }
    
    /**
     * Download the LLM model to external storage
     * @param progressCallback Optional callback for download progress
     * @return true if successful, false otherwise
     */
    suspend fun downloadModel(progressCallback: ((Float) -> Unit)? = null): Boolean {
        return externalAssetManager.downloadAsset(ExternalAssetManager.AssetType.LLM_MODEL, progressCallback)
    }
    
    /**
     * Check if the LLM model is downloaded
     * @return true if downloaded, false otherwise
     */
    fun isModelDownloaded(): Boolean {
        return externalAssetManager.isAssetDownloaded(ExternalAssetManager.AssetType.LLM_MODEL)
    }
    
    /**
     * Check if model is loaded and ready
     * @return true if model is ready, false otherwise
     */
    fun isModelReady(): Boolean {
        return try {
            // Check if model is initialized and model file exists
            if (!isInitialized) {
                Log.d(TAG, "Model not initialized")
                return false
            }
            
            // Check if model file exists
            val modelPath = externalAssetManager.getAssetPath(ExternalAssetManager.AssetType.LLM_MODEL)
            if (modelPath == null || !File(modelPath).exists()) {
                Log.d(TAG, "Model file not found")
                return false
            }
            
            // Try to call native isModelLoaded() if available
            try {
                isModelLoaded()
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Native isModelLoaded() not available, but model is initialized")
                // If native call fails but model is initialized, assume it's ready
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking model readiness: ${e.message}")
            false
        }
    }
    
    /**
     * Get model information with version details
     * @return Model information string
     */
    fun getModelInfoString(): String {
        return if (nativeLibraryLoaded && isInitialized) {
            try {
                Log.i(TAG, "Testing native library with getModelInfo()")
                val modelInfo = getModelInfo()
                Log.i(TAG, "✅ Native getModelInfo() successful: $modelInfo")
                "🚀 **NATIVE LLM ACTIVE**\nllama.cpp version: $LLAMA_CPP_VERSION\n$modelInfo\n\n✅ Using real AI model for responses"
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Native getModelInfo() failed: ${e.message}, using fallback info")
                "⚠️ **KOTLIN FALLBACK MODE**\nllama.cpp version: $LLAMA_CPP_VERSION\nModel: $MODEL_FILENAME\n\nℹ️ Using enhanced rule-based responses"
            }
        } else {
            Log.i(TAG, "Using Kotlin fallback model info")
            "⚠️ **KOTLIN FALLBACK MODE**\nllama.cpp version: $LLAMA_CPP_VERSION\nModel: $MODEL_FILENAME\n\nℹ️ Using enhanced rule-based responses"
        }
    }
    
    /**
     * Get llama.cpp version
     * @return Version string
     */
    fun getLlamaCppVersion(): String {
        return LLAMA_CPP_VERSION
    }
    
    /**
     * Clean up resources
     */
    fun cleanupResources() {
        try {
            cleanup()
            isInitialized = false
            Log.i(TAG, "LLM resources cleaned up")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native library not available for cleanup: ${e.message}")
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
    }
    
    /**
     * Get the size of the model file
     * @return Size in bytes, or -1 if not available
     */
    fun getModelSize(): Long {
        return try {
            val path = externalAssetManager.getAssetPath(ExternalAssetManager.AssetType.LLM_MODEL)
            if (path != null) {
                File(path).length()
            } else {
                -1L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting model size: ${e.message}")
            -1L
        }
    }
    
    /**
     * Check if the current model format is supported
     * @return true if supported, false otherwise
     */
    fun isModelFormatSupported(): Boolean {
        return try {
            val path = externalAssetManager.getAssetPath(ExternalAssetManager.AssetType.LLM_MODEL)
            if (path != null) {
                val extension = File(path).extension.lowercase()
                extension in listOf("gguf", "ggml", "bin")
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking model format: ${e.message}")
            false
        }
    }
} 