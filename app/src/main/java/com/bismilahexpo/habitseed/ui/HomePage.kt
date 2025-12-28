package com.bismilahexpo.habitseed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismilahexpo.habitseed.model.Habit
import com.bismilahexpo.habitseed.ui.components.CircularProgressBar
import com.bismilahexpo.habitseed.ui.theme.*

@Composable
fun HomePage(
    userName: String,
    habits: List<Habit>,
    onLogout: () -> Unit
) {
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
            HeaderSection(userName, onLogout)

            Spacer(modifier = Modifier.height(24.dp))
            
            // Filter habits for today only
            val todayHabits = habits.filter { habit ->
                habit.createdAt?.let {
                    try {
                        val instant = java.time.Instant.parse(it)
                        val habitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        habitDate == java.time.LocalDate.now()
                    } catch (e: Exception) {
                        false
                    }
                } ?: false
            }
            
            val completedCount = todayHabits.count { it.isCompleted }
            val totalCount = todayHabits.size
            val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
            
            val motivation = when {
                totalCount == 0 -> "Belum ada habit hari ini"
                progress == 0f -> "Ayo selesaikan habit hari ini!"
                progress < 0.5f -> "Awal yang baik, teruskan!"
                progress < 1f -> "Hampir selesai, jangan putus asa!"
                else -> "Semua habit hari ini selesai! 🎉"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LightCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         CircularProgressBar(
                            percentage = progress,
                            number = 100,
                            radius = 45.dp,
                            strokeWidth = 8.dp,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Progress Harian",
                            style = MaterialTheme.typography.titleSmall,
                            color = LightSecondaryContent,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            motivation,
                            style = MaterialTheme.typography.titleLarge,
                            color = LightPrimaryContent,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, onLogout: () -> Unit) {
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting, $userName! \uD83D\uDC4B",
                style = MaterialTheme.typography.headlineSmall,
                color = LightSecondaryContent
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        IconButton(onClick = onLogout) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = LightSecondaryContent
            )
        }
    }
}
