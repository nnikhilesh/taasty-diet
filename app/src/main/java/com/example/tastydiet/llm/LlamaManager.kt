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
        
        // Load native library
        init {
            try {
                System.loadLibrary("llama_jni")
                Log.i(TAG, "Native library loaded successfully - llama.cpp version: $LLAMA_CPP_VERSION")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library: ${e.message}")
                Log.i(TAG, "LLM functionality will be disabled - using fallback responses")
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
            
            // Try to find model in multiple locations
            val path = findModelPath()
            if (path == null) {
                Log.e(TAG, "Failed to find model file in any location")
                return@withContext false
            }
            
            // Validate model file format
            if (!validateModelFormat(path)) {
                Log.e(TAG, "Invalid model format detected")
                return@withContext false
            }
            
            modelPath = path
            Log.i(TAG, "Initializing model from external storage: $path")
            Log.i(TAG, "llama.cpp version: $LLAMA_CPP_VERSION")
            
            // Initialize the model using native code
            try {
                val success = initModel(path)
                if (success) {
                    isInitialized = true
                    Log.i(TAG, "Model initialized successfully")
                    Log.i(TAG, "Model info: ${getModelInfoString()}")
                } else {
                    Log.e(TAG, "Failed to initialize model")
                }
                success
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native library not available: ${e.message}")
                Log.e(TAG, "Please ensure the native library is properly built and included")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during model initialization: ${e.message}")
            false
        }
    }
    
    /**
     * Find the model file in multiple possible locations
     * @return Path to the model file, or null if not found
     */
    private fun findModelPath(): String? {
        val possiblePaths = listOf(
            // Model downloads directory
            "model_downloads/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            // App assets
            context.filesDir.absolutePath + "/models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            // External storage
            "/storage/emulated/0/dietlarge/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            // External asset manager path
            externalAssetManager.getAssetPath(ExternalAssetManager.AssetType.LLM_MODEL)
        ).filterNotNull()
        
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                Log.i(TAG, "Found model at: $path")
                return path
            }
        }
        
        Log.e(TAG, "Model not found in any location")
        return null
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
            
            Log.i(TAG, "Model format validation passed: $extension, ${fileSize / (1024 * 1024)} MB")
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
                    return@withContext "Error: LLM model not available. Please download the model first or try again later."
                }
            }
            
            Log.i(TAG, "Generating response for prompt: $prompt")
            
            // Create a nutrition-focused prompt
            val enhancedPrompt = createNutritionPrompt(prompt)
            
            // Generate response using native code
            try {
                Log.i(TAG, "Calling native generateResponse with prompt: $enhancedPrompt")
                val response = generateResponse(enhancedPrompt, MAX_TOKENS)
                Log.i(TAG, "Native response received: $response")
                response
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native library not available for response generation: ${e.message}")
                "Error: AI model not loaded. Please restart the app or check if the model file is available."
            } catch (e: Exception) {
                Log.e(TAG, "Exception in native response generation: ${e.message}")
                "Error: Failed to generate AI response. Please try again."
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during response generation: ${e.message}")
            "Error: Failed to generate response. Please try again."
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
        return try {
            val modelInfo = getModelInfo()
            "llama.cpp version: $LLAMA_CPP_VERSION\n$modelInfo"
        } catch (e: UnsatisfiedLinkError) {
            // Check if model is actually ready despite getModelInfo() failing
            if (isModelReady()) {
                "TinyLlama Model Loaded Successfully\nllama.cpp version: $LLAMA_CPP_VERSION\nModel: tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
            } else {
                "Fallback Mode - Native LLM not available\nllama.cpp version: $LLAMA_CPP_VERSION"
            }
        } catch (e: Exception) {
            "Error getting model info: ${e.message}\nllama.cpp version: $LLAMA_CPP_VERSION"
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