package com.cuso.mobile.view.home.hr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.hr.MemberItem
import com.cuso.mobile.model.hr.displayName
import com.cuso.mobile.model.hr.displayRole
import com.cuso.mobile.model.hr.displayStatus
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.FilterDrawer
import com.cuso.mobile.view.composable.FilterOption
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.rememberFilterDrawerState
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.DepartmentViewModel
import com.cuso.mobile.viewmodel.DesignationViewModel
import com.cuso.mobile.viewmodel.HrViewModel
import kotlinx.coroutines.delay
import com.cuso.mobile.view.composable.FilterSection
import com.cuso.mobile.view.composable.FilterSectionType
import com.cuso.mobile.view.composable.ListSkeleton

// ── Design tokens (match screenshot) ──
private val AccentColor = Color(0xFF4F39F6)
private val TitleColor = Color(0xFF111827)
//private val LabelColor = Color(0xFF6B7280)
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
    hrViewModel: HrViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel= hiltViewModel(),
    departmentViewModel: DepartmentViewModel= hiltViewModel(),
    designationViewModel : DesignationViewModel = hiltViewModel(),
    onBreadCrumbClick: () -> Unit ={}

) {
    val members by hrViewModel.members.collectAsStateWithLifecycle()
    val isLoading by hrViewModel.isLoadingMembers.collectAsStateWithLifecycle()
    val error by hrViewModel.membersError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember {
        mutableStateOf(listOf(
            FilterSection("Status", icon = Icons.Filled.Sell, type = FilterSectionType.CHECKBOX_LIST,
                options = listOf(FilterOption("active", "Active"), FilterOption("inactive","Inactive"))),
            FilterSection("Role", icon = Icons.Filled.People, type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true, options = emptyList())   // filled from hrViewModel.roles
        ))
    }
    val selectedStatus = filterSections.find { it.title == "Status" }?.options?.firstOrNull { it.isSelected }?.id

    // Initial load
    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers()
        hrViewModel.fetchRoles()
        branchViewModel.loadBranches()
        departmentViewModel.loadDepartments()
        designationViewModel.loadDesignations()
    }

    // Debounced search — refetches from the API 400ms after the user stops typing
    LaunchedEffect(searchQuery, selectedStatus) {
        delay(400)
        hrViewModel.fetchMembers(search = searchQuery.ifBlank { null }, status = selectedStatus)
    }
    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Employee",
            icon = Icons.Default.Add,
            onClick = onAddEmployee
        )
    ) {
        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Employees",
            sections = filterSections,
            onApply = { filterSections = it },   // triggers LaunchedEffect → API refetch
            onClearAll = { filterSections = filterSections.map { s -> s.copy(options = s.options.map { it.copy(isSelected = false) }) } }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar("All Employees", onClose= onDismiss)

            }

            Column{

                // ── Breadcrumb ──
                ScreenBreadcrumb(listOf("HR", "All Employees"), onClick = { onBreadCrumbClick() })

                // ── Search + Filter ──
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Employees...",
                        accentColor = AccentColor,
                        borderColor = BorderColor,
                        textSecondaryColor = MutedColor,
                        onFilterClick = { filterDrawerState.open() }   //   opens FilterDrawer
                    )

            }

            HorizontalDivider(color = BorderColor)

            when {
                isLoading && members.isEmpty() -> {
                    ListSkeleton()
                }
                error != null && members.isEmpty() -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error ?: "Something went wrong", color = Color(0xFFDC2626), fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { hrViewModel.fetchMembers(search = searchQuery.ifBlank { null }) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                members.isEmpty() -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No employees found", color = MutedColor, fontSize = 14.sp)
                    }
                }
                else -> {
                    // ── Employee list (reusing shared DataCard) ──
                    LazyColumn(modifier = Modifier.weight(1f).background(Primary_background)) {
                        items(members, key = { it._id }) { member ->
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
                                        tint = Color(0xFFDC2626), textColor = Color(0xFFDC2626),
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
    }
}