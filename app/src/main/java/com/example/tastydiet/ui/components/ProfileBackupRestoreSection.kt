package com.example.tastydiet.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tastydiet.utils.BackupManager
import kotlinx.coroutines.launch

@Composable
fun ProfileBackupRestoreSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isRestoring = true
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = kotlin.io.path.createTempFile(suffix = ".json").toFile()
                    inputStream?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }
                    val backupManager = BackupManager(context)
                    backupManager.restoreBackup(tempFile)
                    Toast.makeText(context, "Restore successful!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isRestoring = false
                }
            }
        }
    }
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Button(onClick = {
            scope.launch {
                isBackingUp = true
                try {
                    val backupManager = BackupManager(context)
                    val backupFile = backupManager.createBackup()
                    val path = backupFile?.absolutePath ?: "Backup failed"
                    Toast.makeText(context, "Backup saved: $path", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isBackingUp = false
                }
            }
        }, enabled = !isBackingUp) {
            Text(if (isBackingUp) "Backing up..." else "Manual Backup")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            filePickerLauncher.launch("application/json")
        }, enabled = !isRestoring) {
            Text(if (isRestoring) "Restoring..." else "Restore from Backup")
        }
    }
} 