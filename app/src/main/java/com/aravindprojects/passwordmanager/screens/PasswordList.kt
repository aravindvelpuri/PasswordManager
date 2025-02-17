package com.aravindprojects.passwordmanager.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aravindprojects.passwordmanager.model.PasswordEntry

@Composable
fun PasswordList(
    passwordEntries: List<PasswordEntry>,
    onDelete: (String) -> Unit,
    onEdit: (PasswordEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(passwordEntries) { entry ->
            PasswordItem(entry, onDelete, onEdit)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PasswordItem(
    entry: PasswordEntry,
    onDelete: (String) -> Unit,
    onEdit: (PasswordEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPassword by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.website,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37B5FF)
                )
                IconButton(onClick = { onEdit(entry) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF37B5FF))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = "User", tint = Color(0xFF37B5FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF555555),
                    modifier = Modifier.weight(1f)  // This will push the copy icon to the right
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(entry.username))
                        Toast.makeText(context, "Username Copied!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Username", tint = Color(0xFF37B5FF))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color(0xFF37B5FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showPassword) entry.password else "••••••••",
                    fontSize = 16.sp,
                    color = Color(0xFF555555).copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Password Visibility",
                            tint = Color(0xFF37B5FF)
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(entry.password))
                            Toast.makeText(context, "Password Copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = Color(0xFF37B5FF))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF37B5FF).copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (entry.category == "App" && entry.packageName.isNotBlank()) {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(entry.packageName)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            } else {
                                Toast.makeText(context, "App not found!", Toast.LENGTH_SHORT).show()
                            }
                        } else if (entry.category == "Website" && entry.webUrl.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(entry.webUrl))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "No valid app or website found!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37B5FF), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = "Open", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (entry.category == "App") "Open App" else "Open Website")
                }
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onDelete(entry.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE5E5), contentColor = Color(0xFFFF4C4C)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        }
    }
}
