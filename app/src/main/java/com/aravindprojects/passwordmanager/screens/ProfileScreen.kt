package com.aravindprojects.passwordmanager.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val username by remember { mutableStateOf(user?.displayName ?: "User") }
    val email by remember { mutableStateOf(user?.email ?: "No Email") }

    val primaryBlue = Color(0xFF37B5FF)
    val darkGray = Color(0xFF222222)
    val secondaryGray = Color(0xFF555555)
    val cardBackground = Color(0xFFFCFAFA)
    val errorRed = Color(0xFFD32F2F)
    val errorContainer = Color(0xFFFFCDD2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkGray
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = primaryBlue.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    tint = primaryBlue,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = {},
                label = { Text("Username", color = secondaryGray) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = secondaryGray,
                    disabledBorderColor = secondaryGray,
                    disabledTextColor = darkGray
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email", color = secondaryGray) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = secondaryGray,
                    disabledBorderColor = secondaryGray,
                    disabledTextColor = darkGray
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    auth.signOut()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = errorContainer,
                    contentColor = errorRed
                )
            ) {
                Text("Logout", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
