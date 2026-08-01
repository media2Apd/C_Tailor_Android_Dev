@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "unused_variable",
    "unused_parameter",
    "ASSIGNED_VALUE_IS_NEVER_READ"
)
package com.cuso.mobile.view.home.department

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.model.DepartmentItem
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.view.home.reusablecomposables.PlanLimits
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.home.reusablecomposables.BackFabButton
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.view.home.reusablecomposables.PlanLimitDialog
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabButton
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.DepartmentUiState
import com.cuso.mobile.viewmodel.DepartmentViewModel
import com.cuso.mobile.viewmodel.DesignationCreateState
import com.cuso.mobile.viewmodel.DepartmentUpdateUiState
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.cuso.mobile.view.home.reusablecomposables.SheetValue
import com.cuso.mobile.view.home.reusablecomposables.SmoothBottomSheet
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// DepartmentSettingsScreen - Updated with SmoothBottomSheet
// ─────────────────────────────────────────────────────────────

@Suppress("UNUSED_PARAMETER")
@Composable
fun DepartmentSettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val departmentViewModel: DepartmentViewModel = hiltViewModel()
    val salesViewModel: SalesViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

    var addSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var editingDepartment by remember { mutableStateOf<DepartmentItem?>(null) }
    var editSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var showPlanLimitDialog by remember { mutableStateOf(false) }

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
                coroutineScope.launch { snackbarHostState.showSnackbar("Department created successfully") }
            }
            is DesignationCreateState.Error -> {
                departmentViewModel.resetCreateState()
                val isLimitError = state.message.contains("limit", true) || state.message.contains("exceed", true) || state.message.contains("maximum", true)
                if (isLimitError && isDepartmentLimitReached) {
                    addSheetState = SheetValue.Hidden
                    showPlanLimitDialog = true
                } else {
                    coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
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
                coroutineScope.launch { snackbarHostState.showSnackbar("Department updated successfully") }
            }
            is DepartmentUpdateUiState.Error -> {
                departmentViewModel.resetUpdateState()
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> Unit
        }
    }

    val isCreating = createState is DesignationCreateState.Loading
    val isUpdating = updateState is DepartmentUpdateUiState.Loading

    val filteredDepartments = allDepartments.filter { d -> searchQuery.isBlank() || d.name.contains(searchQuery, ignoreCase = true) }
    val totalPages = maxOf(1, if (filteredDepartments.isNotEmpty()) (filteredDepartments.size + itemsPerPage - 1) / itemsPerPage else 1)
    val pagedDepartments = filteredDepartments.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)

    Box(modifier = Modifier.fillMaxSize()) {
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
            snackbarHostState = snackbarHostState
        ) {
            Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(22.dp).clickable { onBack() }, tint = Color(0xFF111827))
                            Text("Department", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                        }
                        if (planLimits != null) {
                            Text("$currentDepartments/${planLimits.departmentLimit}", fontSize = 13.sp, color = if (isDepartmentLimitReached) Color.Red else Color(0xFF6B7280))
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    ScreenBreadcrumb(segments = listOf("Settings", "Department"), onClick = {})

                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        placeholder = "Search Departments...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { /* TODO: open filter drawer */ }
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when (val state = uiState) {
                        is DepartmentUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ListSkeleton()
                        }
                        is DepartmentUiState.Error -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Something went wrong, Please try again later", color = Color.Red)
                                    Spacer(Modifier.height(12.dp))
                                    Button(onClick = { departmentViewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)), shape = RoundedCornerShape(8.dp)) {
                                        Text("Retry", color = Color.White)
                                    }
                                }
                            }
                        }
                        is DepartmentUiState.Success -> {
                            if (filteredDepartments.isEmpty()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Groups, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text(if (searchQuery.isNotBlank()) "No matching departments found" else "No departments found", color = Color.Gray, fontSize = 15.sp)
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
                                                topBadgeText = badgeText,
                                                topBadgeTextColor = badgeColor,
                                                topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                                title = department.name,
                                                subtitle = department.description ?: "-",
                                                footerFields = listOf(
                                                    DataCardField(icon = Icons.Default.Person, text = headNameFor(department, staffList)),
                                                    DataCardField(icon = Icons.Default.People, text = "${department.totalEmployees} Employees")
                                                ),
                                                actions = listOf(
                                                    MenuAction("Edit", Icons.Default.Edit) {
                                                        editingDepartment = department
                                                        editSheetState = SheetValue.Expanded
                                                    },
                                                    MenuAction("View Teams", Icons.Default.Visibility) { /* TODO: navigate to detail */ }
                                                )
                                            )
                                        }
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))) {
                                        Column {
                                            HorizontalDivider(color = Color(0xFFF0F0F0))
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "Showing ${if (filteredDepartments.isEmpty()) 0 else (currentPage - 1) * itemsPerPage + 1} - ${minOf(currentPage * itemsPerPage, filteredDepartments.size)} of ${filteredDepartments.size}",
                                                    fontSize = 13.sp, color = Color(0xFF6B7280)
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    IconButton(onClick = { if (currentPage > 1) currentPage-- }, enabled = currentPage > 1, modifier = Modifier.size(28.dp)) {
                                                        Icon(Icons.Default.ChevronLeft, "Previous", tint = if (currentPage > 1) Color(0xFF374151) else Color(0xFFD1D5DB))
                                                    }
                                                    Text("$currentPage - $totalPages", fontSize = 13.sp, color = Color(0xFF374151))
                                                    IconButton(onClick = { if (currentPage < totalPages) currentPage++ }, enabled = currentPage < totalPages, modifier = Modifier.size(28.dp)) {
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

        // ── SmoothBottomSheet for Add Department ──
        SmoothBottomSheet(
            state = addSheetState,
            onStateChange = { newState ->
                addSheetState = newState
                if (newState == SheetValue.Hidden) {
                    departmentViewModel.resetCreateState()
                }
            },
            modifier = Modifier.fillMaxSize(),
            peekHeight = 200.dp,
            topInset = 48.dp,
            maxBlurRadius = 14.dp,
            maxScrimAlpha = 0.35f,
            sheetBackgroundColor = Color.White,
            collapsedCornerRadius = 24.dp,
            dragCloseEnabled = true,
            scrollableContent = true,
            onDismissRequest = {
                addSheetState = SheetValue.Hidden
                departmentViewModel.resetCreateState()
            }
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

        // ── SmoothBottomSheet for Edit Department ──
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
                modifier = Modifier.fillMaxSize(),
                peekHeight = 200.dp,
                topInset = 48.dp,
                maxBlurRadius = 14.dp,
                maxScrimAlpha = 0.35f,
                sheetBackgroundColor = Color.White,
                collapsedCornerRadius = 24.dp,
                dragCloseEnabled = true,
                scrollableContent = true,
                onDismissRequest = {
                    editSheetState = SheetValue.Hidden
                    editingDepartment = null
                    departmentViewModel.resetUpdateState()
                }
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

        // ── Plan Limit Dialog ──
        if (showPlanLimitDialog) {
            PlanLimitDialog(
                title = "Plan Limit Reached",
                message = "Department limit exceeded ($currentDepartments/${planLimits?.departmentLimit ?: 0}). Upgrade your plan to add more departments.",
                onDismiss = { showPlanLimitDialog = false },
                onUpgrade = { /* navigate to upgrade screen */ }
            )
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
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Add New Department",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                "Create a new department in your organization",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF374151)
                ),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                modifier = Modifier.weight(1f),
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
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create ", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Edit Department",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                "Update department information",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniSwitch(
                checked = status,
                onCheckedChange = { status = it }
            )
            Column {
                Text(
                    if (status) "Active" else "Inactive",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Text(
                    if (status) "Department is active" else "Department is inactive",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF374151)
                ),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                modifier = Modifier.weight(1f),
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
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Update ", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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