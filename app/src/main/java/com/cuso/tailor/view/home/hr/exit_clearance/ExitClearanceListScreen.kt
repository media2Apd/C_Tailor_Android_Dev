
@file:Suppress("SpellCheckingInspection", "unused", "AssignedVariableIsNeverRead","UnusedVariable")

package com.cuso.tailor.view.home.hr.exit_clearance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.hr.exit_request.formatExitDate
import com.cuso.tailor.viewmodel.HrViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private fun getClearanceFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Clearance Status",
        options = listOf(
            FilterOption("Pending", "Pending"),
            FilterOption("In_Progress", "In Progress"),
            FilterOption("Completed", "Completed")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Exit Type",
        options = listOf(
            FilterOption("Resignation", "Resignation"),
            FilterOption("Retirement", "Retirement"),
            FilterOption("Termination", "Termination"),
            FilterOption("Layoff", "Layoff"),
            FilterOption("Contract_End", "Contract End"),
            FilterOption("Absconding", "Absconding"),
            FilterOption("Other", "Other")
        ),
        isMultiSelect = true
    )
)

@Composable
fun ExitClearanceListScreen(
    viewModel: HrViewModel,
    onClose: () -> Unit,
    onOpenClearanceDetail: (String) -> Unit,
    onOpenManagerApproval: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val requests by viewModel.exitRequests.collectAsState()
    val isLoading by viewModel.isLoadingExitRequests.collectAsState()
    val errorMsg by viewModel.exitRequestsError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var selectedClearanceStatuses by remember { mutableStateOf(setOf<String>()) }
    var selectedExitTypes by remember { mutableStateOf(setOf<String>()) }

    val filterDrawerState = rememberFilterDrawerState()
    val filterSections = remember(selectedClearanceStatuses, selectedExitTypes) {
        getClearanceFilterSections().map { section ->
            when (section.title) {
                "Clearance Status" -> section.copy(
                    options = section.options.map {
                        it.copy(isSelected = selectedClearanceStatuses.contains(it.id))
                    }
                )
                "Exit Type" -> section.copy(
                    options = section.options.map {
                        it.copy(isSelected = selectedExitTypes.contains(it.id))
                    }
                )
                else -> section
            }
        }
    }

    // Reuse view-all exit request endpoint for Exit Clearance list
    LaunchedEffect(Unit) {
        viewModel.fetchExitClearanceList()
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let { DynamicIslandManager.showError(it) }
    }

    val filteredList = remember(requests, searchQuery, selectedClearanceStatuses, selectedExitTypes) {
        requests.filter { item ->
            val empName = item.organizationMemberId.extractMemberName()
            val deptName = item.departmentId.extractEntityName("")
            val clearanceStatus = resolveEffectiveClearanceStatus(item)

            val matchesSearch = searchQuery.isBlank() ||
                    empName.contains(searchQuery, ignoreCase = true) ||
                    deptName.contains(searchQuery, ignoreCase = true) ||
                    item.exitType.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedClearanceStatuses.isEmpty() ||
                    selectedClearanceStatuses.any { it.equals(clearanceStatus, ignoreCase = true) }

            val matchesType = selectedExitTypes.isEmpty() ||
                    selectedExitTypes.contains(item.exitType)

            matchesSearch && matchesStatus && matchesType
        }
    }

    val activeFilterCount = selectedClearanceStatuses.size + selectedExitTypes.size

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            TitleBar(
                title = "Exit Clearance",
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
                            text = "No exit clearance records found",
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
                            val employeeName = item.organizationMemberId.extractMemberName()
                            val progress = item.toClearanceProgress()
                            val clearanceStatus = resolveEffectiveClearanceStatus(item)
                            val statusColors = resolveClearanceStatusColors(clearanceStatus)

                            // Reuse DataCard with named content slot
                            DataCard(
                                item = item,
                                onClick = { onOpenClearanceDetail(it.id) },
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
                                            // Reuse AppCheckbox for list row selection
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
                                                text = employeeName,
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
                                                text = clearanceStatus.replace("_", " "),
                                                bgColor = statusColors.first,
                                                textColor = statusColors.second
                                            )

                                            ActionDropdownMenu(
                                                actions = listOf(
                                                    MenuAction(
                                                        label = "View",
                                                        icon = Icons.Default.Visibility,
                                                        tint = Primary,
                                                        textColor = TextPrimary,
                                                        onClick = { onOpenManagerApproval(item.id) }
                                                    ),
                                                    MenuAction(
                                                        label = "Clearance Checklist",
                                                        icon = Icons.Default.CheckCircle,
                                                        tint = Primary,
                                                        textColor = TextPrimary,
                                                        onClick = { onOpenClearanceDetail(item.id) }
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
                                        ClearanceKeyValueRow(
                                            label = "Department",
                                            value = item.departmentId.extractEntityName()
                                        )
                                        ClearanceKeyValueRow(
                                            label = "Exit Type",
                                            value = item.exitType.replace("_", " ")
                                        )
                                        ClearanceKeyValueRow(
                                            label = "Last Working Day",
                                            value = formatExitDate(
                                                item.approvedLastWorkingDate ?: item.requestedLastWorkingDate
                                            )
                                        )

                                        // Clearance Progress row with bar and percentage
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Clearance Progress",
                                                    fontSize = tokens.bodySmall,
                                                    color = headerGrey
                                                )
                                                Text(
                                                    text = "${progress.percentage}% (${progress.cleared}/${progress.total})",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (progress.percentage == 100) greentext else TextSecondary
                                                )
                                            }
                                            DataCardProgressBar(
                                                progress = progress.percentage / 100f,
                                                progressColor = if (progress.percentage == 100) Primary else Primary,
                                                trackColor = light_grey
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

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Checklist",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextSecondary,
                                                    modifier = Modifier.clickable {
                                                        onOpenClearanceDetail(item.id)
                                                    }
                                                )
                                                Text(
                                                    text = "View",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Primary,
                                                    modifier = Modifier.clickable {
                                                        onOpenManagerApproval(item.id)
                                                    }
                                                )
                                            }
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
            title = "Filter Exit Clearance",
            sections = filterSections,
            onApply = { updatedSections ->
                selectedClearanceStatuses = updatedSections
                    .find { it.title == "Clearance Status" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
                selectedExitTypes = updatedSections
                    .find { it.title == "Exit Type" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            },
            onClearAll = {
                selectedClearanceStatuses = emptySet()
                selectedExitTypes = emptySet()
            }
        )
    }
}

@Composable
fun ExitClearanceDetailScreen(
    exitRequestId: String,
    viewModel: HrViewModel,
    onClose: () -> Unit,
    onOpenManagerApproval: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val exitRequest by viewModel.exitRequestDetail.collectAsState()
    val isLoading by viewModel.isLoadingExitRequestDetail.collectAsState()
    val updatingItemId by viewModel.updatingClearanceItemId.collectAsState()
    val isFinalizing by viewModel.isFinalizingClearance.collectAsState()

    var newNote by remember { mutableStateOf("") }
    var adminNotes by remember { mutableStateOf<List<String>>(emptyList()) }

    // Fetch clearance checklist details from view-one API
    LaunchedEffect(exitRequestId) {
        viewModel.fetchExitClearanceDetail(exitRequestId)
    }

    // Seed initial remarks into Admin Notes when record loads
    LaunchedEffect(exitRequest?.id) {
        val initialRemark = exitRequest?.clearanceRemarks ?: exitRequest?.remarks
        if (!initialRemark.isNullOrBlank() && adminNotes.isEmpty()) {
            adminNotes = listOf(initialRemark)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearExitRequestDetail() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = "Exit Clearance Checklist",
            onClose = onClose
        )

        when {
            isLoading && exitRequest == null -> {
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
                        text = "Exit clearance record not found",
                        fontSize = tokens.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            else -> {
                val request = exitRequest!!
                val memberName = request.organizationMemberId.extractMemberName()
                val deptLabel = request.departmentId.extractEntityName("General Department")
                val progress = request.toClearanceProgress()
                val clearanceStatus = resolveEffectiveClearanceStatus(request)
                val statusColors = resolveClearanceStatusColors(clearanceStatus)
                val isAlreadyFinalized = request.clearanceStatus.equals("Completed", ignoreCase = true) ||
                        request.status in listOf("Clearance_Completed", "Completed")

                // Group backend clearanceItems by department category matching the web checklist
                val groupedCategories = remember(request.clearanceItems) {
                    buildClearanceCategories(request.clearanceItems)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.1f)
                ) {
                    // Departmental Overview card (progress bar + 4 metric columns)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.9f)
                        ) {
                            Text(
                                text = "Departmental Overview",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Overall Progress",
                                    fontSize = tokens.bodySmall,
                                    color = headerGrey
                                )
                                Text(
                                    text = "${progress.percentage}% Completed",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary
                                )
                            }

                            DataCardProgressBar(
                                progress = progress.percentage / 100f,
                                progressColor = Primary,
                                trackColor = light_grey
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OverviewStatColumn(
                                    label = "Total Tasks",
                                    value = progress.total.toString(),
                                    valueColor = title_color
                                )
                                OverviewStatColumn(
                                    label = "Completed",
                                    value = progress.cleared.toString(),
                                    valueColor = Primary
                                )
                                OverviewStatColumn(
                                    label = "In Progress",
                                    value = progress.inProgress.toString(),
                                    valueColor = title_color
                                )
                                OverviewStatColumn(
                                    label = "Blocked",
                                    value = progress.pending.toString(),
                                    valueColor = redText
                                )
                            }
                        }
                    }

                    // Interactive Department Checklist Cards
                    if (groupedCategories.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                            color = whiteBg,
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Text(
                                text = "No clearance checklist items registered.",
                                fontSize = tokens.bodySmall,
                                color = headerGrey,
                                modifier = Modifier.padding(tokens.cardPadding * 0.8f)
                            )
                        }
                    } else {
                        groupedCategories.forEach { category ->
                            val deptItems = category.items
                            val deptTotal = deptItems.size
                            val deptCleared = deptItems.count {
                                it.status.equals("Cleared", ignoreCase = true) ||
                                        it.status.equals("Completed", ignoreCase = true)
                            }
                            val isDone = deptTotal > 0 && deptCleared == deptTotal
                            val deptBadgeBg = if (isDone) primary_light else redBg
                            val deptBadgeText = if (isDone) Primary else redText

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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category.title,
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = title_color
                                        )
                                        StatusBadge(
                                            text = if (isDone) "Done" else "Pending",
                                            bgColor = deptBadgeBg,
                                            textColor = deptBadgeText
                                        )
                                    }

                                    deptItems.forEach { item ->
                                        val isItemCleared = item.status.equals("Cleared", ignoreCase = true) ||
                                                item.status.equals("Completed", ignoreCase = true)
                                        val isItemUpdating = updatingItemId == item.id

                                        // Tap any row to toggle between "Cleared" and "Pending" via API
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                                .clickable(enabled = updatingItemId == null) {
                                                    viewModel.toggleClearanceItemStatus(
                                                        exitRequestId = request.id,
                                                        itemId = item.id,
                                                        currentStatus = item.status,
                                                        onSuccess = { msg ->
                                                            DynamicIslandManager.showSuccess(msg)
                                                        },
                                                        onError = { err ->
                                                            DynamicIslandManager.showError(err)
                                                        }
                                                    )
                                                }
                                                .padding(vertical = tokens.extraPadding * 0.25f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                        ) {
                                            if (isItemUpdating) {
                                                CircularProgressIndicator(
                                                    color = Primary,
                                                    strokeWidth = 2.dp,
                                                    modifier = Modifier.size(tokens.iconSize)
                                                )
                                            } else {
                                                // Reuse AppCheckbox; parent Row handles click for both checkbox and text
                                                AppCheckbox(
                                                    checked = isItemCleared,
                                                    onCheckedChange = null,
                                                    size = tokens.iconSize,
                                                    enabled = updatingItemId == null
                                                )
                                            }

                                            Text(
                                                text = item.title,
                                                fontSize = tokens.bodySmall,
                                                color = if (isItemCleared) headerGrey else TextPrimary,
                                                textDecoration = if (isItemCleared) {
                                                    TextDecoration.LineThrough
                                                } else {
                                                    TextDecoration.None
                                                }
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = grey_border, thickness = 0.8.dp)

                                    Text(
                                        text = "$deptCleared of $deptTotal cleared",
                                        fontSize = tokens.caption,
                                        color = headerGrey
                                    )
                                }
                            }
                        }
                    }

                    // Admin Notes card
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
                                text = "Admin Notes",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            if (adminNotes.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                        .background(Primary_background)
                                        .padding(tokens.extraPadding)
                                ) {
                                    Text(
                                        text = "No notes recorded.",
                                        fontSize = tokens.caption,
                                        fontStyle = FontStyle.Italic,
                                        color = headerGrey
                                    )
                                }
                            } else {
                                adminNotes.forEach { note ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                            .background(Primary_background)
                                            .padding(tokens.extraPadding)
                                    ) {
                                        Text(
                                            text = "\"$note\"",
                                            fontSize = tokens.caption,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }

                            FormTextArea(
                                value = newNote,
                                onValueChange = { newNote = it },
                                placeholder = "Add a clearance note...",
                                minLines = 2,
                                maxLines = 4,
                                borderColor = sectionBorder
                            )

                            if (newNote.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        adminNotes = adminNotes + newNote.trim()
                                        newNote = ""
                                        DynamicIslandManager.showSuccess("Note recorded internally")
                                    },
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    border = BorderStroke(1.dp, Primary),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        text = "Save Note",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = Primary
                                    )
                                }
                            }
                        }
                    }

                    // Activity Log card (newest first) from view-one activityLog
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
                                text = "Activity Log",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            val logs = remember(request.activityLog) {
                                request.activityLog.sortedByDescending { it.performedAt.orEmpty() }
                            }

                            if (logs.isEmpty()) {
                                Text(
                                    text = "No activity logs recorded yet.",
                                    fontSize = tokens.caption,
                                    color = headerGrey
                                )
                            } else {
                                logs.forEachIndexed { index, log ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = tokens.extraPadding * 0.4f)
                                                .size(tokens.extraPadding * 0.7f)
                                                .clip(CircleShape)
                                                .background(if (index == 0) Primary else headerGrey)
                                        )
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.15f)
                                        ) {
                                            Text(
                                                text = log.description.ifBlank { log.action.replace("_", " ") },
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = if (index == 0) TextPrimary else TextSecondary
                                            )
                                            if (!log.performedAt.isNullOrBlank()) {
                                                Text(
                                                    text = formatExitDateTime(log.performedAt),
                                                    fontSize = tokens.caption,
                                                    color = headerGrey
                                                )
                                            }
                                        }
                                    }
                                }
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

//                        Button(
//                            onClick = {
//                                if (progress.total == 0 || progress.cleared != progress.total) {
//                                    DynamicIslandManager.showError(
//                                        "All clearance tasks must be cleared before final approval."
//                                    )
//                                    return@Button
//                                }
//                                viewModel.finalizeExitClearance(
//                                    exitRequestId = request.id,
//                                    onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
//                                    onError = { err -> DynamicIslandManager.showError(err) }
//                                )
//                            },
//                            enabled = isFinalizing && isAlreadyFinalized,
//                            colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
//                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(tokens.buttonHeight)
//                        ) {
//                            if (isFinalizing) {
//                                CircularProgressIndicator(
//                                    color = whiteBg,
//                                    strokeWidth = 2.dp,
//                                    modifier = Modifier.size(tokens.iconSize)
//                                )
//                            } else {
//                                Text(
//                                    text = "Final Approval",
//                                    fontSize = tokens.bodySmall,
//                                    fontWeight = FontWeight.Medium,
//                                    color = whiteBg
//                                )
//                            }
//                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewStatColumn(
    label: String,
    value: String,
    valueColor: Color
) {
    val tokens = LocalAppTokens.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
    ) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = headerGrey
        )
        Text(
            text = value,
            fontSize = tokens.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

private data class ClearanceCategoryGroup(
    val key: String,
    val title: String,
    val items: List<ExitClearanceItemDto>
)

private fun getDeptKey(raw: String): String {
    val d = raw.lowercase().trim()
    if (d.isEmpty()) return "Other"
    if (d == "it" || d.startsWith("it ") || d.startsWith("it_") || d.contains("information tech")) return "IT"
    if (d.contains("admin")) return "Admin"
    if (d.contains("finance") || d.contains("account")) return "Finance"
    if (d.contains("dept") || d.contains("hod") || d.contains("manager") || d == "department") return "Department"
    return "Other"
}

private fun buildClearanceCategories(items: List<ExitClearanceItemDto>): List<ClearanceCategoryGroup> {
    val standardOrder = listOf(
        "IT" to "IT Clearance",
        "Admin" to "Admin Clearance",
        "Finance" to "Finance Clearance",
        "Department" to "Dept HOD Clearance",
        "Other" to "General Clearance"
    )
    return standardOrder.mapNotNull { (key, title) ->
        val matching = items.filter { getDeptKey(it.resolveDepartmentName()) == key }
        if (matching.isNotEmpty()) ClearanceCategoryGroup(key, title, matching) else null
    }
}

private fun formatExitDateTime(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd"
    )
    for (pattern in patterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed = parser.parse(isoDate)
            if (parsed != null) {
                return SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.US).format(parsed)
            }
        } catch (_: Exception) {
            // Try next format
        }
    }
    return isoDate
}

@Composable
private fun ClearanceKeyValueRow(
    label: String,
    value: String
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
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun resolveEffectiveClearanceStatus(item: ExitRequestDto): String {
    val progress = item.toClearanceProgress()
    return when {
        progress.total > 0 && progress.cleared == progress.total -> "Completed"
        progress.cleared > 0 || progress.inProgress > 0 -> "In_Progress"
        !item.clearanceStatus.isNullOrBlank() -> item.clearanceStatus
        else -> "Pending"
    }
}

private fun resolveClearanceStatusColors(status: String): Pair<Color, Color> {
    return when {
        status.equals("Completed", ignoreCase = true) -> greenBg to greentext
        status.equals("In_Progress", ignoreCase = true) ||
                status.equals("In Process", ignoreCase = true) -> purpleBg to purpleText
        else -> yellowBg to yellowText
    }
}