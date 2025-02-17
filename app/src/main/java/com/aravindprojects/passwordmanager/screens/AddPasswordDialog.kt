package com.aravindprojects.passwordmanager.screens

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

val customPrimaryColor = Color(0xFF37B5FF)  // Main color
val customTextColor = Color(0xFF333333)  // Text color for consistency
val customCardColor = Color(0xFFFFFFFF)  // Card background color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String, String, String) -> Unit,
    initialCategory: String = "Website",
    initialWebsite: String = "",
    initialWebUrl: String = "",
    initialUsername: String = "",
    initialPassword: String = "",
    initialAppName: String = "",
    initialPackageName: String = ""
) {
    if (!showDialog) return

    val context = LocalContext.current

    var category by remember { mutableStateOf(initialCategory) }
    var webUrl by remember { mutableStateOf(initialWebUrl) }
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf(initialPassword) }
    var appName by remember { mutableStateOf(initialAppName) }
    var packageName by remember { mutableStateOf(initialPackageName) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var showAppList by remember { mutableStateOf(false) }
    var websiteTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val isAddEnabled = when (category) {
        "App" -> appName.isNotBlank() && username.isNotBlank() && password.isNotBlank()
        "Website" -> webUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()
        else -> false
    }

    var installedApps by remember { mutableStateOf(emptyList<Pair<String, String>>()) }

    LaunchedEffect(Unit) {
        installedApps = getInstalledApps(context)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = customCardColor)
        ) {
            Column(modifier = Modifier.padding(vertical = 25.dp, horizontal = 16.dp)) {
                Text(
                    text = if (initialWebsite.isBlank()) "Add New Password" else "Edit Password",
                    color = customTextColor,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = isCategoryExpanded,
                    onExpandedChange = { isCategoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category", color = customTextColor) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { isCategoryExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customPrimaryColor,
                            unfocusedBorderColor = customPrimaryColor,
                            focusedLabelColor = customPrimaryColor,
                            unfocusedLabelColor = customTextColor
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryExpanded,
                        modifier = Modifier.background(Color.White).border(1.dp, customPrimaryColor),
                        onDismissRequest = { isCategoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("App", color = customTextColor) },
                            onClick = {
                                category = "App"
                                isCategoryExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Website", color = customTextColor) },
                            onClick = {
                                category = "Website"
                                isCategoryExpanded = false
                            }
                        )
                    }
                }

                if (category == "App") {
                    ExposedDropdownMenuBox(
                        expanded = showAppList,
                        modifier = Modifier.background(Color.White),
                        onExpandedChange = { showAppList = it }
                    ) {
                        OutlinedTextField(
                            value = appName,
                            onValueChange = { appName = it },
                            label = { Text("Select Installed App", color = customTextColor) },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAppList) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .clickable { showAppList = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = customPrimaryColor,
                                unfocusedBorderColor = customPrimaryColor,
                                focusedLabelColor = customPrimaryColor,
                                unfocusedLabelColor = customTextColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showAppList,
                            modifier = Modifier.background(Color.White).border(1.dp, customPrimaryColor),
                            onDismissRequest = { showAppList = false }
                        ) {
                            installedApps.forEach { (name, packageId) ->
                                DropdownMenuItem(
                                    text = { Text(name, color = customTextColor) },
                                    onClick = {
                                        appName = name
                                        packageName = packageId
                                        showAppList = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (category == "Website") {
                    OutlinedTextField(
                        value = webUrl,
                        onValueChange = { newValue ->
                            webUrl = newValue
                            if (newValue.isNotBlank()) {
                                val formattedUrl = formatUrl(newValue)
                                fetchWebsiteTitle(formattedUrl) { title, error ->
                                    websiteTitle = title
                                    errorMessage = error
                                }
                            }
                        },
                        label = { Text("Website URL", color = customTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customPrimaryColor,
                            unfocusedBorderColor = customPrimaryColor,
                            focusedLabelColor = customPrimaryColor,
                            unfocusedLabelColor = customTextColor
                        )
                    )

                    OutlinedTextField(
                        value = websiteTitle.ifEmpty { errorMessage },
                        onValueChange = {},
                        label = { Text("Website Title", color = customTextColor) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customPrimaryColor,
                            unfocusedBorderColor = customPrimaryColor,
                            focusedLabelColor = customPrimaryColor,
                            unfocusedLabelColor = customTextColor
                        )
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username or Email", color = customTextColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customPrimaryColor,
                        unfocusedBorderColor = customPrimaryColor,
                        focusedLabelColor = customPrimaryColor,
                        unfocusedLabelColor = customTextColor
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = customTextColor) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customPrimaryColor,
                        unfocusedBorderColor = customPrimaryColor,
                        focusedLabelColor = customPrimaryColor,
                        unfocusedLabelColor = customTextColor
                    )
                )

                Row(modifier = Modifier.fillMaxWidth().padding(top = 25.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = customTextColor)
                    }

                    Button(
                        onClick = {
                            val formattedUrl = formatUrl(webUrl)
                            onAdd(
                                category,
                                if (category == "App") appName else websiteTitle,
                                formattedUrl,
                                username,
                                password,
                                appName,
                                packageName,
                                websiteTitle
                            )
                        },
                        enabled = isAddEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = customPrimaryColor)
                    ) {
                        Text(if (initialWebsite.isBlank()) "Add" else "Save", color = Color.White)
                    }
                }
            }
        }
    }
}

@SuppressLint("QueryPermissionsNeeded")
fun getInstalledApps(context: android.content.Context): List<Pair<String, String>> {
    val pm = context.packageManager
    return pm.getInstalledPackages(PackageManager.GET_META_DATA)
        .mapNotNull { packageInfo ->
            try {
                val appInfo = pm.getApplicationInfo(packageInfo.packageName, 0)
                if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                    pm.getApplicationLabel(appInfo).toString() to appInfo.packageName
                } else null
            } catch (e: Exception) {
                null
            }
        }
        .sortedBy { it.first }
}

fun formatUrl(input: String): String {
    return when {
        input.startsWith("http://") || input.startsWith("https://") -> input
        Patterns.WEB_URL.matcher(input).matches() -> "https://$input"
        else -> "https://$input"
    }
}

fun fetchWebsiteTitle(url: String, onTitleFetched: (String, String) -> Unit) {
    if (!Patterns.WEB_URL.matcher(url).matches()) {
        onTitleFetched("", "Invalid URL format")
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val doc = Jsoup.connect(url)
                .timeout(10000)
                .get()
            val title = doc.title()
            withContext(Dispatchers.Main) {
                onTitleFetched(title, "")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onTitleFetched("", "Failed to fetch title")
            }
        }
    }
}