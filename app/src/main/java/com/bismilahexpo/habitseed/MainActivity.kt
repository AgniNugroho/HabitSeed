package com.bismilahexpo.habitseed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bismilahexpo.habitseed.data.Supabase
import com.bismilahexpo.habitseed.data.StorageHelper
import com.bismilahexpo.habitseed.model.Challenge
import com.bismilahexpo.habitseed.model.Habit
import com.bismilahexpo.habitseed.model.UserChallenge
import com.bismilahexpo.habitseed.ui.EditProfilePage
import com.bismilahexpo.habitseed.ui.HabitGalleryPage
import com.bismilahexpo.habitseed.ui.HabitPage
import com.bismilahexpo.habitseed.ui.HomePage
import com.bismilahexpo.habitseed.ui.LoginPage
import com.bismilahexpo.habitseed.ui.ProfilePage
import com.bismilahexpo.habitseed.ui.RegisterPage
import com.bismilahexpo.habitseed.ui.SettingsPage
import com.bismilahexpo.habitseed.ui.theme.HabitSeedTheme
import com.bismilahexpo.habitseed.ui.theme.SeedGreen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
    var isAuthLoading by remember { mutableStateOf(true) }
    var habits by remember { mutableStateOf(listOf<Habit>()) }
    var userName by rememberSaveable { mutableStateOf("User") }
    var userAvatarUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var user by remember { mutableStateOf<io.github.jan.supabase.auth.user.UserInfo?>(null) }
    var currentChallenge by remember { mutableStateOf<Challenge?>(null) }
    var isChallengeTaken by remember { mutableStateOf(false) }
    var isChallengeCompleted by remember { mutableStateOf(false) }
    
    val localScope = rememberCoroutineScope()
    val context = LocalContext.current

    suspend fun refreshHabits() {
        val currentUser = user
        if (currentUser != null) {
            try {
                habits = Supabase.client.from("habits").select {
                    filter { eq("user_id", currentUser.id) }
                }.decodeList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun handleChallengeCompletion(habit: Habit) {
        val currUser = user
        val currChall = currentChallenge
        if (currUser != null && currChall != null) {
            try {
                val challData = buildJsonObject {
                    put("user_id", JsonPrimitive(currUser.id))
                    put("challenge_id", JsonPrimitive(currChall.id!!))
                    put("completed_at", JsonPrimitive(java.time.LocalDate.now().toString()))
                }
                Supabase.client.from("user_challenges").insert(challData)
                isChallengeCompleted = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    LaunchedEffect(Unit) {
        Supabase.client.auth.sessionStatus.collectLatest { status ->
             when (status) {
                 is SessionStatus.Authenticated -> {
                     user = Supabase.client.auth.currentUserOrNull()
                     isAuthLoading = false
                 }
                 is SessionStatus.NotAuthenticated -> {
                     user = null
                     isAuthLoading = false
                 }
                 else -> {
                     isAuthLoading = true
                 }
             }
        }
    }

    LaunchedEffect(user) {
        val currentUser = user    
        if (currentUser != null) {
            try {
                 val result = Supabase.client.from("users").select {
                     filter { eq("id", currentUser.id) }
                 }.decodeSingleOrNull<Map<String, String>>()
                 
                 if (result != null) {
                     userName = result["username"] ?: "User"
                     userAvatarUrl = result["avatar_url"]
                 } else {
                     // Auto-repair missing user record
                     val fallbackName = currentUser.email?.substringBefore("@") ?: "User"
                     val metadataName = currentUser.userMetadata?.get("username")?.toString()?.replace("\"", "")
                     val finalUsername = metadataName ?: fallbackName
                     
                     val userData = buildJsonObject {
                         put("id", currentUser.id)
                         put("email", currentUser.email ?: "")
                         put("username", finalUsername)
                         put("last_login", java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now()))
                     }
                     
                     try {
                         Supabase.client.from("users").insert(userData)
                         userName = finalUsername
                     } catch (e: Exception) {
                         userName = "User"
                         e.printStackTrace()
                     }
                     userAvatarUrl = null
                 }
            } catch (e: Exception) {
                userName = "User"
                userAvatarUrl = null
                e.printStackTrace()
            }
            
            try {
                habits = Supabase.client.from("habits").select { 
                    filter { eq("user_id", currentUser.id) }
                }.decodeList()
            } catch(e: Exception) { 
                e.printStackTrace()
            }

            try {
                val challenges = Supabase.client.from("challenges").select().decodeList<Challenge>()
                if (challenges.isNotEmpty()) {
                    val dayOfYear = java.time.LocalDate.now().dayOfYear
                    currentChallenge = challenges[dayOfYear % challenges.size]
                    
                    val completion = Supabase.client.from("user_challenges").select {
                        filter {
                            eq("user_id", currentUser.id)
                            eq("challenge_id", currentChallenge!!.id!!)
                            eq("completed_at", java.time.LocalDate.now().toString())
                        }
                    }.decodeSingleOrNull<UserChallenge>()
                    
                    isChallengeCompleted = completion != null
                    
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

    if (isAuthLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator(color = SeedGreen)
        }
        return
    }

    val startDestination = if (user != null) "home" else "login"

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController, 
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable("login") { LoginPage(navController = navController) }
                composable("register") { RegisterPage(navController = navController) }
                composable("home") {
                    HomePage(
                        userName = userName,
                        habits = habits,
                        currentChallenge = currentChallenge,
                        isChallengeTaken = isChallengeTaken,
                        isChallengeCompleted = isChallengeCompleted,
                        onLogout = { localScope.launch { Supabase.client.auth.signOut() } },
                        onTakeChallenge = { challenge ->
                            localScope.launch {
                                try {
                                    val currentUser = Supabase.client.auth.currentUserOrNull()
                                    if (currentUser != null) {
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
                                        habits = Supabase.client.from("habits").select { 
                                            filter { eq("user_id", currentUser.id) }
                                        }.decodeList()
                                        Toast.makeText(context, "Tantangan diambil!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
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
                                        val isNewLocalEvidence = finalEvidenceUri != null && 
                                                (finalEvidenceUri.startsWith("content://") || finalEvidenceUri.startsWith("file://"))

                                        if (isNewLocalEvidence) {
                                            // 1. Upload file if it's a local URI
                                            StorageHelper.uploadFile(
                                                context = context,
                                                bucketName = "habit-evidence",
                                                uri = android.net.Uri.parse(finalEvidenceUri!!),
                                                onSuccess = { publicUrl ->
                                                    // 2. Perform DB update after successful upload
                                                    localScope.launch {
                                                        try {
                                                            val updateData = buildJsonObject {
                                                                put("is_completed", JsonPrimitive(updatedHabit.isCompleted))
                                                                put("evidence_uri", JsonPrimitive(publicUrl))
                                                            }
                                                            Supabase.client.from("habits").update(updateData) {
                                                                filter { eq("id", updatedHabit.id) }
                                                            }
                                                            
                                                            // SYNC CHALLENGE COMPLETION (inside success)
                                                            if (updatedHabit.isChallenge && updatedHabit.isCompleted) {
                                                                handleChallengeCompletion(updatedHabit)
                                                            }
                                                            
                                                            refreshHabits()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Gagal simpan data: ${e.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                onError = { error ->
                                                    localScope.launch {
                                                        Toast.makeText(context, "Upload gagal: $error", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            )
                                        } else {
                                            // 1. Direct DB update if no new file to upload
                                            val updateData = buildJsonObject {
                                                put("is_completed", JsonPrimitive(updatedHabit.isCompleted))
                                                if (finalEvidenceUri == null) {
                                                    put("evidence_uri", JsonPrimitive(null as String?))
                                                }
                                            }
                                            Supabase.client.from("habits").update(updateData) {
                                                filter { eq("id", updatedHabit.id) }
                                            }

                                            if (updatedHabit.isChallenge && updatedHabit.isCompleted) {
                                                handleChallengeCompletion(updatedHabit)
                                            }
                                            
                                            refreshHabits()
                                        }
                                    }
                                } catch(e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onAddHabit = { name, goal ->
                            localScope.launch {
                                val currentUser = user
                                if (currentUser != null) {
                                     val habitData = buildJsonObject {
                                         put("user_id", JsonPrimitive(currentUser.id))
                                         put("name", JsonPrimitive(name))
                                         put("goal", JsonPrimitive(goal))
                                         put("is_completed", JsonPrimitive(false))
                                     }
                                     try {
                                         Supabase.client.from("habits").insert(habitData)
                                         Toast.makeText(context, "Habit ditanam!", Toast.LENGTH_SHORT).show()
                                         habits = Supabase.client.from("habits").select { 
                                             filter { eq("user_id", currentUser.id) }
                                         }.decodeList()
                                     } catch(e: Exception) {
                                         Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                                     }
                                }
                            }
                        }
                    )
                }
                composable("profile") {
                    ProfilePage(
                        userName = userName,
                        userEmail = user?.email ?: "",
                        userAvatarUrl = userAvatarUrl,
                        onEditProfile = { navController.navigate("edit_profile") },
                        onGalleryClick = { navController.navigate("gallery") },
                        onSettingsClick = { navController.navigate("settings") },
                        onLogout = { localScope.launch { Supabase.client.auth.signOut() } }
                    )
                }
                composable("gallery") {
                    HabitGalleryPage(
                        habits = habits,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("edit_profile") {
                    EditProfilePage(
                        currentUserName = userName,
                        currentUserAvatarUrl = userAvatarUrl,
                        onBack = { navController.popBackStack() },
                        onSave = { newName ->
                            localScope.launch {
                                try {
                                    val currentUser = user
                                    if (currentUser != null) {
                                        val updateData = buildJsonObject { put("username", JsonPrimitive(newName)) }
                                        Supabase.client.from("users").update(updateData) {
                                            filter { eq("id", currentUser.id) }
                                        }
                                        userName = newName
                                        Toast.makeText(context, "Profil diperbarui!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal update: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onSaveAvatar = { avatarUri ->
                            localScope.launch {
                                try {
                                    val currentUser = user
                                    if (currentUser != null) {
                                         StorageHelper.uploadFile(
                                             context = context,
                                             bucketName = "avatars",
                                             uri = android.net.Uri.parse(avatarUri),
                                             onSuccess = { publicUrl ->
                                                 localScope.launch {
                                                     val updateData = buildJsonObject {
                                                         put("avatar_url", JsonPrimitive(publicUrl))
                                                     }
                                                     Supabase.client.from("users").update(updateData) {
                                                         filter { eq("id", currentUser.id) }
                                                     }
                                                     userAvatarUrl = publicUrl
                                                     Toast.makeText(context, "Foto profil diperbarui!", Toast.LENGTH_SHORT).show()
                                                 }
                                             }
                                         )
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal update foto: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
                composable("settings") { SettingsPage(onBack = { navController.popBackStack() }) }
            }

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            if (currentRoute == "home" || currentRoute == "habits" || currentRoute == "profile") {
                androidx.compose.material3.Card(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
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
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
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
                            icon = { Icon(Icons.Default.List, contentDescription = "Habits") },
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
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profil") },
                            selected = currentRoute == "profile",
                            onClick = {
                                navController.navigate("profile") {
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