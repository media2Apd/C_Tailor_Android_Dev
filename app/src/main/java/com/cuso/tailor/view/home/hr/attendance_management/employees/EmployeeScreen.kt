@file:Suppress("unused")

package com.cuso.tailor.view.home.hr.attendance_management.employees

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.MemberItem
import com.cuso.tailor.model.hr.displayName
import com.cuso.tailor.model.hr.displayRole
import com.cuso.tailor.model.hr.displayStatus
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.PlanLimitDialog
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.DepartmentViewModel
import com.cuso.tailor.viewmodel.DesignationViewModel
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.PermissionViewModel
import kotlinx.coroutines.delay

private val AccentColor = Color(0xFF4F39F6)
private val TitleColor = Color(0xFF111827)
private val MutedColor = Color(0xFF9CA3AF)
private val BorderColor = Color(0xFFE3E4E8)
private val ActiveBg = Color(0xFFDCFCE7)
private val ActiveText = Color(0xFF16A34A)
private val InactiveBg = Color(0xFFFEE2E2)
private val InactiveText = Color(0xFFDC2626)

@Composable
fun AllEmployeesScreen(
    onDismiss: () -> Unit = {},
    onAddEmployee: () -> Unit = {},
    onView: (MemberItem) -> Unit = {},
    onEdit: (MemberItem) -> Unit = {},
    onDelete: (MemberItem) -> Unit = {},
    onUpgradePlan: () -> Unit = {},
    hrViewModel: HrViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel(),
    designationViewModel: DesignationViewModel = hiltViewModel(),
    permissionViewModel: PermissionViewModel = hiltViewModel(),
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val membersPagingState by hrViewModel.membersPagingState.collectAsStateWithLifecycle()
    val error by hrViewModel.membersError.collectAsStateWithLifecycle()
    val planLimitsState by permissionViewModel.planLimits.collectAsStateWithLifecycle()
    var showEmployeeLimitDialog by remember { mutableStateOf(false) }

    val employeeLimitResult = remember(planLimitsState, membersPagingState.items.size) {
        permissionViewModel.getPlanLimitStatus("employee", fallbackUsedCount = membersPagingState.items.size)
    }

    var searchQuery by remember { mutableStateOf("") }

    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Status",
                    icon = Icons.Filled.Sell,
                    type = FilterSectionType.CHECKBOX_LIST,
                    options = listOf(
                        FilterOption("active", "Active"),
                        FilterOption("inactive", "Inactive")
                    )
                ),
                FilterSection(
                    title = "Role",
                    icon = Icons.Filled.People,
                    type = FilterSectionType.CHIP_ROW,
                    isMultiSelect = true,
                    options = emptyList()
                )
            )
        )
    }
    val selectedStatus = filterSections.find { it.title == "Status" }?.options?.firstOrNull { it.isSelected }?.id

    // Initial load
    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers()
        hrViewModel.fetchRoles()
        branchViewModel.loadBranches()
        departmentViewModel.loadDepartments()
        designationViewModel.loadDesignations()
        permissionViewModel.fetchPlanLimitsAndModules()
    }

    // Debounced search and filter triggers
    LaunchedEffect(searchQuery, selectedStatus) {
        delay(400)
        hrViewModel.fetchMembers(search = searchQuery.trim().ifBlank { null }, status = selectedStatus)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier.fillMaxSize(),
            fab = FabConfig(
                label = "Employee",
                icon = Icons.Default.Add,
                onClick = {
                    if (employeeLimitResult.isExceeded) {
                        showEmployeeLimitDialog = true
                    } else {
                        onAddEmployee()
                    }
                }
            )
        ) {
            FilterDrawer(
                state = filterDrawerState,
                title = "Filter Employees",
                sections = filterSections,
                onApply = { filterSections = it },
                onClearAll = {
                    filterSections = filterSections.map { s ->
                        s.copy(options = s.options.map { it.copy(isSelected = false) })
                    }
                }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TitleBar(title = "All Employees", onClose = onDismiss)
                }

                Column {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Employees...",
                        accentColor = AccentColor,
                        borderColor = BorderColor,
                        textSecondaryColor = MutedColor,
                        onFilterClick = { filterDrawerState.open() }
                    )
                }

                HorizontalDivider(color = BorderColor)

                when {
                    // Persistent error state
                    error != null && membersPagingState.items.isEmpty() && !membersPagingState.isInitialLoading -> {
                        AppErrorState(
                            title = "Failed to load employees",
                            message = error ?: "Something went wrong. Please check your connection.",
                            onRetry = {
                                hrViewModel.fetchMembers(
                                    search = searchQuery.trim().ifBlank { null },
                                    status = selectedStatus
                                )
                            }
                        )
                    }

                    // Empty state
                    !membersPagingState.isInitialLoading && membersPagingState.items.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No employees found", color = MutedColor, fontSize = tokens.bodyMedium)
                        }
                    }

                    // Paginated employee list using PaginatedDataList
                    else -> {
                        PaginatedDataList(
                            state = membersPagingState,
                            onLoadMore = { hrViewModel.loadMoreMembers() },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Primary_background)
                        ) { member ->
                            val isActive = member.status.equals("active", ignoreCase = true)
                            DataCard(
                                item = member,
                                title = member.memberId ?: member._id,
                                subtitle = "${member.displayName()} • ${member.displayRole()}",
                                topBadgeText = member.displayStatus(),
                                topBadgeTextColor = if (isActive) ActiveText else InactiveText,
                                topBadgeBgColor = if (isActive) ActiveBg else InactiveBg,
                                topBadgeInline = false,
                                footerAsRows = true,
                                footerFields = listOf(
                                    DataCardField(
                                        label = "Department",
                                        text = member.departmentId?.name ?: "—",
                                        labelColor = MutedColor,
                                        textColor = TitleColor
                                    ),
                                    DataCardField(
                                        label = "Contact",
                                        text = member.userId?.mobile ?: "—",
                                        labelColor = MutedColor,
                                        textColor = TitleColor
                                    ),
                                    DataCardField(
                                        label = "Branch",
                                        text = member.branchId?.name ?: "—",
                                        labelColor = MutedColor,
                                        textColor = TitleColor
                                    )
                                ),
                                actions = listOf(
                                    MenuAction("View", Icons.Filled.Visibility, onClick = { onView(member) }),
                                    MenuAction("Edit", Icons.Filled.Edit, onClick = { onEdit(member) }),
                                    MenuAction(
                                        "Delete", Icons.Filled.Delete,
                                        tint = InactiveText, textColor = InactiveText,
                                        onClick = { onDelete(member) }
                                    )
                                ),
                                onClick = { onView(it) }
                            )
                        }
                    }
                }
            }
        }

        if (showEmployeeLimitDialog) {
            PlanLimitDialog(
                title = "Employee limit reached",
                message = "This organization has reached the maximum number of employees allowed under your current plan.",
                resourceLabel = "Employees",
                usedCount = employeeLimitResult.used,
                maxLimit = employeeLimitResult.limit,
                onDismiss = { showEmployeeLimitDialog = false },
                onUpgradePlan = {
                    showEmployeeLimitDialog = false
                    onUpgradePlan()
                }
            )
        }
    }
}