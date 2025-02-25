package com.aravindprojects.passwordmanager.repository

import android.util.Log
import com.aravindprojects.passwordmanager.model.PasswordEntry
import com.aravindprojects.passwordmanager.utils.CryptoUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PasswordRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference.child("passwords")

    private val _passwordEntries = MutableStateFlow<List<PasswordEntry>>(emptyList())
    val passwordEntries: StateFlow<List<PasswordEntry>> get() = _passwordEntries

    init {
        auth.addAuthStateListener { fetchPasswords() } // Auto refresh on login
    }

    fun fetchPasswords() {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = mutableListOf<PasswordEntry>()
                for (child in snapshot.children) {
                    val entry = child.getValue(PasswordEntry::class.java)
                    if (entry != null) {
                        // Decrypt the sensitive fields
                        val decryptedEntry = entry.copy(
                            username = CryptoUtils.decrypt(entry.username),
                            password = CryptoUtils.decrypt(entry.password),
                            webUrl = CryptoUtils.decrypt(entry.webUrl),
                            appName = CryptoUtils.decrypt(entry.appName),
                            packageName = CryptoUtils.decrypt(entry.packageName),
                            websiteTitle = CryptoUtils.decrypt(entry.websiteTitle) // Decrypt websiteTitle
                        )
                        entries.add(decryptedEntry)
                    }
                }
                _passwordEntries.value = entries
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PasswordRepository", "Database error: ${error.message}")
            }
        })
    }

    fun addPassword(entry: PasswordEntry) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Encrypt the sensitive fields before saving to the database
            val encryptedEntry = entry.copy(
                username = CryptoUtils.encrypt(entry.username),
                password = CryptoUtils.encrypt(entry.password),
                webUrl = CryptoUtils.encrypt(entry.webUrl),
                appName = CryptoUtils.encrypt(entry.appName),
                packageName = CryptoUtils.encrypt(entry.packageName),
                websiteTitle = CryptoUtils.encrypt(entry.websiteTitle) // Encrypt websiteTitle
            )
            database.child(userId).child(entry.id).setValue(encryptedEntry)
                .addOnSuccessListener {
                    Log.d("PasswordRepository", "Password added successfully: ${entry.website}")
                }
                .addOnFailureListener { e ->
                    Log.e("PasswordRepository", "Failed to add password: ${e.message}")
                }
        }
    }

    fun deletePassword(entry: PasswordEntry) {  // ✅ Expects PasswordEntry
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child(userId).child(entry.id).removeValue()
                .addOnSuccessListener {
                    Log.d("PasswordRepository", "Password deleted successfully: ${entry.website}")
                }
                .addOnFailureListener { e ->
                    Log.e("PasswordRepository", "Failed to delete password: ${e.message}")
                }
        }
    }

    fun updatePassword(updatedEntry: PasswordEntry) {
        val userId = auth.currentUser?.uid ?: return
        // Encrypt the sensitive fields before updating the database
        val encryptedEntry = updatedEntry.copy(
            username = CryptoUtils.encrypt(updatedEntry.username),
            password = CryptoUtils.encrypt(updatedEntry.password),
            webUrl = CryptoUtils.encrypt(updatedEntry.webUrl),
            appName = CryptoUtils.encrypt(updatedEntry.appName),
            packageName = CryptoUtils.encrypt(updatedEntry.packageName)
        )
        database.child(userId).child(updatedEntry.id).setValue(encryptedEntry)
            .addOnSuccessListener {
                Log.d("PasswordRepository", "Password updated successfully: ${updatedEntry.website}")
            }
            .addOnFailureListener { e ->
                Log.e("PasswordRepository", "Failed to update password: ${e.message}")
            }
    }
}