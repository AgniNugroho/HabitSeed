
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
import com.bismilahexpo.habitseed.ui.theme.*
import androidx.compose.ui.graphics.Color

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
                text = "HabitSeed", 
                style = MaterialTheme.typography.displayMedium,
                color = SeedGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Plant your habits, grow your life.",
                style = MaterialTheme.typography.bodyMedium,
                color = LightSecondaryContent
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Welcome Back", 
                style = MaterialTheme.typography.headlineSmall,
                color = LightPrimaryContent
            )
            Spacer(modifier = Modifier.height(24.dp))

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
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val cleanEmail = email.trim() 
                    if (cleanEmail.isNotBlank()) {
                        val db = com.google.firebase.Firebase.firestore
                        
                        db.collection("users")
                            .whereEqualTo("email", cleanEmail)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
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
                    containerColor = SeedGreen,
                    contentColor = Color.White
                )
                ) {
                Text(
                    text = "Login dengan Email Link",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(com.bismilahexpo.habitseed.R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)

            val googleLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                        val idToken = account.idToken
                        val email = account.email

                        if (idToken != null && email != null) {
                            val db = Firebase.firestore
                            db.collection("users")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (!documents.isEmpty) {
                                        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                                        Firebase.auth.signInWithCredential(credential)
                                            .addOnCompleteListener { authTask ->
                                                if (authTask.isSuccessful) {
                                                     val user = Firebase.auth.currentUser
                                                     saveEmail(context, email)
                                                     
                                                     if (user != null) {
                                                         db.collection("users").document(user.uid)
                                                             .update("lastLogin", System.currentTimeMillis())
                                                     }
                                                     
                                                     Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                                     navController.navigate("home") { popUpTo(0) }
                                                } else {
                                                    Toast.makeText(context, "Login Gagal: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(context, "Email belum terdaftar. Silakan daftar terlebih dahulu.", Toast.LENGTH_LONG).show()
                                        googleSignInClient.signOut()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Gagal mengecek email: ${e.message}", Toast.LENGTH_LONG).show()
                                    googleSignInClient.signOut()
                                }
                        } else {
                            Toast.makeText(context, "Gagal Login: ID Token tidak ditemukan. Cek konfigurasi google-services.json.", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: com.google.android.gms.common.api.ApiException) {
                        Toast.makeText(context, "Google Sign In Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            // OutlinedButton(
            //     onClick = {
            //         googleSignInClient.signOut() // Ensure fresh login
            //         googleLauncher.launch(googleSignInClient.signInIntent)
            //     },
            //     modifier = Modifier.fillMaxWidth().height(50.dp),
            //     shape = MaterialTheme.shapes.medium,
            //      colors = ButtonDefaults.outlinedButtonColors(
            //         contentColor = SeedGreen
            //     ),
            //     border = androidx.compose.foundation.BorderStroke(1.dp, SeedGreen)
            // ) {
            //      Row(verticalAlignment = Alignment.CenterVertically) {
            //         // Placeholder for G icon if needed, or just text
            //         Text("Login dengan Google", style = MaterialTheme.typography.titleMedium)
            //     }
            // }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text(
                    text = "Belum punya akun? Daftar",
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
