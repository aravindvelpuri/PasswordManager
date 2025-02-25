package com.aravindprojects.passwordmanager.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun PasswordList(passwordEntries: List<PasswordEntry>, onItemClick: (PasswordEntry) -> Unit) {
    LazyColumn {
        items(passwordEntries) { entry ->
            PasswordItem(entry, onItemClick)
        }
    }
}

@Composable
fun PasswordItem(entry: PasswordEntry, onItemClick: (PasswordEntry) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onItemClick(entry) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 📌 Placeholder Icon (You can replace with a real favicon)
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "App Icon",
                tint = PrimaryBlue,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = entry.websiteTitle.ifEmpty { entry.website },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
                Text(
                    text = entry.username,
                    fontSize = 14.sp,
                    color = PlaceholderGray
                )
            }
        }
    }
}
