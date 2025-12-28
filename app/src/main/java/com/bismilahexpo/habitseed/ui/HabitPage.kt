package com.bismilahexpo.habitseed.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bismilahexpo.habitseed.model.Habit
import com.bismilahexpo.habitseed.ui.theme.*

@Composable
fun HabitPage(
    habits: List<Habit>,
    onToggleHabit: (Habit) -> Unit,
    onAddHabit: (String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedHabitForEvidence by remember { mutableStateOf<Habit?>(null) }
    var showEvidenceDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                                        selectedHabitForEvidence = habit
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
        
        if (showEvidenceDialog && selectedHabitForEvidence != null) {
            com.bismilahexpo.habitseed.ui.components.EvidenceUploadDialog(
                onDismiss = { showEvidenceDialog = false },
                onEvidenceSelected = { uri ->
                    selectedHabitForEvidence?.let { habit ->
                         onToggleHabit(habit.copy(isCompleted = true, evidenceUri = uri.toString()))
                    }
                    showEvidenceDialog = false
                    selectedHabitForEvidence = null
                }
            )
        }
    }
}

@Composable
fun HabitCard(habit: Habit, onToggle: (Habit) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightCardBackground)
            .clickable { onToggle(habit) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
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
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightPrimaryContent,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = habit.goal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightSecondaryContent
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = SeedGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Selesaikan")
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
                    label = { Text("Target Habit") },
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
