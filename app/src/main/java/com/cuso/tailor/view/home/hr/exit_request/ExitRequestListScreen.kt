@file:Suppress("SpellCheckingInspection")

package com.cuso.tailor.view.home.hr.exit_request

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
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
import com.cuso.tailor.model.hr.ExitRequestDto
import com.cuso.tailor.model.hr.extractEntityName
import com.cuso.tailor.model.hr.extractMemberName
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val exitStageTabs = listOf(
    "Exit Requests",
    "Approval",
    "Clearance",
    "Settlement"
)

private fun getExitFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Exit Type",
        options = listOf(
            FilterOption("Resignation", "Resignation"),
            FilterOption("Retirement", "Retirement"),
            FilterOption("Termination", "Termination"),
            FilterOption("Layoff", "Layoff"),
            FilterOption("Contract_End", "Contract End"),
            FilterOption("Absconding", "Absconding"),
            FilterOption("Death", "Death"),
            FilterOption("Other", "Other")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Status",
        options = listOf(
            FilterOption("Draft", "Draft"),
            FilterOption("Submitted", "Submitted"),
            FilterOption("Pending_Approval", "Pending Approval"),
            FilterOption("Approved", "Approved"),
            FilterOption("Rejected", "Rejected"),
            FilterOption("Withdrawn", "Withdrawn"),
            FilterOption("Clearance_Pending", "Clearance Pending"),
            FilterOption("Clearance_Completed", "Clearance Completed"),
            FilterOption("Settlement_Pending", "Settlement Pending"),
            FilterOption("Settled", "Settled"),
            FilterOption("Completed", "Completed"),
            FilterOption("Cancelled", "Cancelled")
        ),
        isMultiSelect = true
    )
)

@Composable
fun ExitManagementScreen(
    viewModel: HrViewModel,
    onClose: () -> Unit,
    onCreateNewRequest: () -> Unit,
    onOpenApproval: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val requests by viewModel.exitRequests.collectAsState()
    val isLoading by viewModel.isLoadingExitRequests.collectAsState()
    val errorMsg by viewModel.exitRequestsError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(exitStageTabs.first()) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var selectedExitTypes by remember { mutableStateOf(setOf<String>()) }
    var selectedStatuses by remember { mutableStateOf(setOf<String>()) }
    var deleteTarget by remember { mutableStateOf<ExitRequestDto?>(null) }

    val filterDrawerState = rememberFilterDrawerState()
    val filterSections = remember(selectedExitTypes, selectedStatuses) {
        getExitFilterSections().map { section ->
            when (section.title) {
                "Exit Type" -> section.copy(
                    options = section.options.map { it.copy(isSelected = selectedExitTypes.contains(it.id)) }
                )
                "Status" -> section.copy(
                    options = section.options.map { it.copy(isSelected = selectedStatuses.contains(it.id)) }
                )
                else -> section
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchExitRequests()
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let { DynamicIslandManager.showError(it) }
    }

    val filteredList = remember(requests, searchQuery, selectedTab, selectedExitTypes, selectedStatuses) {
        requests.filter { item ->
            val empName = item.organizationMemberId.extractMemberName()
            val deptName = item.departmentId.extractEntityName("")
            val code = item.exitCode ?: item.id.takeLast(6)

            val matchesSearch = searchQuery.isBlank() ||
                    empName.contains(searchQuery, ignoreCase = true) ||
                    deptName.contains(searchQuery, ignoreCase = true) ||
                    item.exitType.contains(searchQuery, ignoreCase = true) ||
                    code.contains(searchQuery, ignoreCase = true)

            val matchesTab = when (selectedTab) {
                "Approval" -> item.status in listOf("Submitted", "Pending_Approval", "Approved", "Rejected")
                "Clearance" -> item.status in listOf("Clearance_Pending", "Clearance_Completed")
                "Settlement" -> item.status in listOf("Settlement_Pending", "Settled", "Completed")
                else -> true
            }

            val matchesType = selectedExitTypes.isEmpty() || selectedExitTypes.contains(item.exitType)
            val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.contains(item.status)

            matchesSearch && matchesTab && matchesType && matchesStatus
        }
    }

    val activeFilterCount = selectedExitTypes.size + selectedStatuses.size

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            fab = FabConfig(
                label = "New Request",
                icon = Icons.Default.Add,
                onClick = onCreateNewRequest
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(
                    title = "Exit Management",
                    onClose = onClose
                )

                // Search and Filter bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search employee, exit ID...",
                    filterCount = activeFilterCount,
                    onFilterClick = { filterDrawerState.open() },
                    accentColor = Primary,
                    borderColor = sectionBorder,
                    textSecondaryColor = mutedText,
                    height = tokens.fieldHeight
                )

                // Stage filter tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(
                            horizontal = tokens.screenPadding,
                            vertical = tokens.extraPadding * 0.6f
                        ),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                ) {
                    exitStageTabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(if (isSelected) primary_light else whiteBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Primary else sectionBorder,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f)
                                )
                                .clickable { selectedTab = tab }
                                .padding(
                                    horizontal = tokens.screenPadding * 0.85f,
                                    vertical = tokens.extraPadding * 0.6f
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Primary else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                Text(
                                    text = "No exit requests found",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Button(
                                    onClick = onCreateNewRequest,
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                    modifier = Modifier.height(tokens.buttonHeight)
                                ) {
                                    Text(
                                        text = "New Request",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = whiteBg
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = tokens.extraPadding * 0.4f,
                                bottom = tokens.buttonHeight * 2.2f
                            )
                        ) {
                            itemsIndexed(
                                items = filteredList,
                                key = { _, item -> item.id }
                            ) { index, item ->
                                val displayCode = item.exitCode?.takeIf { it.isNotBlank() }
                                    ?: "Ext-${String.format(Locale.US, "%03d", index + 1)}"
                                val isChecked = selectedIds.contains(item.id)
                                val statusColors = resolveExitStatusColors(item.status)
                                val displayStatus = item.status.replace("_", " ")

                                DataCard(
                                    item = item,
                                    onClick = { onOpenApproval(it.id) },
                                    showHeaderDivider = true,
                                    headerContent = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                            ) {
                                                // Reuse AppCheckbox for exit request selection
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
                                                    text = displayCode,
                                                    fontSize = tokens.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Primary
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                                            ) {
                                                StatusBadge(
                                                    text = displayStatus,
                                                    bgColor = statusColors.first,
                                                    textColor = statusColors.second
                                                )

                                                ActionDropdownMenu(
                                                    actions = listOf(
                                                        MenuAction(
                                                            label = "Review",
                                                            icon = Icons.Default.Visibility,
                                                            tint = Primary,
                                                            textColor = TextPrimary,
                                                            onClick = { onOpenApproval(item.id) }
                                                        ),
                                                        MenuAction(
                                                            label = "Delete",
                                                            icon = Icons.Default.DeleteOutline,
                                                            tint = redText,
                                                            textColor = redText,
                                                            onClick = { deleteTarget = item }
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    // Pass body as named parameter 'content' since 'showDivider' is the last parameter
                                    content = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)
                                        ) {
                                            ExitKeyValueRow(
                                                label = "Employee",
                                                value = item.organizationMemberId.extractMemberName()
                                            )
                                            ExitKeyValueRow(
                                                label = "Department",
                                                value = item.departmentId.extractEntityName()
                                            )
                                            ExitKeyValueRow(
                                                label = "Exit Type",
                                                value = item.exitType.replace("_", " ")
                                            )
                                            ExitKeyValueRow(
                                                label = "Requested Date",
                                                value = formatExitDate(item.resignationDate ?: item.submittedAt ?: item.createdAt)
                                            )
                                            ExitKeyValueRow(
                                                label = "Last Working Date",
                                                value = formatExitDate(item.approvedLastWorkingDate ?: item.requestedLastWorkingDate)
                                            )

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
                                                    text = "Actions",
                                                    fontSize = tokens.bodySmall,
                                                    color = headerGrey
                                                )

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Review",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Primary,
                                                        modifier = Modifier.clickable {
                                                            onOpenApproval(item.id)
                                                        }
                                                    )
                                                    Text(
                                                        text = "Reject",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = redText,
                                                        modifier = Modifier.clickable {
                                                            onOpenApproval(item.id)
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
        }

        // Slide-in Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Exit Requests",
            sections = filterSections,
            onApply = { updatedSections ->
                selectedExitTypes = updatedSections
                    .find { it.title == "Exit Type" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
                selectedStatuses = updatedSections
                    .find { it.title == "Status" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            },
            onClearAll = {
                selectedExitTypes = emptySet()
                selectedStatuses = emptySet()
            }
        )

        // Delete confirmation dialog
        deleteTarget?.let { target ->
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                containerColor = whiteBg,
                title = {
                    Text(
                        text = "Delete Exit Request",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete the exit request for ${target.organizationMemberId.extractMemberName()}?",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val idToDelete = target.id
                            deleteTarget = null
                            viewModel.deleteExitRequest(
                                id = idToDelete,
                                onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                                onError = { err -> DynamicIslandManager.showError(err) }
                            )
                        }
                    ) {
                        Text(
                            text = "Delete",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = redText
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTarget = null }) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ExitKeyValueRow(
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

internal fun resolveExitStatusColors(status: String): Pair<Color, Color> {
    return when (status) {
        "Submitted", "Pending_Approval" -> yellowBg to yellowText
        "Approved", "Settled", "Completed" -> greenBg to greentext
        "Rejected", "Cancelled" -> redBg to redText
        "Clearance_Pending", "Clearance_Completed", "Settlement_Pending" -> purpleBg to purpleText
        else -> light_grey to TextSecondary
    }
}

internal fun formatExitDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "-"
    val inputFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd"
    )
    for (pattern in inputFormats) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed = parser.parse(isoDate)
            if (parsed != null) {
                return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(parsed)
            }
        } catch (_: Exception) {
            // Try next format
        }
    }
    return isoDate
}