package com.cuso.tailor.view.home.security.mobile_access.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.MobileSecurityAuditItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun SecurityAuditTabContent(
    viewModel: SecurityViewModel,
    onAuditClick: (MobileSecurityAuditItem) -> Unit
) {
    val tokens = LocalAppTokens.current
    val pagingState by viewModel.mobileAuditsPagingState.collectAsState()
    val searchQuery by viewModel.mobileAuditSearch.collectAsState()

    val filterDrawerState = rememberFilterDrawerState()

    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Event Type",
                options = listOf(
                    FilterOption("blocked_device", "Blocked Device"),
                    FilterOption("new_device", "New Device"),
                    FilterOption("secure_api_failure", "Secure API Failure"),
                    FilterOption("jailbreak_detected", "Jailbreak Detected"),
                    FilterOption("otp_brute_force", "OTP Brute Force"),
                    FilterOption("biometric_failure", "Biometric Failure")
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Severity",
                options = listOf(
                    FilterOption("low", "Low"),
                    FilterOption("medium", "Medium"),
                    FilterOption("high", "High"),
                    FilterOption("critical", "Critical")
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("open", "Open"),
                    FilterOption("resolved", "Resolved")
                ),
                isMultiSelect = false
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.setAuditSearch(it) },
                placeholder = "Search audit events...",
                showFilterIcon = true,
                filterCount = 0,
                onFilterClick = { filterDrawerState.open() }
            )

            if (!pagingState.isInitialLoading && pagingState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No events match your search", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Events appear here when mobile security activity is detected.", fontSize = tokens.caption, color = TextSecondary)
                    }
                }
            } else {
                PaginatedDataList(
                    state = pagingState,
                    onLoadMore = { viewModel.loadMoreMobileSecurityAudit() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding)
                ) { item ->
                    DataCard(
                        item = item,
                        title = item.eventType.replace('_', ' ').toTitleCase(),
                        headerContent = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    EventTypeIcon(item.eventType)
                                    Text(item.eventType.replace('_', ' ').toTitleCase(), fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                                SeverityBadge(item.severity)
                            }
                        },
                        actions = listOf(
                            MenuAction(label = "View", icon = Icons.Default.Visibility, onClick = { onAuditClick(item) })
                        ),
                        onClick = { onAuditClick(item) },
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("USER", fontSize = tokens.label, color = mutedText)
                                    Text(item.userEmail ?: item.userName ?: "—", fontSize = tokens.bodySmall, color = TextPrimary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("DEVICE & OS", fontSize = tokens.label, color = mutedText)
                                    Text("${item.deviceModel ?: "Unknown"} (${item.osInfo ?: "—"})", fontSize = tokens.bodySmall, color = TextPrimary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TIMESTAMP", fontSize = tokens.label, color = mutedText)
                                    Text(item.timestamp?.take(19)?.replace('T', ' ') ?: "—", fontSize = tokens.caption, color = TextSecondary)
                                }
                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(light_grey).padding(8.dp)) {
                                    Text(item.detailsSummary ?: "—", fontSize = tokens.caption, color = TextSecondary)
                                }
                            }
                        }
                    )
                }
            }
        }

        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Audits",
            sections = filterSections,
            onApply = { applied ->
                val evType = applied.find { it.title == "Event Type" }?.options?.find { it.isSelected }?.id
                val sev = applied.find { it.title == "Severity" }?.options?.find { it.isSelected }?.id
                val stat = applied.find { it.title == "Status" }?.options?.find { it.isSelected }?.id
                viewModel.applyAuditFilters(evType, sev, stat)
            },
            onClearAll = { viewModel.applyAuditFilters(null, null, null) }
        )
    }
}