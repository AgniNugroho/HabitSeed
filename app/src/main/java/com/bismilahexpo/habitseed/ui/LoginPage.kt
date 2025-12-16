
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
import com.google.firebase.firestore.firestore

@Composable
fun LoginPage(navController: NavController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    val url = "https://agninugroho.github.io/finishLogin"

    val actionCodeSettings = ActionCodeSettings.newBuilder()
        .setUrl(url) 
        .setHandleCodeInApp(true)
        .setAndroidPackageName(
            "com.bismilahexpo.habitseed",
            true, /* installIfNotAvailable */
            null /* minimumVersion */
        )
        .build()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background 
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "HabitSeed", 
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary 
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Plant your habits, grow your life.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Welcome Back", 
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.medium
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val cleanEmail = email.trim() // Hapus spasi tidak sengaja
                    if (cleanEmail.isNotBlank()) {
                        val db = com.google.firebase.Firebase.firestore
                        
                        // Debugging: Beri tahu user apa yang sedang dicari
                        // Toast.makeText(context, "Mencek: '$cleanEmail'", Toast.LENGTH_SHORT).show() 

                        db.collection("users")
                            .whereEqualTo("email", cleanEmail)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    // Email found, proceed to send link
                                    Firebase.auth.sendSignInLinkToEmail(cleanEmail, actionCodeSettings)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                saveEmail(context, cleanEmail)
                                                Toast.makeText(context, "Link login dikirim ke $cleanEmail", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Error mengirim link: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } else {
                                    // Email not found
                                    Toast.makeText(context, "Email '$cleanEmail' tidak ditemukan di database.", Toast.LENGTH_LONG).show()
                                    navController.navigate("register")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(context, "Gagal akses database: ${exception.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Mohon isi email terlebih dahulu", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Login with Email Link",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text(
                    text = "Don't have an account? Sign Up",
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
