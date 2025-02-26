package com.aravindprojects.passwordmanager

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import com.aravindprojects.passwordmanager.screens.SplashScreen
import com.aravindprojects.passwordmanager.screens.auth.LoginScreen
import com.aravindprojects.passwordmanager.screens.PasswordManagerApp
import com.aravindprojects.passwordmanager.repository.PasswordRepository
import com.aravindprojects.passwordmanager.ui.theme.PasswordManagerTheme
import com.aravindprojects.passwordmanager.utils.BiometricRetryBottomSheet
import com.aravindprojects.passwordmanager.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseUtils.initialize(this)

        setContent {
            PasswordManagerTheme {
                val repository = PasswordRepository()
                var showSplash by remember { mutableStateOf(true) }
                var showBottomSheet by remember { mutableStateOf(false) }
                var isAuthenticated by remember { mutableStateOf(false) }
                var isUserLoggedIn by remember { mutableStateOf(false) }
                var biometricTriggered by remember { mutableStateOf(false) }
                var isNewLogin by remember { mutableStateOf(false) } // ✅ Track new logins

                val auth = FirebaseAuth.getInstance()

                LaunchedEffect(auth.currentUser) {
                    isUserLoggedIn = auth.currentUser != null

                    if (isUserLoggedIn) {
                        if (!isAuthenticated && !biometricTriggered && !isNewLogin) {
                            biometricTriggered = true
                            authenticateWithBiometrics(
                                activity = this@MainActivity,
                                onSuccess = {
                                    isAuthenticated = true
                                    showSplash = false
                                },
                                onFailure = {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        showBottomSheet = true
                                    }, 2000)
                                }
                            )
                        } else {
                            showSplash = false
                        }
                    } else {
                        showSplash = false
                    }
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    when {
                        !isUserLoggedIn -> {
                            LoginScreen(
                                onLoginSuccess = {
                                    isUserLoggedIn = true
                                    isAuthenticated = true  // ✅ Directly authenticate new login
                                    biometricTriggered = false
                                    isNewLogin = true // ✅ Set flag for new login
                                },
                                repository = repository
                            )
                        }

                        !isAuthenticated -> {
                            BiometricRetryBottomSheet(
                                onRetry = {
                                    showBottomSheet = false
                                    authenticateWithBiometrics(
                                        activity = this@MainActivity,
                                        onSuccess = {
                                            isAuthenticated = true
                                        },
                                        onFailure = {
                                            showBottomSheet = true
                                        }
                                    )
                                },
                                onExit = { finish() }
                            )
                        }

                        else -> {
                            PasswordManagerApp(repository = repository)
                        }
                    }
                }
            }
        }
    }

    private fun authenticateWithBiometrics(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor: Executor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activity.mainExecutor
            } else {
                ContextCompat.getMainExecutor(activity)
            }

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)

                        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            onFailure() // ❌ Show bottom sheet when user cancels
                            return
                        }

                        onFailure()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onFailure()
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Authenticate to access your passwords")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            // ✅ If biometrics are NOT supported, proceed to main app
            onSuccess()
        }
    }
}
