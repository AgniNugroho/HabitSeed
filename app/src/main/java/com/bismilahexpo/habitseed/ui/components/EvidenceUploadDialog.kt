package com.bismilahexpo.habitseed.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.widget.Toast
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
    var tempUriString by rememberSaveable { mutableStateOf<String?>(null) }

    // Helper to create a temp file and URI
    fun createTempPictureUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "avatar_$timeStamp"
        val storageDir = context.cacheDir
        val file = File(storageDir, "$imageFileName.jpg")
        if (file.exists()) file.delete()
        file.createNewFile()

        return FileProvider.getUriForFile(
             context,
             "${context.packageName}.fileprovider",
             file
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uriStr = tempUriString
        if (success && uriStr != null) {
            onEvidenceSelected(Uri.parse(uriStr))
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
        title = { Text("Upload Foto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {           
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { 
                            if (context.checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
                                // In a real app, you'd request permission here
                            } else {
                                try {
                                    val uri = createTempPictureUri()
                                    tempUriString = uri.toString()
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Tidak dapat membuka kamera: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SeedGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kamera")
                    }
                    
                     Button(
                        onClick = { 
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Tidak dapat membuka galeri", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galeri")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kembali", color = Color.Gray)
            }
        }
    )
}
