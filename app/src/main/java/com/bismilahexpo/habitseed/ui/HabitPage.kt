package com.bismilahexpo.habitseed.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bismilahexpo.habitseed.model.Habit
import com.bismilahexpo.habitseed.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HabitPage(
    habits: List<Habit>,
    onToggleHabit: (Habit) -> Unit,
    onAddHabit: (String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedHabitIdForEvidence by rememberSaveable { mutableStateOf<String?>(null) }
    var showEvidenceDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    
    var tempEvidenceUriString by rememberSaveable { mutableStateOf<String?>(null) }

    fun createTempPictureUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "habit_proof_$timeStamp"
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
        val uriStr = tempEvidenceUriString
        if (success && uriStr != null) {
            val habitId = selectedHabitIdForEvidence
            if (habitId != null) {
                val habit = habits.find { it.id == habitId }
                if (habit != null) {
                    onToggleHabit(habit.copy(isCompleted = true, evidenceUri = uriStr))
                }
            }
            showEvidenceDialog = false
            selectedHabitIdForEvidence = null
            tempEvidenceUriString = null
        } else {
            Toast.makeText(context, "Gagal/Batal mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val habitId = selectedHabitIdForEvidence
            if (habitId != null) {
                val habit = habits.find { it.id == habitId }
                if (habit != null) {
                    onToggleHabit(habit.copy(isCompleted = true, evidenceUri = uri.toString()))
                }
            }
            showEvidenceDialog = false
            selectedHabitIdForEvidence = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(24.dp)
        ) {
            Text(
                "Habit",
                style = MaterialTheme.typography.titleLarge,
                color = LightPrimaryContent,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Belum ada habit, yuk tambah!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LightSecondaryContent
                    )
                }
            } else {
                // Group habits by date
                val groupedHabits = habits
                    .sortedByDescending { it.createdAt }
                    .groupBy { habit ->
                        habit.createdAt?.let { 
                            try {
                                val instant = java.time.Instant.parse(it)
                                val localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                localDate
                            } catch (e: Exception) {
                                java.time.LocalDate.now()
                            }
                        } ?: java.time.LocalDate.now()
                    }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    groupedHabits.forEach { (date, habitsForDate) ->
                        // Date header
                        item {
                            val today = java.time.LocalDate.now()
                            val dateText = when {
                                date == today -> "Hari Ini"
                                date == today.minusDays(1) -> "Kemarin"
                                date.year == today.year -> date.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM", java.util.Locale("id", "ID")))
                                else -> date.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale("id", "ID")))
                            }
                            
                            val completedCount = habitsForDate.count { it.isCompleted }
                            val totalCount = habitsForDate.size
                            
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateText,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = LightPrimaryContent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$completedCount/$totalCount selesai",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (completedCount == totalCount) SeedGreen else LightSecondaryContent,
                                        fontWeight = if (completedCount == totalCount) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        // Habits for this date
                        items(habitsForDate) { habit ->
                            HabitCard(
                                habit = habit, 
                                onToggle = { 
                                    if (!habit.isCompleted) {
                                        selectedHabitIdForEvidence = habit.id
                                        showEvidenceDialog = true
                                    } else {
                                        onToggleHabit(habit.copy(isCompleted = false, evidenceUri = null))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp),
            containerColor = SeedGreen,
            contentColor = LightPrimaryContent
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Habit")
        }

        if (showAddDialog) {
            AddHabitDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, goal ->
                    onAddHabit(name, goal)
                    showAddDialog = false
                }
            )
        }
        
        if (showEvidenceDialog && selectedHabitIdForEvidence != null) {
            com.bismilahexpo.habitseed.ui.components.EvidenceUploadDialog(
                onDismiss = { 
                    showEvidenceDialog = false
                    selectedHabitIdForEvidence = null
                },
                onLaunchCamera = {
                    try {
                        val uri = createTempPictureUri()
                        tempEvidenceUriString = uri.toString()
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
    }
}

@Composable
fun HabitCard(habit: Habit, onToggle: (Habit) -> Unit) {
    val isToday = habit.createdAt?.let {
        try {
            val instant = java.time.Instant.parse(it)
            val habitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            habitDate == java.time.LocalDate.now()
        } catch (e: Exception) {
            true
        }
    } ?: true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightCardBackground)
            .clickable(enabled = isToday && !habit.isCompleted) { onToggle(habit) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
             if (habit.isCompleted && habit.evidenceUri != null) {
                 AsyncImage(
                     model = habit.evidenceUri,
                     contentDescription = "Bukti",
                     modifier = Modifier
                         .size(50.dp)
                         .clip(RoundedCornerShape(8.dp)),
                     contentScale = androidx.compose.ui.layout.ContentScale.Crop
                 )
                 Spacer(modifier = Modifier.width(16.dp))
             }
             
             Column {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = LightPrimaryContent,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (habit.isChallenge) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = SeedGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Tantangan",
                                style = MaterialTheme.typography.labelSmall,
                                color = SeedGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                 }
                Text(
                    text = habit.goal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightSecondaryContent,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        if (habit.isCompleted) {
            Text(
                text = "Selesai",
                color = SeedGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
        } else {
            Button(
                onClick = { onToggle(habit) },
                enabled = isToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SeedGreen,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(if (isToday) "Selesaikan" else "Terlewat")
            }
        }
    }
}

@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Habit", color = LightPrimaryContent) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Habit") },
                    singleLine = true,
                     colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightPrimaryContent,
                        unfocusedTextColor = LightPrimaryContent,
                        focusedBorderColor = SeedGreen,
                        unfocusedBorderColor = LightSecondaryContent,
                        focusedLabelColor = SeedGreen,
                        unfocusedLabelColor = LightSecondaryContent,
                        cursorColor = SeedGreen
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Deskripsi Habit") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightPrimaryContent,
                        unfocusedTextColor = LightPrimaryContent,
                        focusedBorderColor = SeedGreen,
                        unfocusedBorderColor = LightSecondaryContent,
                        focusedLabelColor = SeedGreen,
                        unfocusedLabelColor = LightSecondaryContent,
                        cursorColor = SeedGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name, goal) },
                colors = ButtonDefaults.buttonColors(containerColor = SeedGreen, contentColor = Color.White)
            ) {
                Text("Tanam Habit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kembali", color = LightSecondaryContent)
            }
        },
        containerColor = LightCardBackground,
        textContentColor = LightSecondaryContent
    )
}
