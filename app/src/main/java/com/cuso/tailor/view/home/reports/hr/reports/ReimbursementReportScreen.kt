@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.hr.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
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
import com.cuso.tailor.model.report.ReimbursementRecordDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
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
fun ReimbursementReportScreen(
    onClose: () -> Unit,
    onViewDetail: (ReimbursementRecordDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val analytics by viewModel.reimbursementAnalytics.collectAsState()
    val pagingState by viewModel.reimbursementPagingState.collectAsState()
    val filteredList by viewModel.filteredReimbursementList.collectAsState()
    val searchQuery by viewModel.reimbursementSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Approval Status",
                    options = listOf(
                        FilterOption("Pending_Approval", "Pending Approval"),
                        FilterOption("Approved", "Approved"),
                        FilterOption("Paid", "Paid"),
                        FilterOption("Rejected", "Rejected"),
                        FilterOption("Cancelled", "Cancelled")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Payment Status",
                    options = listOf(
                        FilterOption("Paid", "Paid"),
                        FilterOption("Not_Paid", "Not Paid"),
                        FilterOption("Payment_Pending", "Payment Pending")
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
        viewModel.loadReimbursementReport()
    }

    val summary = analytics?.reimbursementSummary
    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL CLAIMS FILED",
                value = (summary?.totalClaims ?: 0).toString(),
                subtitle = "All submitted requests",
                icon = Icons.Default.Description,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "TOTAL CLAIM VALUE",
                value = viewModel.formatCurrency(summary?.totalClaimAmount ?: 0.0),
                subtitle = "Claimed expenses",
                icon = Icons.Default.CurrencyRupee,
                valueColor = purpleText,
                subtitleColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "SETTLED & PAID",
                value = viewModel.formatCurrency(summary?.paidAmount ?: 0.0),
                subtitle = "${summary?.paidClaims ?: 0} claims disbursed",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "PENDING PAYOUTS",
                value = viewModel.formatCurrency(summary?.pendingPaymentAmount ?: 0.0),
                subtitle = "Awaiting finance release",
                icon = Icons.Default.Schedule,
                valueColor = yellowText,
                subtitleColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Reimbursement Claims & Settlement Report",
        subtitle = null,
        sectionTitle = "Reimbursement Settlement Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onReimbursementSearchQueryChange,
        searchPlaceholder = "Search by employee, department or claim type...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Approval Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val paymentStatuses = updated.find { it.title == "Payment Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyReimbursementFilters(statuses, paymentStatuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearReimbursementFilters()
        },
        onLoadMore = viewModel::loadMoreReimbursements,
        onShareClick = { DynamicIslandManager.showSuccess("Reimbursement report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Reimbursement report export started") },
        onClose = onClose,
        emptyMessage = "No reimbursement claims found."
    ) { item ->
        val approvalLabel = viewModel.formatStatusLabel(item.status)
        val paymentLabel = viewModel.formatStatusLabel(item.paymentStatus)
        val (statusTextColor, statusBgColor) = when (item.status.lowercase()) {
            "paid", "approved" -> greentext to greenBg
            "rejected", "cancelled" -> redText to redBg
            "pending_approval", "submitted" -> yellowText to yellowBg
            else -> mutedText to grey_border
        }
        val memberName = item.organizationMemberId?.fullName ?: "--"
        val departmentName = item.departmentId?.name?.ifBlank { "--" } ?: "--"
        val expenseDate = viewModel.formatDate(item.expenseDate)
        val loggedDate = viewModel.formatDate(item.createdAt)

        DataCard(
            item = item,
            title = memberName,
            subtitle = "$departmentName  •  ${item.claimType}",
            topBadgeText = approvalLabel,
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
                    ReportInlineMetricRow(label = "Claim Amount") {
                        Text(
                            text = viewModel.formatCurrency(item.totalClaimAmount),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Approval", approvalLabel),
                                ReportGridCell("Disbursement", paymentLabel),
                                ReportGridCell("Approver", item.resolveApproverName())
                            ),
                            listOf(
                                ReportGridCell("Expense Date", expenseDate),
                                ReportGridCell("Department", departmentName),
                                ReportGridCell("Claim Type", item.claimType.ifBlank { "--" })
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Logged on $loggedDate")
                }
            }
        )
    }
}