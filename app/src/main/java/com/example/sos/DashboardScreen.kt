package com.example.sos

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.sos.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════
//  DESIGN TOKENS (Shared with AuthDesignTokens)
// ═══════════════════════════════════════════════════════════════════════════
object DashboardTokens {
    val BlackCore = Color(0xFF080808)
    val CardBg = Color(0xFF101010)
    val CardBg2 = Color(0xFF141414)
    val Rim = Color(0xFF1E1E1E)
    val Rim2 = Color(0xFF2A2A2A)
    val WhitePure = Color(0xFFFFFFFF)
    val White60 = Color(0x99FFFFFF)
    val White35 = Color(0x59FFFFFF)
    
    val RedHot = AuthDesignTokens.RedHot // #E8001D
    val RedDeep = AuthDesignTokens.RedDeep // #9B0013
    val RedGlow = AuthDesignTokens.RedGlow // #FF1A35
    val RedDim = Color(0x1EE8001D) // rgba(232,0,29,0.12)
    val Green = Color(0xFF22C55E)
    val Orange = Color(0xFFF59E0B)
    val Blue = Color(0xFF3B82F6)
}

// ═══════════════════════════════════════════════════════════════════════════
//  UI STATE & VIEWMODEL
// ═══════════════════════════════════════════════════════════════════════════
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Idle(val userName: String) : DashboardUiState()
}

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Idle("Rahul"))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
}

// ═══════════════════════════════════════════════════════════════════════════
//  MAIN SCREEN
// ═══════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    var currentRoute by remember { mutableStateOf("dashboard") }

    Scaffold(
        containerColor = DashboardTokens.BlackCore,
        bottomBar = { DashboardBottomNav(currentRoute) { currentRoute = it } },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Animation
            DashboardBackground()
            
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // TopBar
                DashboardTopBar(onLogout = onLogout)

                if (uiState is DashboardUiState.Idle) {
                    val name = (uiState as DashboardUiState.Idle).userName
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item { GreetingRow(name) }
                        item { TowBanner() }
                        item { StatCardsGrid() }
                        item { MapCard() }
                        item { NearbyServicesCard() }
                        item { AiChatCard() }
                        item { RecentIncidentsCard() }
                        item { CommunityFeedCard() }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  BACKGROUND & TOPBAR
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun DashboardBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(7000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "s1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.13f,
        animationSpec = infiniteRepeatable(tween(7000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "a1"
    )
    
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "s2"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.13f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "a2"
    )

    Box(Modifier.fillMaxSize()) {
        // Blob 1 (Top Left)
        Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = (-200).dp)
                .size(600.dp)
                .graphicsLayer { scaleX = scale1; scaleY = scale1; alpha = alpha1 }
                .background(Brush.radialGradient(listOf(DashboardTokens.RedHot, Color.Transparent), radius = 800f))
        )
        // Blob 2 (Bottom Right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(400.dp)
                .graphicsLayer { scaleX = scale2; scaleY = scale2; alpha = alpha2 }
                .background(Brush.radialGradient(listOf(DashboardTokens.RedDeep, Color.Transparent), radius = 600f))
        )
        // Grid Overlay
        // Dot grid
        Box(modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val color = Color(0x06E8001D)
                val cellPx = 48.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(color, Offset(0f, y), Offset(size.width, y), 1f)
                    y += cellPx
                }
                var x = 0f
                while (x < size.width) {
                    drawLine(color, Offset(x, 0f), Offset(x, size.height), 1f)
                    x += cellPx
                }
            }
        )
    }
}

@Composable
private fun DashboardTopBar(onLogout: () -> Unit = {}) {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart), label = "dotAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xD8080808))
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "RescueLink",
                fontFamily = BebasNeueFontFamily,
                fontSize = 22.sp,
                letterSpacing = 2.sp,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(listOf(Color.White, DashboardTokens.RedGlow))
                )
            )
            Text(
                text = "EMERGENCY ROAD ASSIST",
                fontFamily = OutfitFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = DashboardTokens.White35,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(Modifier.weight(1f))
        // Live Badge
        Row(
            modifier = Modifier
                .background(DashboardTokens.RedDim, RoundedCornerShape(100.dp))
                .border(1.dp, Color(0x40E8001D), RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .graphicsLayer { alpha = dotAlpha }
                    .background(DashboardTokens.RedHot, CircleShape)
            )
            Spacer(Modifier.width(7.dp))
            Text("Live Tracking ON", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.RedHot)
        }
        Spacer(Modifier.width(10.dp))
        // Notif Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(DashboardTokens.CardBg2, RoundedCornerShape(11.dp))
                .border(1.dp, DashboardTokens.Rim2, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", tint = DashboardTokens.White60, modifier = Modifier.size(20.dp))
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset((-8).dp, 8.dp)
                    .size(7.dp)
                    .background(DashboardTokens.RedHot, CircleShape)
                    .border(1.5.dp, DashboardTokens.BlackCore, CircleShape)
            )
        }
        Spacer(Modifier.width(8.dp))
        // Logout Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(DashboardTokens.CardBg2, RoundedCornerShape(11.dp))
                .border(1.dp, DashboardTokens.Rim2, RoundedCornerShape(11.dp))
                .clickable(onClick = onLogout),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Logout, contentDescription = "Logout", tint = DashboardTokens.White60, modifier = Modifier.size(18.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  GREETING & SOS
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun GreetingRow(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text("Good Evening, ", fontFamily = OutfitFontFamily, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DashboardTokens.WhitePure)
                Text(name, fontFamily = OutfitFontFamily, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DashboardTokens.RedHot)
                Text(" \uD83D\uDC4B", fontSize = 24.sp)
            }
            Text("Your vehicle is tracked & 3 mechanics are nearby. Stay safe.", fontFamily = OutfitFontFamily, fontSize = 13.sp, color = DashboardTokens.White60)
        }
        
        // SOS Button
        Button(
            onClick = { /* Handle SOS */ },
            modifier = Modifier.height(44.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 22.dp)
        ) {
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(DashboardTokens.RedHot, DashboardTokens.RedDeep))), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(Color.White, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text("SOS Emergency", fontFamily = OutfitFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOW BANNER
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun TowBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0x14E8001D), Color(0x1E9B0013))), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x38E8001D), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).background(DashboardTokens.RedDim, RoundedCornerShape(12.dp)).border(1.dp, Color(0x33E8001D), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.LocalShipping, contentDescription = null, tint = DashboardTokens.RedHot, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Tow Truck En Route — Sharma Towing Co.", fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DashboardTokens.WhitePure)
            Text("Driver Vikram Singh • Mahindra Bolero Pickup • MH-12-AB-4321", fontFamily = OutfitFontFamily, fontSize = 12.sp, color = DashboardTokens.White60)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(3.dp).background(DashboardTokens.Rim2, CircleShape)) {
                Box(Modifier.fillMaxWidth(0.62f).fillMaxHeight().background(Brush.horizontalGradient(listOf(DashboardTokens.RedHot, DashboardTokens.RedGlow)), CircleShape))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.background(DashboardTokens.RedDim, RoundedCornerShape(10.dp)).border(1.dp, Color(0x33E8001D), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("12", fontFamily = BebasNeueFontFamily, fontSize = 24.sp, color = DashboardTokens.RedHot)
            Text("MIN ETA", fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.White35, letterSpacing = 1.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  STAT CARDS (2x2 Grid for Mobile)
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun StatCardsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Rounded.Build, value = "3", label = "Mechanics Nearby", change = "↑ 2 new", isUp = true)
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Rounded.LocalShipping, value = "1", label = "Active Tow Request", change = "In Progress", isUp = true, customColor = DashboardTokens.Orange)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Rounded.People, value = "24", label = "Community Members", change = "↑ 5 online", isUp = true)
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Rounded.Warning, value = "7", label = "Total Past Incidents", change = "last: 3d ago", isUp = false)
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, change: String, isUp: Boolean, customColor: Color? = null) {
    val mainColor = customColor ?: DashboardTokens.RedHot
    Box(
        modifier = modifier
            .background(DashboardTokens.CardBg, RoundedCornerShape(16.dp))
            .border(1.dp, DashboardTokens.Rim, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, DashboardTokens.RedHot.copy(alpha = 0.4f), Color.Transparent))).align(Alignment.TopCenter))
        
        Column {
            Box(Modifier.size(38.dp).background(DashboardTokens.RedDim, RoundedCornerShape(10.dp)).border(1.dp, Color(0x2EE8001D), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = mainColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(value, fontFamily = BebasNeueFontFamily, fontSize = 34.sp, color = DashboardTokens.WhitePure, lineHeight = 34.sp)
            Text(label, fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DashboardTokens.White60)
        }
        
        val changeBg = if (isUp) (customColor ?: DashboardTokens.Green).copy(alpha = 0.1f) else DashboardTokens.RedDim
        val changeColor = if (isUp) (customColor ?: DashboardTokens.Green) else DashboardTokens.RedHot
        Text(
            text = change,
            fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = changeColor,
            modifier = Modifier.align(Alignment.TopEnd).background(changeBg, RoundedCornerShape(100.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  MAP CARD
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun MapCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "mapAnimations")
    val pinOffset by infiniteTransition.animateFloat(initialValue = 0f, targetValue = -8f, animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pin")
    val ringRadius by infiniteTransition.animateFloat(initialValue = 18f, targetValue = 38f, animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing)), label = "ringR")
    val ringAlpha by infiniteTransition.animateFloat(initialValue = 0.6f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing)), label = "ringA")
    val towTruckX by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 40f, animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse), label = "towTruck")

    Column(
        modifier = Modifier.fillMaxWidth().background(DashboardTokens.CardBg, RoundedCornerShape(16.dp)).border(1.dp, DashboardTokens.Rim, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Live Location Map", fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DashboardTokens.WhitePure)
                Text("NH-48, Near Mansa, Gujarat", fontFamily = OutfitFontFamily, fontSize = 11.sp, color = DashboardTokens.White35)
            }
            Row(modifier = Modifier.background(DashboardTokens.RedDim, RoundedCornerShape(100.dp)).border(1.dp, Color(0x40E8001D), RoundedCornerShape(100.dp)).padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(DashboardTokens.RedHot, CircleShape))
                Spacer(Modifier.width(5.dp))
                Text("GPS Active", fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.RedHot)
            }
        }
        HorizontalDivider(color = DashboardTokens.Rim)
        
        // Canvas Map
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFF0D0D0D))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Main Roads
                drawPath(Path().apply { moveTo(0f, h/2); quadraticTo(w*0.25f, h*0.4f, w/2, h/2); quadraticTo(w*0.75f, h*0.6f, w, h/2) }, DashboardTokens.Rim2, style = Stroke(width = 14.dp.toPx()))
                drawPath(Path().apply { moveTo(w/2, 0f); quadraticTo(w*0.45f, h*0.25f, w/2, h/2); quadraticTo(w*0.55f, h*0.75f, w/2, h) }, DashboardTokens.Rim2, style = Stroke(width = 14.dp.toPx()))
                
                // Dash lines
                drawPath(Path().apply { moveTo(0f, h/2); quadraticTo(w*0.25f, h*0.4f, w/2, h/2); quadraticTo(w*0.75f, h*0.6f, w, h/2) }, DashboardTokens.RedDim, style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
                drawPath(Path().apply { moveTo(w/2, 0f); quadraticTo(w*0.45f, h*0.25f, w/2, h/2); quadraticTo(w*0.55f, h*0.75f, w/2, h) }, DashboardTokens.RedDim, style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
                
                // User Location
                val cx = w/2; val cy = h/2
                drawCircle(DashboardTokens.RedHot.copy(alpha = ringAlpha), radius = ringRadius.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 1.5.dp.toPx()))
                
                // Bouncing Pin
                drawCircle(DashboardTokens.RedHot.copy(alpha = 0.2f), radius = 10.dp.toPx(), center = Offset(cx, cy + pinOffset))
                drawCircle(DashboardTokens.RedHot, radius = 5.dp.toPx(), center = Offset(cx, cy + pinOffset))
                drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(cx, cy + pinOffset))
                
                // Mechanics Mock
                drawRoundRect(DashboardTokens.Green.copy(alpha = 0.2f), topLeft = Offset(cx - 70.dp.toPx(), cy - 40.dp.toPx()), size = androidx.compose.ui.geometry.Size(22.dp.toPx(), 22.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
                drawRoundRect(DashboardTokens.Green.copy(alpha = 0.2f), topLeft = Offset(cx + 60.dp.toPx(), cy - 30.dp.toPx()), size = androidx.compose.ui.geometry.Size(22.dp.toPx(), 22.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
                
                // Tow Truck Mock (Moving)
                drawRoundRect(DashboardTokens.RedHot.copy(alpha = 0.2f), topLeft = Offset(cx - 50.dp.toPx() + towTruckX.dp.toPx(), cy - 10.dp.toPx()), size = androidx.compose.ui.geometry.Size(22.dp.toPx(), 22.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
            }
            
            // Legend
            Row(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MapLegendItem(DashboardTokens.RedHot, "You")
                MapLegendItem(DashboardTokens.Green, "Mechanic")
                MapLegendItem(DashboardTokens.Orange, "Tow Truck")
            }
        }
    }
}

@Composable
private fun MapLegendItem(color: Color, label: String) {
    Row(modifier = Modifier.background(Color(0xCC080808), RoundedCornerShape(100.dp)).border(1.dp, DashboardTokens.Rim2, RoundedCornerShape(100.dp)).padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(label, fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.White60)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  NEARBY SERVICES & LISTS
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun NearbyServicesCard() {
    CardContainer("Nearby Services", "See All") {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ServiceRow(Icons.Rounded.Build, "Patel Auto Workshop", "★★★★★  Open Now", "1.2 km", "~4 min", DashboardTokens.Green)
            ServiceRow(Icons.Rounded.Build, "Singh Motors & Repair", "★★★★☆  Open Now", "2.8 km", "~9 min", DashboardTokens.Green)
            ServiceRow(Icons.Rounded.LocalShipping, "Sharma Towing Co.", "★★★★★  En Route", "Active", "12 min ETA", DashboardTokens.RedHot)
            ServiceRow(Icons.Rounded.LocalGasStation, "NH-48 Petrol Station", "★★★☆☆  24 hrs", "0.8 km", "~2 min", DashboardTokens.Blue)
        }
    }
}

@Composable
private fun ServiceRow(icon: androidx.compose.ui.graphics.vector.ImageVector, name: String, meta: String, dist: String, eta: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().background(DashboardTokens.CardBg2, RoundedCornerShape(12.dp)).border(1.dp, DashboardTokens.Rim, RoundedCornerShape(12.dp)).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).background(color.copy(alpha=0.1f), RoundedCornerShape(10.dp)).border(1.dp, color.copy(alpha=0.2f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontFamily = OutfitFontFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.WhitePure, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(meta, fontFamily = OutfitFontFamily, fontSize = 11.sp, color = DashboardTokens.White35)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(dist, fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if(dist=="Active") DashboardTokens.Orange else DashboardTokens.RedHot)
            Text(eta, fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White35)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  AI CHAT
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun AiChatCard() {
    var inputText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(
        Pair("ai", "Hey Rahul! I see your vehicle is stopped on NH-48. I've already dispatched the nearest tow truck. How can I help further?"),
        Pair("user", "Engine won't start. Battery or alternator?"),
        Pair("ai", "Likely dead battery. Try jump-starting — I've notified Patel Auto Workshop 1.2km away. They can assist in ~4 min!")
    )) }

    CardContainer("AI Assistant", badge = "RESCUE AI • Online") {
        Column(modifier = Modifier.height(260.dp)) {
            // Messages
            LazyColumn(modifier = Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(messages) { (sender, text) ->
                    val isAi = sender == "ai"
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = if (isAi) 0.dp else 16.dp), horizontalAlignment = if (isAi) Alignment.Start else Alignment.End) {
                        Text(if (isAi) "RESCUE AI" else "YOU", fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.White35, letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(4.dp))
                        val bubbleShape = RoundedCornerShape(12.dp, 12.dp, if(isAi) 12.dp else 4.dp, if(isAi) 4.dp else 12.dp)
                        Box(modifier = Modifier
                            .background(
                                if (isAi) DashboardTokens.CardBg2 else Color.Transparent,
                                bubbleShape
                            )
                            .then(
                                if (!isAi) Modifier.background(
                                    Brush.linearGradient(listOf(DashboardTokens.RedHot, DashboardTokens.RedDeep)),
                                    bubbleShape
                                ) else Modifier
                            )
                            .border(1.dp, if(isAi) DashboardTokens.Rim2 else Color.Transparent, bubbleShape)
                            .padding(9.dp)) {
                            Text(text, fontFamily = OutfitFontFamily, fontSize = 12.sp, color = DashboardTokens.WhitePure)
                        }
                    }
                }
            }
            HorizontalDivider(color = DashboardTokens.Rim)
            // Input
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText, onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f).height(46.dp),
                    placeholder = { Text("Ask anything...", fontFamily = OutfitFontFamily, fontSize = 13.sp, color = DashboardTokens.White35) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = OutfitFontFamily, fontSize = 13.sp, color = DashboardTokens.WhitePure),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DashboardTokens.CardBg2, unfocusedContainerColor = DashboardTokens.CardBg2,
                        focusedBorderColor = DashboardTokens.RedHot, unfocusedBorderColor = DashboardTokens.Rim2,
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { if (inputText.isNotBlank()) { messages = messages + Pair("user", inputText); inputText = "" } },
                    modifier = Modifier.size(46.dp).background(Brush.linearGradient(listOf(DashboardTokens.RedHot, DashboardTokens.RedDeep)), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Rounded.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  RECENT INCIDENTS & COMMUNITY FEED
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun RecentIncidentsCard() {
    CardContainer("Recent Incidents", "View All") {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IncidentRow(DashboardTokens.RedHot, "Engine Breakdown — NH-48", "Tow requested • Sharma Towing", "Now")
            IncidentRow(DashboardTokens.Green, "Flat Tyre — Ahmedabad Highway", "Resolved by Patel Workshop", "3d ago")
            IncidentRow(DashboardTokens.Green, "Fuel Empty — Gandhinagar Rd", "Community help — Amit S.", "9d ago")
            IncidentRow(DashboardTokens.Orange, "Battery Dead — Ring Road", "Jump-start requested", "14d ago")
        }
    }
}

@Composable
private fun IncidentRow(statusColor: Color, title: String, sub: String, time: String) {
    Row(modifier = Modifier.fillMaxWidth().background(DashboardTokens.CardBg2, RoundedCornerShape(12.dp)).border(1.dp, DashboardTokens.Rim, RoundedCornerShape(12.dp)).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(statusColor, CircleShape))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = OutfitFontFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.WhitePure)
            Text(sub, fontFamily = OutfitFontFamily, fontSize = 11.sp, color = DashboardTokens.White35)
        }
        Text(time, fontFamily = OutfitFontFamily, fontSize = 11.sp, color = DashboardTokens.White35)
    }
}

@Composable
private fun CommunityFeedCard() {
    CardContainer("Community Feed", "See All") {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CommunityRow("AS", "Amit Shah", "2 min ago", "Traffic jam on NH-48 near Mansa — accident reported ahead. Suggest alternate route via SH-41.", 14, DashboardTokens.RedDeep)
            CommunityRow("PV", "Priya Verma", "18 min ago", "Anyone near Mahesana knows a good diesel mechanic? My truck won't start after refueling.", 3, DashboardTokens.Green)
            CommunityRow("RD", "Ravi Desai", "1 hr ago", "Pothole alert! Big crater on SH-41 km 34. Drive carefully at night.", 29, DashboardTokens.Blue)
        }
    }
}

@Composable
private fun CommunityRow(initials: String, name: String, time: String, msg: String, upvotes: Int, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().background(DashboardTokens.CardBg2, RoundedCornerShape(12.dp)).border(1.dp, DashboardTokens.Rim, RoundedCornerShape(12.dp)).padding(11.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(30.dp).background(Brush.linearGradient(listOf(color.copy(alpha=0.5f), color)), CircleShape), contentAlignment = Alignment.Center) {
            Text(initials, fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DashboardTokens.WhitePure)
                Text(time, fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White35)
            }
            Text(msg, fontFamily = OutfitFontFamily, fontSize = 12.sp, color = DashboardTokens.White60, lineHeight = 16.sp, modifier = Modifier.padding(vertical = 4.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("▲ $upvotes helpful", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.White35)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  HELPERS & BOTTOM NAV
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun CardContainer(title: String, actionText: String? = null, badge: String? = null, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(DashboardTokens.CardBg, RoundedCornerShape(16.dp)).border(1.dp, DashboardTokens.Rim, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(title, fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DashboardTokens.WhitePure)
                if (badge != null) Text(badge, fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = DashboardTokens.White35)
            }
            if (actionText != null) Text(actionText, fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.RedHot)
            if (badge != null) Box(Modifier.size(8.dp).background(DashboardTokens.Green, CircleShape))
        }
        HorizontalDivider(color = DashboardTokens.Rim)
        content()
    }
}

@Composable
private fun DashboardBottomNav(currentRoute: String, onRoute: (String) -> Unit) {
    NavigationBar(containerColor = DashboardTokens.CardBg, tonalElevation = 0.dp, modifier = Modifier.border(1.dp, DashboardTokens.Rim)) {
        val items = listOf(
            Triple("dashboard", Icons.Rounded.Home, "Dashboard"),
            Triple("map", Icons.Rounded.Map, "Map"),
            Triple("community", Icons.Rounded.People, "Community"),
            Triple("chat", Icons.Rounded.Chat, "AI Chat"),
            Triple("more", Icons.Rounded.Menu, "More")
        )
        items.forEach { (route, icon, label) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onRoute(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontFamily = OutfitFontFamily, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DashboardTokens.RedHot,
                    unselectedIconColor = DashboardTokens.White35,
                    selectedTextColor = DashboardTokens.RedHot,
                    unselectedTextColor = DashboardTokens.White35,
                    indicatorColor = DashboardTokens.RedDim
                )
            )
        }
    }
}
