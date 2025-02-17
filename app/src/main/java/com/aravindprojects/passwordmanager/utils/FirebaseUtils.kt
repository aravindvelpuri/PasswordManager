package com.aravindprojects.passwordmanager.utils

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseUtils {
    fun initialize(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }
        FirebaseAuth.getInstance()  // Ensure Firebase Auth is initialized
    }
}