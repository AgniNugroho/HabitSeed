
package com.bismilahexpo.habitseed.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun HomePage(navController: NavController) {
    val user = Firebase.auth.currentUser

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Selamat Datang!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Email: ${user?.email ?: "Tidak diketahui"}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            Firebase.auth.signOut()
            navController.navigate("login") {
                // Hapus semua backstack agar pengguna tidak bisa kembali ke home setelah logout
                popUpTo(0)
            }
        }) {
            Text("Logout")
        }
    }
}
