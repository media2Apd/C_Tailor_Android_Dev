@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "unused_variable",
    "unused_parameter",
    "ASSIGNED_VALUE_IS_NEVER_READ", "VariableNeverRead"
)
package com.cuso.mobile.view.home.department

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.DepartmentItem
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.PlanLimits
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardStat
import com.cuso.mobile.view.composable.DataCardStatsRow
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.PlanLimitDialog
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.DepartmentUiState
import com.cuso.mobile.viewmodel.DepartmentViewModel
import com.cuso.mobile.viewmodel.DesignationCreateState
import com.cuso.mobile.viewmodel.DepartmentUpdateUiState
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.blurScrim

// ─────────────────────────────────────────────────────────────
// DepartmentSettingsScreen - Updated with SmoothBottomSheet
// Design values now pulled from LocalAppTokens for consistency
// across screen sizes (compact / medium / expanded).
// ─────────────────────────────────────────────────────────────

@Suppress("UNUSED_PARAMETER")
@Composable
fun DepartmentSettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Adaptive design tokens, provided higher up in the composition tree
    val tokens = LocalAppTokens.current

    val departmentViewModel: DepartmentViewModel = hiltViewModel()
    val salesViewModel: SalesViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

    var addSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var editingDepartment by remember { mutableStateOf<DepartmentItem?>(null) }
    var editSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var showPlanLimitDialog by remember { mutableStateOf(false) }

    // Separate blur/scrim state per sheet, same pattern as CreateOrderScreen
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
    val createState by departmentViewModel.createState.collectAsStateWithLifecycle()
    val updateState by departmentViewModel.updateState.collectAsStateWithLifecycle()
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    //   NEW — Dynamic Island messages (replaces snackbar for success/error)
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        departmentViewModel.loadDepartments()
        salesViewModel.fetchStaff()
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
                addSheetState = SheetValue.Hidden
                departmentViewModel.resetCreateState()
                departmentViewModel.loadDepartments()
                successMessage = "Department created successfully"
            }
            is DesignationCreateState.Error -> {
                departmentViewModel.resetCreateState()
                val isLimitError = state.message.contains("limit", true) || state.message.contains("exceed", true) || state.message.contains("maximum", true)
                if (isLimitError && isDepartmentLimitReached) {
                    addSheetState = SheetValue.Hidden
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
                editSheetState = SheetValue.Hidden
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

    // Scaffold with topBar slot — topBar always paints above body content
    Scaffold(
        topBar = {
            TitleBar("Department", onClose = onBack)
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

            FabScaffold(
                fab = FabConfig(
                    label = "Add Department",
                    icon = Icons.Default.Add,
                    onClick = {
                        if (planLimits != null && isDepartmentLimitReached) {
                            showPlanLimitDialog = true
                        } else {
                            addSheetState = SheetValue.Expanded
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
                        .blurScrim(addSheetBlur.coerceAtLeast(editSheetBlur))   // Blurs only this body content
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ScreenBreadcrumb(segments = listOf("Settings", "Department"), onClick = {})

                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                            placeholder = "Search Departments...",
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            onFilterClick = { /* TODO: open filter drawer */ }
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))


                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        when (val state = uiState) {
                            is DepartmentUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                ListSkeleton()
                            }
                            is DepartmentUiState.Error -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(tokens.iconSize * 2.5f))
                                        Spacer(Modifier.height(tokens.extraPadding / 2))
                                        Text("Something went wrong, Please try again later", color = Color.Red, fontSize = tokens.bodyMedium)
                                        Spacer(Modifier.height(tokens.extraPadding))
                                        Button(onClick = { departmentViewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)), shape = RoundedCornerShape(8.dp)) {
                                            Text("Retry", color = whiteBg, fontSize = tokens.bodyMedium)
                                        }
                                    }
                                }
                            }
                            is DepartmentUiState.Success -> {
                                if (filteredDepartments.isEmpty()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Groups, null, tint = Color.LightGray, modifier = Modifier.size(tokens.iconSize * 2.5f))
                                            Spacer(Modifier.height(tokens.extraPadding / 2))
                                            Text(if (searchQuery.isNotBlank()) "No matching departments found" else "No departments found", color = Color.Gray, fontSize = tokens.bodyMedium)
                                        }
                                    }
                                } else {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxWidth().weight(1f),
                                        ) {
                                            items(pagedDepartments) { department ->
                                                val (badgeText, badgeColor) = if (department.status) "Active" to Color(0xFF16A34A) else "Inactive" to Color(0xFF6B7280)
                                                DataCard(
                                                    item = department,
                                                    titleFontWeight = FontWeight.SemiBold,
                                                    titleColor = title_color,
                                                    smalltitle = "${department.name}  .  Department",
                                                    topBadgeText = badgeText,
                                                    topBadgeTextColor = badgeColor,
                                                    topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                                    topBadgeInline = true,
                                                    actions = listOf(
                                                        MenuAction("Edit", Icons.Default.Edit) {
                                                            editingDepartment = department
                                                            editSheetState = SheetValue.Expanded
                                                        },
                                                        MenuAction("View Teams", Icons.Default.Visibility) { /* TODO: navigate to detail */ }
                                                    ),
                                                    content = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(tokens.iconSize + 6.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFF3E8FF)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = headNameFor(department, staffList).firstOrNull()?.uppercase() ?: "?",
                                                                    fontSize = tokens.caption,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = Color(0xFF7C3AED)
                                                                )
                                                            }
                                                            Spacer(Modifier.width(tokens.extraPadding / 2))
                                                            Text(
                                                                text = headNameFor(department, staffList),
                                                                fontSize = tokens.bodySmall,
                                                                color = Color(0xFF111827)
                                                            )
                                                        }

                                                        Spacer(Modifier.height(tokens.extraPadding))

                                                        DataCardStatsRow(
                                                            stats = listOf(
                                                                DataCardStat(
                                                                    label = "Employees",
                                                                    value = "${department.totalEmployees}"
                                                                ),
                                                                DataCardStat(label = "Teams", value = "null"),
                                                                DataCardStat(label = "Code", value = "null")
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                        Box(modifier = Modifier.fillMaxWidth().background(whiteBg, RoundedCornerShape(topStart = tokens.cardCornerRadius, topEnd = tokens.cardCornerRadius))) {
                                            Column {
                                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        "Showing ${if (filteredDepartments.isEmpty()) 0 else (currentPage - 1) * itemsPerPage + 1} - ${minOf(currentPage * itemsPerPage, filteredDepartments.size)} of ${filteredDepartments.size}",
                                                        fontSize = tokens.bodySmall, color = Color(0xFF6B7280)
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)) {
                                                        IconButton(onClick = { if (currentPage > 1) currentPage-- }, enabled = currentPage > 1, modifier = Modifier.size(tokens.iconSize + 10.dp)) {
                                                            Icon(Icons.Default.ChevronLeft, "Previous", tint = if (currentPage > 1) Color(0xFF374151) else Color(0xFFD1D5DB))
                                                        }
                                                        Text("$currentPage - $totalPages", fontSize = tokens.bodySmall, color = Color(0xFF374151))
                                                        IconButton(onClick = { if (currentPage < totalPages) currentPage++ }, enabled = currentPage < totalPages, modifier = Modifier.size(tokens.iconSize + 10.dp)) {
                                                            Icon(Icons.Default.ChevronRight, "Next", tint = if (currentPage < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB))
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
                }
            }

            // Plan Limit Dialog — kept inside this Box, same as before
            // Plan Limit Dialog — kept inside this Box, same as before
            if (showPlanLimitDialog) {
                PlanLimitDialog(
                    title = "Plan Limit Reached",
                    message = "Department limit exceeded ($currentDepartments/${planLimits?.departmentLimit ?: 0}). Upgrade your plan to add more departments.",
                    onDismiss = { showPlanLimitDialog = false },
                    onUpgrade = { /* navigate to upgrade screen */ }
                )
            }

            //   NEW — Dynamic Island success/error banners
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

        // SmoothBottomSheet for Add Department — sibling of the Box above,
        // still inside Scaffold's content lambda; topBar is outside this lambda entirely
        SmoothBottomSheet(
            state = addSheetState,
            onStateChange = { newState ->
                addSheetState = newState
                if (newState == SheetValue.Hidden) {
                    departmentViewModel.resetCreateState()
                }
            },
            peekHeight = 200.dp,
            topInset = 66.dp,   // Matches TitleBar height, same as CreateOrderScreen's topInset
            sheetBackgroundColor = whiteBg,
            collapsedCornerRadius = tokens.cardCornerRadius,
            dragCloseEnabled = true,
            scrollableContent = true,
            onDismissRequest = {
                addSheetState = SheetValue.Hidden
                departmentViewModel.resetCreateState()
            },
            onBlurScrimChange = { r, s -> addSheetBlur = r; addSheetScrim = s }
        ) {
            AddDepartmentSheetContent(
                staffList = staffList,
                isLoading = isCreating,
                onDismiss = {
                    addSheetState = SheetValue.Hidden
                    departmentViewModel.resetCreateState()
                },
                onCreate = { request ->
                    if (isDepartmentLimitReached) {
                        addSheetState = SheetValue.Hidden
                        showPlanLimitDialog = true
                        return@AddDepartmentSheetContent
                    }
                    departmentViewModel.createDepartment(
                        name = request.name,
                        description = request.description,
                        departmentHead = request.departmentHead
                    )
                }
            )
        }

        // SmoothBottomSheet for Edit Department
        editingDepartment?.let { department ->
            SmoothBottomSheet(
                state = editSheetState,
                onStateChange = { newState ->
                    editSheetState = newState
                    if (newState == SheetValue.Hidden) {
                        editingDepartment = null
                        departmentViewModel.resetUpdateState()
                    }
                },
                peekHeight = 200.dp,
                topInset = 66.dp,
                sheetBackgroundColor = Primary_background,
                collapsedCornerRadius = tokens.cardCornerRadius,
                dragCloseEnabled = true,
                scrollableContent = true,
                onDismissRequest = {
                    editSheetState = SheetValue.Hidden
                    editingDepartment = null
                    departmentViewModel.resetUpdateState()
                },
                onBlurScrimChange = { r, s -> editSheetBlur = r; editSheetScrim = s }
            ) {
                EditDepartmentSheetContent(
                    department = department,
                    staffList = staffList,
                    isLoading = isUpdating,
                    onDismiss = {
                        editSheetState = SheetValue.Hidden
                        editingDepartment = null
                        departmentViewModel.resetUpdateState()
                    },
                    onUpdate = { name, description, departmentHead, status ->
                        departmentViewModel.updateDepartment(
                            id = department._id,
                            name = name,
                            description = description,
                            departmentHead = departmentHead,
                            status = status
                        )
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// AddDepartmentSheetContent
// ─────────────────────────────────────────────────────────────

@Composable
fun AddDepartmentSheetContent(
    staffList: List<StaffDto>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onCreate: (DepartmentRequest) -> Unit
) {
    val tokens = LocalAppTokens.current

    var departmentName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedStaff by remember { mutableStateOf("") }
    var staffExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var departmentHeadError by remember { mutableStateOf(false) }

    val staffDisplayList = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == selectedStaff }?.key ?: "Select an option"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
            .padding(bottom = tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Add New Department",
                fontSize = tokens.h2,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                "Create a new department in your organization",
                fontSize = tokens.bodySmall,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 2))

        Column {
            FormLabel("Department Name", isRequired = true)
            FormTextField(
                value = departmentName,
                onValueChange = { departmentName = it; nameError = false },
                placeholder = "Enter department name",
                isError = nameError,
                errorMessage = "Department name is required"
            )
        }

        Column {
            FormLabel("Description")
            FormTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Enter description"
            )
        }

        FormDropdown(
            label = "Department Head",
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
            isRequired = true,
            isError = departmentHeadError,
            errorMessage = "Please select a department head"
        )

        Spacer(modifier = Modifier.height(tokens.extraPadding / 2))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF374151)
                ),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
            ) {
                Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = {
                    var hasError = false
                    if (departmentName.isBlank()) {
                        nameError = true
                        hasError = true
                    }
                    if (selectedStaff.isEmpty()) {
                        departmentHeadError = true
                        hasError = true
                    }
                    if (hasError) return@Button

                    onCreate(
                        DepartmentRequest(
                            name = departmentName,
                            description = description,
                            departmentHead = selectedStaff
                        )
                    )
                },
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    disabledContainerColor = BluePrimary.copy(alpha = 0.6f)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = whiteBg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create ", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 2))
    }
}

// ─────────────────────────────────────────────────────────────
// EditDepartmentSheetContent
// ─────────────────────────────────────────────────────────────

@Composable
fun EditDepartmentSheetContent(
    department: DepartmentItem,
    staffList: List<StaffDto>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onUpdate: (name: String, description: String?, departmentHead: String?, status: Boolean?) -> Unit
) {
    val tokens = LocalAppTokens.current

    var departmentName by remember { mutableStateOf(department.name) }
    var description by remember { mutableStateOf(department.description ?: "") }
    var selectedStaff by remember { mutableStateOf(department.departmentHeadId ?: "") }
    var status by remember { mutableStateOf(department.status) }
    var staffExpanded by remember { mutableStateOf(false) }

    val staffDisplayList = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == selectedStaff }?.key ?: "Select an option"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
            .padding(bottom = tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Edit Department",
                fontSize = tokens.h2,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                "Update department information",
                fontSize = tokens.bodySmall,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 2))

        Column {
            FormLabel("Department Name", isRequired = true)
            FormTextField(
                value = departmentName,
                onValueChange = { departmentName = it },
                placeholder = "Enter department name"
            )
        }

        Column {
            FormLabel("Description")
            FormTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Enter description"
            )
        }

        FormDropdown(
            label = "Department Head",
            value = selectedStaffLabel,
            expanded = staffExpanded,
            onExpandChange = { expanded ->
                if (staffList.isNotEmpty() || !expanded) staffExpanded = expanded
            },
            options = staffDisplayList,
            onOptionSelected = { label ->
                selectedStaff = staffIdMap[label] ?: ""
            },
            isRequired = true
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            MiniSwitch(
                checked = status,
                onCheckedChange = { status = it }
            )
            Column {
                Text(
                    if (status) "Active" else "Inactive",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Text(
                    if (status) "Department is active" else "Department is inactive",
                    fontSize = tokens.label,
                    color = Color(0xFF9CA3AF)
                )
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 2))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF374151)
                ),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
            ) {
                Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = {
                    onUpdate(
                        departmentName,
                        description.ifEmpty { null },
                        selectedStaff.ifEmpty { null },
                        status
                    )
                },
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    disabledContainerColor = BluePrimary.copy(alpha = 0.6f)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CirculerProgressIndicatorSmall()
                } else {
                    Text("Update ", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 2))
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