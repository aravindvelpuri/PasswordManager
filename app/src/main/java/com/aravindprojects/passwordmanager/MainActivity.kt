package com.aravindprojects.passwordmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import com.aravindprojects.passwordmanager.screens.SplashScreen
import com.aravindprojects.passwordmanager.screens.PasswordManagerApp
import com.aravindprojects.passwordmanager.repository.PasswordRepository
import com.aravindprojects.passwordmanager.ui.theme.PasswordManagerTheme
import com.aravindprojects.passwordmanager.utils.FirebaseUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseUtils.initialize(this)

        setContent {
            PasswordManagerTheme {  // ✅ Wrap the app in PasswordManagerTheme
                val repository = PasswordRepository()
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    PasswordManagerApp(repository = repository)
                }
            }
        }
    }
}
