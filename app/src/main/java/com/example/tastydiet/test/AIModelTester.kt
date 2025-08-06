package com.example.tastydiet.test

import android.content.Context
import android.util.Log
import com.example.tastydiet.llm.LlamaManager
import com.example.tastydiet.utils.ExternalAssetManager
import kotlinx.coroutines.runBlocking
import java.io.File

class AIModelTester(private val context: Context) {
    private val TAG = "AIModelTester"
    private val llamaManager = LlamaManager(context)
    private val externalAssetManager = ExternalAssetManager(context)

    fun runCompleteTest() {
        Log.i(TAG, "=== STARTING AI MODEL TEST ===")
        
        // Test 0: Copy model file to internal storage
        copyModelToInternalStorage()
        
        // Test 1: Check if model files exist
        testModelFilesExist()
        
        // Test 2: Test model initialization
        testModelInitialization()
        
        // Test 3: Test basic questions
        testBasicQuestions()
        
        Log.i(TAG, "=== AI MODEL TEST COMPLETED ===")
    }
    
    private fun copyModelToInternalStorage() {
        Log.i(TAG, "--- Copying Model to Internal Storage ---")
        try {
            val modelsDir = File(context.filesDir, "models")
            if (!modelsDir.exists()) {
                modelsDir.mkdirs()
                Log.i(TAG, "Created models directory: ${modelsDir.absolutePath}")
            }
            
            val sourceFile = File("/data/local/tmp/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf")
            val destFile = File(modelsDir, "tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf")
            
            if (sourceFile.exists()) {
                sourceFile.copyTo(destFile, overwrite = true)
                Log.i(TAG, "Successfully copied model to: ${destFile.absolutePath}")
                Log.i(TAG, "File size: ${destFile.length()} bytes")
            } else {
                Log.w(TAG, "Source model file not found: ${sourceFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error copying model file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun testModelFilesExist() {
        Log.i(TAG, "--- Testing Model Files ---")
        
        val isLLMModelDownloaded = externalAssetManager.isAssetDownloaded(ExternalAssetManager.AssetType.LLM_MODEL)
        Log.i(TAG, "LLM Model downloaded: $isLLMModelDownloaded")
        
        val isVoskEnDownloaded = externalAssetManager.isAssetDownloaded(ExternalAssetManager.AssetType.VOSK_EN)
        Log.i(TAG, "Vosk English downloaded: $isVoskEnDownloaded")
        
        val isVoskTeDownloaded = externalAssetManager.isAssetDownloaded(ExternalAssetManager.AssetType.VOSK_TE)
        Log.i(TAG, "Vosk Telugu downloaded: $isVoskTeDownloaded")
        
        // Model file information
        Log.i(TAG, "Expected model file: tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf")
        Log.i(TAG, "Expected Vosk EN dir: vosk-model-small-en-in-0.4")
        Log.i(TAG, "Expected Vosk TE dir: vosk-model-small-te-0.42")
    }

    private fun testModelInitialization() {
        Log.i(TAG, "--- Testing Model Initialization ---")
        
        runBlocking {
            try {
                val isInitialized = llamaManager.initializeModel()
                Log.i(TAG, "Model initialization result: $isInitialized")
                
                val isModelReady = llamaManager.isModelReady()
                Log.i(TAG, "Model ready status: $isModelReady")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during model initialization: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun testBasicQuestions() {
        Log.i(TAG, "--- Testing Basic Questions ---")
        
        val testQuestions = listOf(
            "How many calories are in an apple?",
            "What is a healthy breakfast?",
            "Tell me about protein",
            "What foods are good for weight loss?",
            "How much water should I drink daily?"
        )
        
        runBlocking {
            for (question in testQuestions) {
                try {
                    Log.i(TAG, "Asking: $question")
                    val response = llamaManager.generateResponse(question)
                    Log.i(TAG, "Response: $response")
                    
                    // Check if response is meaningful (not fallback)
                    val isFallback = response.contains("fallback") || 
                                   response.contains("not available") ||
                                   response.contains("Error:") ||
                                   response.length < 20
                    
                    Log.i(TAG, "Is fallback response: $isFallback")
                    Log.i(TAG, "Response length: ${response.length}")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error asking question '$question': ${e.message}")
                    e.printStackTrace()
                }
                
                // Small delay between questions
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun testSpecificQuestion(question: String) {
        Log.i(TAG, "--- Testing Specific Question ---")
        Log.i(TAG, "Question: $question")
        
        runBlocking {
            try {
                val response = llamaManager.generateResponse(question)
                Log.i(TAG, "Response: $response")
                
                val isFallback = response.contains("fallback") || 
                               response.contains("not available") ||
                               response.contains("Error:")
                
                Log.i(TAG, "Is fallback response: $isFallback")
                Log.i(TAG, "Response length: ${response.length}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
} 