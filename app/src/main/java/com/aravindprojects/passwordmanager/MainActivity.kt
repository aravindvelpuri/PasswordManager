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
import com.aravindprojects.passwordmanager.screens.PasswordManagerApp
import com.aravindprojects.passwordmanager.repository.PasswordRepository
import com.aravindprojects.passwordmanager.ui.theme.PasswordManagerTheme
import com.aravindprojects.passwordmanager.utils.BiometricRetryBottomSheet
import com.aravindprojects.passwordmanager.utils.FirebaseUtils
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

                LaunchedEffect(Unit) {
                    authenticateWithBiometrics(
                        activity = this@MainActivity,
                        onSuccess = {
                            showSplash = false
                        },
                        onFailure = {
                            Handler(Looper.getMainLooper()).postDelayed({
                                showBottomSheet = true // Show bottom sheet when user cancels
                            }, 2000)
                        }
                    )
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    PasswordManagerApp(repository = repository)
                }

                if (showBottomSheet) {
                    BiometricRetryBottomSheet(
                        onRetry = {
                            showBottomSheet = false
                            authenticateWithBiometrics(
                                activity = this@MainActivity,
                                onSuccess = { showSplash = false },
                                onFailure = { showBottomSheet = true }
                            )
                        },
                        onExit = { finish() }
                    )
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
                            onFailure() // ❌ Show Bottom Sheet when user cancels
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
            // ✅ If biometrics are NOT supported, skip authentication and proceed
            onSuccess()
        }
    }
}
