
package com.bismilahexpo.habitseed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bismilahexpo.habitseed.ui.HomePage
import com.bismilahexpo.habitseed.ui.LoginPage
import com.bismilahexpo.habitseed.ui.RegisterPage
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bismilahexpo.habitseed.model.Habit
import com.bismilahexpo.habitseed.model.Challenge
import com.bismilahexpo.habitseed.model.UserChallenge
import com.bismilahexpo.habitseed.ui.HabitPage
import com.bismilahexpo.habitseed.ui.theme.SeedGreen
import com.bismilahexpo.habitseed.ui.theme.HabitSeedTheme
import com.bismilahexpo.habitseed.data.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var currentIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent = intent
        enableEdgeToEdge()
        setContent {
            HabitSeedTheme {
                currentIntent?.let { AppNavigation(intent = it) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent = intent
    }
}

@Composable
fun AppNavigation(intent: Intent) {
    val navController = rememberNavController()
    var habits by remember { mutableStateOf(listOf<Habit>()) }
    var userName by remember { mutableStateOf("User") }
    var user by remember { mutableStateOf(Supabase.client.auth.currentUserOrNull()) }
    var currentChallenge by remember { mutableStateOf<Challenge?>(null) }
    var isChallengeTaken by remember { mutableStateOf(false) }
    var isChallengeCompleted by remember { mutableStateOf(false) }
    
    val localScope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val startDestination = if (user != null) "home" else "login"
    
    LaunchedEffect(Unit) {
        Supabase.client.auth.sessionStatus.collectLatest { status ->
             if (status is SessionStatus.Authenticated) {
                 user = Supabase.client.auth.currentUserOrNull()
             } else {
                 user = null
             }
        }
    }

    LaunchedEffect(user) {
        val currentUser = user    
        if (currentUser != null) {
            try {
                 val result = Supabase.client.from("users").select {
                     filter {
                         eq("id", currentUser.id)
                     }
                 }.decodeSingleOrNull<Map<String, String>>()
                 
                 if (result != null) {
                     userName = result["username"] ?: "User"
                     Toast.makeText(context, "Berhasil Login!", Toast.LENGTH_SHORT).show()
                 } else {
                     userName = "User"
                     Toast.makeText(context, "Data user tidak ditemukan di database", Toast.LENGTH_LONG).show()
                 }
            } catch (e: Exception) {
                userName = "User"
                Toast.makeText(context, "Error load username: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            
            try {
                habits = Supabase.client.from("habits").select { 
                    filter { eq("user_id", currentUser.id) }
                }.decodeList()
            } catch(e: Exception) { 
                android.util.Log.e("MainActivity", "Error fetching habits", e)
                e.printStackTrace()
            }

            // Fetch Daily Challenge
            try {
                val challenges = Supabase.client.from("challenges").select().decodeList<Challenge>()
                if (challenges.isNotEmpty()) {
                    // Pick a challenge based on current day of year
                    val dayOfYear = java.time.LocalDate.now().dayOfYear
                    currentChallenge = challenges[dayOfYear % challenges.size]
                    
                    // Check if completed today
                    val completion = Supabase.client.from("user_challenges").select {
                        filter {
                            eq("user_id", currentUser.id)
                            eq("challenge_id", currentChallenge!!.id!!)
                            eq("completed_at", java.time.LocalDate.now().toString())
                        }
                    }.decodeSingleOrNull<UserChallenge>()
                    
                    isChallengeCompleted = completion != null
                    
                    // Check if taken today (exists in habits table as is_challenge = true)
                    isChallengeTaken = habits.any { 
                        it.isChallenge && it.createdAt?.let { date ->
                            try {
                                val instant = java.time.Instant.parse(date)
                                val habitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                habitDate == java.time.LocalDate.now()
                            } catch (e: Exception) { false }
                        } ?: false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error fetching challenges", e)
            }
        } else {
            habits = emptyList()
            userName = "?"
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController, 
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable("login") {
                    LoginPage(navController = navController)
                }
                composable("register") {
                    RegisterPage(navController = navController)
                }
                composable("home") {
                    HomePage(
                        userName = userName,
                        habits = habits,
                        currentChallenge = currentChallenge,
                        isChallengeTaken = isChallengeTaken,
                        isChallengeCompleted = isChallengeCompleted,
                        onLogout = {
                             localScope.launch {
                                 Supabase.client.auth.signOut()
                             }
                        },
                        onTakeChallenge = { challenge ->
                            localScope.launch {
                                try {
                                    val currentUser = Supabase.client.auth.currentUserOrNull()
                                    if (currentUser != null) {
                                        // ONLY add to habits table as a challenge habit (NOT completed yet)
                                        val habitData = buildJsonObject {
                                            put("user_id", JsonPrimitive(currentUser.id))
                                            put("name", JsonPrimitive(challenge.title))
                                            put("goal", JsonPrimitive(challenge.description))
                                            put("is_completed", JsonPrimitive(false))
                                            put("is_challenge", JsonPrimitive(true))
                                            put("created_at", JsonPrimitive(java.time.Instant.now().toString()))
                                        }
                                        Supabase.client.from("habits").insert(habitData)
                                        
                                        isChallengeTaken = true
                                        
                                        // Refresh habits
                                        habits = Supabase.client.from("habits").select { 
                                            filter { eq("user_id", currentUser.id) }
                                        }.decodeList()
                                        
                                        Toast.makeText(context, "Tantangan diambil! Cek daftar habit-mu �", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MainActivity", "Error taking challenge", e)
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
                composable("habits") {
                    HabitPage(
                        habits = habits,
                        onToggleHabit = { updatedHabit ->
                            localScope.launch {
                                try {
                                    if (updatedHabit.id != null) {
                                        var finalEvidenceUri = updatedHabit.evidenceUri
                                        
                                        if (finalEvidenceUri != null && (finalEvidenceUri.startsWith("content://") || finalEvidenceUri.startsWith("file://"))) {
                                            try {
                                                val uri = android.net.Uri.parse(finalEvidenceUri)
                                                val inputStream = context.contentResolver.openInputStream(uri)
                                                val bytes = inputStream?.readBytes()
                                                if (bytes != null) {
                                                    val fileName = "${System.currentTimeMillis()}.jpg"
                                                    val bucket = Supabase.client.storage.from("habit-evidence")
                                                    
                                                    bucket.upload(fileName, bytes)
                                                    
                                                    finalEvidenceUri = bucket.publicUrl(fileName)
                                                } else {
                                                }
                                                inputStream?.close()
                                            } catch (e: Exception) {
                                                localScope.launch {
                                                    Toast.makeText(context, "Gagal upload: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }

                                        val updateData = buildJsonObject {
                                            put("is_completed", JsonPrimitive(updatedHabit.isCompleted))
                                            if (finalEvidenceUri != null) {
                                                put("evidence_uri", JsonPrimitive(finalEvidenceUri))
                                            } else {
                                                put("evidence_uri", JsonPrimitive(null as String?))
                                            }
                                        }

                                        Supabase.client.from("habits").update(updateData) {
                                            filter { eq("id", updatedHabit.id) }
                                        }

                                        // SYNC CHALLENGE COMPLETION
                                        if (updatedHabit.isChallenge && updatedHabit.isCompleted) {
                                            try {
                                                val currUser = Supabase.client.auth.currentUserOrNull()
                                                val currChall = currentChallenge
                                                if (currUser != null && currChall != null) {
                                                    val challData = buildJsonObject {
                                                        put("user_id", JsonPrimitive(currUser.id))
                                                        put("challenge_id", JsonPrimitive(currChall.id!!))
                                                        put("completed_at", JsonPrimitive(java.time.LocalDate.now().toString()))
                                                    }
                                                    Supabase.client.from("user_challenges").insert(challData)
                                                    isChallengeCompleted = true
                                                }
                                            } catch (e: Exception) {
                                            }
                                        }
                                        
                                        // Refresh habits
                                        val currentUser = Supabase.client.auth.currentUserOrNull()
                                        if (currentUser != null) {
                                            habits = Supabase.client.from("habits").select { 
                                                filter { eq("user_id", currentUser.id) }
                                            }.decodeList()
                                        }
                                    }
                                } catch(e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onAddHabit = { name, goal ->
                            localScope.launch {
                                val currentUser = Supabase.client.auth.currentUserOrNull()
                                if (currentUser != null) {
                                     val habitData = kotlinx.serialization.json.buildJsonObject {
                                         put("user_id", kotlinx.serialization.json.JsonPrimitive(currentUser.id))
                                         put("name", kotlinx.serialization.json.JsonPrimitive(name))
                                         put("goal", kotlinx.serialization.json.JsonPrimitive(goal))
                                         put("is_completed", kotlinx.serialization.json.JsonPrimitive(false))
                                     }
                                     
                                     try {
                                         Supabase.client.from("habits").insert(habitData)
                                         Toast.makeText(context, "Habit ditanam!", Toast.LENGTH_SHORT).show()
                                         
                                         try {
                                             habits = Supabase.client.from("habits").select { 
                                                 filter { eq("user_id", currentUser.id) }
                                             }.decodeList()
                                         } catch(refreshError: Exception) {
                                             android.util.Log.e("MainActivity", "Error refreshing habits", refreshError)
                                         }
                                     } catch(e: Exception) {
                                         Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                                         e.printStackTrace()
                                     }
                                } else {
                                    Toast.makeText(context, "Silakan login ulang", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            if (currentRoute == "home" || currentRoute == "habits") {
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
                ) {
                     NavigationBar(
                         containerColor = Color.Transparent, 
                         contentColor = SeedGreen,
                         modifier = Modifier.padding(horizontal = 8.dp).height(64.dp),
                         windowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp)
                     ) {
                         NavigationBarItem(
                             icon = { Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = "Home") },
                             label = { Text("Dasbor") },
                             selected = currentRoute == "home",
                             onClick = {
                                 navController.navigate("home") {
                                     popUpTo(navController.graph.startDestinationId) { saveState = true }
                                     launchSingleTop = true
                                     restoreState = true
                                 }
                             },
                             colors = NavigationBarItemDefaults.colors(
                                 selectedIconColor = SeedGreen,
                                 selectedTextColor = SeedGreen,
                                 indicatorColor = SeedGreen.copy(alpha = 0.1f),
                                 unselectedIconColor = Color.LightGray,
                                 unselectedTextColor = Color.LightGray
                             )
                         )
                         NavigationBarItem(
                             icon = { Icon(androidx.compose.material.icons.Icons.Default.List, contentDescription = "Habits") },
                             label = { Text("Habit") },
                             selected = currentRoute == "habits",
                             onClick = {
                                 navController.navigate("habits") {
                                     popUpTo(navController.graph.startDestinationId) { saveState = true }
                                     launchSingleTop = true
                                     restoreState = true
                                 }
                             },
                             colors = NavigationBarItemDefaults.colors(
                                 selectedIconColor = SeedGreen,
                                 selectedTextColor = SeedGreen,
                                 indicatorColor = SeedGreen.copy(alpha = 0.1f),
                                 unselectedIconColor = Color.LightGray,
                                 unselectedTextColor = Color.LightGray
                             )
                         )
                     }
                }
            }
        }
    }
}