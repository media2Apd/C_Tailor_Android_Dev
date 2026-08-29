package com.cuso.mobile.view.home.profile_settings.all_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.StatusBadge
import com.cuso.mobile.view.composable.StatusBadgeVariant
import com.cuso.mobile.view.composable.TitleBar

private val TextDark = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val CardBorderColor = Color(0xFFE5E7EB)

@Composable
fun SettingsOverviewScreen(
    onClose: () -> Unit,
    onNavigateToOrganizationSettings: () -> Unit,
    onNavigateToModuleSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // --- Title Bar ---
        TitleBar(title = "Settings", onClose = onClose)
        HorizontalDivider(color = grey_border)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // --- Header Badge (System Administration) ---
            StatusBadge(
                text = "System Administration",
                variant = StatusBadgeVariant.PRIMARY,
                cornerRadius = 6.dp,
                horizontalPadding = 10.dp,
                verticalPadding = 4.dp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            // --- Header Text ---
            Text(
                text = "Settings Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Choose the area you want to configure.",
                fontSize = 13.sp,
                color = TextMuted
            )

            Spacer(Modifier.height(20.dp))

            // --- Card 1: Organization Settings ---
            SettingsOverviewCard(
                icon = R.drawable.home,
                iconBg = Color(0xFFEDE9FE),
                iconTint = Color(0xFF4F46E5),
                title = "Organization Settings",
                description = "Configure company-wide information, structures, access and regional preferences used across CUSO.",
                tags = listOf("Organization Profile", "+ More"),
                actionText = "Explore Organization Settings →",
                onClick = onNavigateToOrganizationSettings
            )

            Spacer(Modifier.height(16.dp))

            // --- Card 2: Module Settings ---
            SettingsOverviewCard(
                icon = R.drawable.box,
                iconBg = Color(0xFFEDE9FE),
                iconTint = Color(0xFF4F46E5),
                title = "Module Settings",
                description = "Configure rules, preferences and operational behavior for each CUSO module individually.",
                tags = listOf("Sales", "Finance", "Inventory", "+ More"),
                actionText = "Explore Module Settings →",
                onClick = onNavigateToModuleSettings
            )
        }
    }
}

@Composable
private fun SettingsOverviewCard(
    icon: Int,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String,
    tags: List<String>,
    actionText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(whiteBg)
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        // Icon and Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
        }

        Spacer(Modifier.height(12.dp))

        // Description
        Text(
            text = description,
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(14.dp))

        // Tags List using StatusBadge
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tags.forEach { tag ->
                StatusBadge(
                    text = tag,
                    variant = StatusBadgeVariant.DEFAULT
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action Link
        Text(
            text = actionText,
            fontSize = 15.sp,
            color = Primary
        )
    }
}