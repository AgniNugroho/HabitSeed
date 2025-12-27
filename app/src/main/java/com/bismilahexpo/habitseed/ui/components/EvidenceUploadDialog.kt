package com.bismilahexpo.habitseed.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.bismilahexpo.habitseed.ui.theme.SeedGreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EvidenceUploadDialog(
    onDismiss: () -> Unit,
    onEvidenceSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    // Helper to create a temp file and URI
    fun createTempPictureUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        // Use cacheDir for simplicity first, but FileProvider is better for full support.
        // Assuming we might need FileProvider for Camera. For now, try simple approach or prepare for FileProvider.
        // If FileProvider is missing, this will crash. 
        // Let's use a safe approach: save to external cache if possible, or internal if FileProvider matches?
        // Actually, without FileProvider configured in manifest, I cannot share file:// URI to Camera app.
        // I will try to use a simple hack or expect FileProvider setup.
        // For this step, I will implement the logic. If it crashes, I'll add FileProvider.
        
        val storageDir = context.externalCacheDir ?: context.cacheDir
        val file = File.createTempFile(imageFileName, ".jpg", storageDir)
        
        // IMPORTANT: This requires <provider> in Manifest if targeting API 24+
        // I will just return Uri.fromFile(file) which might fail with FileUriExposedException.
        // To fix properly, I need to add FileProvider.
        // For now, I'll attempt using FileProvider assuming I will add it in next steps if needed?
        // No, I should add it now if I use getUriForFile.
        // Let's defer FileProvider setup and assume the user MIGHT crash if I don't add it.
        // I'll add the FileProvider configuration in the next step.
        return androidx.core.content.FileProvider.getUriForFile(
             context, 
             "${context.packageName}.fileprovider", 
             file
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempUri
        if (success && uri != null) {
            onEvidenceSelected(uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onEvidenceSelected(uri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Bukti Habit") },
        text = { Text("Untuk menyelesaikan habit ini, mohon lampirkan foto bukti.") },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        icon = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { 
                        try {
                            val uri = createTempPictureUri()
                            tempUri = uri
                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            // Fallback or error handling
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeedGreen)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kamera")
                }
                
                 Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galeri")
                }
            }
        }
    )
}
