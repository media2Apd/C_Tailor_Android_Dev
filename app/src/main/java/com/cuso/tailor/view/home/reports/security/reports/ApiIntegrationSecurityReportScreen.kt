@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.security.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.report.SecurityApiIntegrationReportItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.purpleBg
import com.cuso.tailor.ui.theme.purpleText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.home.reports.ReportCardFooterCaption
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun ApiIntegrationSecurityReportScreen(
    onClose: () -> Unit,
    onViewDetail: (SecurityApiIntegrationReportItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.securityApiIntegrationSummary.collectAsState()
    val pagingState by viewModel.securityApiIntegrationsPagingState.collectAsState()
    val filteredList by viewModel.filteredSecurityApiIntegrationsList.collectAsState()
    val searchQuery by viewModel.securityApiIntegrationSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Connection Status",
                    options = listOf(
                        FilterOption("Connected", "Connected"),
                        FilterOption("Error", "Error"),
                        FilterOption("Disconnected", "Disconnected")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Category",
                    options = listOf(
                        FilterOption("REST API", "REST API"),
                        FilterOption("Webhook", "Webhook")
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
        viewModel.loadSecurityApiIntegrationsReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL INTEGRATIONS",
                value = (summary?.totalIntegrations ?: 0).toString(),
                subtitle = "All configured keys",
                icon = Icons.Default.Hub,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "CONNECTED & HEALTHY",
                value = (summary?.connectedActiveCount ?: 0).toString(),
                subtitle = "Live connections",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "FAILED / DISRUPTED",
                value = (summary?.failedErrorCount ?: 0).toString(),
                subtitle = "Requires attention",
                icon = Icons.Default.ErrorOutline,
                valueColor = redText,
                subtitleColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "DELIVERY SUCCESS SLA",
                value = summary?.avgDeliverySuccessRate ?: "0%",
                subtitle = "Avg latency: ${summary?.avgLatency ?: "0ms"}",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "API & Integration Security Report",
        subtitle = null,
        sectionTitle = "API & Webhook Gateway Directory",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSecurityApiIntegrationSearchQueryChange,
        searchPlaceholder = "Search by integration, category or owner...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Connection Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val categories = updated.find { it.title == "Category" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applySecurityApiIntegrationFilters(statuses, categories)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearSecurityApiIntegrationFilters()
        },
        onLoadMore = viewModel::loadMoreSecurityApiIntegrations,
        onShareClick = { DynamicIslandManager.showSuccess("API security report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("API security report export started") },
        onClose = onClose,
        emptyMessage = "No API integrations found."
    ) { item ->
        val (statusTextColor, statusBgColor) = when (item.status.lowercase()) {
            "connected", "active" -> greentext to greenBg
            "error", "failed", "disconnected" -> redText to redBg
            else -> yellowText to yellowBg
        }
        val lastSyncDate = viewModel.formatDate(item.lastSync ?: item.createdAt)

        DataCard(
            item = item,
            title = item.name.ifBlank { "--" },
            subtitle = "${item.category}  •  ${item.authType}",
            topBadgeText = item.status.ifBlank { "--" },
            topBadgeTextColor = statusTextColor,
            topBadgeBgColor = statusBgColor,
            topBadgeInline = true,
            actions = listOf(
                MenuAction(
                    label = "View Details",
                    icon = Icons.Default.Visibility,
                    onClick = { onViewDetail(item) }
                )
            ),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Integration Category") {
                        Text(
                            text = item.category.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Connection", item.status.ifBlank { "--" }, valueColor = statusTextColor),
                                ReportGridCell("Rate Limit Usage", item.rateLimitUsage.ifBlank { "--" }),
                                ReportGridCell("Success Rate", item.deliverySuccessRate.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Latency", item.latency.ifBlank { "--" }),
                                ReportGridCell("Admin Owner", item.createdBy.ifBlank { "--" }),
                                ReportGridCell("Last Sync", lastSyncDate)
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Last audited: $lastSyncDate")
                }
            }
        )
    }
}