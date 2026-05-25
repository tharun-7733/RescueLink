package com.example.sos

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sos.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ═══════════════════════════════════════════════════════════════════════════
//  DESIGN TOKENS  (mobile-optimised sizes)
// ═══════════════════════════════════════════════════════════════════════════
object DashboardTokens {
    val BlackCore = Color(0xFF080808)
    val CardBg    = Color(0xFF101010)
    val CardBg2   = Color(0xFF141414)
    val Rim       = Color(0xFF1E1E1E)
    val Rim2      = Color(0xFF2A2A2A)
    val WhitePure = Color(0xFFFFFFFF)
    val White60   = Color(0x99FFFFFF)
    val White35   = Color(0x59FFFFFF)
    val White15   = Color(0x14FFFFFF)

    val RedHot  = AuthDesignTokens.RedHot
    val RedDeep = AuthDesignTokens.RedDeep
    val RedGlow = AuthDesignTokens.RedGlow
    val RedDim  = Color(0x1EE8001D)

    val Green  = Color(0xFF22C55E)
    val Orange = Color(0xFFF59E0B)
    val Blue   = Color(0xFF3B82F6)
}

// ═══════════════════════════════════════════════════════════════════════════
//  DATA MODELS
// ═══════════════════════════════════════════════════════════════════════════
data class Incident(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val status: String = "active",   // active | resolved | pending
    val timestamp: Long = 0L
)

data class CommunityPost(
    val id: String = "",
    val author: String = "",
    val initials: String = "",
    val timeAgo: String = "",
    val content: String = "",
    val likes: Int = 0,
    val comments: Int = 0
)

// ═══════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — real Firebase data
// ═══════════════════════════════════════════════════════════════════════════
data class DashboardState(
    val userInitials: String = "·",
    val incidents: List<Incident> = emptyList(),
    val communityPosts: List<CommunityPost> = emptyList(),
    val isLoadingIncidents: Boolean = true,
    val isLoadingCommunity: Boolean = true,
    val sosActive: Boolean = false
)

class DashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db   = try { FirebaseDatabase.getInstance() } catch (e: Exception) { null }

    private var incidentListener: ValueEventListener? = null
    private var communityListener: ValueEventListener? = null

    init {
        loadUserInfo()
        observeIncidents()
        observeCommunityPosts()
    }

    // ── User ──────────────────────────────────────────────────────────────
    private fun loadUserInfo() {
        val user = auth.currentUser
        val initials = when {
            user?.displayName?.isNotBlank() == true ->
                user.displayName!!.trim().split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
            user?.email?.isNotBlank() == true ->
                user.email!!.first().uppercase()
            else -> "·"
        }
        _state.value = _state.value.copy(userInitials = initials)
    }

    // ── Incidents (real-time) ─────────────────────────────────────────────
    private fun observeIncidents() {
        val uid = auth.currentUser?.uid ?: run {
            _state.value = _state.value.copy(isLoadingIncidents = false)
            return
        }
        val ref = db?.reference?.child("incidents")?.child(uid) ?: run {
            _state.value = _state.value.copy(isLoadingIncidents = false)
            return
        }

        incidentListener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = snap.children.mapNotNull { child ->
                    try {
                        Incident(
                            id       = child.key ?: "",
                            title    = child.child("title").getValue(String::class.java) ?: "",
                            subtitle = child.child("subtitle").getValue(String::class.java) ?: "",
                            status   = child.child("status").getValue(String::class.java) ?: "active",
                            timestamp= child.child("timestamp").getValue(Long::class.java) ?: 0L
                        )
                    } catch (e: Exception) { null }
                }.sortedByDescending { it.timestamp }
                _state.value = _state.value.copy(incidents = list, isLoadingIncidents = false)
            }
            override fun onCancelled(error: DatabaseError) {
                _state.value = _state.value.copy(isLoadingIncidents = false)
            }
        }
        ref.addValueEventListener(incidentListener!!)
    }

    // ── Community posts (real-time) ───────────────────────────────────────
    private fun observeCommunityPosts() {
        val ref = db?.reference?.child("community") ?: run {
            _state.value = _state.value.copy(
                isLoadingCommunity = false,
                communityPosts = sampleCommunityPosts()
            )
            return
        }

        communityListener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = snap.children.mapNotNull { child ->
                    try {
                        CommunityPost(
                            id       = child.key ?: "",
                            author   = child.child("author").getValue(String::class.java) ?: "User",
                            initials = child.child("initials").getValue(String::class.java) ?: "U",
                            timeAgo  = child.child("timeAgo").getValue(String::class.java) ?: "",
                            content  = child.child("content").getValue(String::class.java) ?: "",
                            likes    = child.child("likes").getValue(Long::class.java)?.toInt() ?: 0,
                            comments = child.child("comments").getValue(Long::class.java)?.toInt() ?: 0
                        )
                    } catch (e: Exception) { null }
                }
                val finalList = if (list.isEmpty()) sampleCommunityPosts() else list
                _state.value = _state.value.copy(communityPosts = finalList, isLoadingCommunity = false)
            }
            override fun onCancelled(error: DatabaseError) {
                _state.value = _state.value.copy(
                    communityPosts = sampleCommunityPosts(),
                    isLoadingCommunity = false
                )
            }
        }
        ref.limitToLast(20).addValueEventListener(communityListener!!)
    }

    fun toggleSos() {
        _state.value = _state.value.copy(sosActive = !_state.value.sosActive)
    }

    private fun sampleCommunityPosts() = listOf(
        CommunityPost("1","Amit Sharma","AS","2h ago","Heavy traffic near Gandhinagar exit due to road work. Avoid service lane.",24,5),
        CommunityPost("2","Priya Patel","PP","4h ago","Flat tyre repair stall available near NH-48 toll booth.",18,3),
        CommunityPost("3","Raj Kumar","RK","Yesterday","Police checking going on near Ahmedabad bypass. Keep documents ready.",31,8)
    )

    override fun onCleared() {
        val uid = auth.currentUser?.uid
        incidentListener?.let {
            uid?.let { id -> db?.reference?.child("incidents")?.child(id)?.removeEventListener(it) }
        }
        communityListener?.let {
            db?.reference?.child("community")?.removeEventListener(it)
        }
        super.onCleared()
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  MAIN SCREEN
// ═══════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    vm: DashboardViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var currentRoute by remember { mutableStateOf("home") }

    Scaffold(
        containerColor = DashboardTokens.BlackCore,
        bottomBar = {
            DashboardBottomNav(currentRoute) { currentRoute = it }
        }
    ) { innerPadding ->
        // Background always behind everything
        Box(modifier = Modifier.fillMaxSize()) {
            DashboardBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)   // accounts for bottom nav height
            ) {
                DashboardTopBar(initials = state.userInitials, onLogout = onLogout)

                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                    label = "route",
                    modifier = Modifier.fillMaxSize()
                ) { route ->
                    when (route) {
                        "home"      -> HomeScreen(state, vm::toggleSos)
                        "map"       -> MapScreen()
                        "community" -> CommunityScreen(state)
                        "chat"      -> AiChatScreen()
                        "more"      -> MoreScreen(onLogout)
                        else        -> HomeScreen(state, vm::toggleSos)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  HOME SCREEN
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun HomeScreen(state: DashboardState, onSosTap: () -> Unit) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HeroCard(state.sosActive, onSosTap) }
        item { StatusStrip() }
        item { ServiceHistorySection(state.incidents, state.isLoadingIncidents) }
        item { ChatbotPreviewSection() }
        item { Spacer(Modifier.height(4.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HERO CARD  (intro + SOS)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroCard(sosActive: Boolean, onSosTap: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "hero")
    val glowAlpha by inf.animateFloat(0.3f, 0.7f,
        infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse), "glow")
    val pulse by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(1200), RepeatMode.Restart), "pulse")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF180005), DashboardTokens.CardBg)),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, DashboardTokens.Rim, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Glow blob
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(90.dp)
                .offset(x = 10.dp, y = (-10).dp)
                .graphicsLayer { alpha = glowAlpha }
                .background(Brush.radialGradient(
                    listOf(DashboardTokens.RedHot.copy(0.22f), Color.Transparent)
                ))
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Title
            Column {
                Text(
                    "RescueLink",
                    fontFamily = BebasNeueFontFamily,
                    fontSize = 22.sp,
                    letterSpacing = 1.sp,
                    style = TextStyle(brush = Brush.linearGradient(
                        listOf(Color.White, DashboardTokens.RedGlow)
                    ))
                )
                Text(
                    "Your emergency road assistant is active and ready.",
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    color = DashboardTokens.White60,
                    lineHeight = 17.sp
                )
            }

            // Status tags
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusTag("🛡️ Active")
                StatusTag("📍 GPS On")
                StatusTag("🤖 AI Ready")
            }

            // SOS Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(
                        Brush.linearGradient(
                            if (sosActive)
                                listOf(Color(0xFF3D0010), Color(0xFF1A0008))
                            else
                                listOf(DashboardTokens.RedHot, DashboardTokens.RedDeep)
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (sosActive) DashboardTokens.RedHot else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onSosTap),
                contentAlignment = Alignment.Center
            ) {
                if (!sosActive) {
                    Box(
                        Modifier.fillMaxSize()
                            .graphicsLayer { scaleX = 1f + pulse * 0.04f; scaleY = 1f + pulse * 0.04f; alpha = (1f - pulse) * 0.5f }
                            .border(2.dp, DashboardTokens.RedHot, RoundedCornerShape(12.dp))
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(
                        if (sosActive) "TAP TO CANCEL SOS" else "SEND SOS ALERT",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusTag(text: String) {
    Box(
        modifier = Modifier
            .background(DashboardTokens.RedDim, RoundedCornerShape(100.dp))
            .border(1.dp, Color(0x28E8001D), RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White60)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STATUS STRIP
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatusStrip() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusChip(Icons.Rounded.LocalGasStation, "Fuel",  "64%", DashboardTokens.Green,  Modifier.weight(1f))
        StatusChip(Icons.Rounded.Speed,           "Tyre",  "32 PSI", DashboardTokens.Orange, Modifier.weight(1f))
        StatusChip(Icons.Rounded.LocationOn,      "GPS",   "Active", DashboardTokens.Blue,   Modifier.weight(1f))
    }
}

@Composable
private fun StatusChip(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(DashboardTokens.CardBg, RoundedCornerShape(12.dp))
            .border(1.dp, DashboardTokens.Rim, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(value, fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontFamily = OutfitFontFamily, fontSize = 9.sp, color = DashboardTokens.White35)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SERVICE HISTORY
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ServiceHistorySection(incidents: List<Incident>, isLoading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionHeader("Service History", "View All")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DashboardTokens.CardBg, RoundedCornerShape(14.dp))
                .border(1.dp, DashboardTokens.Rim, RoundedCornerShape(14.dp))
        ) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = DashboardTokens.RedHot,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                incidents.isEmpty() -> {
                    EmptyState("No service history yet", "Your incidents will appear here")
                }
                else -> {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        incidents.forEachIndexed { idx, incident ->
                            HistoryRow(incident)
                            if (idx < incidents.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    color = DashboardTokens.Rim,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(incident: Incident) {
    val dotColor = when (incident.status) {
        "resolved" -> DashboardTokens.Green
        "pending"  -> DashboardTokens.Orange
        else       -> DashboardTokens.RedHot
    }
    val isLive = incident.status == "active"

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(dotColor, CircleShape))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                incident.title,
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(incident.subtitle, fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White60)
        }
        Spacer(Modifier.width(6.dp))
        Text(
            if (isLive) "Now" else formatTimestamp(incident.timestamp),
            fontFamily = OutfitFontFamily,
            fontSize = 9.sp,
            color = if (isLive) DashboardTokens.RedHot else DashboardTokens.White35,
            fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun formatTimestamp(ts: Long): String {
    if (ts == 0L) return ""
    val diff = System.currentTimeMillis() - ts
    val hours = diff / 3_600_000
    val days  = diff / 86_400_000
    return when {
        hours < 1  -> "Just now"
        hours < 24 -> "${hours}h ago"
        days < 30  -> "${days}d ago"
        else       -> "${days / 30}mo ago"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CHATBOT PREVIEW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ChatbotPreviewSection() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionHeader("AI Emergency Assistant", "Open Chat")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DashboardTokens.CardBg, RoundedCornerShape(14.dp))
                .border(1.dp, DashboardTokens.Rim, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // AI bubble
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(DashboardTokens.RedDim, CircleShape)
                            .border(1.dp, Color(0x28E8001D), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = DashboardTokens.RedHot, modifier = Modifier.size(14.dp))
                    }
                    Box(
                        modifier = Modifier
                            .background(DashboardTokens.Rim2,
                                RoundedCornerShape(topStart = 2.dp, topEnd = 10.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
                            .padding(9.dp)
                    ) {
                        Text(
                            "I'm your AI road assistant. Need help with a breakdown, nearby services, or emergency guidance?",
                            fontFamily = OutfitFontFamily, fontSize = 11.sp, color = Color.White, lineHeight = 16.sp
                        )
                    }
                }

                // Quick chips
                Text("Quick actions:", fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White35)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickChip("🔧 Mechanic"); QuickChip("⛽ Fuel")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickChip("🚑 Hospital"); QuickChip("👮 Police")
                }
            }
        }
    }
}

@Composable
private fun QuickChip(text: String) {
    Box(
        modifier = Modifier
            .background(DashboardTokens.CardBg2, RoundedCornerShape(8.dp))
            .border(1.dp, DashboardTokens.Rim2, RoundedCornerShape(8.dp))
            .clickable {}
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(text, fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White60)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  MAP SCREEN
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun MapScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text("Map & Navigation", fontFamily = OutfitFontFamily, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Locate nearby assistance", fontFamily = OutfitFontFamily, fontSize = 11.sp, color = DashboardTokens.White60)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, DashboardTokens.Rim, RoundedCornerShape(14.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF0C0C0C))) {
                val step = 36.dp.toPx()
                for (i in 0..30) {
                    drawLine(Color(0x0AFFFFFF), Offset(i * step, 0f), Offset(i * step, size.height))
                    drawLine(Color(0x0AFFFFFF), Offset(0f, i * step), Offset(size.width, i * step))
                }
                drawRect(Color(0xFF151515), Offset(0f, size.height * 0.42f), Size(size.width, 24.dp.toPx()))
                drawRect(Color(0xFF151515), Offset(size.width * 0.52f, 0f), Size(18.dp.toPx(), size.height))
                drawCircle(DashboardTokens.RedHot, 7.dp.toPx(), Offset(size.width * 0.42f, size.height * 0.44f))
                drawCircle(DashboardTokens.Blue,   5.dp.toPx(), Offset(size.width * 0.72f, size.height * 0.24f))
                drawCircle(DashboardTokens.Green,  5.dp.toPx(), Offset(size.width * 0.22f, size.height * 0.62f))
                drawCircle(DashboardTokens.Orange, 5.dp.toPx(), Offset(size.width * 0.82f, size.height * 0.72f))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f))))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, tint = DashboardTokens.RedHot, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Near Ahmedabad Hwy, Gujarat", fontFamily = OutfitFontFamily, fontSize = 11.sp, color = Color.White)
                    Spacer(Modifier.weight(1f))
                    Text("Expand", fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.RedHot, fontWeight = FontWeight.Bold)
                }
            }
        }

        SectionHeader("Nearby Assistance", "See All")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServiceCard("Mechanics", Icons.Rounded.Build,           DashboardTokens.Orange, "3 nearby")
            ServiceCard("Fuel",      Icons.Rounded.LocalGasStation, DashboardTokens.Green,  "1.2 km")
            ServiceCard("Hospital",  Icons.Rounded.MedicalServices, DashboardTokens.RedHot, "2.8 km")
            ServiceCard("Police",    Icons.Rounded.Security,        DashboardTokens.Blue,   "0.9 km")
        }
    }
}

@Composable
private fun ServiceCard(name: String, icon: ImageVector, color: Color, distance: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(DashboardTokens.CardBg2, RoundedCornerShape(12.dp))
                .border(1.dp, DashboardTokens.Rim2, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.height(4.dp))
        Text(name,     fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White60)
        Text(distance, fontFamily = OutfitFontFamily, fontSize = 9.sp,  color = color)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  COMMUNITY SCREEN
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun CommunityScreen(state: DashboardState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Community", fontFamily = OutfitFontFamily, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Live updates from travellers", fontFamily = OutfitFontFamily, fontSize = 11.sp, color = DashboardTokens.White60)
                }
                Box(
                    modifier = Modifier
                        .background(DashboardTokens.RedHot, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text("Post", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (state.isLoadingCommunity) {
            item {
                Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DashboardTokens.RedHot, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            }
        } else {
            items(state.communityPosts) { post -> CommunityPostCard(post) }
        }
    }
}

@Composable
private fun CommunityPostCard(post: CommunityPost) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DashboardTokens.CardBg, RoundedCornerShape(14.dp))
            .border(1.dp, DashboardTokens.Rim, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(DashboardTokens.RedDim, CircleShape)
                        .border(1.dp, Color(0x28E8001D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(post.initials, fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DashboardTokens.RedHot)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(post.author,  fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(post.timeAgo, fontFamily = OutfitFontFamily, fontSize = 9.sp,  color = DashboardTokens.White35)
                }
            }
            Text(post.content, fontFamily = OutfitFontFamily, fontSize = 12.sp, color = DashboardTokens.WhitePure, lineHeight = 17.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ThumbUp, null, tint = DashboardTokens.RedHot, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(3.dp))
                Text("${post.likes}",    fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White60)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Rounded.Comment, null, tint = DashboardTokens.White35, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(3.dp))
                Text("${post.comments}", fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White60)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  AI CHAT SCREEN
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun AiChatScreen(
    chatVm: ChatViewModel = viewModel()
) {
    val messages = chatVm.chatMessages
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xD9080808))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(DashboardTokens.RedDim, CircleShape)
                    .border(1.dp, Color(0x38E8001D), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = DashboardTokens.RedHot, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("AI Emergency Assistant", fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Online · Responds instantly", fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.Green)
            }
            Spacer(Modifier.weight(1f))
            Text("History", fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.RedHot, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.reversed()) { msg ->
                val isAi = !msg.isUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    if (isAi) {
                        Box(
                            modifier = Modifier.size(24.dp).background(DashboardTokens.RedDim, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = DashboardTokens.RedHot, modifier = Modifier.size(12.dp)) }
                        Spacer(Modifier.width(6.dp))
                    }
                    Box(
                        modifier = Modifier
                            .widthIn(max = 240.dp)
                            .background(
                                if (isAi) DashboardTokens.CardBg2 else DashboardTokens.RedHot,
                                RoundedCornerShape(
                                    topStart = if (isAi) 2.dp else 10.dp,
                                    topEnd = if (isAi) 10.dp else 2.dp,
                                    bottomStart = 10.dp, bottomEnd = 10.dp
                                )
                            )
                            .border(
                                1.dp,
                                if (isAi) DashboardTokens.Rim2 else Color.Transparent,
                                RoundedCornerShape(
                                    topStart = if (isAi) 2.dp else 10.dp,
                                    topEnd = if (isAi) 10.dp else 2.dp,
                                    bottomStart = 10.dp, bottomEnd = 10.dp
                                )
                            )
                            .padding(9.dp)
                    ) {
                        Text(msg.text, fontFamily = OutfitFontFamily, fontSize = 12.sp, color = Color.White, lineHeight = 17.sp)
                    }
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DashboardTokens.CardBg)
                .drawBehind {
                    drawLine(
                        DashboardTokens.Rim,
                        Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()
                    )
                }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Ask anything...", fontSize = 12.sp, color = DashboardTokens.White35) },
                modifier = Modifier.weight(1f).height(46.dp),
                textStyle = TextStyle(fontFamily = OutfitFontFamily, fontSize = 12.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = DashboardTokens.CardBg2,
                    unfocusedContainerColor = DashboardTokens.CardBg2,
                    focusedBorderColor      = DashboardTokens.RedHot,
                    unfocusedBorderColor    = DashboardTokens.Rim2
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
            Spacer(Modifier.width(7.dp))
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        Brush.linearGradient(listOf(DashboardTokens.RedHot, DashboardTokens.RedDeep)),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        if (input.isNotBlank()) {
                            chatVm.sendMessage(input)
                            input = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  MORE SCREEN
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun MoreScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val email = auth.currentUser?.email ?: "—"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Settings", fontFamily = OutfitFontFamily, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(Modifier.height(2.dp))
            Text("Manage your preferences", fontFamily = OutfitFontFamily, fontSize = 11.sp, color = DashboardTokens.White60)
        }

        // Profile card — shows email, no username
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1A0005), DashboardTokens.CardBg)),
                        RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, DashboardTokens.Rim, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.linearGradient(listOf(DashboardTokens.RedDeep, DashboardTokens.RedHot)),
                                CircleShape
                            )
                            .border(2.dp, DashboardTokens.Rim2, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Account", fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(email, fontFamily = OutfitFontFamily, fontSize = 11.sp, color = DashboardTokens.White60, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Member", fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.RedHot, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item { MoreGroupLabel("Account") }
        item { MoreRow(Icons.Rounded.Person,       "Edit Profile",         "Update your details") }
        item { MoreRow(Icons.Rounded.Notifications,"Notifications",        "Manage alerts & sounds") }
        item { MoreRow(Icons.Rounded.Security,     "Privacy & Security",   "Two-factor, data settings") }

        item { MoreGroupLabel("Vehicle") }
        item { MoreRow(Icons.Rounded.LocalShipping,"My Vehicle",           "Manage vehicle details") }
        item { MoreRow(Icons.Rounded.Build,        "Service Records",      "View maintenance logs") }
        item { MoreRow(Icons.Rounded.LocationOn,   "Emergency Contacts",   "SOS contact list") }

        item { MoreGroupLabel("App") }
        item { MoreRow(Icons.Rounded.Info,         "About RescueLink",     "Version 1.0.0") }
        item { MoreRow(Icons.Rounded.ContactSupport,"Help & Support",      "FAQs and contact us") }

        item { Spacer(Modifier.height(4.dp)) }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DashboardTokens.RedDim, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x38E8001D), RoundedCornerShape(12.dp))
                    .clickable(onClick = onLogout)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sign Out", fontFamily = OutfitFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DashboardTokens.RedHot)
            }
        }
    }
}

@Composable
private fun MoreGroupLabel(title: String) {
    Text(
        title,
        fontFamily = OutfitFontFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = DashboardTokens.White35,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun MoreRow(icon: ImageVector, title: String, sub: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DashboardTokens.CardBg, RoundedCornerShape(12.dp))
            .border(1.dp, DashboardTokens.Rim, RoundedCornerShape(12.dp))
            .clickable {}
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(DashboardTokens.CardBg2, RoundedCornerShape(9.dp))
                    .border(1.dp, DashboardTokens.Rim2, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = DashboardTokens.White60, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontFamily = OutfitFontFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(sub,   fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.White35)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = DashboardTokens.White35, modifier = Modifier.size(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  BACKGROUND
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun DashboardBackground() {
    val inf = rememberInfiniteTransition(label = "bg")
    val a1 by inf.animateFloat(0.04f, 0.11f, infiniteRepeatable(tween(7000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "a1")
    val a2 by inf.animateFloat(0.04f, 0.09f, infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "a2")
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.offset((-70).dp, (-140).dp).size(400.dp).graphicsLayer { alpha = a1 }
            .background(Brush.radialGradient(listOf(DashboardTokens.RedHot, Color.Transparent))))
        Box(Modifier.align(Alignment.BottomEnd).offset(70.dp, 70.dp).size(280.dp).graphicsLayer { alpha = a2 }
            .background(Brush.radialGradient(listOf(DashboardTokens.RedDeep, Color.Transparent))))
        Box(Modifier.fillMaxSize().drawBehind {
            val c = Color(0x05E8001D); val cell = 44.dp.toPx()
            var y = 0f; while (y < size.height) { drawLine(c, Offset(0f, y), Offset(size.width, y), 1f); y += cell }
            var x = 0f; while (x < size.width) { drawLine(c, Offset(x, 0f), Offset(x, size.height), 1f); x += cell }
        })
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun DashboardTopBar(initials: String, onLogout: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "topbar")
    val dotAlpha by inf.animateFloat(1f, 0f, infiniteRepeatable(tween(1400), RepeatMode.Restart), "dot")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xD9080808))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "RescueLink",
                fontFamily = BebasNeueFontFamily,
                fontSize = 18.sp,
                letterSpacing = 1.5.sp,
                style = TextStyle(brush = Brush.linearGradient(listOf(Color.White, DashboardTokens.RedGlow)))
            )
            Text(
                "EMERGENCY ROAD ASSIST",
                fontFamily = OutfitFontFamily,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = DashboardTokens.White35,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(Modifier.weight(1f))

        // Live badge
        Row(
            modifier = Modifier
                .background(DashboardTokens.RedDim, RoundedCornerShape(100.dp))
                .border(1.dp, Color(0x38E8001D), RoundedCornerShape(100.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(5.dp).graphicsLayer { alpha = dotAlpha }.background(DashboardTokens.RedHot, CircleShape))
            Spacer(Modifier.width(4.dp))
            Text("Live", fontFamily = OutfitFontFamily, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.RedHot)
        }
        Spacer(Modifier.width(7.dp))

        // Notifications
        Box(
            modifier = Modifier.size(32.dp)
                .background(DashboardTokens.CardBg2, RoundedCornerShape(9.dp))
                .border(1.dp, DashboardTokens.Rim2, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Notifications, null, tint = DashboardTokens.White60, modifier = Modifier.size(16.dp))
            Box(Modifier.align(Alignment.TopEnd).offset((-5).dp, 5.dp).size(5.dp)
                .background(DashboardTokens.RedHot, CircleShape)
                .border(1.dp, DashboardTokens.BlackCore, CircleShape))
        }
        Spacer(Modifier.width(6.dp))

        // Avatar — shows initials from Firebase auth, no username text
        Box(
            modifier = Modifier.size(32.dp)
                .background(Brush.linearGradient(listOf(DashboardTokens.RedDeep, DashboardTokens.RedHot)), CircleShape)
                .border(1.5.dp, DashboardTokens.Rim2, CircleShape)
                .clickable(onClick = onLogout),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  SHARED HELPERS
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun SectionHeader(title: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title,  fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(action, fontFamily = OutfitFontFamily, fontSize = 10.sp, color = DashboardTokens.RedHot, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Rounded.History, null, tint = DashboardTokens.White35, modifier = Modifier.size(28.dp))
        Text(title,    fontFamily = OutfitFontFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DashboardTokens.White60)
        Text(subtitle, fontFamily = OutfitFontFamily, fontSize = 11.sp, color = DashboardTokens.White35)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  BOTTOM NAV
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun DashboardBottomNav(currentRoute: String, onRoute: (String) -> Unit) {
    NavigationBar(
        containerColor = DashboardTokens.CardBg,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(58.dp)
            .drawBehind {
                drawLine(DashboardTokens.Rim, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
            }
    ) {
        val items = listOf(
            Triple("home",      Icons.Rounded.Home,                  "Home"),
            Triple("map",       Icons.Rounded.Map,                   "Map"),
            Triple("community", Icons.Rounded.People,                "Community"),
            Triple("chat",      Icons.AutoMirrored.Rounded.Chat,     "AI Chat"),
            Triple("more",      Icons.Rounded.Menu,                  "More")
        )
        items.forEach { (route, icon, label) ->
            val selected = currentRoute == route
            NavigationBarItem(
                selected = selected,
                onClick  = { onRoute(route) },
                icon     = { Icon(icon, label, modifier = Modifier.size(19.dp)) },
                label    = { Text(label, fontFamily = OutfitFontFamily, fontSize = 8.sp) },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor   = DashboardTokens.RedHot,
                    unselectedIconColor = DashboardTokens.White35,
                    selectedTextColor   = DashboardTokens.RedHot,
                    unselectedTextColor = DashboardTokens.White35,
                    indicatorColor      = DashboardTokens.RedDim
                )
            )
        }
    }
}
