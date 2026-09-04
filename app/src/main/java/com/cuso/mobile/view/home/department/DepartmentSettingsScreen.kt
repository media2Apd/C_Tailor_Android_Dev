@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "unused_variable",
    "unused_parameter",
    "ASSIGNED_VALUE_IS_NEVER_READ", "VariableNeverRead"
)
package com.cuso.mobile.view.home.department

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.model.settings.DepartmentItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.close_color
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AppErrorState
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.PlanLimitDialog
import com.cuso.mobile.view.composable.PlanLimits
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.DepartmentUiState
import com.cuso.mobile.viewmodel.DepartmentUpdateUiState
import com.cuso.mobile.viewmodel.DepartmentViewModel
import com.cuso.mobile.viewmodel.DesignationCreateState
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.SalesViewModel

enum class DepartmentScreenMode {
    LIST, FORM
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun DepartmentSettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var currentScreenMode by remember { mutableStateOf(DepartmentScreenMode.LIST) }
    val tokens = LocalAppTokens.current

    val departmentViewModel: DepartmentViewModel = hiltViewModel()
    val salesViewModel: SalesViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val branchViewModel: BranchViewModel = hiltViewModel()

    var addSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var editingDepartment by remember { mutableStateOf<DepartmentItem?>(null) }
    var editSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var showPlanLimitDialog by remember { mutableStateOf(false) }

    var addSheetBlur by remember { mutableStateOf(0.dp) }
    var addSheetScrim by remember { mutableFloatStateOf(0f) }
    var editSheetBlur by remember { mutableStateOf(0.dp) }
    var editSheetScrim by remember { mutableFloatStateOf(0f) }

    val isAnySheetOpen = addSheetState != SheetValue.Hidden || editSheetState != SheetValue.Hidden

    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10

    val uiState by departmentViewModel.uiState.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val branchUiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val createState by departmentViewModel.createState.collectAsStateWithLifecycle()
    val updateState by departmentViewModel.updateState.collectAsStateWithLifecycle()
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        departmentViewModel.loadDepartments()
        salesViewModel.fetchStaff()
        branchViewModel.loadBranches()
        profileViewModel.loadOrganization("")
    }

    val planLimits = (profileUiState as? ProfileUiState.Success)?.data?.organization?.let { org ->
        org.plan?.let { plan ->
            PlanLimits(
                branchLimit = plan.branchLimit,
                departmentLimit = plan.departmentLimit,
                employeeLimit = plan.employeeLimit,
                orderLimit = plan.orderLimit,
                categoryLimit = plan.categoryLimit
            )
        }
    }

    val allDepartments = (uiState as? DepartmentUiState.Success)?.departments ?: emptyList()
    val currentDepartments = allDepartments.size
    val isDepartmentLimitReached = planLimits != null && currentDepartments >= planLimits.departmentLimit

    LaunchedEffect(createState) {
        when (val state = createState) {
            is DesignationCreateState.Success -> {
                currentScreenMode = DepartmentScreenMode.LIST
                departmentViewModel.resetCreateState()
                departmentViewModel.loadDepartments()
                successMessage = "Department created successfully"
            }
            is DesignationCreateState.Error -> {
                departmentViewModel.resetCreateState()
                val isLimitError = state.message.contains("limit", true) || state.message.contains("exceed", true) || state.message.contains("maximum", true)
                if (isLimitError && isDepartmentLimitReached) {
                    showPlanLimitDialog = true
                } else {
                    errorMessage = state.message
                }
            }
            else -> Unit
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is DepartmentUpdateUiState.Success -> {
                currentScreenMode = DepartmentScreenMode.LIST
                editingDepartment = null
                departmentViewModel.resetUpdateState()
                departmentViewModel.loadDepartments()
                successMessage = "Department updated successfully"
            }
            is DepartmentUpdateUiState.Error -> {
                departmentViewModel.resetUpdateState()
                errorMessage = state.message
            }
            else -> Unit
        }
    }

    val isCreating = createState is DesignationCreateState.Loading
    val isUpdating = updateState is DepartmentUpdateUiState.Loading

    val filteredDepartments = allDepartments.filter { d -> searchQuery.isBlank() || d.name.contains(searchQuery, ignoreCase = true) }
    val totalPages = maxOf(1, if (filteredDepartments.isNotEmpty()) (filteredDepartments.size + itemsPerPage - 1) / itemsPerPage else 1)
    val pagedDepartments = filteredDepartments.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TitleBar("Department", onClose = onBack)
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                ) {
                    when (currentScreenMode) {
                        DepartmentScreenMode.LIST -> {
                            FabScaffold(
                                fab = FabConfig(
                                    label = "Add Department",
                                    icon = Icons.Default.Add,
                                    onClick = {
                                        if (planLimits != null && isDepartmentLimitReached) {
                                            showPlanLimitDialog = true
                                        } else {
                                            editingDepartment = null
                                            currentScreenMode = DepartmentScreenMode.FORM
                                        }
                                    }
                                ),
                                fabVisible = !isAnySheetOpen,
                                snackbarHostState = snackbarHostState
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(padding)
                                        .background(Color.Transparent)
                                        .blurScrim(addSheetBlur.coerceAtLeast(editSheetBlur))
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        SearchFilterBar(
                                            query = searchQuery,
                                            onQueryChange = { searchQuery = it },
                                            placeholder = "Search Departments...",
                                            accentColor = BluePrimary,
                                            borderColor = BorderGray,
                                            textSecondaryColor = TextSecondary,
                                            onFilterClick = { }
                                        )
                                    }
                                    HorizontalDivider(color = title_border)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) {
                                        when (val state = uiState) {
                                            is DepartmentUiState.Loading -> Box(
                                                Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ListSkeleton()
                                            }

                                            is DepartmentUiState.Error -> {
                                                AppErrorState(
                                                    title = "Failed to load department",
                                                    message = "Something went wrong. Please check your connection and try again.",
                                                    onRetry = { departmentViewModel.refresh() }
                                                )
                                            }

                                            is DepartmentUiState.Success -> {
                                                if (filteredDepartments.isEmpty()) {
                                                    Box(
                                                        Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Icon(
                                                                Icons.Default.Groups,
                                                                null,
                                                                tint = Color.LightGray,
                                                                modifier = Modifier.size(tokens.iconSize * 2.5f)
                                                            )
                                                            Spacer(Modifier.height(tokens.extraPadding / 2))
                                                            Text(
                                                                if (searchQuery.isNotBlank()) "No matching departments found" else "No departments found",
                                                                color = Color.Gray,
                                                                fontSize = tokens.bodyMedium
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Column(modifier = Modifier.fillMaxSize()) {
                                                        LazyColumn(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .weight(1f),
                                                        ) {
                                                            items(pagedDepartments) { department ->
                                                                DataCard(
                                                                    item = department,
                                                                    title = department.name,
                                                                    titleColor = Color(0xFF0F172A),
                                                                    subtitle = "not found",
                                                                    topBadgeText = if (department.status) "Active" else "Inactive",
                                                                    topBadgeTextColor = if (department.status) Color(0xFF16A34A) else Color(0xFF6B7280),
                                                                    topBadgeBgColor = if (department.status) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                                                    topBadgeShowDot = false,
                                                                    topBadgeInline = true,
                                                                    showHeaderDivider = true,
                                                                    actions = listOf(
                                                                        MenuAction(
                                                                            "Edit",
                                                                            Icons.Default.Edit
                                                                        ) {
                                                                            editingDepartment = department
                                                                            currentScreenMode = DepartmentScreenMode.FORM
                                                                        },
                                                                        MenuAction(
                                                                            "View Teams",
                                                                            Icons.Default.Visibility
                                                                        ) { }
                                                                    ),
                                                                    content = {
                                                                        val iconColor = Color(0xFF64748B)
                                                                        val textBodyColor = Color(0xFF334155)

                                                                        Column(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                                        ) {
                                                                            // Head Row
                                                                            Row(
                                                                                verticalAlignment = Alignment.CenterVertically,
                                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                            ) {
                                                                                Icon(
                                                                                    painter = painterResource(R.drawable.ic_person),
                                                                                    contentDescription = null,
                                                                                    tint = iconColor,
                                                                                    modifier = Modifier.size(18.dp)
                                                                                )
                                                                                Text(
                                                                                    text = buildAnnotatedString {
                                                                                        withStyle(
                                                                                            SpanStyle(
                                                                                                color = Color(0xFF64748B),
                                                                                                fontWeight = FontWeight.Normal
                                                                                            )
                                                                                        ) {
                                                                                            append("Head: ")
                                                                                        }
                                                                                        withStyle(
                                                                                            SpanStyle(
                                                                                                color = Color(0xFF0F172A),
                                                                                                fontWeight = FontWeight.SemiBold
                                                                                            )
                                                                                        ) {
                                                                                            append(
                                                                                                headNameFor(
                                                                                                    department,
                                                                                                    staffList
                                                                                                )
                                                                                            )
                                                                                        }
                                                                                    },
                                                                                    fontSize = 14.sp
                                                                                )
                                                                            }

                                                                            // Branch Location Row
                                                                            Row(
                                                                                verticalAlignment = Alignment.CenterVertically,
                                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                            ) {
                                                                                Icon(
                                                                                    painter = painterResource(R.drawable.ic_location),
                                                                                    contentDescription = null,
                                                                                    tint = iconColor,
                                                                                    modifier = Modifier.size(18.dp)
                                                                                )
                                                                                Text(
                                                                                    text = "Not found",
                                                                                    fontSize = 14.sp,
                                                                                    color = textBodyColor,
                                                                                    maxLines = 1,
                                                                                    overflow = TextOverflow.Ellipsis
                                                                                )
                                                                            }

                                                                            // Employees & Teams Row
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Row(
                                                                                    modifier = Modifier.weight(1f),
                                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                                ) {
                                                                                    Icon(
                                                                                        painter = painterResource(R.drawable.ic_users),
                                                                                        contentDescription = null,
                                                                                        tint = iconColor,
                                                                                        modifier = Modifier.size(18.dp)
                                                                                    )
                                                                                    Text(
                                                                                        text = "${department.totalEmployees} Employees",
                                                                                        fontSize = 14.sp,
                                                                                        color = textBodyColor
                                                                                    )
                                                                                }

                                                                                Row(
                                                                                    modifier = Modifier.weight(1f),
                                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                                ) {
                                                                                    Icon(
                                                                                        painter = painterResource(R.drawable.ic_shopping_bag),
                                                                                        contentDescription = null,
                                                                                        tint = iconColor,
                                                                                        modifier = Modifier.size(18.dp)
                                                                                    )
                                                                                    Text(
                                                                                        text = "Teams",
                                                                                        fontSize = 14.sp,
                                                                                        color = textBodyColor
                                                                                    )
                                                                                }
                                                                            }

                                                                            // View Teams Action
                                                                            Box(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                contentAlignment = Alignment.CenterEnd
                                                                            ) {
                                                                                Text(
                                                                                    text = "View Teams",
                                                                                    fontSize = 14.sp,
                                                                                    fontWeight = FontWeight.Medium,
                                                                                    color = Color(0xFF4F46E5),
                                                                                    modifier = Modifier
                                                                                        .clickable(
                                                                                            indication = null,
                                                                                            interactionSource = remember { MutableInteractionSource() }
                                                                                        ) { }
                                                                                        .padding(top = 2.dp)
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
                                        }
                                    }
                                }
                            }
                        }
                        DepartmentScreenMode.FORM -> {
                            val branches = (branchUiState as? com.cuso.mobile.viewmodel.BranchUiState.Success)?.branches ?: emptyList()

                            AddDepartmentPage(
                                department = editingDepartment,
                                staffList = staffList,
                                branchList = branches,
                                isLoading = if (editingDepartment != null) isUpdating else isCreating,
                                onBack = {
                                    currentScreenMode = DepartmentScreenMode.LIST
                                    editingDepartment = null
                                    departmentViewModel.resetCreateState()
                                    departmentViewModel.resetUpdateState()
                                },
                                onSubmit = { name, description, departmentHead, branchId, status ->
                                    if (editingDepartment != null) {
                                        departmentViewModel.updateDepartment(
                                            id = editingDepartment!!._id,
                                            name = name,
                                            description = description,
                                            departmentHead = departmentHead,
                                            status = status
                                        )
                                    } else {
                                        if (isDepartmentLimitReached) {
                                            showPlanLimitDialog = true
                                            return@AddDepartmentPage
                                        }
                                        departmentViewModel.createDepartment(
                                            name = name,
                                            description = description,
                                            departmentHead = departmentHead
                                        )
                                    }
                                }
                            )
                        }
                    }

                    if (showPlanLimitDialog) {
                        PlanLimitDialog(
                            title = "Plan Limit Reached",
                            message = "Department limit exceeded ($currentDepartments/${planLimits?.departmentLimit ?: 0}). Upgrade your plan to add more departments.",
                            onDismiss = { showPlanLimitDialog = false },
                            onUpgrade = { }
                        )
                    }
                }
            }
        }

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMessage,
            onDismiss = { successMessage = null }
        )
        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// AddDepartmentPage (Unified Add & Edit Page)
// ─────────────────────────────────────────────────────────────

@Composable
fun AddDepartmentPage(
    department: DepartmentItem? = null,
    staffList: List<StaffDto>,
    branchList: List<com.cuso.mobile.model.settings.BranchItem> = emptyList(),
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onSubmit: (name: String, description: String, departmentHead: String, branchId: String, status: Boolean) -> Unit
) {
    val isEditMode = department != null

    var departmentName by remember(department) { mutableStateOf(department?.name ?: "") }
    var description by remember(department) { mutableStateOf(department?.description ?: "") }
    var selectedStaff by remember(department) { mutableStateOf(department?.departmentHeadId ?: "") }
    var selectedBranch by remember(department) { mutableStateOf("") }
    var status by remember(department) { mutableStateOf(department?.status ?: true) }

    var staffExpanded by remember { mutableStateOf(false) }
    var branchExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var departmentHeadError by remember { mutableStateOf(false) }

    val staffDisplayList = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == selectedStaff }?.key ?: ""

    val branchDisplayList = branchList.map { it.name ?: "" }
    val branchIdMap = branchList.associate { (it.name ?: "") to it.id }
    val selectedBranchLabel = branchIdMap.entries.firstOrNull { it.value == selectedBranch }?.key ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBFBFB))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleBar(
                    if (isEditMode) "Edit Department" else "Add New Department",
                    onClose = onBack
                )
                HorizontalDivider(color = title_border)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Department Name
                Column {
                    FormLabel("Department Name", isRequired = true)
                    FormTextField(
                        value = departmentName,
                        onValueChange = {
                            departmentName = it
                            nameError = false
                        },
                        placeholder = "Enter department name",
                        isError = nameError,
                        errorMessage = if (nameError) "Department name is required" else null
                    )
                }

//                Column {
//                    FormLabel("Description")
//                    FormTextField(
//                        value = description,
//                        onValueChange = { description = it },
//                        placeholder = "Enter description"
//                    )
//                }

                // 2. Department Head Dropdown
                Column {
                    FormLabel("Department Head", isRequired = true)
                    FormDropdown(
                        value = selectedStaffLabel,
                        expanded = staffExpanded,
                        onExpandChange = { expanded ->
                            if (staffList.isNotEmpty() || !expanded) staffExpanded = expanded
                        },
                        options = staffDisplayList,
                        onOptionSelected = { label ->
                            selectedStaff = staffIdMap[label] ?: ""
                            departmentHeadError = false
                        },
                        isRequired = false
                    )
                }

                // 3. Branch Name Dropdown
                Column {
                    FormLabel("Branch Name")
                    FormDropdown(
                        value = selectedBranchLabel,
                        expanded = branchExpanded,
                        onExpandChange = { expanded ->
                            if (branchList.isNotEmpty() || !expanded) branchExpanded = expanded
                        },
                        options = branchDisplayList,
                        onOptionSelected = { label ->
                            selectedBranch = branchIdMap[label] ?: ""
                        },
                        isRequired = false
                    )
                }

//                if (isEditMode) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(12.dp),
//                        modifier = Modifier.padding(top = 4.dp)
//                    ) {
//                        MiniSwitch(
//                            checked = status,
//                            onCheckedChange = { status = it }
//                        )
//                        Column {
//                            Text(
//                                text = if (status) "Active" else "Inactive",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = Color(0xFF374151)
//                            )
//                            Text(
//                                text = if (status) "Department is active" else "Department is inactive",
//                                fontSize = 12.sp,
//                                color = Color(0xFF9CA3AF)
//                            )
//                        }
//                    }
//                }
            }
        }

        StepNavigationFab(
            showBack = true,
            onBack = onBack,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Update(
                isLoading = isLoading,
                label = if (isEditMode) "Update Department" else "Add Department",
                onClick = {
                    var hasError = false
                    if (departmentName.isBlank()) {
                        nameError = true
                        hasError = true
                    }
                    if (hasError) return@Update

                    onSubmit(departmentName, description, selectedStaff, selectedBranch, status)
                }
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Helper Functions
// ─────────────────────────────────────────────────────────────

private fun headNameFor(department: DepartmentItem, staffList: List<StaffDto>): String =
    department.departmentHeadId
        ?.let { headId -> staffList.firstOrNull { it.id == headId } }
        ?.let { "${it.firstName} ${it.lastName}" }
        ?: "-"

// ─────────────────────────────────────────────────────────────
// Data Class
// ─────────────────────────────────────────────────────────────

data class DepartmentRequest(
    val name: String,
    val description: String,
    val departmentHead: String
)