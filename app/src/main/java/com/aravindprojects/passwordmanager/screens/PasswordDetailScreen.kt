package com.aravindprojects.passwordmanager.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aravindprojects.passwordmanager.model.PasswordEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(entry: PasswordEntry, onBack: () -> Unit) {
    val context = LocalContext.current
    var showPassword by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 📌 Display Details
            DetailItem(label = "Website", value = entry.website)
            DetailItem(label = "URL", value = entry.webUrl, selectable = true)
            DetailItem(label = "Username", value = entry.username, selectable = true)

            // 🔐 Password with visibility toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Password", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGray)
                    Text(
                        if (showPassword) entry.password else "••••••••••",
                        fontSize = 14.sp,
                        color = PlaceholderGray
                    )
                }
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password",
                        tint = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✂️ Copy Username
            ActionButton(text = "Copy Username", icon = Icons.Default.Person, onClick = {
                copyToClipboard(context, "Username", entry.username)
            })

            Spacer(modifier = Modifier.height(8.dp))

            // 🔑 Copy Password
            ActionButton(text = "Copy Password", icon = Icons.Default.Lock, onClick = {
                copyToClipboard(context, "Password", entry.password)
            })

            Spacer(modifier = Modifier.height(8.dp))

            // 🌐 Open Website/App
            if (entry.webUrl.isNotEmpty()) {
                ActionButton(text = "Open Website", icon = Icons.Default.OpenInBrowser, onClick = {
                    openUrl(context, entry.webUrl)
                })
            } else if (entry.packageName.isNotEmpty()) {
                ActionButton(text = "Open App", icon = Icons.AutoMirrored.Filled.Launch, onClick = {
                    openApp(context, entry.packageName)
                })
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, selectable: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGray)
        if (selectable) {
            SelectionContainer {
                Text(value, fontSize = 14.sp, color = PlaceholderGray)
            }
        } else {
            Text(value, fontSize = 14.sp, color = PlaceholderGray)
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
    ) {
        Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.padding(end = 8.dp))
        Text(text, color = Color.White)
    }
}

// 📋 Copy to Clipboard Function
fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}

// 🌐 Open URL in Browser
fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

// 📲 Open App by Package Name
fun openApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
    }
}
