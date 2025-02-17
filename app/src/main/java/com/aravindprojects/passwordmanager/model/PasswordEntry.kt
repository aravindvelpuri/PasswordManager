package com.aravindprojects.passwordmanager.model

data class PasswordEntry(
    val id: String = "",
    val appName: String = "",
    val category: String = "",
    val packageName: String = "",
    val password: String = "",
    val username: String = "",
    val webUrl: String = "",
    val website: String = "",
    val websiteTitle: String = "" // Add this field
)
