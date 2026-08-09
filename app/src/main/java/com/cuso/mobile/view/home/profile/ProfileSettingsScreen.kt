package com.cuso.mobile.view.home.profile

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.TextSecondary

// ── Data model for each settings row ──
private data class SettingsMenuItem(
    val icon: Int,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

/**
 *   NOTE ON THE SLIDE ANIMATION:
 * This screen used to wrap itself in its own `AnimatedVisibility` (slide-in on enter,
 * slide-out + 300ms delay before calling onClose() on exit). That created a DOUBLE
 * animation: this screen's own exit-slide would finish first (revealing blank/white
 * space because HomeScreen's outer `AnimatedContent` hadn't swapped screens yet),
 * and only after the 300ms delay would onClose() fire and trigger the OUTER
 * AnimatedContent transition in HomeScreen.kt — causing that white flash in between.
 *
 * HomeScreen.kt already animates every screen swap via a single `AnimatedContent`
 * (slide + fade, 300ms). So this screen now behaves like every other screen in the
 * app (CreateLeadScreen, BranchSettingsScreen, etc.) — a plain composable with no
 * animation of its own — and lets that one outer transition drive the slide.
 * That removes the double-transition gap and matches the same smooth, single-source
 * animation used everywhere else in the app.
 */
@Suppress("UNUSED_PARAMETER")
@SuppressLint("ContextCastToActivity")
@Composable
fun ProfileSettingsScreen(
    onClose: () -> Unit,
    onGoToProfile: () -> Unit = {},
    onOrganizationSetup: () -> Unit = {},
    onBranchManagement: () -> Unit = {},
    onDepartment: () -> Unit = {},
    onTeams: () -> Unit = {},
    onDesignation: () -> Unit = {},
    onHelpSupport: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val authViewModel: Authenticate = hiltViewModel(
        LocalContext.current as ComponentActivity   //   இதை சேருங்க
    )
    val userEntity by authViewModel.user.collectAsStateWithLifecycle()

    val firstName = userEntity?.firstName.orEmpty()
    val lastName = userEntity?.lastName.orEmpty()
    val fullName = "$firstName $lastName".trim().ifBlank { "User" }
    val email = userEntity?.email.orEmpty()
    val profilePicture = userEntity?.profilePicture

    val organizationItems = listOf(
        SettingsMenuItem(
            icon = R.drawable.ic_building,
            iconBg = Color(0xFFEDE9FE),
            iconTint = Color(0xFF6C4FF6),
            title = "Organization Setup",
            subtitle = "Manage organization details, billing & settings",
            onClick = onOrganizationSetup
        )
    )

    val managementItems = listOf(
        SettingsMenuItem(
            icon = R.drawable.ic_location,
            iconBg = Color(0xFFDCEAFE),
            iconTint = Color(0xFF3B82F6),
            title = "Branch Management",
            subtitle = "Add, edit and manage your branches",
            onClick = onBranchManagement
        ),
        SettingsMenuItem(
            icon = R.drawable.ic_building,
            iconBg = Color(0xFFD1FAE5),
            iconTint = Color(0xFF10B981),
            title = "Department & Teams",
            subtitle = "Manage departments in your organization",
            onClick = onDepartment
        ),

        SettingsMenuItem(
            icon = R.drawable.ic_code,
            iconBg = Color(0xFFFFEDD5),
            iconTint = Color(0xFFF97316),
            title = "Designation",
            subtitle = "Manage job titles and roles",
            onClick = onDesignation
        )
    )

    val supportItems = listOf(
        SettingsMenuItem(
            icon = R.drawable.ic_info,
            iconBg = Color(0xFFDCEAFE),
            iconTint = Color(0xFF3B82F6),
            title = "Help & Support",
            subtitle = "FAQs, guides and contact support",
            onClick = onHelpSupport
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7FB))
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onClose() }
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Profile card ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (!profilePicture.isNullOrBlank()) {
                        AsyncImage(
                            model = profilePicture,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B3BF9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = buildString {
                                    firstName.firstOrNull()?.let { append(it.uppercaseChar()) }
                                    lastName.firstOrNull()?.let { append(it.uppercaseChar()) }
                                },
                                color = whiteBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(fullName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Spacer(Modifier.height(2.dp))
                        Text("Administrator", fontSize = 13.sp, color = Color(0xFF6B7280))
                        Spacer(Modifier.height(2.dp))
                        Text(email, fontSize = 13.sp, color = Color(0xFF6B7280))

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onGoToProfile() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF374151),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Go to Profile", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ── ORGANIZATION ──
            item { SettingsSectionLabel("ORGANIZATION") }
            item { SettingsCardGroup(organizationItems) }

            // ── MANAGEMENT ──
            item { SettingsSectionLabel("MANAGEMENT") }
            item { SettingsCardGroup(managementItems) }

            // ── SUPPORT & HELP ──
            item { SettingsSectionLabel("SUPPORT & HELP") }
            item { SettingsCardGroup(supportItems) }

            // ── Log Out ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(14.dp))
                        .clickable { onLogout() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Log Out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
                        Text("Sign out from CUSO Tailor", fontSize = 12.sp, color = Color(0xFFEF4444).copy(alpha = 0.7f))
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF9CA3AF),
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsCardGroup(items: List<SettingsMenuItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(24.dp)),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { item.onClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(item.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon( painterResource( item.icon), contentDescription = null, tint = item.iconTint, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Text(item.subtitle, fontSize = 11.sp, color = TextSecondary)
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (index != items.lastIndex) {
                HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(start = 66.dp))
            }
        }
    }
}