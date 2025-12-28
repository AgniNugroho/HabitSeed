package com.bismilahexpo.habitseed.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismilahexpo.habitseed.ui.components.ProfileMenuCard
import com.bismilahexpo.habitseed.ui.theme.SeedGreen

@Composable
fun ProfilePage(
    userName: String,
    userEmail: String,
    userAvatarUrl: String?,
    onEditProfile: () -> Unit,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SeedGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (userAvatarUrl != null) {
                AsyncImage(
                    model = userAvatarUrl,
                    contentDescription = "Foto Profil",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = SeedGreen
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Username and Edit Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentSize()
        ) {
            // Invisible spacer to balance the IconButton on the right
            Spacer(modifier = Modifier.size(48.dp)) 
            
            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            IconButton(
                onClick = onEditProfile,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = SeedGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Text(
            text = userEmail,
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Gallery Card
        ProfileMenuCard(
            icon = Icons.Default.Collections,
            title = "Galeri Habit",
            onClick = onGalleryClick
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Settings Card
        ProfileMenuCard(
            icon = Icons.Default.Settings,
            title = "Pengaturan",
            onClick = onSettingsClick
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout Card
        OutlinedCard(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Red),
            colors = CardDefaults.outlinedCardColors(
                containerColor = Color.Transparent,
                contentColor = Color.Red
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Keluar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Space for BottomBar
    }
}
