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
fun RegisterPage(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    val url = "https://agninugroho.github.io/finishLogin"
    val actionCodeSettings = ActionCodeSettings.newBuilder()
        .setUrl(url)
        .setHandleCodeInApp(true)
        .setAndroidPackageName(
            "com.bismilahexpo.habitseed",
            true,
            null
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
            // Header
            Text(
                text = "Join HabitSeed",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start your journey today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Form
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
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

            Spacer(modifier = Modifier.height(16.dp))

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
                    val cleanEmail = email.trim()
                    val cleanUsername = username.trim()

                    if (cleanEmail.isNotBlank() && cleanUsername.isNotBlank()) {
                        val db = Firebase.firestore
                        val auth = Firebase.auth

                        // 1. Check if email exists
                        db.collection("users")
                            .whereEqualTo("email", cleanEmail)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    Toast.makeText(context, "Email sudah terdaftar. Silakan login.", Toast.LENGTH_LONG).show()
                                } else {
                                    // 2. Create new user
                                    val newUser = hashMapOf(
                                        "username" to cleanUsername,
                                        "email" to cleanEmail,
                                        "createdAt" to System.currentTimeMillis()
                                    )

                                    db.collection("users")
                                        .add(newUser)
                                        .addOnSuccessListener { docRef ->
                                            // 3. Send Verification Link
                                            auth.sendSignInLinkToEmail(cleanEmail, actionCodeSettings)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        saveEmail(context, cleanEmail)
                                                        Toast.makeText(context, "SUCCESS! Cek email untuk login.", Toast.LENGTH_LONG).show()
                                                        navController.navigate("login")
                                                    } else {
                                                        Toast.makeText(context, "Gagal kirim link: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error database: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
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
                    text = "Create Account",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("login") }) {
                Text(
                    text = "Already have an account? Login",
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
