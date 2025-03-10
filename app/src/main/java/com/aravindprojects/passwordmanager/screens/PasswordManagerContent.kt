package com.aravindprojects.passwordmanager.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

// 🎨 Color Palette
val PrimaryBlue = Color(0xFF37B5FF)
val DarkGray = Color(0xFF222222)
val PlaceholderGray = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerContent(repository: PasswordRepository, onProfileClick: () -> Unit) {
    val passwordEntries by repository.passwordEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<PasswordEntry?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    // 🔄 Refresh Logic
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            repository.fetchPasswords()
            delay(1000)
            isRefreshing = false
        }
    }
    Crossfade(targetState = selectedEntry) { entry ->
                if (entry == null) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Password Manager",
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
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Password",
                                    tint = Color.White
                                )
                            }
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            SearchBar(searchQuery, onQueryChange = { searchQuery = it })

                            SwipeRefresh(
                                state = swipeRefreshState,
                                onRefresh = { isRefreshing = true },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val filteredEntries = passwordEntries.filter {
                                    it.website.contains(searchQuery, ignoreCase = true) ||
                                            it.username.contains(searchQuery, ignoreCase = true)
                                }

                                val categorizedEntries = filteredEntries
                                    .groupBy { it.category }
                                    .toSortedMap() // Sort categories alphabetically

                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (passwordEntries.isEmpty()) {
                                        EmptyPasswordListMessage()
                                    } else {
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            categorizedEntries.forEach { (category, entries) ->
                                                item {
                                                    CategoryHeader(category)
                                                }
                                                items(entries.sortedBy { it.website }) { entry ->
                                                    PasswordListItem(
                                                        entry,
                                                        onClick = { selectedEntry = entry })
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    PasswordDetailScreen(
                        entry = entry,
                        onBack = { selectedEntry = null },
                        onDelete = { entryToDelete ->
                            repository.deletePassword(entryToDelete)
                            repository.fetchPasswords() // 🔄 Fetch updated data after deletion
                            selectedEntry = null
                        },
                        onUpdate = { updatedEntry ->
                            repository.updatePassword(updatedEntry)
                            repository.fetchPasswords() // 🔄 Refresh list after update
                            selectedEntry = null // Close details screen
                        }
                    )
                }
            }


    if (showAddDialog) {
        AddPasswordDialog(
            showDialog = true,
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
}


@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Make it White for better contrast
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = PrimaryBlue,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search by website or username",
                        fontSize = 17.sp,
                        color = Color.Gray // Darker placeholder color
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 17.sp, color = DarkGray),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    cursorColor = PrimaryBlue,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )

            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = PrimaryBlue)
                }
            }
        }
    }
}

// 📜 Category Header
@Composable
fun CategoryHeader(category: String) {
    Text(
        text = category,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryBlue,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// 🔑 Password List Item
@Composable
fun PasswordListItem(entry: PasswordEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }, // ✅ Make the item clickable
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5),contentColor = DarkGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.packageName.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = "Password Icon",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(40.dp)
                )
            } else if (entry.webUrl.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Web,
                    contentDescription = "Password Icon",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(entry.website, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkGray)
                Text("Username: "+entry.username, fontSize = 16.sp, color = Color.DarkGray)
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to Details",
                tint = PlaceholderGray
            )
        }
    }
}

// 📜 Empty List Message
@Composable
fun EmptyPasswordListMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No passwords saved yet.", fontSize = 18.sp, color = DarkGray)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tap the + button to add one.", fontSize = 14.sp, color = PlaceholderGray)
    }
}
