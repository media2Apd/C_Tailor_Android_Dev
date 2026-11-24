
@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.legal.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.report.ComplianceLicenseItemDto
import com.cuso.tailor.model.report.ContractExpiryItemDto
import com.cuso.tailor.model.report.LegalCaseDisputeItemDto
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
import com.cuso.tailor.view.home.reports.ReportCardFooterCaption
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

// Maps risk levels to theme text color
private fun riskLevelColor(risk: String): Color = when (risk.lowercase()) {
    "high", "critical" -> redText
    "medium" -> yellowText
    "low" -> greentext
    else -> TextPrimary
}

// Maps status strings to theme badge colors
private fun legalStatusBadgeColors(status: String): Pair<Color, Color> {
    val key = status.lowercase()
    return when {
        key.contains("expired") || key.contains("breach") || key.contains("high") -> redText to redBg
        key.contains("pending") || key.contains("expiring") -> yellowText to yellowBg
        key.contains("active") || key.contains("renew") || key.contains("completed") || key.contains("resolved") -> greentext to greenBg
        else -> Primary to primary_light
    }
}

// =============================================================================
// 1. CONTRACTS & EXPIRY AUDIT REPORT
// =============================================================================

@Composable
fun ContractsExpiryAuditReportScreen(
    onClose: () -> Unit,
    onViewDetail: (ContractExpiryItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.contractsExpirySummary.collectAsState()
    val pagingState by viewModel.contractsExpiryPaging.collectAsState()
    val filteredList by viewModel.filteredContractsExpiryList.collectAsState()
    val searchQuery by viewModel.contractsExpirySearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Risk Level",
                    options = listOf(
                        FilterOption("Low", "Low"),
                        FilterOption("Medium", "Medium"),
                        FilterOption("High", "High")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Renewal Status",
                    options = listOf(
                        FilterOption("Renewal_Notice", "Renewal Notice"),
                        FilterOption("Renewed", "Renewed"),
                        FilterOption("Expired", "Expired")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { s -> s.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        viewModel.loadContractsExpiryReport(isLoadMore = false)
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL CONTRACTS",
                value = (summary?.totalContracts ?: 0).toString(),
                subtitle = "In current portfolio",
                icon = Icons.Default.Description,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "PORTFOLIO VALUE",
                value = viewModel.formatCurrency(summary?.totalPortfolioValue ?: 0.0),
                subtitle = "Total active commitment",
                icon = Icons.Default.CurrencyRupee,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "EXPIRING IN 30 DAYS",
                value = (summary?.expiringIn30Days ?: 0).toString(),
                subtitle = "${summary?.pendingRenewals ?: 0} Pending renewals",
                icon = Icons.Default.Schedule,
                valueColor = yellowText,
                subtitleColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "HIGH RISK CONTRACTS",
                value = (summary?.highRiskContracts ?: 0).toString(),
                subtitle = "${summary?.expiredContracts ?: 0} Already expired",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                subtitleColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Contracts & Expiry Audit Report",
        sectionTitle = "Contracts Master Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onContractsExpirySearchChange,
        searchPlaceholder = "Search by contract number, name or party...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id.ifBlank { it.contractNumber } },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val risks = updated.find { it.title == "Risk Level" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val statuses = updated.find { it.title == "Renewal Status" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyContractsExpiryFilters(risks, statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearContractsExpiryFilters()
        },
        onLoadMore = { viewModel.loadContractsExpiryReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Contracts & expiry report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Contracts & expiry report export started") },
        onClose = onClose,
        emptyMessage = "No contracts found."
    ) { item ->
        val statusLabel = viewModel.formatStatusLabel(item.renewalStatus)
        val (badgeText, badgeBg) = legalStatusBadgeColors(item.renewalStatus)

        DataCard(
            item = item,
            title = item.contractName.ifBlank { "--" },
            subtitle = item.contractNumber.ifBlank { "--" },
            topBadgeText = statusLabel,
            topBadgeTextColor = badgeText,
            topBadgeBgColor = badgeBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Counterparty") {
                        Text(
                            text = item.counterparty.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportInlineMetricRow(label = "Value (\u20B9)") {
                        Text(
                            text = viewModel.formatCurrency(item.contractValue),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Type", item.contractType.ifBlank { "--" }),
                                ReportGridCell("Start Date", viewModel.formatDate(item.startDate)),
                                ReportGridCell("End Date", viewModel.formatDate(item.endDate))
                            ),
                            listOf(
                                ReportGridCell(
                                    label = "Expiry Timeline",
                                    value = item.daysRemaining.ifBlank { "--" },
                                    valueColor = if (item.isExpired || item.daysRemaining.contains("ago", ignoreCase = true)) redText else TextPrimary
                                ),
                                ReportGridCell(
                                    label = "Risk",
                                    value = item.riskLevel.ifBlank { "--" },
                                    valueColor = riskLevelColor(item.riskLevel)
                                ),
                                ReportGridCell("Legal Owner", item.legalOwner.ifBlank { "--" })
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Status: $statusLabel")
                }
            }
        )
    }
}

// =============================================================================
// 2. COMPLIANCE & LICENSE AUDIT REPORT
// =============================================================================

@Composable
fun ComplianceLicenseAuditReportScreen(
    onClose: () -> Unit,
    onViewDetail: (ComplianceLicenseItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.complianceLicenseSummary.collectAsState()
    val pagingState by viewModel.complianceLicensePaging.collectAsState()
    val filteredList by viewModel.filteredComplianceLicenseList.collectAsState()
    val searchQuery by viewModel.complianceLicenseSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Status",
                    options = listOf(
                        FilterOption("Active", "Active"),
                        FilterOption("Expired", "Expired"),
                        FilterOption("Pending", "Pending")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Risk Level",
                    options = listOf(
                        FilterOption("Low", "Low"),
                        FilterOption("Medium", "Medium"),
                        FilterOption("High", "High")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { s -> s.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        viewModel.loadComplianceLicenseReport(isLoadMore = false)
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL LICENSES",
                value = (summary?.totalLicenses ?: 0).toString(),
                subtitle = "${summary?.activeCompliancesTracked ?: 0} Statutory Compliances",
                icon = Icons.Default.VerifiedUser,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ACTIVE & COMPLIANT",
                value = (summary?.activeLicenses ?: 0).toString(),
                subtitle = "Fully authorized",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "EXPIRING IN 30 DAYS",
                value = (summary?.expiringIn30Days ?: 0).toString(),
                subtitle = "Action required",
                icon = Icons.Default.Schedule,
                valueColor = yellowText,
                subtitleColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "HIGH RISK / OVERDUE",
                value = (summary?.highRiskLicenses ?: 0).toString(),
                subtitle = "${summary?.expiredLicenses ?: 0} Expired permits",
                icon = Icons.Default.ErrorOutline,
                valueColor = redText,
                subtitleColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Compliance & License Audit Report",
        sectionTitle = "License & Permit Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onComplianceLicenseSearchChange,
        searchPlaceholder = "Search by license name, code, number or owner...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id.ifBlank { it.licenseCode } },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Status" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val risks = updated.find { it.title == "Risk Level" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyComplianceLicenseFilters(statuses, risks)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearComplianceLicenseFilters()
        },
        onLoadMore = { viewModel.loadComplianceLicenseReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Compliance & license report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Compliance & license report export started") },
        onClose = onClose,
        emptyMessage = "No licenses or compliance records found."
    ) { item ->
        val statusLabel = viewModel.formatStatusLabel(item.status)
        val (badgeText, badgeBg) = legalStatusBadgeColors(item.status)

        DataCard(
            item = item,
            title = item.licenseName.ifBlank { "--" },
            subtitle = item.licenseCode.ifBlank { "--" },
            topBadgeText = statusLabel,
            topBadgeTextColor = badgeText,
            topBadgeBgColor = badgeBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Type: ${item.licenseType.ifBlank { "--" }}") {
                        Text(
                            text = "Custodian: ${item.licenseOwner.ifBlank { "--" }}",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Reg #", item.licenseNumber.ifBlank { "--" }),
                                ReportGridCell("Issuing Authority", item.issuingAuthority.ifBlank { "--" }),
                                ReportGridCell("Jurisdiction", item.jurisdiction.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Issue Date", viewModel.formatDate(item.issueDate)),
                                ReportGridCell("Expiry Date", viewModel.formatDate(item.expiryDate)),
                                ReportGridCell(
                                    label = "Validity",
                                    value = item.daysRemaining.ifBlank { "--" },
                                    valueColor = if (item.isExpired || item.daysRemaining.contains("overdue", ignoreCase = true)) redText else TextPrimary
                                )
                            ),
                            listOf(
                                ReportGridCell(
                                    label = "Risk Level",
                                    value = item.riskLevel.ifBlank { "--" },
                                    valueColor = riskLevelColor(item.riskLevel)
                                )
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Status: $statusLabel")
                }
            }
        )
    }
}

// =============================================================================
// 3. LEGAL CASES & DISPUTES REPORT
// =============================================================================

@Composable
fun LegalCasesDisputesReportScreen(
    onClose: () -> Unit,
    onViewDetail: (LegalCaseDisputeItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.legalCasesSummary.collectAsState()
    val pagingState by viewModel.legalCasesPaging.collectAsState()
    val filteredList by viewModel.filteredLegalCasesList.collectAsState()
    val searchQuery by viewModel.legalCasesSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Risk Level",
                    options = listOf(
                        FilterOption("Low", "Low"),
                        FilterOption("Medium", "Medium"),
                        FilterOption("High", "High"),
                        FilterOption("Critical", "Critical")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Status",
                    options = listOf(
                        FilterOption("Active", "Active"),
                        FilterOption("Completed", "Completed"),
                        FilterOption("Resolved", "Resolved")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { s -> s.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        viewModel.loadLegalCasesDisputesReport(isLoadMore = false)
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL MATTERS",
                value = (summary?.totalCases ?: 0).toString(),
                subtitle = "All notices & disputes",
                icon = Icons.Default.Balance,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ACTIVE DISPUTES",
                value = (summary?.activeDisputes ?: 0).toString(),
                subtitle = "Under negotiation/trial",
                icon = Icons.Default.ErrorOutline,
                valueColor = yellowText,
                subtitleColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "CRITICAL / HIGH RISK",
                value = (summary?.criticalHighRisk ?: 0).toString(),
                subtitle = "Exposure warning",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                subtitleColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "UPCOMING HEARINGS",
                value = (summary?.upcomingDeadlines ?: 0).toString(),
                subtitle = "${summary?.resolvedCases ?: 0} Resolved matters",
                icon = Icons.Default.Schedule,
                valueColor = purpleText,
                subtitleColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Legal Cases & Disputes Report",
        sectionTitle = "Litigation & Dispute Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onLegalCasesSearchChange,
        searchPlaceholder = "Search by case code, title, party or counsel...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id.ifBlank { it.disputeCode.ifBlank { it.caseCode } } },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val risks = updated.find { it.title == "Risk Level" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val statuses = updated.find { it.title == "Status" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyLegalCasesFilters(risks, statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearLegalCasesFilters()
        },
        onLoadMore = { viewModel.loadLegalCasesDisputesReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Legal cases & disputes report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Legal cases & disputes report export started") },
        onClose = onClose,
        emptyMessage = "No legal cases or disputes found."
    ) { item ->
        val displayTitle = item.title.ifBlank { item.subject.ifBlank { "--" } }
        val displayCode = item.disputeCode.ifBlank { item.caseCode.ifBlank { item.noticeCode.ifBlank { "--" } } }
        val statusLabel = viewModel.formatStatusLabel(item.status)
        val (badgeText, badgeBg) = legalStatusBadgeColors(item.status)

        DataCard(
            item = item,
            title = displayTitle,
            subtitle = displayCode,
            topBadgeText = statusLabel,
            topBadgeTextColor = badgeText,
            topBadgeBgColor = badgeBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Type: ${item.caseType.ifBlank { "--" }}") {
                        Text(
                            text = "Party Involved: ${item.partyInvolved.ifBlank { "--" }}",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Jurisdiction / Forum", item.jurisdiction.ifBlank { "--" }),
                                ReportGridCell("Hearing / Deadline", viewModel.formatDate(item.hearingDate ?: item.deadlineDate)),
                                ReportGridCell(
                                    label = "Timeline Alert",
                                    value = item.timelineAlert.ifBlank { "--" },
                                    valueColor = if (item.isOverdue || item.timelineAlert.contains("overdue", ignoreCase = true)) redText else TextPrimary
                                )
                            ),
                            listOf(
                                ReportGridCell("Stage", item.stage.ifBlank { "--" }),
                                ReportGridCell(
                                    label = "Risk Level",
                                    value = item.riskLevel.ifBlank { "--" },
                                    valueColor = riskLevelColor(item.riskLevel)
                                ),
                                ReportGridCell("Legal Counsel", item.legalCounsel.ifBlank { "--" })
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Status: $statusLabel")
                }
            }
        )
    }
}