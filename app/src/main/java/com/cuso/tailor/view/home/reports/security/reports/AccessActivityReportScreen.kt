@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.security.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.report.SecurityAccessReportItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun AccessActivityReportScreen(
    onClose: () -> Unit,
    onAuditLogClick: (SecurityAccessReportItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.securityAccessSummary.collectAsState()
    val pagingState by viewModel.securityAccessPagingState.collectAsState()
    val filteredList by viewModel.filteredSecurityAccessList.collectAsState()
    val searchQuery by viewModel.securityAccessSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Severity",
                    options = listOf(
                        FilterOption("LOW", "Low"),
                        FilterOption("MEDIUM", "Medium"),
                        FilterOption("HIGH", "High"),
                        FilterOption("CRITICAL", "Critical")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Security Flag",
                    options = listOf(
                        FilterOption("suspicious_only", "Security Flagged Only")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }

    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        viewModel.loadSecurityAccessReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL ACCESS EVENTS",
                value = (summary?.totalAccessEvents ?: 0).toString(),
                subtitle = "All login attempts",
                icon = Icons.Default.PersonOutline,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "SUCCESSFUL LOGINS",
                value = (summary?.successfulLogins ?: 0).toString(),
                subtitle = "Authorized sessions",
                icon = Icons.Default.HowToReg,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "FAILED LOGINS",
                value = (summary?.failedLogins ?: 0).toString(),
                subtitle = "Password/auth errors",
                icon = Icons.Default.LockOpen,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "BREACH / ESCALATION",
                value = (summary?.unauthorizedBreaches ?: 0).toString(),
                subtitle = "Suspicious IP / privilege",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Access & Permission Activity Report",
        subtitle = "Chronological audit of member authentications, privilege boundary transitions, failed login attempts, and suspicious access",
        sectionTitle = "Authentication & Activity Log",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSecurityAccessSearchQueryChange,
        searchPlaceholder = "Search by account, IP, event or action...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val severities = updated.find { it.title == "Severity" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val suspiciousOnly = updated.find { it.title == "Security Flag" }
                ?.options?.any { it.id == "suspicious_only" && it.isSelected } ?: false
            viewModel.applySecurityAccessFilters(severities, suspiciousOnly)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearSecurityAccessFilters()
        },
        onLoadMore = viewModel::loadMoreSecurityAccess,
        onShareClick = { DynamicIslandManager.showSuccess("Access activity report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Access activity report export started") },
        onClose = onClose,
        emptyMessage = "No access activity records found."
    ) { item ->
        val badgeTextColor = if (item.isSuspicious) redText else greentext
        val badgeBgColor = if (item.isSuspicious) redBg else greenBg
        val cornerRadius = tokens.cardCornerRadius * 0.65f
        val borderThickness = tokens.extraPadding * 0.1f

        DataCard(
            item = item,
            title = item.user.ifBlank { "--" },
            subtitle = "Identity / User Account",
            topBadgeText = item.statusBadge.ifBlank { "Normal Operation" },
            topBadgeTextColor = badgeTextColor,
            topBadgeBgColor = badgeBgColor,
            topBadgeInline = true,
            actions = listOf(
                MenuAction(
                    label = "Audit Log",
                    icon = Icons.Default.Visibility,
                    onClick = { onAuditLogClick(item) }
                )
            ),
            onClick = onAuditLogClick,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Activity Type") {
                        Text(
                            text = item.eventType.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(Primary_background)
                            .border(borderThickness, grey_border, RoundedCornerShape(cornerRadius))
                            .padding(tokens.extraPadding * 1.2f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                            ) {
                                Text(
                                    text = "Origin IP",
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.ipAddress.ifBlank { "--" },
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                            ) {
                                Text(
                                    text = "Location",
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.location.ifBlank { "--" },
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                            ) {
                                Text(
                                    text = "Detail / Action",
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.actionDetail.ifBlank { "--" },
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Safety Status",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                            Text(
                                text = item.statusBadge.ifBlank { "--" },
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = badgeTextColor
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)) {
                            Text(
                                text = "Client App",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                            Text(
                                text = item.deviceAgent.ifBlank { "--" },
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(color = dividerColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timestamp: ${viewModel.formatDate(item.createdAt)}",
                            fontSize = tokens.caption,
                            color = mutedText
                        )

                        Row(
                            modifier = Modifier.clickable { onAuditLogClick(item) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                        ) {
                            Text(
                                text = "Audit log",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Audit log",
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize * 0.8f)
                            )
                        }
                    }
                }
            }
        )
    }
}