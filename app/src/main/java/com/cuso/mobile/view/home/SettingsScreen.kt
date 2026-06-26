// com/cuso/mobile/view/home/SettingsScreen.kt

package com.cuso.mobile.view.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Pair(Icons.Outlined.Badge, "Profile"),
        Pair(Icons.Outlined.Business, "Localization")
    )

    // Shared token from auth viewmodel — used by both tabs
    val authViewModel: Authenticate = hiltViewModel()
    val tokensEntity by authViewModel.tokens.collectAsStateWithLifecycle()
    val token = tokensEntity?.accessToken ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ── Tab Row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .padding(12.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, (icon, label) ->
                val isSelected = selectedTab == index
                Row(
                    modifier = Modifier
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .background(
                            if (isSelected) Color.White else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedTab = index }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.Black else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color.Gray
                    )
                }
            }
        }

        // ── Tab Content ──
        when (selectedTab) {
            0 -> ProfileTab(token = token)
            1 -> LocalizationTab(token = token)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ProfileTab - Fetches data from API via ProfileViewModel
// ─────────────────────────────────────────────────────────────
@Composable
fun ProfileTab(
    token: String,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(token) {
        if (token.isNotEmpty()) viewModel.loadOrganization(token)
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is ProfileUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = Color.Red)
            }
        }

        is ProfileUiState.Success -> {
            val org = state.data.organization
            val stats = state.data.stats

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // ── Header ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Organization Information",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Text(
                                        "Core details about your organization",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                                OutlinedButton(
                                    onClick = { },
                                    shape = RoundedCornerShape(8.dp),
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit",
                                        modifier = Modifier.size(14.dp), tint = Color.Black)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit", color = Color.Black, fontSize = 14.sp)
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // ── Logo ── (Organization has organizationPicture, not logoUrl)
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .border(1.5.dp, Color.LightGray, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (org.organizationPicture.isNullOrBlank()) {
                                    Text("Upload Logo", color = Color.LightGray, fontSize = 13.sp)
                                } else {
                                    AsyncImage(
                                        model = org.organizationPicture,
                                        contentDescription = "Organization Logo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Spacer(Modifier.height(16.dp))

                            // ── Fields matched to real Organization model ──
                            val rows = listOf(
                                "Organization Name" to org.name,
                                "Organization Type" to org.orgType,
                                "Business Type" to org.businessType,
                                "Business ID" to org.businessId,
                                "Email" to org.email,
                                "Mobile" to org.mobile,
                                "Plan Status" to org.subscription.status,
                                "Status" to org.status
                            )

                            rows.forEachIndexed { index, (label, value) ->
                                OrgInfoRow(label, value.ifBlank { "-" })
                                if (index != rows.lastIndex) {
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))
                                } else {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            if (stats == null) {
                                Text("No active subscription", fontSize = 14.sp, color = Color.Gray)
                            } else {
                                val plan = (uiState as? ProfileUiState.Success)?.data?.organization?.plan

                                Text(
                                    "Subscription Usage",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {
                                    SubscriptionRing(
                                        modifier=Modifier.weight(1f),
                                        label = "Orders",
                                        used = 0,
                                        limit = plan?.orderLimit ?: 100
                                    )
                                    SubscriptionRing(
                                        modifier=Modifier.weight(1f),
                                        label = "Employees",
                                        used = org.activeMembers,
                                        limit = plan?.employeeLimit ?: org.totalMembers.coerceAtLeast(1)
                                    )
                                    SubscriptionRing(
                                        modifier=Modifier.weight(1f),
                                        label = "Branches",
                                        used = stats.totalBranches,
                                        limit = plan?.branchLimit ?: stats.totalBranches.coerceAtLeast(1)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionRing(
    modifier: Modifier,
    label: String,
    used: Int,
    limit: Int
) {
    val pct = if (limit > 0) (used.toFloat() / limit) else 0f
    val remaining = (limit - used).coerceAtLeast(0)
    val ringColor = when {
        pct >= 1f    -> Color(0xFFE24B4A)
        pct >= 0.75f -> Color(0xFFEDA100)
        else         -> Color(0xFF2A78D6)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
        // ← no modifier here
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(90.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 10.dp.toPx()
                val radius = (size.minDimension - stroke) / 2
                val topLeft = Offset(stroke / 2, stroke / 2)
                val arcSize = Size(radius * 2, radius * 2)
                drawArc(
                    color = Color(0xFFE1E0D9),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                if (pct > 0f) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * pct.coerceAtMost(1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Text(
                text = "${(pct * 100).roundToInt()}%",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111827)
            )
        }
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
        Text(text = "$used / $limit", fontSize = 12.sp, color = Color(0xFF6B7280))
        Text(text = "$remaining remaining", fontSize = 12.sp, color = Color(0xFF9CA3AF))
    }
}

// Reusable label/value row — used by both ProfileTab and LocalizationTab
@Composable
fun OrgInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

// ─────────────────────────────────────────────────────────────
// LocalizationTab - Reuses ProfileViewModel instead of
// instantiating AuthRepository manually (that won't compile
// with Hilt-injected dependencies)
// ─────────────────────────────────────────────────────────────
@Composable
fun LocalizationTab(
    token: String,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(token) {
        if (token.isNotEmpty()) viewModel.loadOrganization(token)
    }

    val isLoading = uiState is ProfileUiState.Loading
    val org = (uiState as? ProfileUiState.Success)?.data?.organization
    val settings = org?.settings
    val errorMessage = (uiState as? ProfileUiState.Error)?.message

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Localization & Address Card ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Localization & Address",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                "Location and regional settings",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                        OutlinedButton(
                            onClick = { },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit",
                                modifier = Modifier.size(14.dp), tint = Color.Black)
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", color = Color.Black, fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(Modifier.height(16.dp))

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        errorMessage != null -> {
                            Text(errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 16.dp))
                        }
                        settings != null -> {
                            val rows = listOf(
                                "Country" to settings.country,
                                "State" to settings.state,
                                "City" to settings.city,
                                "Pincode" to settings.pincode,
                                "Address" to settings.address
                            )
                            rows.forEachIndexed { index, (label, value) ->
                                OrgInfoRow(label, value.ifBlank { "-" })
                                if (index != rows.lastIndex) {
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                        else -> {
                            Text("No data available", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                }
            }
        }

        // ── Regional Settings Card ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Regional Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Timezone, currency and language", fontSize = 13.sp, color = Color.Gray)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(Modifier.height(16.dp))

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        settings != null -> {
                            val rows = listOf(
                                "Timezone" to settings.timezone,
                                "Currency" to settings.currency,
                                "Language" to settings.language
                            )
                            rows.forEachIndexed { index, (label, value) ->
                                OrgInfoRow(label, value.ifBlank { "-" })
                                if (index != rows.lastIndex) {
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                        else -> {
                            Text("No data available", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                }
            }
        }

        // ── Portal Info Card ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Portal Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Portal and company details", fontSize = 13.sp, color = Color.Gray)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(Modifier.height(16.dp))

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        org != null -> {
                            OrgInfoRow("Portal Name", settings?.portalName ?: "-")
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Spacer(Modifier.height(16.dp))

                            val totalMembers = org.totalMembers
                            val size = when {
                                totalMembers <= 10 -> "1-10 employees"
                                totalMembers <= 50 -> "11-50 employees"
                                totalMembers <= 200 -> "51-200 employees"
                                totalMembers <= 500 -> "201-500 employees"
                                else -> "500+ employees"
                            }
                            OrgInfoRow("Company Size", size)
                        }
                        else -> {
                            Text("No data available", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}