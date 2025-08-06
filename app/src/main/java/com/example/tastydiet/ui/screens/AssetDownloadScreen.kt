package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tastydiet.utils.ExternalAssetManager
import com.example.tastydiet.llm.LlamaManager
import com.example.tastydiet.voice.VoskManager
import kotlinx.coroutines.launch

@Composable
fun AssetDownloadScreen(
    context: android.content.Context,
    onBackPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val externalAssetManager = remember { ExternalAssetManager(context) }
    val llamaManager = remember { LlamaManager(context) }
    val voskManager = remember { VoskManager(context) }
    
    var downloadProgress by remember { mutableStateOf<Map<ExternalAssetManager.AssetType, Float>>(emptyMap()) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadStatus by remember { mutableStateOf<Map<ExternalAssetManager.AssetType, String>>(emptyMap()) }
    
    val assets = listOf(
        AssetInfo(
            type = ExternalAssetManager.AssetType.LLM_MODEL,
            name = "LLM Model (Gemma-2B)",
            description = "Large language model for AI assistance",
            size = "1.3 GB",
            isDownloaded = externalAssetManager.isAssetDownloaded(ExternalAssetManager.AssetType.LLM_MODEL)
        ),
        AssetInfo(
            type = ExternalAssetManager.AssetType.VOSK_EN,
            name = "English Voice Model",
            description = "Speech recognition for English",
            size = "57 MB",
            isDownloaded = externalAssetManager.isAssetDownloaded(ExternalAssetManager.AssetType.VOSK_EN)
        ),
        AssetInfo(
            type = ExternalAssetManager.AssetType.VOSK_TE,
            name = "Telugu Voice Model",
            description = "Speech recognition for Telugu",
            size = "128 MB",
            isDownloaded = externalAssetManager.isAssetDownloaded(ExternalAssetManager.AssetType.VOSK_TE)
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Download Assets",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = onBackPressed) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Large Assets Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Some features require large model files that are copied from your device storage. Make sure you have copied the model files to the specified paths in your device storage.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The app will copy these files to its internal storage for faster access.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Assets list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(assets) { asset ->
                AssetCard(
                    asset = asset,
                    progress = downloadProgress[asset.type] ?: 0f,
                    status = downloadStatus[asset.type] ?: "",
                    isDownloading = isDownloading,
                    onDownload = { assetType ->
                        scope.launch {
                            isDownloading = true
                            downloadStatus = downloadStatus + (assetType to "Copying...")
                            
                            val success = when (assetType) {
                                ExternalAssetManager.AssetType.LLM_MODEL -> {
                                    llamaManager.downloadModel { progress ->
                                        downloadProgress = downloadProgress + (assetType to progress)
                                    }
                                }
                                ExternalAssetManager.AssetType.VOSK_EN -> {
                                    voskManager.downloadModel(VoskManager.LANGUAGE_ENGLISH) { progress ->
                                        downloadProgress = downloadProgress + (assetType to progress)
                                    }
                                }
                                ExternalAssetManager.AssetType.VOSK_TE -> {
                                    voskManager.downloadModel(VoskManager.LANGUAGE_TELUGU) { progress ->
                                        downloadProgress = downloadProgress + (assetType to progress)
                                    }
                                }
                            }
                            
                            downloadStatus = downloadStatus + (assetType to if (success) "Copied" else "Failed")
                            isDownloading = false
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Copy all button
        Button(
            onClick = {
                scope.launch {
                    isDownloading = true
                    
                    // Copy all assets that aren't already copied
                    assets.filter { !it.isDownloaded }.forEach { asset ->
                        downloadStatus = downloadStatus + (asset.type to "Copying...")
                        
                        val success = when (asset.type) {
                            ExternalAssetManager.AssetType.LLM_MODEL -> {
                                llamaManager.downloadModel { progress ->
                                    downloadProgress = downloadProgress + (asset.type to progress)
                                }
                            }
                            ExternalAssetManager.AssetType.VOSK_EN -> {
                                voskManager.downloadModel(VoskManager.LANGUAGE_ENGLISH) { progress ->
                                    downloadProgress = downloadProgress + (asset.type to progress)
                                }
                            }
                            ExternalAssetManager.AssetType.VOSK_TE -> {
                                voskManager.downloadModel(VoskManager.LANGUAGE_TELUGU) { progress ->
                                    downloadProgress = downloadProgress + (asset.type to progress)
                                }
                            }
                        }
                        
                        downloadStatus = downloadStatus + (asset.type to if (success) "Copied" else "Failed")
                    }
                    
                    isDownloading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDownloading && assets.any { !it.isDownloaded }
        ) {
            Text("Copy All Missing Assets")
        }
    }
}

@Composable
private fun AssetCard(
    asset: AssetInfo,
    progress: Float,
    status: String,
    isDownloading: Boolean,
    onDownload: (ExternalAssetManager.AssetType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = asset.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = asset.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Size: ${asset.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (asset.isDownloaded) {
                    Text(
                        text = "✓ Available",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Button(
                        onClick = { onDownload(asset.type) },
                        enabled = !isDownloading
                    ) {
                        Text("Copy")
                    }
                }
            }
            
            if (status.isNotEmpty() && !asset.isDownloaded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (progress > 0f && progress < 100f) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${progress.toInt()}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class AssetInfo(
    val type: ExternalAssetManager.AssetType,
    val name: String,
    val description: String,
    val size: String,
    val isDownloaded: Boolean
) 