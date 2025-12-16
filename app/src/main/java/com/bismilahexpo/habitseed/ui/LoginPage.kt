
package com.bismilahexpo.habitseed.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.auth

@Composable
fun LoginPage(navController: NavController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    // PENTING: Ganti <USERNAME-ANDA> dengan username GitHub Anda.
    val url = "https://agninugroho.github.io/finishLogin"

    val actionCodeSettings = ActionCodeSettings.newBuilder()
        .setUrl(url) // <-- Menggunakan URL GitHub Pages
        .setHandleCodeInApp(true)
        .setAndroidPackageName(
            "com.bismilahexpo.habitseed",
            true, /* installIfNotAvailable */
            null /* minimumVersion */
        )
        .build()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "HabitSeed", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveEmail(context, email)
                            Toast.makeText(context, "Tautan login telah dikirim ke email Anda.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Gagal mengirim tautan: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        TextButton(onClick = { navController.navigate("register") }) {
            Text("Belum punya akun? Daftar")
        }
    }
}

private fun saveEmail(context: Context, email: String) {
    val sharedPref = context.getSharedPreferences("HabitSeedPrefs", Context.MODE_PRIVATE) ?: return
    with (sharedPref.edit()) {
        putString("USER_EMAIL", email)
        apply()
    }
}
