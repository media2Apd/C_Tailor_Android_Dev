
@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.hr.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
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
import com.cuso.tailor.model.report.PayrollRunRecordDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
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
fun PayrollRunReportScreen(
    onClose: () -> Unit,
    onViewDetail: (PayrollRunRecordDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val analytics by viewModel.payrollAnalytics.collectAsState()
    val pagingState by viewModel.payrollPagingState.collectAsState()
    val filteredList by viewModel.filteredPayrollList.collectAsState()
    val searchQuery by viewModel.payrollSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Run Status",
                    options = listOf(
                        FilterOption("draft", "Draft"),
                        FilterOption("calculating", "Calculating"),
                        FilterOption("calculated", "Calculated"),
                        FilterOption("approved", "Approved"),
                        FilterOption("paid", "Paid"),
                        FilterOption("cancelled", "Cancelled")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Pay Frequency",
                    options = listOf(
                        FilterOption("monthly", "Monthly"),
                        FilterOption("weekly", "Weekly"),
                        FilterOption("biweekly", "Biweekly")
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
        viewModel.loadPayrollRunReport()
    }

    val summary = analytics?.payrollRunSummary
    val metrics = remember(summary) {
        val draftInProgress = (summary?.draftRuns ?: 0) + (summary?.calculatingRuns ?: 0)
        listOf(
            ReportMetricSpec(
                label = "TOTAL PAYROLL RUNS",
                value = (summary?.totalRuns ?: 0).toString(),
                subtitle = "All executions",
                icon = Icons.Default.AccountBalanceWallet,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "EMPLOYEES PROCESSED",
                value = (summary?.totalEmployees ?: 0).toString(),
                subtitle = "${summary?.pieceRateEmployees ?: 0} piece-rate workers",
                icon = Icons.Default.Group,
                valueColor = purpleText,
                subtitleColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "DISBURSED & PAID",
                value = (summary?.paidRuns ?: 0).toString(),
                subtitle = "${summary?.approvedRuns ?: 0} approved runs",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "DRAFT / IN PROGRESS",
                value = draftInProgress.toString(),
                subtitle = "Pending final approval",
                icon = Icons.Default.Schedule,
                valueColor = yellowText,
                subtitleColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Payroll Run Audit & Analytics Report",
        subtitle = "AUDIT CONSOLE",
        sectionTitle = "Payroll Run Directory",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onPayrollSearchQueryChange,
        searchPlaceholder = "Search by period, cadence or status...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Run Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val frequencies = updated.find { it.title == "Pay Frequency" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyPayrollFilters(statuses, frequencies)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearPayrollFilters()
        },
        onLoadMore = viewModel::loadMorePayrollRuns,
        onShareClick = { DynamicIslandManager.showSuccess("Payroll audit report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Payroll audit report export started") },
        onClose = onClose,
        emptyMessage = "No payroll runs found."
    ) { item ->
        val statusUpper = item.status.uppercase()
        val (statusTextColor, statusBgColor) = when (item.status.lowercase()) {
            "paid", "approved", "calculated" -> greentext to greenBg
            "cancelled" -> redText to redBg
            else -> yellowText to yellowBg
        }
        val payPeriod = "${item.payrollMonth}/${item.payrollYear}"
        val updatedDate = viewModel.formatDate(item.updatedAt)

        DataCard(
            item = item,
            title = "Payroll Run - $payPeriod",
            subtitle = "Pay Period: $payPeriod  •  Cadence: ${item.payFrequency}",
            topBadgeText = statusUpper,
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
                    ReportInlineMetricRow(label = "Cadence") {
                        Text(
                            text = item.payFrequency.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Normal,
                            color = mutedText
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Employees", item.employeeCounts.total.toString()),
                                ReportGridCell("Salaried", item.employeeCounts.monthlySalary.toString()),
                                ReportGridCell("Piece Rate", item.employeeCounts.pieceRate.toString())
                            ),
                            listOf(
                                ReportGridCell("Executed By", item.createdBy?.fullName ?: "--"),
                                ReportGridCell("Updated On", updatedDate),
                                ReportGridCell("Status", statusUpper, valueColor = statusTextColor)
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Updated on $updatedDate")
                }
            }
        )
    }
}