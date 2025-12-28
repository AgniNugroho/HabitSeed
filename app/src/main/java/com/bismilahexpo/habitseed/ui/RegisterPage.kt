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
import com.bismilahexpo.habitseed.data.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import com.bismilahexpo.habitseed.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun RegisterPage(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Join HabitSeed",
                style = MaterialTheme.typography.displaySmall,
                color = SeedGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start your journey today.",
                style = MaterialTheme.typography.bodyMedium,
                color = LightSecondaryContent
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SeedGreen,
                    unfocusedBorderColor = LightSecondaryContent,
                    focusedLabelColor = SeedGreen,
                    unfocusedLabelColor = LightSecondaryContent,
                    cursorColor = SeedGreen,
                    focusedTextColor = LightPrimaryContent,
                    unfocusedTextColor = LightPrimaryContent
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
                    focusedBorderColor = SeedGreen,
                    unfocusedBorderColor = LightSecondaryContent,
                    focusedLabelColor = SeedGreen,
                    unfocusedLabelColor = LightSecondaryContent,
                    cursorColor = SeedGreen,
                    focusedTextColor = LightPrimaryContent,
                    unfocusedTextColor = LightPrimaryContent
                ),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SeedGreen,
                    unfocusedBorderColor = LightSecondaryContent,
                    focusedLabelColor = SeedGreen,
                    unfocusedLabelColor = LightSecondaryContent,
                    cursorColor = SeedGreen,
                    focusedTextColor = LightPrimaryContent,
                    unfocusedTextColor = LightPrimaryContent
                ),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val cleanEmail = email.trim()
                    val cleanUsername = username.trim()
                    val cleanPassword = password
                    
                    if (cleanEmail.isNotBlank() && cleanUsername.isNotBlank() && cleanPassword.isNotBlank()) {
                        scope.launch {
                             isLoading = true
                             try {
                                 Supabase.client.auth.signUpWith(Email) {
                                     this.email = cleanEmail
                                     this.password = cleanPassword
                                 }
                                 
                                 val currentUser = Supabase.client.auth.currentUserOrNull()
                                 if (currentUser != null) {
                                     val userData = mapOf(
                                         "id" to currentUser.id,
                                         "email" to cleanEmail,
                                         "username" to cleanUsername,
                                         "last_login" to java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
                                     )
                                     
                                     Supabase.client.from("users").insert(userData)
                                     
                                     saveEmail(context, cleanEmail)
                                     Toast.makeText(context, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                     navController.navigate("home") { popUpTo(0) }
                                 } else {
                                     Toast.makeText(context, "Cek email untuk konfirmasi!", Toast.LENGTH_LONG).show()
                                     navController.navigate("login")
                                 }
                             } catch (e: Exception) {
                                 Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                             } finally {
                                 isLoading = false
                             }
                        }
                    } else {
                        Toast.makeText(context, "Mohon lengkapi data", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SeedGreen,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Buat Akun",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("login") }) {
                Text(
                    text = "Sudah punya akun? Login",
                    color = SeedGreen
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
