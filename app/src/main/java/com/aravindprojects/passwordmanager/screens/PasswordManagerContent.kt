package com.aravindprojects.passwordmanager.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aravindprojects.passwordmanager.model.PasswordEntry
import com.aravindprojects.passwordmanager.repository.PasswordRepository
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay

// 🎨 Define Color Palette
val PrimaryBlue = Color(0xFF37B5FF)
val LightGray = Color(0xFFF0F4F8)
val DarkGray = Color(0xFF222222)
val PlaceholderGray = Color(0xFF757575)
val DeleteRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerContent(repository: PasswordRepository, onProfileClick: () -> Unit) {
    val passwordEntries by repository.passwordEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<PasswordEntry?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    // 🔄 Fetch passwords on refresh
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            repository.fetchPasswords()
            delay(1000) // Simulated refresh delay
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Password Manager",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue),
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Password", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 🔎 Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightGray),
                elevation = CardDefaults.elevatedCardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).fillMaxWidth().height(55.dp),
                        placeholder = { Text("Search by website or username", fontSize = 17.sp, color = PlaceholderGray) },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 17.sp, color = DarkGray),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            cursorColor = PrimaryBlue,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                        )
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = PlaceholderGray)
                        }
                    }
                }
            }

            // 🔄 Swipe to Refresh
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { isRefreshing = true },
                modifier = Modifier.fillMaxSize()
            ) {
                val filteredEntries = passwordEntries.filter {
                    it.website.contains(searchQuery, ignoreCase = true) ||
                            it.username.contains(searchQuery, ignoreCase = true)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (passwordEntries.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No passwords saved yet.", fontSize = 18.sp, color = DarkGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap the + button to add one.", fontSize = 14.sp, color = PlaceholderGray)
                        }
                    } else {
                        PasswordList(
                            passwordEntries = filteredEntries,
                            onDelete = { id -> showDeleteConfirmation = id },
                            onEdit = { entry -> showEditDialog = entry }
                        )
                    }
                }
            }
        }
    }

    // ➕ Add Password Dialog
    if (showAddDialog) { // ✅ Show only when needed
        AddPasswordDialog(
            showDialog = true,  // ✅ Explicitly set to true when dialog is shown
            onDismiss = { showAddDialog = false },
            onAdd = { category, website, webUrl, username, password, appName, packageName, websiteTitle ->
                repository.addPassword(
                    PasswordEntry(
                        id = System.currentTimeMillis().toString(),
                        category = category,
                        website = website,
                        webUrl = webUrl,
                        username = username,
                        password = password,
                        appName = appName,
                        packageName = packageName,
                        websiteTitle = websiteTitle
                    )
                )
                showAddDialog = false
            }
        )
    }

    // ✏ Edit Password Dialog
    if (showEditDialog != null) { // ✅ Show only when there's an entry to edit
        AddPasswordDialog(
            showDialog = true, // ✅ Explicitly set to true
            onDismiss = { showEditDialog = null },
            onAdd = { category, website, webUrl, username, password, appName, packageName, websiteTitle ->
                repository.updatePassword(
                    showEditDialog!!.copy(
                        category = category,
                        website = website,
                        webUrl = webUrl,
                        username = username,
                        password = password,
                        appName = appName,
                        packageName = packageName,
                        websiteTitle = websiteTitle
                    )
                )
                showEditDialog = null
            },
            initialCategory = showEditDialog?.category ?: "App",
            initialWebsite = showEditDialog?.website ?: "",
            initialWebUrl = showEditDialog?.webUrl ?: "",
            initialUsername = showEditDialog?.username ?: "",
            initialPassword = showEditDialog?.password ?: "",
            initialAppName = showEditDialog?.appName ?: "",
            initialPackageName = showEditDialog?.packageName ?: ""
        )
    }

// ❌ Delete Confirmation Dialog
    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Delete Password", color = DarkGray, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this password?", color = PlaceholderGray, fontSize = 16.sp) },
            confirmButton = {
                Button(onClick = {
                    repository.deletePassword(showDeleteConfirmation!!)
                    showDeleteConfirmation = null
                }, colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel", color = PrimaryBlue)
                }
            },
            containerColor = LightGray
        )
    }
}
