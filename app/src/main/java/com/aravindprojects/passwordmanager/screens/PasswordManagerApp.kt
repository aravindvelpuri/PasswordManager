package com.aravindprojects.passwordmanager.screens

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aravindprojects.passwordmanager.repository.PasswordRepository
import com.aravindprojects.passwordmanager.screens.auth.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun PasswordManagerApp(repository: PasswordRepository) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    // Listen for authentication state changes
    LaunchedEffect(auth) {
        auth.addAuthStateListener { firebaseAuth ->
            isLoggedIn = firebaseAuth.currentUser != null
        }
    }

    // Show SplashScreen for 2 seconds, then navigate to the appropriate screen
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds delay
        navController.navigate(if (isLoggedIn) "home" else "login") {
            popUpTo("splash") { inclusive = true } // Remove SplashScreen from back stack
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash" // Start with SplashScreen
    ) {
        composable("splash") {
            SplashScreen()
        }
        composable("home") {
            PasswordManagerContent(
                repository = repository,
                onProfileClick = { navController.navigate("profile") }
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    auth.signOut()
                    navController.popBackStack("home", inclusive = true)
                    navController.navigate("login") // Explicitly navigate to login screen
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                repository = repository  // Pass repository here
            )
        }
    }
}