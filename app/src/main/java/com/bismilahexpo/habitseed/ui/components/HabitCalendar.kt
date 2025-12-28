package com.bismilahexpo.habitseed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismilahexpo.habitseed.model.Habit
import com.bismilahexpo.habitseed.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun HabitCalendar(habits: List<Habit>, modifier: Modifier = Modifier) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // 0=Sunday, 1=Monday... (Adjusting for Sunday start)
    
    val habitsByDate = remember(habits) {
        habits.groupBy { habit ->
            habit.createdAt?.let {
                try {
                    java.time.Instant.parse(it)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                } catch (e: Exception) { null }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kalender Habit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LightPrimaryContent
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft, 
                            contentDescription = "Previous Month",
                            tint = LightPrimaryContent
                        )
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale("id", "ID"))),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SeedGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 80.dp)
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(
                            Icons.Default.KeyboardArrowRight, 
                            contentDescription = "Next Month",
                            tint = LightPrimaryContent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekday labels
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = LightSecondaryContent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            val totalCells = ((daysInMonth + firstDayOfMonth + 6) / 7) * 7
            Column {
                for (row in 0 until totalCells / 7) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayOfMonth = cellIndex - firstDayOfMonth + 1
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                            ) {
                                if (dayOfMonth in 1..daysInMonth) {
                                    val date = currentMonth.atDay(dayOfMonth)
                                    val dayHabits = habitsByDate[date] ?: emptyList()
                                    
                                    val bgColor = when {
                                        dayHabits.isEmpty() -> Color.Transparent
                                        dayHabits.all { it.isCompleted } -> SeedGreen
                                        dayHabits.any { it.isCompleted } -> AccentYellow
                                        else -> Color.LightGray.copy(alpha = 0.3f)
                                    }

                                    val contentColor = when (bgColor) {
                                        Color.Transparent -> LightSecondaryContent
                                        SeedGreen -> Color.White
                                        AccentYellow -> Color.White
                                        else -> LightSecondaryContent
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgColor)
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = dayOfMonth.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = contentColor,
                                            modifier = Modifier.align(Alignment.TopStart)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
