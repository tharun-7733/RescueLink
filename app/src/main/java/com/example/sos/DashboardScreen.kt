package com.example.sos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit, chatViewModel: ChatViewModel = viewModel()) {
    var isChatOpen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFF070A0F),
            bottomBar = { BottomNavigationBar() },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { isChatOpen = !isChatOpen },
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 50.dp)
                ) {
                    Icon(Icons.Default.Face, contentDescription = "Chat with Tars")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    HeaderSection(
                        onMenuClick = { showMenu = true }
                    )
                }
                item {
                    EmergencySOSCard()
                }
                item {
                    LocationStatusCard()
                }
                item {
                    SectionHeader(title = "How can we help you?", action = "View all")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ServiceCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Build,
                                title = "Nearby Mechanics",
                                description = "Find trusted mechanic shops near you",
                                buttonText = "Find Now",
                                iconColor = Color(0xFF2196F3),
                                borderColor = Color(0xFF152A42)
                            )
                            ServiceCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Refresh,
                                title = "Towing Services",
                                description = "Request towing assistance",
                                buttonText = "Request",
                                iconColor = Color(0xFF9C27B0),
                                borderColor = Color(0xFF2D1B3D)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ServiceCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Person,
                                title = "Community Help",
                                description = "Alert nearby users for help",
                                buttonText = "Alert Now",
                                iconColor = Color(0xFF4CAF50),
                                borderColor = Color(0xFF1B3D21)
                            )
                            ServiceCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Info,
                                title = "Roadside Safety",
                                description = "Safety tips & emergency guide",
                                buttonText = "Explore",
                                iconColor = Color(0xFFFF9800),
                                borderColor = Color(0xFF3D2E1B)
                            )
                        }
                    }
                }
                item {
                    SectionHeader(title = "Live Status")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusItem(Icons.Default.CheckCircle, "Network Status", "Strong", Color(0xFF4CAF50))
                        StatusItem(Icons.Default.Person, "Active Helpers", "24 nearby", Color(0xFF2196F3))
                        StatusItem(Icons.Default.Info, "Safety Mode", "Active", Color(0xFFF44336))
                    }
                }
                item {
                    SectionHeader(title = "Recent Activity", action = "View all")
                    RecentActivityList()
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Custom Drawer/Menu Implementation
        if (showMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showMenu = false }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .clickable(enabled = false) {},
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11141B)),
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "SOS Menu",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        MenuItem(Icons.Default.Person, "Profile")
                        MenuItem(Icons.Default.Settings, "Settings")
                        MenuItem(Icons.Default.Notifications, "Notifications")
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // LOGOUT OPTION
                        TextButton(
                            onClick = {
                                showMenu = false
                                onLogout()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isChatOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ChatFloatingWindow(viewModel = chatViewModel, onClose = { isChatOpen = false })
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun ChatFloatingWindow(viewModel: ChatViewModel, onClose: () -> Unit) {
    var chatText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.chatMessages.size) {
        if (viewModel.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatMessages.size - 1)
        }
    }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .padding(bottom = 140.dp)
            .width(320.dp)
            .height(450.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151821)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6200EE))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tars AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Online", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(viewModel.chatMessages) { message ->
                        ChatBubble(message.text, isUser = message.isUser)
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1C212B),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = chatText,
                        onValueChange = { chatText = it },
                        placeholder = { Text("Ask Tars...", fontSize = 14.sp, color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF11141B),
                            unfocusedContainerColor = Color(0xFF11141B),
                            disabledContainerColor = Color(0xFF11141B),
                            cursorColor = Color(0xFF6200EE),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        textStyle = TextStyle(fontSize = 14.sp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (chatText.isNotBlank()) {
                                viewModel.sendMessage(chatText)
                                chatText = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF6200EE), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (isUser) Color(0xFF6200EE) else Color(0xFF1C212B),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 0.dp,
                bottomEnd = if (isUser) 0.dp else 16.dp
            )
        ) {
            Text(
                text = text, color = Color.White, fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
fun HeaderSection(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "SOS Dashboard", color = Color.White, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stay Safe, We're Here to Help", color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun EmergencySOSCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth().height(160.dp).clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFB71C1C), Color(0xFF4A148C))))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Need Immediate Help?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap the SOS button to alert\nnearby helpers instantly.",
                    color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Help is one tap away", color = Color.White, fontSize = 11.sp)
                }
            }
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)))
                Box(modifier = Modifier.size(75.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)))
                Box(
                    modifier = Modifier
                        .size(60.dp).clip(CircleShape).background(Color.Red)
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SOS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun LocationStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1C212B))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp).clip(CircleShape).background(Color(0xFF1C212B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2196F3))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Your Location", color = Color.Gray, fontSize = 12.sp)
                Text("NH48, Manesar, Haryana", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Live accuracy: 15m", color = Color(0xFF4CAF50), fontSize = 11.sp)
                }
            }
            OutlinedButton(
                onClick = {},
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2196F3)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1C212B)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        if (action != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(action, color = Color(0xFF2196F3), fontSize = 14.sp)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ServiceCard(
    modifier: Modifier, icon: ImageVector, title: String, description: String,
    buttonText: String, iconColor: Color, borderColor: Color
) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                description, color = Color.Gray, fontSize = 10.sp, lineHeight = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp).weight(1f)
            )
            OutlinedButton(
                onClick = {}, modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text(buttonText, color = iconColor, fontSize = 11.sp)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(14.dp), tint = iconColor)
            }
        }
    }
}

@Composable
fun StatusItem(icon: ImageVector, title: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp).clip(CircleShape).background(valueColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = Color.Gray, fontSize = 10.sp)
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecentActivityList() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RecentActivityRow(Icons.Default.Build, "Mechanic Found", "Sharma Auto Care • 2.1 km away", "2 min ago", Color(0xFF4CAF50))
        RecentActivityRow(Icons.Default.Refresh, "Towing Requested", "Towing service on the way", "5 min ago", Color(0xFF2196F3))
        RecentActivityRow(Icons.Default.Notifications, "Community Alert Sent", "Alert sent to 18 nearby users", "8 min ago", Color(0xFF9C27B0))
    }
}

@Composable
fun RecentActivityRow(icon: ImageVector, title: String, subtitle: String, time: String, iconColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF11141B)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(time, color = Color(0xFF4CAF50), fontSize = 10.sp)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = Color(0xFF070A0F), tonalElevation = 8.dp, modifier = Modifier.height(70.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home", fontSize = 10.sp) },
            selected = true, onClick = {},
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2196F3), unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocationOn, null) },
            label = { Text("Map", fontSize = 10.sp) },
            selected = false, onClick = {},
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
        )
        Box(
            modifier = Modifier.padding(bottom = 12.dp).size(56.dp).clip(CircleShape)
                .background(Color.Red).border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Emergency", color = Color.White, fontSize = 7.sp)
            }
        }
        NavigationBarItem(
            icon = { Icon(Icons.Default.Menu, null) },
            label = { Text("Services", fontSize = 10.sp) },
            selected = false, onClick = {},
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Refresh, null) }, // Using Refresh as fallback
            label = { Text("History", fontSize = 10.sp) },
            selected = false, onClick = {},
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
        )
    }
}
