
@file:Suppress("SpellCheckingInspection")

package com.cuso.tailor.view.home.hr.settlement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.hr.exit_request.formatExitDate
import com.cuso.tailor.view.home.hr.exit_request.resolveExitStatusColors
import com.cuso.tailor.viewmodel.HrViewModel
import java.util.Locale

private fun getSettlementFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Settlement Status",
        options = listOf(
            FilterOption("Pending", "Pending"),
            FilterOption("Draft", "Draft"),
            FilterOption("Settled", "Settled"),
            FilterOption("Completed", "Completed")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Overall Status",
        options = listOf(
            FilterOption("Clearance_Completed", "Clearance Completed"),
            FilterOption("Settlement_Pending", "Settlement Pending"),
            FilterOption("Settled", "Settled"),
            FilterOption("Completed", "Completed"),
            FilterOption("Submitted", "Submitted")
        ),
        isMultiSelect = true
    )
)

@Composable
fun ExitSettlementListScreen(
    viewModel: HrViewModel,
    onClose: () -> Unit,
    onProcessSettlement: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val requests by viewModel.exitRequests.collectAsState()
    val isLoading by viewModel.isLoadingExitRequests.collectAsState()
    val errorMsg by viewModel.exitRequestsError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var selectedSettlementStatuses by remember { mutableStateOf(setOf<String>()) }
    var selectedOverallStatuses by remember { mutableStateOf(setOf<String>()) }

    val filterDrawerState = rememberFilterDrawerState()
    val filterSections = remember(selectedSettlementStatuses, selectedOverallStatuses) {
        getSettlementFilterSections().map { section ->
            when (section.title) {
                "Settlement Status" -> section.copy(
                    options = section.options.map {
                        it.copy(isSelected = selectedSettlementStatuses.contains(it.id))
                    }
                )
                "Overall Status" -> section.copy(
                    options = section.options.map {
                        it.copy(isSelected = selectedOverallStatuses.contains(it.id))
                    }
                )
                else -> section
            }
        }
    }

    // Reuse view-all exit request endpoint from HrViewModel
    LaunchedEffect(Unit) {
        viewModel.fetchExitRequests()
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let { DynamicIslandManager.showError(it) }
    }

    val filteredList = remember(
        requests,
        searchQuery,
        selectedSettlementStatuses,
        selectedOverallStatuses
    ) {
        requests.filter { item ->
            val empName = item.organizationMemberId.extractMemberName()
            val deptName = item.departmentId.extractEntityName("")
            val settlementStatus = item.settlementStatus ?: "Pending"

            val matchesSearch = searchQuery.isBlank() ||
                    empName.contains(searchQuery, ignoreCase = true) ||
                    deptName.contains(searchQuery, ignoreCase = true) ||
                    item.status.contains(searchQuery, ignoreCase = true)

            val matchesSettlement = selectedSettlementStatuses.isEmpty() ||
                    selectedSettlementStatuses.any { it.equals(settlementStatus, ignoreCase = true) }

            val matchesOverall = selectedOverallStatuses.isEmpty() ||
                    selectedOverallStatuses.contains(item.status)

            matchesSearch && matchesSettlement && matchesOverall
        }
    }

    val activeFilterCount = selectedSettlementStatuses.size + selectedOverallStatuses.size

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            TitleBar(
                title = "Final Settlement",
                onClose = onClose
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search employee, department...",
                filterCount = activeFilterCount,
                onFilterClick = { filterDrawerState.open() },
                accentColor = Primary,
                borderColor = sectionBorder,
                textSecondaryColor = mutedText,
                height = tokens.fieldHeight
            )

            when {
                isLoading && requests.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                filteredList.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No settlement records found",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = tokens.extraPadding * 0.4f,
                            bottom = tokens.buttonHeight * 1.5f
                        )
                    ) {
                        itemsIndexed(
                            items = filteredList,
                            key = { _, item -> item.id }
                        ) { _, item ->
                            val isChecked = selectedIds.contains(item.id)
                            val empName = item.organizationMemberId.extractMemberName()
                            val settlementStatus = item.settlementStatus ?: "Pending"
                            val settlementBadgeColors = resolveSettlementBadgeColors(settlementStatus)
                            val overallBadgeColors = resolveExitStatusColors(item.status)
                            val payableText = item.settlementAmount?.let {
                                formatSettlementAmount(it)
                            } ?: "—"

                            // Reuse DataCard with named content slot and AppCheckbox
                            DataCard(
                                item = item,
                                onClick = { onProcessSettlement(it.id) },
                                showHeaderDivider = true,
                                headerContent = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                        ) {
                                            AppCheckbox(
                                                checked = isChecked,
                                                size = tokens.iconSize,
                                                onCheckedChange = { checked ->
                                                    selectedIds = if (checked) {
                                                        selectedIds + item.id
                                                    } else {
                                                        selectedIds - item.id
                                                    }
                                                }
                                            )

                                            Text(
                                                text = empName,
                                                fontSize = tokens.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                                        ) {
                                            StatusBadge(
                                                text = settlementStatus.replace("_", " "),
                                                bgColor = settlementBadgeColors.first,
                                                textColor = settlementBadgeColors.second
                                            )

                                            ActionDropdownMenu(
                                                actions = listOf(
                                                    MenuAction(
                                                        label = "Process Settlement",
                                                        icon = Icons.Default.Visibility,
                                                        tint = Primary,
                                                        textColor = TextPrimary,
                                                        onClick = { onProcessSettlement(item.id) }
                                                    )
                                                )
                                            )
                                        }
                                    }
                                },
                                content = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)
                                    ) {
                                        SettlementKeyValueRow(
                                            label = "Department",
                                            value = item.departmentId.extractEntityName()
                                        )
                                        SettlementKeyValueRow(
                                            label = "Last Working Day",
                                            value = formatExitDate(
                                                item.approvedLastWorkingDate ?: item.requestedLastWorkingDate
                                            )
                                        )
                                        SettlementKeyValueRow(
                                            label = "Final Payable",
                                            value = payableText,
                                            valueColor = if (item.settlementAmount != null) Primary else TextPrimary
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Overall Status",
                                                fontSize = tokens.bodySmall,
                                                color = headerGrey
                                            )
                                            StatusBadge(
                                                text = item.status.replace("_", " "),
                                                bgColor = overallBadgeColors.first,
                                                textColor = overallBadgeColors.second
                                            )
                                        }

                                        HorizontalDivider(
                                            color = grey_border,
                                            thickness = 1.dp
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Action",
                                                fontSize = tokens.bodySmall,
                                                color = headerGrey
                                            )

                                            Text(
                                                text = "Process Settlement",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Primary,
                                                modifier = Modifier.clickable {
                                                    onProcessSettlement(item.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Final Settlements",
            sections = filterSections,
            onApply = { updatedSections ->
                selectedSettlementStatuses = updatedSections
                    .find { it.title == "Settlement Status" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
                selectedOverallStatuses = updatedSections
                    .find { it.title == "Overall Status" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            },
            onClearAll = {
                selectedSettlementStatuses = emptySet()
                selectedOverallStatuses = emptySet()
            }
        )
    }
}

@Composable
fun ExitSettlementDetailScreen(
    exitRequestId: String,
    viewModel: HrViewModel,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val exitRequest by viewModel.exitRequestDetail.collectAsState()
    val settlement by viewModel.exitSettlement.collectAsState()
    val calculation by viewModel.exitSettlementCalculation.collectAsState()
    val isLoadingRequest by viewModel.isLoadingExitRequestDetail.collectAsState()
    val isLoadingSettlement by viewModel.isLoadingExitSettlement.collectAsState()
    val isSaving by viewModel.isSavingExitSettlement.collectAsState()
    val errorMsg by viewModel.exitSettlementError.collectAsState()

    var remarks by remember { mutableStateOf("") }

    LaunchedEffect(exitRequestId) {
        viewModel.fetchExitSettlementData(exitRequestId)
    }

    LaunchedEffect(settlement?.id) {
        settlement?.remarks?.takeIf { it.isNotBlank() }?.let { remarks = it }
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let { DynamicIslandManager.showError(it) }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearExitSettlementData() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = "Final Settlement",
            onClose = onClose
        )

        when {
            (isLoadingRequest || isLoadingSettlement) && exitRequest == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            exitRequest == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Exit request not found",
                        fontSize = tokens.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            else -> {
                val request = exitRequest!!
                val saved = settlement
                val calc = calculation

                val memberName = saved?.employeeSnapshot?.employeeName?.takeIf { it.isNotBlank() }
                    ?: request.organizationMemberId.extractMemberName()
                val designationName = saved?.employeeSnapshot?.designation?.takeIf { it.isNotBlank() }
                    ?: request.designationId.extractEntityName("Employee")
                val departmentName = saved?.employeeSnapshot?.department?.takeIf { it.isNotBlank() }
                    ?: request.departmentId.extractEntityName("-")
                val currency = saved?.currency ?: "INR"

                val totalReimbursements = saved?.totals?.totalReimbursements
                    ?: calc?.totalReimbursementAmount
                    ?: 0.0
                val grossSettlement = saved?.totals?.grossSettlement
                    ?: calc?.totalEarnings
                    ?: calc?.compensationSummary?.totalEarnings
                    ?: 0.0
                val totalDeductions = saved?.totals?.totalDeductions
                    ?: calc?.totalDeductions
                    ?: calc?.compensationSummary?.totalDeductions
                    ?: 0.0
                val finalPayableAmount = saved?.totals?.finalPayableAmount
                    ?: calc?.netSettlementAmount
                    ?: calc?.compensationSummary?.netSalary
                    ?: 0.0

                val lastWorkingDate = formatExitDate(
                    saved?.employeeSnapshot?.lastWorkingDate
                        ?: request.approvedLastWorkingDate
                        ?: request.requestedLastWorkingDate
                        ?: calc?.approvedLastWorkingDate
                        ?: calc?.requestedLastWorkingDate
                )
                val noticeDays = saved?.noticePeriod?.applicableDays ?: request.noticePeriodDays
                val noticeRecoveryAmount = saved?.noticePeriod?.recoveryAmount ?: 0.0
                val isNoticeWaived = request.noticePeriodWaived || noticeRecoveryAmount == 0.0

                val earningsList = saved?.leaveEncashments.orEmpty().ifEmpty { calc?.earnings.orEmpty() }
                val reimbursementList = saved?.expenseReimbursements.orEmpty()
                    .ifEmpty { calc?.reimbursements.orEmpty() }
                val deductionItems = saved?.deductions?.items.orEmpty()
                    .ifEmpty { calc?.deductions.orEmpty() + calc?.loanDeductions.orEmpty() }

                val statusLabel = when {
                    saved != null -> "${saved.status} • ${saved.settlementNumber.orEmpty()}".trimEnd(' ', '•')
                    calc?.calculationStatus != null -> "${calc.calculationStatus.replace("_", " ")} calculation"
                    else -> "Draft calculation complete"
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.1f)
                ) {
                    // Header subtitle
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.2f)) {
                        Text(
                            text = "Final Settlement",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                        Text(
                            text = "Employee: $memberName • $departmentName",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }

                    // Final Net Payable Amount hero card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.9f),
                        color = Primary
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)
                        ) {
                            Text(
                                text = "FINAL NET PAYABLE AMOUNT",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = primary_light
                            )
                            Text(
                                text = formatSettlementAmount(finalPayableAmount, currency),
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = whiteBg
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(whiteBg.copy(alpha = 0.16f))
                                    .padding(
                                        horizontal = tokens.extraPadding,
                                        vertical = tokens.extraPadding * 0.35f
                                    )
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = tokens.caption,
                                    color = whiteBg
                                )
                            }
                        }
                    }

                    // Notice Period Recovery card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Text(
                                text = "Notice Period Recovery",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                            SettlementKeyValueRow(
                                label = "Last Working Day",
                                value = lastWorkingDate
                            )
                            SettlementKeyValueRow(
                                label = "Notice Period Days",
                                value = "$noticeDays Days"
                            )
                            SettlementKeyValueRow(
                                label = "Notice Period Recovery Amount",
                                value = if (isNoticeWaived) {
                                    "${formatSettlementAmount(noticeRecoveryAmount, currency)} (Waived)"
                                } else {
                                    formatSettlementAmount(noticeRecoveryAmount, currency)
                                },
                                valueColor = redText
                            )
                        }
                    }

                    // Leave Encashment & Earnings card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Text(
                                text = "Leave Encashment & Earnings",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            if (earningsList.isEmpty()) {
                                SettlementKeyValueRow(
                                    label = "Total Earnings",
                                    value = formatSettlementAmount(grossSettlement, currency),
                                    valueColor = Primary
                                )
                                saved?.salaryDetails?.let { salary ->
                                    SettlementKeyValueRow(
                                        label = "Pending Salary",
                                        value = formatSettlementAmount(salary.pendingSalary, currency),
                                        valueColor = Primary
                                    )
                                }
                            } else {
                                earningsList.forEachIndexed { idx, line ->
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = line.resolveLabel(),
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = formatSettlementAmount(line.amount, currency),
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Primary
                                            )
                                        }
                                        if (line.days != null && line.rate != null) {
                                            Text(
                                                text = "${line.days.toInt()} Days @ ${formatSettlementAmount(line.rate, currency)}/day",
                                                fontSize = tokens.caption,
                                                color = headerGrey
                                            )
                                        }
                                    }
                                    if (idx != earningsList.lastIndex) {
                                        HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                                    }
                                }
                            }
                        }
                    }

                    // Expense Reimbursements card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Text(
                                text = "Expense Reimbursements",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            if (reimbursementList.isEmpty()) {
                                SettlementKeyValueRow(
                                    label = "Total Reimbursements",
                                    value = formatSettlementAmount(totalReimbursements, currency)
                                )
                            } else {
                                reimbursementList.forEachIndexed { idx, item ->
                                    SettlementKeyValueRow(
                                        label = item.resolveLabel(),
                                        value = formatSettlementAmount(item.amount, currency)
                                    )
                                    if (idx != reimbursementList.lastIndex) {
                                        HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                                    }
                                }
                            }
                        }
                    }

                    // Total Deductions Breakdown card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Text(
                                text = "Total Deductions Breakdown",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            SettlementKeyValueRow(
                                label = "Provident Fund",
                                value = formatSettlementAmount(saved?.deductions?.providentFund ?: 0.0, currency)
                            )
                            SettlementKeyValueRow(
                                label = "Tax Deductions",
                                value = formatSettlementAmount(saved?.deductions?.incomeTax ?: 0.0, currency)
                            )
                            SettlementKeyValueRow(
                                label = "Asset Loss Recovery",
                                value = formatSettlementAmount(saved?.totals?.totalAssetRecovery ?: 0.0, currency)
                            )
                            deductionItems.forEach { dItem ->
                                SettlementKeyValueRow(
                                    label = dItem.resolveLabel(),
                                    value = formatSettlementAmount(dItem.amount, currency)
                                )
                            }
                            HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                            SettlementKeyValueRow(
                                label = "Total Deductions",
                                value = formatSettlementAmount(totalDeductions, currency),
                                valueColor = redText
                            )
                        }
                    }

                    // Required Approvals / Signatures card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Text(
                                text = "Required Approvals",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            val empSigned = saved?.employeeSignature?.signed == true
                            val hrSigned = saved?.hrSignature?.signed == true
                            val finSigned = saved?.financeSignature?.signed == true

                            SettlementKeyValueRow(
                                label = "Employee Signature",
                                value = if (empSigned) "Signed ($memberName)" else "Pending Signature",
                                valueColor = if (empSigned) greentext else headerGrey
                            )
                            SettlementKeyValueRow(
                                label = "HR Manager Authorization",
                                value = if (hrSigned) "Authorized" else "Pending Authorization",
                                valueColor = if (hrSigned) greentext else yellowText
                            )
                            SettlementKeyValueRow(
                                label = "Finance Head Clearance",
                                value = if (finSigned) "Cleared" else "Pending Clearance",
                                valueColor = if (finSigned) greentext else headerGrey
                            )
                        }
                    }

                    // Remarks field (editable before saving settlement)
                    if (saved == null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                            color = whiteBg,
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                            ) {
                                FormLabel(text = "Settlement Remarks")
                                FormTextArea(
                                    value = remarks,
                                    onValueChange = { remarks = it },
                                    placeholder = "Optional settlement remarks...",
                                    minLines = 2,
                                    maxLines = 4,
                                    borderColor = sectionBorder
                                )
                            }
                        }
                    }
                }

                // Bottom action bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = whiteBg,
                    border = BorderStroke(1.dp, grey_border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        OutlinedButton(
                            onClick = onClose,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            border = BorderStroke(1.dp, sectionBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight)
                        ) {
                            Text(
                                text = "Dismiss",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                val memberId = request.organizationMemberId.extractId()
                                if (memberId.isBlank()) {
                                    DynamicIslandManager.showError("Employee ID is missing")
                                    return@Button
                                }
                                val payload = CreateExitSettlementRequest(
                                    exitRequestId = request.id,
                                    organizationMemberId = memberId,
                                    currency = currency,
                                    deductions = CreateExitSettlementDeductionsPayload(
                                        items = deductionItems
                                    ),
                                    employeeSnapshot = CreateExitSettlementSnapshotPayload(
                                        name = memberName,
                                        designationName = designationName,
                                        departmentName = departmentName
                                    ),
                                    expenseReimbursements = reimbursementList,
                                    remarks = remarks.trim(),
                                    totals = CreateExitSettlementTotalsPayload(
                                        totalReimbursements = totalReimbursements,
                                        grossSettlement = grossSettlement,
                                        totalDeductions = totalDeductions,
                                        finalPayableAmount = finalPayableAmount
                                    )
                                )
                                viewModel.createExitSettlement(
                                    request = payload,
                                    onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                                    onError = { err -> DynamicIslandManager.showError(err) }
                                )
                            },
                            enabled = !isSaving && saved == null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                disabledContainerColor = disabled
                            ),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(tokens.buttonHeight)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = whiteBg,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            } else {
                                Text(
                                    text = if (saved != null) "Settlement Saved" else "Save Settlement",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementKeyValueRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = headerGrey
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun resolveSettlementBadgeColors(status: String): Pair<Color, Color> {
    return when {
        status.equals("Settled", ignoreCase = true) ||
                status.equals("Completed", ignoreCase = true) -> greenBg to greentext
        status.equals("Draft", ignoreCase = true) -> purpleBg to purpleText
        else -> yellowBg to yellowText
    }
}

private fun formatSettlementAmount(amount: Double, currency: String = "INR"): String {
    val symbol = when (currency.uppercase()) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "₹"
    }
    return "$symbol${String.format(Locale.US, "%,.2f", amount)}"
}