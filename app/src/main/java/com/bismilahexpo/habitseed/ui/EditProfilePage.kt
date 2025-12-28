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
import com.bismilahexpo.habitseed.ui.components.EvidenceUploadDialog
import com.bismilahexpo.habitseed.ui.theme.SeedGreen

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
                EvidenceUploadDialog(
                    onDismiss = { showAvatarDialog = false },
                    onEvidenceSelected = { uri ->
                         onSaveAvatar(uri.toString())
                         showAvatarDialog = false
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
