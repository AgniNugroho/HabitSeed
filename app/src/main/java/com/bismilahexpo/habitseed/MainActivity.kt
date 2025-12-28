
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

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
    val user = Firebase.auth.currentUser
    val startDestination = "home" // if (user != null) "home" else "login"
    val context = LocalContext.current
    
    var habits by remember { mutableStateOf(listOf<Habit>()) }
    var userName by remember { mutableStateOf("User") }
    
    LaunchedEffect(Unit) {
        val auth = Firebase.auth
        val db = Firebase.firestore
        
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                if (!currentUser.displayName.isNullOrBlank()) {
                    userName = currentUser.displayName!!
                } else {
                    currentUser.email?.let { email ->
                        db.collection("users")
                            .whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    val name = documents.documents[0].getString("username")
                                    if (!name.isNullOrBlank()) {
                                        userName = name
                                    }
                                }
                            }
                    }
                }
            } else {
                userName = "?"
            }
        }
        
        auth.addAuthStateListener(listener)
    }

    // Habits Real-time Listener
    LaunchedEffect(Unit) {
        val auth = Firebase.auth
        val db = Firebase.firestore
        
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                // Listen to Habits
                 db.collection("users").document(currentUser.uid).collection("habits")
                     .addSnapshotListener { snapshots, e ->
                         if (e != null) {
                             return@addSnapshotListener
                         }
                         if (snapshots != null) {
                             val fetchedHabits = snapshots.toObjects(Habit::class.java)
                             habits = fetchedHabits
                         }
                     }
            } else {
                habits = emptyList()
            }
        }
        auth.addAuthStateListener(listener)
    }
    

    LaunchedEffect(user) {
        if (user != null && userName == "User") {
             if (!user.displayName.isNullOrBlank()) {
                userName = user.displayName!!
            } else {
                val db = Firebase.firestore
                user.email?.let { email ->
                     db.collection("users").whereEqualTo("email", email).get()
                        .addOnSuccessListener { docs ->
                            if (!docs.isEmpty) userName = docs.documents[0].getString("username") ?: "User"
                        }
                }
            }
        }
    }

    LaunchedEffect(intent) {
        handleSignInLink(intent, context, navController)
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
                             Firebase.auth.signOut()
                             navController.navigate("login") { popUpTo(0) }
                        }
                    )
                }
                composable("habits") {
                    HabitPage(
                        habits = habits,
                        onToggleHabit = { updatedHabit ->
                            val user = Firebase.auth.currentUser
                            if (user != null) {
                                val db = Firebase.firestore
                                db.collection("users").document(user.uid)
                                    .collection("habits").document(updatedHabit.id)
                                    .set(updatedHabit)
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Gagal update habit: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        onAddHabit = { name, goal ->
                            val user = Firebase.auth.currentUser
                            if (user != null) {
                                val newHabit = Habit(name = name, goal = goal)
                                val db = Firebase.firestore
                                db.collection("users").document(user.uid)
                                    .collection("habits").document(newHabit.id)
                                    .set(newHabit)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Habit ditanam!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Gagal simpan habit: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Silakan login ulang.", Toast.LENGTH_LONG).show()
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

private fun getEmail(context: Context): String? {
    val sharedPref = context.getSharedPreferences("HabitSeedPrefs", Context.MODE_PRIVATE)
    return sharedPref.getString("USER_EMAIL", null)
}

private fun handleSignInLink(intent: Intent, context: Context, navController: NavController) {
    val auth = Firebase.auth
    val emailLink = intent.data?.toString()

    if (emailLink != null && auth.isSignInWithEmailLink(emailLink)) {
        val email = getEmail(context)
        if (email == null) {
            Toast.makeText(context, "Gagal login: email tidak ditemukan. Silakan coba lagi.", Toast.LENGTH_LONG).show()
            return
        }
        auth.signInWithEmailLink(email, emailLink)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Berhasil login!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo(0)
                    }
                } else {
                    Toast.makeText(context, "Gagal login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
