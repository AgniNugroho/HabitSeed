package com.bismilahexpo.habitseed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismilahexpo.habitseed.ui.components.ProfileMenuCard
import com.bismilahexpo.habitseed.ui.components.AvatarUploadDialog
import com.bismilahexpo.habitseed.ui.theme.SeedGreen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePage(
    currentUserName: String,
    currentUserAvatarUrl: String?,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    onSaveAvatar: (String) -> Unit
) {
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showAvatarDialog by rememberSaveable { mutableStateOf(false) }
    var newUserName by remember { mutableStateOf(currentUserName) }
    
    val context = LocalContext.current
    var tempAvatarUriString by rememberSaveable { mutableStateOf<String?>(null) }

    fun createTempAvatarUri(): Uri {
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
        val uriStr = tempAvatarUriString
        if (success && uriStr != null) {
            onSaveAvatar(uriStr)
            showAvatarDialog = false
            tempAvatarUriString = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onSaveAvatar(uri.toString())
            showAvatarDialog = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileMenuCard(
                icon = Icons.Default.Person,
                title = "Edit Nama Pengguna",
                onClick = { showEditDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ProfileMenuCard(
                icon = Icons.Default.PhotoCamera,
                title = "Edit Foto Profil",
                onClick = { showAvatarDialog = true }
            )
            
            if (showAvatarDialog) {
                AvatarUploadDialog(
                    onDismiss = { showAvatarDialog = false },
                    onLaunchCamera = {
                        try {
                            val uri = createTempAvatarUri()
                            tempAvatarUriString = uri.toString()
                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onLaunchGallery = {
                        try {
                            galleryLauncher.launch("image/*")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
            
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Edit Nama Pengguna") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newUserName,
                                onValueChange = { newUserName = it },
                                label = { Text("Nama Baru") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SeedGreen,
                                    focusedLabelColor = SeedGreen
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { 
                                onSave(newUserName)
                                showEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SeedGreen)
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}
