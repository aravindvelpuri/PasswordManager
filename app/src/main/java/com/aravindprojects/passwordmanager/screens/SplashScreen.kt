package com.aravindprojects.passwordmanager.screens

import android.content.pm.PackageManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aravindprojects.passwordmanager.R

@Composable
fun SplashScreen() {
    // Get app version name dynamically
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "1.0" // Default fallback version
    }

    // UI Colors
    val backgroundColor = Color.White
    val textColor = Color(0xFF0288D1)
    val iconBackground = Color.White

    // Animation for Loading Indicator
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(.25f)) // Adjusted slightly for better balance

            // App Logo
            Box(
                modifier = Modifier
                    .size(120.dp) // Increased size for better visibility
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.play_store_512),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(120.dp) // Adjusted to fit in the Box
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Password Manager",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Securely store and manage your passwords",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading Indicator with Animation
            CircularProgressIndicator(
                color = textColor,
                strokeWidth = 3.dp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.fillMaxHeight(.5f)) // Reduced from .6f for better layout balance

            // Version Name Display
            Text(
                text = "Version $versionName",
                fontSize = 16.sp, // Reduced slightly for better UI consistency
                fontWeight = FontWeight.Medium, // Looks more natural with other text
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
