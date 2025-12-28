
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
import com.bismilahexpo.habitseed.ui.HabitPage
import com.bismilahexpo.habitseed.ui.theme.SeedGreen
import com.bismilahexpo.habitseed.ui.theme.HabitSeedTheme
import com.bismilahexpo.habitseed.data.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
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
                         onLogout = {
                             localScope.launch {
                                 Supabase.client.auth.signOut()
                                 navController.navigate("login") { popUpTo(0) }
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
                                        Supabase.client.from("habits").update(
                                            {
                                                set("is_completed", updatedHabit.isCompleted)
                                                set("evidence_uri", updatedHabit.evidenceUri)
                                            }
                                        ) {
                                            filter { eq("id", updatedHabit.id) }
                                        }

                                        val currentUser = Supabase.client.auth.currentUserOrNull()
                                        if (currentUser != null) {
                                            try {
                                                habits = Supabase.client.from("habits").select { 
                                                    filter { eq("user_id", currentUser.id) }
                                                }.decodeList()
                                            } catch(refreshError: Exception) {
                                                android.util.Log.e("MainActivity", "Error refreshing habits", refreshError)
                                            }
                                        }
                                    }
                                } catch(e: Exception) {
                                    android.util.Log.e("MainActivity", "Error updating habit", e)
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    e.printStackTrace()
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