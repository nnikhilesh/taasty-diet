package com.example.tastydiet.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.tastydiet.utils.ExternalAssetManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * VoskManager handles offline speech recognition using Vosk models
 * Supports Indian English and Telugu languages
 */
class VoskManager(private val context: Context) {
    
    companion object {
        private const val TAG = "VoskManager"
        private const val SAMPLE_RATE = 16000
        private const val BUFFER_SIZE = 4096
        
        // Model file names
        private const val ENGLISH_MODEL_NAME = "vosk-model-small-en-in-0.4"
        private const val TELUGU_MODEL_NAME = "vosk-model-small-te-0.42"
        
        // Language codes
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_TELUGU = "te"
    }
    
    // State flows
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()
    
    private val _isModelLoading = MutableStateFlow(false)
    val isModelLoading: StateFlow<Boolean> = _isModelLoading.asStateFlow()
    
    private val _currentLanguage = MutableStateFlow(LANGUAGE_ENGLISH)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
    
    // Vosk components
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var audioRecord: AudioRecord? = null
    
    // Coroutine scope for background operations
    private val voskScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Recognition job
    private var recognitionJob: Job? = null
    
    // External asset manager
    private val externalAssetManager = ExternalAssetManager(context)
    
    /**
     * Initialize Vosk with the specified language
     */
    suspend fun initializeModel(language: String): Boolean = withContext(Dispatchers.IO) {
        try {
            _isModelLoading.value = true
            _currentLanguage.value = language
            
            // Cleanup existing model
            cleanup()
            
            // Get model path
            val modelPath = getModelPath(language)
            if (modelPath == null) {
                Log.e(TAG, "Model not found for language: $language")
                return@withContext false
            }
            
            // Load model
            model = Model(modelPath)
            recognizer = Recognizer(model, SAMPLE_RATE.toFloat())
            
            Log.d(TAG, "Vosk model loaded successfully for language: $language")
            return@withContext true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Vosk model: ${e.message}", e)
            return@withContext false
        } finally {
            _isModelLoading.value = false
        }
    }
    
    /**
     * Start voice recognition
     */
    fun startRecognition() {
        if (_isListening.value || model == null || recognizer == null) {
            Log.w(TAG, "Cannot start recognition: already listening or model not loaded")
            return
        }
        
        recognitionJob = voskScope.launch {
            try {
                _isListening.value = true
                _recognizedText.value = ""
                
                // Initialize audio recording
                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
                
                audioRecord?.startRecording()
                
                val buffer = ByteArray(BUFFER_SIZE)
                
                while (_isListening.value && isActive) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    
                    if (readSize > 0) {
                        val result = recognizer?.acceptWaveForm(buffer, readSize)
                        
                        if (result == true) {
                            // Final result
                            val jsonResult = recognizer?.getResult()
                            processRecognitionResult(jsonResult)
                        } else {
                            // Partial result
                            val partialResult = recognizer?.getPartialResult()
                            processPartialResult(partialResult)
                        }
                    }
                }
                
                // Get final result
                val finalResult = recognizer?.getFinalResult()
                processRecognitionResult(finalResult)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during voice recognition: ${e.message}", e)
                _recognizedText.value = "Error: ${e.message}"
            } finally {
                stopAudioRecording()
                _isListening.value = false
            }
        }
    }
    
    /**
     * Stop voice recognition
     */
    fun stopRecognition() {
        _isListening.value = false
        recognitionJob?.cancel()
        stopAudioRecording()
    }
    
    /**
     * Process recognition result
     */
    private fun processRecognitionResult(jsonResult: String?) {
        if (jsonResult.isNullOrEmpty()) return
        
        try {
            val json = JSONObject(jsonResult)
            val text = json.optString("text", "").trim()
            
            if (text.isNotEmpty()) {
                _recognizedText.value = text
                Log.d(TAG, "Recognized text: $text")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing recognition result: ${e.message}", e)
        }
    }
    
    /**
     * Process partial recognition result
     */
    private fun processPartialResult(partialResult: String?) {
        if (partialResult.isNullOrEmpty()) return
        
        try {
            val json = JSONObject(partialResult)
            val partialText = json.optString("partial", "").trim()
            
            if (partialText.isNotEmpty()) {
                // Update partial text (optional - for real-time feedback)
                Log.d(TAG, "Partial text: $partialText")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing partial result: ${e.message}", e)
        }
    }
    
    /**
     * Get the path to the Vosk model for the specified language
     */
    private fun getModelPath(language: String): String? {
        val assetType = when (language) {
            LANGUAGE_ENGLISH -> ExternalAssetManager.AssetType.VOSK_EN
            LANGUAGE_TELUGU -> ExternalAssetManager.AssetType.VOSK_TE
            else -> return null
        }
        
        return externalAssetManager.getAssetPath(assetType)
    }
    
    /**
     * Download Vosk model to external storage
     */
    suspend fun downloadModel(language: String, progressCallback: ((Float) -> Unit)? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val assetType = when (language) {
                LANGUAGE_ENGLISH -> ExternalAssetManager.AssetType.VOSK_EN
                LANGUAGE_TELUGU -> ExternalAssetManager.AssetType.VOSK_TE
                else -> return@withContext false
            }
            
            return@withContext externalAssetManager.downloadAsset(assetType, progressCallback)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model: ${e.message}", e)
            return@withContext false
        }
    }
    
    /**
     * Check if Vosk model is downloaded
     */
    fun isModelDownloaded(language: String): Boolean {
        val assetType = when (language) {
            LANGUAGE_ENGLISH -> ExternalAssetManager.AssetType.VOSK_EN
            LANGUAGE_TELUGU -> ExternalAssetManager.AssetType.VOSK_TE
            else -> return false
        }
        
        return externalAssetManager.isAssetDownloaded(assetType)
    }
    

    
    /**
     * Stop audio recording
     */
    private fun stopAudioRecording() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio recording: ${e.message}", e)
        }
    }
    
    /**
     * Check if model is loaded
     */
    fun isModelLoaded(): Boolean {
        return model != null && recognizer != null
    }
    
    /**
     * Get current recognized text
     */
    fun getRecognizedText(): String {
        return _recognizedText.value
    }
    
    /**
     * Clear recognized text
     */
    fun clearRecognizedText() {
        _recognizedText.value = ""
    }
    
    /**
     * Get available languages
     */
    fun getAvailableLanguages(): List<String> {
        val languages = mutableListOf<String>()
        
        if (getModelPath(LANGUAGE_ENGLISH) != null) {
            languages.add(LANGUAGE_ENGLISH)
        }
        
        if (getModelPath(LANGUAGE_TELUGU) != null) {
            languages.add(LANGUAGE_TELUGU)
        }
        
        return languages
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopRecognition()
        
        try {
            recognizer?.close()
            recognizer = null
            
            model?.close()
            model = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }
    
    /**
     * Dispose of resources
     */
    fun dispose() {
        cleanup()
        voskScope.cancel()
    }
} 