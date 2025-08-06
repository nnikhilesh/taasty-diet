package com.example.tastydiet.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.net.HttpURLConnection

class ExternalAssetManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ExternalAssetManager"
        private const val ASSETS_DIR = "TastyDietAssets"
        
        // Asset file names for copying - prefer smaller, faster models
        private const val LLM_MODEL_FILE = "tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
        private const val VOSK_EN_DIR = "vosk-model-small-en-in-0.4"
        private const val VOSK_TE_DIR = "vosk-model-small-te-0.42"
    }
    
    private val externalDir: File = File(Environment.getExternalStorageDirectory(), ASSETS_DIR)
    
    init {
        if (!externalDir.exists()) {
            externalDir.mkdirs()
        }
    }
    
    /**
     * Get the correct path for AI models by checking multiple possible locations
     */
    private fun getModelBasePath(): String? {
        // First check internal storage where the app copied the model
        val internalModelsPath = context.filesDir.absolutePath + "/models"
        val internalModelsDir = File(internalModelsPath)
        if (internalModelsDir.exists() && internalModelsDir.isDirectory) {
            Log.d(TAG, "Found internal models directory: $internalModelsPath")
            return internalModelsPath
        }
        
        val possiblePaths = listOf(
            "/storage/emulated/0/dietlarge",
            "/storage/emulated/0/Download/dietlarge",
            "/storage/emulated/0/Documents/dietlarge",
            "/storage/emulated/0/OnePlus 7/Internal shared storage/dietlarge",
            "/storage/emulated/0/OnePlus 7/Internal shared storage",
            Environment.getExternalStorageDirectory().absolutePath + "/dietlarge"
        )
        
        Log.d(TAG, "Searching for model directory in possible paths:")
        for (path in possiblePaths) {
            val dir = File(path)
            Log.d(TAG, "Checking path: $path - exists: ${dir.exists()}, isDirectory: ${dir.isDirectory}")
            if (dir.exists() && dir.isDirectory) {
                Log.d(TAG, "Found model directory: $path")
                return path
            }
        }
        Log.w(TAG, "No model directory found in any of the expected locations")
        return null
    }
    
    /**
     * Get the full path for a specific model
     */
    private fun getModelPath(modelName: String): String? {
        val basePath = getModelBasePath() ?: return null
        val fullPath = "$basePath/$modelName"
        Log.d(TAG, "Model path for $modelName: $fullPath - exists: ${File(fullPath).exists()}")
        return fullPath
    }
    
    /**
     * Find any GGUF model file in the dietlarge directory
     * Prefers smaller, faster models for better performance
     */
    private fun findAnyGGUFModel(): String? {
        val basePath = getModelBasePath() ?: return null
        val dir = File(basePath)
        
        if (!dir.exists() || !dir.isDirectory) {
            Log.w(TAG, "Model directory does not exist: $basePath")
            return null
        }
        
        // Look for any .gguf file
        val ggufFiles = dir.listFiles { file ->
            file.isFile && file.name.lowercase().endsWith(".gguf")
        }
        
        if (ggufFiles.isNullOrEmpty()) {
            // Debug: List all files to see what's there
            val allFiles = dir.listFiles()
            Log.d(TAG, "All files in directory $basePath:")
            allFiles?.forEach { file ->
                Log.d(TAG, "  - ${file.name} (isFile: ${file.isFile}, size: ${file.length()} bytes)")
            }
            Log.w(TAG, "No GGUF files found in directory: $basePath")
            return null
        }
        
        // Prefer smaller models for better performance
        val sortedFiles = ggufFiles.sortedBy { it.length() }
        val modelFile = sortedFiles.first() // Use the smallest model
        
        Log.i(TAG, "Found GGUF model: ${modelFile.name} (${modelFile.length() / (1024*1024)} MB)")
        Log.i(TAG, "Available models: ${ggufFiles.joinToString { "${it.name} (${it.length() / (1024*1024)} MB)" }}")
        return modelFile.absolutePath
    }
    
    /**
     * Check if a large asset is available (either copied or exists at source path)
     */
    fun isAssetDownloaded(assetType: AssetType): Boolean {
        Log.d(TAG, "Checking if asset is downloaded: $assetType")
        
        val result = when (assetType) {
            AssetType.LLM_MODEL -> {
                // Check if copied to app directory
                val copiedExists = File(externalDir, LLM_MODEL_FILE).exists()
                val sourceExists = findAnyGGUFModel() != null
                Log.d(TAG, "LLM Model - copied exists: $copiedExists, source exists: $sourceExists")
                copiedExists || sourceExists
            }
            AssetType.VOSK_EN -> {
                val copiedExists = File(externalDir, VOSK_EN_DIR).exists()
                val sourceExists = getModelPath(VOSK_EN_DIR)?.let { File(it).exists() } ?: false
                Log.d(TAG, "Vosk EN - copied exists: $copiedExists, source exists: $sourceExists")
                copiedExists || sourceExists
            }
            AssetType.VOSK_TE -> {
                val copiedExists = File(externalDir, VOSK_TE_DIR).exists()
                val sourceExists = getModelPath(VOSK_TE_DIR)?.let { File(it).exists() } ?: false
                Log.d(TAG, "Vosk TE - copied exists: $copiedExists, source exists: $sourceExists")
                copiedExists || sourceExists
            }
        }
        
        Log.d(TAG, "Asset $assetType downloaded: $result")
        return result
    }
    
    /**
     * Get the path to an asset (either copied or source path)
     */
    fun getAssetPath(assetType: AssetType): String? {
        return when (assetType) {
            AssetType.LLM_MODEL -> {
                // First check if copied to app directory
                val copiedFile = File(externalDir, LLM_MODEL_FILE)
                if (copiedFile.exists()) {
                    copiedFile.absolutePath
                } else {
                    // Otherwise return any GGUF model found
                    findAnyGGUFModel()
                }
            }
            AssetType.VOSK_EN -> {
                val copiedDir = File(externalDir, VOSK_EN_DIR)
                if (copiedDir.exists()) {
                    copiedDir.absolutePath
                } else {
                    getModelPath(VOSK_EN_DIR)?.let { path ->
                        val sourceDir = File(path)
                        if (sourceDir.exists()) sourceDir.absolutePath else null
                    }
                }
            }
            AssetType.VOSK_TE -> {
                val copiedDir = File(externalDir, VOSK_TE_DIR)
                if (copiedDir.exists()) {
                    copiedDir.absolutePath
                } else {
                    getModelPath(VOSK_TE_DIR)?.let { path ->
                        val sourceDir = File(path)
                        if (sourceDir.exists()) sourceDir.absolutePath else null
                    }
                }
            }
        }
    }
    
    /**
     * Copy a large asset from source path to app directory
     */
    suspend fun downloadAsset(assetType: AssetType, progressCallback: ((Float) -> Unit)? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val (sourcePath, destFileName) = when (assetType) {
                    AssetType.LLM_MODEL -> {
                        val path = getModelPath(LLM_MODEL_FILE)
                        if (path == null) {
                            Log.e(TAG, "LLM model path not found")
                            return@withContext false
                        }
                        path to LLM_MODEL_FILE
                    }
                    AssetType.VOSK_EN -> {
                        val path = getModelPath(VOSK_EN_DIR)
                        if (path == null) {
                            Log.e(TAG, "Vosk EN model path not found")
                            return@withContext false
                        }
                        path to VOSK_EN_DIR
                    }
                    AssetType.VOSK_TE -> {
                        val path = getModelPath(VOSK_TE_DIR)
                        if (path == null) {
                            Log.e(TAG, "Vosk TE model path not found")
                            return@withContext false
                        }
                        path to VOSK_TE_DIR
                    }
                }
                
                val sourceFile = File(sourcePath)
                if (!sourceFile.exists()) {
                    Log.e(TAG, "Source file not found: $sourcePath")
                    return@withContext false
                }
                
                val destFile = File(externalDir, destFileName)
                
                Log.d(TAG, "Copying $assetType from $sourcePath to ${destFile.absolutePath}")
                
                if (assetType == AssetType.LLM_MODEL) {
                    // Copy single file
                    copyFile(sourceFile, destFile, progressCallback)
                } else {
                    // Copy directory for Vosk models
                    copyDirectory(sourceFile, destFile, progressCallback)
                }
                
                Log.d(TAG, "Successfully copied $assetType")
                true
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to copy $assetType", e)
                false
            }
        }
    }
    
    /**
     * Copy a single file with progress
     */
    private fun copyFile(source: File, dest: File, progressCallback: ((Float) -> Unit)?) {
        val fileSize = source.length()
        var copiedSize = 0L
        
        source.inputStream().use { input ->
            FileOutputStream(dest).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    copiedSize += bytesRead
                    
                    if (fileSize > 0) {
                        val progress = (copiedSize.toFloat() / fileSize) * 100
                        progressCallback?.invoke(progress)
                    }
                }
            }
        }
    }
    
    /**
     * Copy a directory recursively with progress
     */
    private fun copyDirectory(source: File, dest: File, progressCallback: ((Float) -> Unit)?) {
        if (!source.isDirectory) {
            Log.e(TAG, "Source is not a directory: ${source.absolutePath}")
            return
        }
        
        // Calculate total size for progress
        val totalSize = calculateDirectorySize(source)
        var copiedSize = 0L
        
        copyDirectoryRecursive(source, dest) { bytesCopied ->
            copiedSize += bytesCopied
            if (totalSize > 0) {
                val progress = (copiedSize.toFloat() / totalSize) * 100
                progressCallback?.invoke(progress)
            }
        }
    }
    
    /**
     * Calculate total size of a directory
     */
    private fun calculateDirectorySize(dir: File): Long {
        var totalSize = 0L
        dir.walkTopDown().forEach { file ->
            if (file.isFile) {
                totalSize += file.length()
            }
        }
        return totalSize
    }
    
    /**
     * Copy directory recursively
     */
    private fun copyDirectoryRecursive(source: File, dest: File, onBytesCopied: (Long) -> Unit) {
        if (source.isFile) {
            // Copy single file
            dest.parentFile?.mkdirs()
            copyFileWithCallback(source, dest, onBytesCopied)
        } else if (source.isDirectory) {
            // Create destination directory and copy contents
            dest.mkdirs()
            source.listFiles()?.forEach { child ->
                val childDest = File(dest, child.name)
                copyDirectoryRecursive(child, childDest, onBytesCopied)
            }
        }
    }
    
    /**
     * Copy file with callback for bytes copied
     */
    private fun copyFileWithCallback(source: File, dest: File, onBytesCopied: (Long) -> Unit) {
        source.inputStream().use { input ->
            FileOutputStream(dest).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    onBytesCopied(bytesRead.toLong())
                }
            }
        }
    }
    
    /**
     * Get the total size of copied assets
     */
    fun getCopiedAssetsSize(): Long {
        return externalDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    /**
     * Clear all copied assets
     */
    fun clearAllAssets() {
        externalDir.deleteRecursively()
        externalDir.mkdirs()
    }
    
    enum class AssetType {
        LLM_MODEL,
        VOSK_EN,
        VOSK_TE
    }
} 