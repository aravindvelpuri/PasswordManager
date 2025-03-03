@file:Suppress("DEPRECATION")

package com.aravindprojects.passwordmanager.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.outlinedButtonBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aravindprojects.passwordmanager.model.PasswordEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    entry: PasswordEntry,
    onBack: () -> Unit,
    onDelete: (PasswordEntry) -> Unit,
    onUpdate: (PasswordEntry) -> Unit
) {
    val context = LocalContext.current
    var showPassword by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) { // ✅ Open Edit Dialog
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.elevatedCardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (entry.packageName.isNotEmpty()) {
                        DetailItem(Icons.Default.Apps, "App Name", entry.appName)
                        DetailItem(Icons.Default.Code, "Package", entry.packageName, selectable = true)
                    } else if (entry.webUrl.isNotEmpty()) {
                        DetailItem(Icons.Default.Language, "Website", entry.website)
                        DetailItem(Icons.Default.Link, "URL", entry.webUrl, selectable = true)
                    }
                    DetailItem(Icons.Default.Person, "Username", entry.username, selectable = true)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Password Icon",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Password", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGray)
                            Text(
                                if (showPassword) entry.password else "••••••••••",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
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

                        IconButton(onClick = {
                            coroutineScope.launch {
                                copyToClipboard(context, "Password", entry.password, snackbarHostState)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Password",
                                tint = PrimaryBlue
                            )
                        }
                    }
                }
            }

            ActionButton("Copy Username", Icons.Default.ContentCopy) {
                coroutineScope.launch {
                    copyToClipboard(context, "Username", entry.username, snackbarHostState)
                }
            }

            if (entry.packageName.isNotEmpty()) {
                OutlinedActionButton("Open App", Icons.AutoMirrored.Filled.Launch) {
                    openApp(context, entry.packageName)
                }
            } else if (entry.webUrl.isNotEmpty()) {
                OutlinedActionButton("Open Website", Icons.Default.OpenInBrowser) {
                    openUrl(context, entry.webUrl)
                }
            }
        }
    }

    // ✅ Show Edit Dialog
    if (showEditDialog) {
        EditPasswordDialog(
            entry = entry,
            onDismiss = { showEditDialog = false }, // ✅ Properly dismiss dialog
            onConfirm = { updatedEntry ->
                showEditDialog = false
                onUpdate(updatedEntry) // ✅ Call update function
            }
        )
    }

    // ✅ Show Delete Dialog
    if (showDeleteDialog) {
        CustomDeleteDialog(
            showDialog = showDeleteDialog,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete(entry)
            }
        )
    }
}

@Composable
fun EditPasswordDialog(
    entry: PasswordEntry,
    onDismiss: () -> Unit,
    onConfirm: (PasswordEntry) -> Unit
) {
    var updatedUsername by remember { mutableStateOf(entry.username) }
    var updatedPassword by remember { mutableStateOf(entry.password) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)), // Dimmed background
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)), // Light background
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Password",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = updatedUsername,
                    onValueChange = { updatedUsername = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = updatedPassword,
                    onValueChange = { updatedPassword = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { updatedPassword = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onDismiss, // ✅ Dismiss dialog
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontSize = 16.sp, color = Color.Black)
                    }
                    Button(
                        onClick = {
                            val updatedEntry = entry.copy(username = updatedUsername, password = updatedPassword)
                            onConfirm(updatedEntry) // ✅ Save the updated password
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDeleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)), // Dark transparent overlay
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)), // Glassmorphism effect
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 🔹 Warning Icon
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🔹 Title
                    Text(
                        text = "Delete Password?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 🔹 Subtitle
                    Text(
                        text = "Are you sure you want to delete this password? This action cannot be undone.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 🔹 Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            border = outlinedButtonBorder.copy(width = 1.dp)
                        ) {
                            Text("Cancel", fontSize = 16.sp, color = Color.Black)
                        }
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Delete", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String, selectable: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGray)
            if (selectable) {
                SelectionContainer {
                    Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PlaceholderGray)
                }
            } else {
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PlaceholderGray)
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
    ) {
        Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.padding(end = 8.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OutlinedActionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = outlinedButtonBorder.copy(width = 1.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
    ) {
        Icon(icon, contentDescription = text, tint = PrimaryBlue, modifier = Modifier.padding(end = 8.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

suspend fun copyToClipboard(context: Context, label: String, text: String, snackbarHostState: SnackbarHostState) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    snackbarHostState.showSnackbar("$label copied to clipboard!", duration = SnackbarDuration.Short)
}

fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

fun openApp(context: Context, packageName: String) {
    context.packageManager.getLaunchIntentForPackage(packageName)?.let {
        context.startActivity(it)
    } ?: Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
}