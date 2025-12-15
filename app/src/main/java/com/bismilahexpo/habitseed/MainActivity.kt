
package com.bismilahexpo.habitseed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.bismilahexpo.habitseed.ui.theme.HabitSeedTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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
    val startDestination = if (user != null) "home" else "login"
    val context = LocalContext.current

    // Handle the sign-in link
    LaunchedEffect(intent) {
        handleSignInLink(intent, context, navController)
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginPage(navController = navController)
        }
        composable("register") {
            RegisterPage(navController = navController)
        }
        composable("home") {
            HomePage(navController = navController)
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
