// com/cuso/mobile/view/home/SettingsScreen.kt

package com.cuso.mobile.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
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

                            Text(
                                "Subscription Usage",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Spacer(Modifier.height(8.dp))

                            if (stats == null) {
                                Text("No active subscription", fontSize = 14.sp, color = Color.Gray)
                            } else {
                                Text("Branches: ${stats.totalBranches}", fontSize = 14.sp, color = Color.Gray)
                                Text("Departments: ${stats.totalDepartments}", fontSize = 14.sp, color = Color.Gray)
                                Text("Employees: ${stats.totalEmployees}", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
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