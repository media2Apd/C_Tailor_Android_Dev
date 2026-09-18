@file:Suppress("unused")

package com.cuso.tailor.view.home.profile_settings.all_settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar

// ─────────────────────────────────────────────────────────────
// SCREEN: ORGANIZATION SETTINGS SCREEN
// ─────────────────────────────────────────────────────────────

@Composable
fun OrganizationSettingsScreen(
    onClose: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    // Accordion expand/collapse states
    var isTaxesExpanded by remember { mutableStateOf(false) }
    var isSubscriptionExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Organization Settings",
                onClose = onClose
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
        ) {
            // Search and Filter Bar Component
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Customers...",
                showFilterIcon = true,
                onFilterClick = { /* Handle Filter Action */ },
                height = tokens.fieldHeight * 1.15f
            )

            HorizontalDivider(color = grey_border)

            // Scrollable Menu List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding)
                    .padding(top = 14.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Navigation items
                // Example inside OrganizationSettingsScreen.kt
                SettingsNavCard(
                    title = "Organization Profile",
                    tokens = tokens,
                    onClick = { onNavigate("organization_profile") }
                )

                SettingsNavCard(
                    title = "Business Setup",
                    tokens = tokens,
                    onClick = { onNavigate("business_setup") }
                )

                SettingsAccordionCard(
                    title = "Taxes & GST",
                    isExpanded = isTaxesExpanded,
                    tokens = tokens,
                    onHeaderClick = { isTaxesExpanded = !isTaxesExpanded }
                ) {
                    SettingsSubItemRow(title = "Tax Rates", tokens = tokens) { onNavigate("tax_rates") }
                    HorizontalDivider(color = BorderGray)
                    SettingsSubItemRow(title = "GST Settings", tokens = tokens) { onNavigate("gst_settings") }
                }

                SettingsNavCard(
                    title = "Branch Management",
                    tokens = tokens,
                    onClick = { onNavigate("branch_management") }
                )

                SettingsNavCard(
                    title = "Department & Teams",
                    tokens = tokens,
                    onClick = { onNavigate("department_teams") }
                )

                SettingsNavCard(
                    title = "Designation",
                    tokens = tokens,
                    onClick = { onNavigate("designation") }
                )

                SettingsNavCard(
                    title = "Role",
                    tokens = tokens,
                    onClick = { onNavigate("role") }
                )

                SettingsNavCard(
                    title = "Opening Balance",
                    tokens = tokens,
                    onClick = { onNavigate("opening_balance") }
                )

                SettingsNavCard(
                    title = "Warehouse",
                    tokens = tokens,
                    onClick = { onNavigate("warehouse") }
                )

                // Manage Subscription Accordion
                SettingsAccordionCard(
                    title = "Manage Subcription",
                    isExpanded = isSubscriptionExpanded,
                    tokens = tokens,
                    onHeaderClick = { isSubscriptionExpanded = !isSubscriptionExpanded }
                ) {
                    val subscriptionItems = listOf(
                        "Overview",
                        "Current Plan",
                        "Usage & Limits",
                        "Compare Plans",
                        "Upgrade / Downgrade",
                        "Billing Cycle",
                        "Payment Methods",
                        "Billing Information",
                        "Invoices & Receipts",
                        "Add-ons",
                        "Usage Alerts",
                        "Renewal & Cancellation",
                        "Subscription History"
                    )

                    subscriptionItems.forEachIndexed { index, item ->
                        SettingsSubItemRow(
                            title = item,
                            tokens = tokens,
                            onClick = { onNavigate(item.lowercase().replace(" ", "_")) }
                        )
                        if (index < subscriptionItems.lastIndex) {
                            HorizontalDivider(color = BorderGray, thickness = 0.8.dp)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SINGLE NAVIGATION CARD
// ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsNavCard(
    title: String,
    tokens: AppDesignTokens,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight * 1.3f)
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .clickable { onClick() }
            .padding(horizontal = tokens.screenPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize * 0.9f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ACCORDION (EXPANDABLE) CONTAINER CARD
// ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsAccordionCard(
    title: String,
    isExpanded: Boolean,
    tokens: AppDesignTokens,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.fieldHeight * 1.3f)
                    .clickable { onHeaderClick() }
                    .padding(horizontal = tokens.screenPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = mutedText,
                    modifier = Modifier.size(tokens.iconSize * 1.1f)
                )
            }

            // Expandable Content Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = sectionBorder, thickness = 1.dp)
                    content()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ACCORDION SUB-ITEM ROW
// ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsSubItemRow(
    title: String,
    tokens: AppDesignTokens,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight * 1.15f)
            .clickable { onClick() }
            .padding(start = tokens.screenPadding * 1.8f, end = tokens.screenPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Normal,
            color = TextPrimary
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = mutedText,
            modifier = Modifier.size(tokens.iconSize * 0.9f)
        )
    }
}