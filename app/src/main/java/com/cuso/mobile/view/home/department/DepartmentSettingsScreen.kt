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
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// REPLACE in DepartmentSettingsScreen.kt:
//  1) fun DepartmentSettingsScreen(...)  -> replace fully with version below
//  2) fun DepartmentTable(...)           -> replace fully with version below
//  3) ADD new composable: DepartmentCardItem (new, paste anywhere below DepartmentTableRow)
// Everything else in that file (DepartmentTableRow, dialogs, fields, data classes) stays unchanged.
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

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDepartment by remember { mutableStateOf<DepartmentItem?>(null) }
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
                showAddDialog = false
                departmentViewModel.resetCreateState()
                departmentViewModel.loadDepartments()
                coroutineScope.launch { snackbarHostState.showSnackbar("Department created successfully") }
            }
            is DesignationCreateState.Error -> {
                departmentViewModel.resetCreateState()
                val isLimitError = state.message.contains("limit", true) || state.message.contains("exceed", true) || state.message.contains("maximum", true)
                if (isLimitError && isDepartmentLimitReached) {
                    showAddDialog = false
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

    FabScaffold(
        fab = FabConfig(
            label = "Add Department",
            icon = Icons.Default.Add,
            onClick = {
                if (planLimits != null && isDepartmentLimitReached) showPlanLimitDialog =
                    true else showAddDialog = true
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
                                                MenuAction("Edit", Icons.Default.Edit) { editingDepartment = department },
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

        if (showPlanLimitDialog) {
            PlanLimitDialog(
                title = "Plan Limit Reached",
                message = "Employee limit exceeded ($currentDepartments/${planLimits?.employeeLimit ?: 0}). Upgrade your plan to add more employees.",
                onDismiss = { showPlanLimitDialog = false },
                onUpgrade = { /* navigate to upgrade screen */ }
            )
        }
    }

    if (showAddDialog) {
        AddDepartmentDialog(
            isLoading = isCreating,
            staffList = staffList,
            onDismiss = { showAddDialog = false; departmentViewModel.resetCreateState() },
            onCreate = { request ->
                if (isDepartmentLimitReached) { showAddDialog = false; showPlanLimitDialog = true; return@AddDepartmentDialog }
                departmentViewModel.createDepartment(name = request.name, description = request.description, departmentHead = request.departmentHead)
            }
        )
    }

    editingDepartment?.let { department ->
        EditDepartmentDialog(
            department = department,
            staffList = staffList,
            isLoading = isUpdating,
            onDismiss = { editingDepartment = null; departmentViewModel.resetUpdateState() },
            onUpdate = { name, description, departmentHead, status ->
                departmentViewModel.updateDepartment(id = department._id, name = name, description = description, departmentHead = departmentHead, status = status)
            }
        )
    }
}



// ─────────────────────────────────────────────────────────────
// Edit Department Dialog
// ─────────────────────────────────────────────────────────────
@Composable
fun EditDepartmentDialog(
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Edit Department", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text("Update department information", fontSize = 13.sp, color = Color(0xFF6B7280))
                }

                Column {
                    FormLabel("Department Name", isRequired = true)
                    FormTextField(value = departmentName, onValueChange = { departmentName = it }, placeholder = "Enter department name")
                }
                Column {
                    FormLabel("Description")
                    FormTextField(value = description, onValueChange = { description = it }, placeholder = "Enter description")
                }

                // ── Department Head Dropdown ──
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
                    }
                )

                // ── Status Toggle ──
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniSwitch(
                        checked = status,
                        onCheckedChange = { status = it }
                    )
                    Column {
                        Text(if (status) "Active" else "Inactive", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Text(if (status) "Department is active" else "Department is inactive", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Buttons — StepNavigationFab design, inline ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackFabButton(
                        onClick = onDismiss,
                        label = "Cancel"
                    )

                    TrailingFabButton(
                        action = TrailingFabAction.Update(
                            isLoading = isLoading,
                            label = "Update",
                            enabled = departmentName.isNotBlank(),
                            onClick = {
                                onUpdate(departmentName, description.ifEmpty { null }, selectedStaff.ifEmpty { null }, status)
                            }
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Add Department Dialog
// ─────────────────────────────────────────────────────────────

@Composable
fun AddDepartmentDialog(
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header ──
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Add New Department", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text("Create a new department in your organization", fontSize = 13.sp, color = Color(0xFF6B7280))
                }

                // ── Department Name ──
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

                // ── Description ──
                Column {
                    FormLabel("Description")
                    FormTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Enter description"
                    )
                }

                // ── Department Head ──
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

                Spacer(modifier = Modifier.height(8.dp))

                // ─── Action Buttons ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackFabButton(
                        onClick = onDismiss,
                        label = "Cancel"
                    )

                    TrailingFabButton(
                        action = TrailingFabAction.Update(
                            isLoading = isLoading,
                            label = "Create",
                            enabled = departmentName.isNotBlank(),
                            onClick = {
                                var hasError = false
                                if (departmentName.isBlank()) { nameError = true; hasError = true }
                                if (selectedStaff.isEmpty()) { departmentHeadError = true; hasError = true }
                                if (hasError) return@Update
                                onCreate(DepartmentRequest(name = departmentName, description = description, departmentHead = selectedStaff))
                            }
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Department Field Component
// ─────────────────────────────────────────────────────────────

@Composable
fun DepartmentField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDescription: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isError) Color.Red else Color(0xFF374151))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isDescription) 100.dp else 48.dp)
                .background(if (isError) Color(0xFFFFF3F3) else Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                .border(width = if (isError) 1.dp else 0.dp, color = Color.Red, shape = RoundedCornerShape(8.dp))
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                singleLine = !isDescription,
                maxLines = if (isDescription) 3 else 1,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF111827)),
                cursorBrush = SolidColor(Color(0xFF3B3BF9))
            )
            if (value.isEmpty()) {
                Text(text = placeholder, fontSize = 14.sp, color = Color(0xFF9CA3AF), modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 16.dp))
            }
        }
        if (isError && errorMessage.isNotEmpty()) {
            Text(text = errorMessage, fontSize = 12.sp, color = Color.Red, modifier = Modifier.padding(start = 4.dp))
        }
    }
}


private fun headNameFor(department: DepartmentItem, staffList: List<StaffDto>): String =
    department.departmentHeadId
        ?.let { headId -> staffList.firstOrNull { it.id == headId } }
        ?.let { "${it.firstName} ${it.lastName}" }
        ?: "-"

// ─────────────────────────────────────────────────────────────
// 🔁 ONE column list for departments (needs staffList, so it's a
// function of staffList rather than a static val).
// ─────────────────────────────────────────────────────────────
//private fun departmentColumns(
//    staffList: List<StaffDto>,
//    onEditClick: (DepartmentItem) -> Unit,
//    onViewClick: (DepartmentItem) -> Unit
//): List<DataColumn<DepartmentItem>> = listOf(
//    DataColumn("name", "Department", 220.dp) { d ->
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
//            Box(modifier = Modifier.size(32.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
//                Icon(Icons.Default.Groups, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
//            }
//            Text(d.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827),
//                maxLines = 1, overflow = TextOverflow.Ellipsis)
//        }
//    },
//    DataColumn("head", "Department Head", 180.dp) { d ->
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            Box(modifier = Modifier.size(22.dp).background(Color(0xFFF3F4F6), CircleShape), contentAlignment = Alignment.Center) {
//                Icon(Icons.Default.Person, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
//            }
//            Text(headNameFor(d, staffList), fontSize = 13.sp, color = Color(0xFF374151),
//                maxLines = 1, overflow = TextOverflow.Ellipsis)
//        }
//    },
//    DataColumn("description", "Description", 260.dp) { d ->
//        Text(d.description ?: "-", fontSize = 13.sp, color = Color(0xFF6B7280), maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("employees", "Total Employees", 150.dp) { d ->
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//            Icon(Icons.Default.People, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
//            Text(d.totalEmployees.toString(), fontSize = 13.sp, color = Color(0xFFD97706), fontWeight = FontWeight.SemiBold)
//        }
//    },
//    DataColumn("teams", "Teams", 100.dp) { Text("0 Teams", fontSize = 13.sp, color = Color(0xFF374151)) },
//    DataColumn("status", "Status", 100.dp) { d ->
//        val (badgeText, bg, textColor) = if (d.status) Triple("active", Color(0xFFD1FAE5), Color(0xFF059669))
//        else Triple("inactive", Color(0xFFF3F4F6), Color(0xFF6B7280))
//        Box(modifier = Modifier.background(bg, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
//            Text(badgeText, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
//        }
//    },
//    DataColumn("action", "Action", 70.dp, cellAlignment = Alignment.Center) { d ->
//        ActionDropdownMenu(
//            actions = listOf(
//                MenuAction("Edit", Icons.Default.Edit) { onEditClick(d) },
//                MenuAction("View Teams", Icons.Default.Visibility) { onViewClick(d) }
//            )
//        )
//    }
//)
//
//
//// ── Card view — same call site as before ──
//@Composable
//fun DepartmentCardItem(
//    department: DepartmentItem,
//    staffList: List<StaffDto>,
//    onEditClick: (DepartmentItem) -> Unit,
//    onViewClick: (DepartmentItem) -> Unit
//) {
//    val columns = departmentColumns(staffList, onEditClick, onViewClick)
//    val statusColumn = columns.first { it.key == "status" }
//    val footerFields = columns.filter { it.key in listOf("employees", "teams") }
//    val headColumn = columns.first { it.key == "head" }
//
//    DataCard(
//        item = department,
//        leading = {
//            Box(modifier = Modifier.size(32.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
//                Icon(Icons.Default.Groups, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
//            }
//        },
//        title = {
//            Text(department.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827),
//                maxLines = 1, overflow = TextOverflow.Ellipsis)
//        },
//        trailing = {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                statusColumn.cellContent(department)
//                ActionDropdownMenu(
//                    actions = listOf(
//                        MenuAction("Edit", Icons.Default.Edit) { onEditClick(department) },
//                        MenuAction("View Teams", Icons.Default.Visibility) { onViewClick(department) }
//                    )
//                )
//            }
//        },
//        middleContent = {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                headColumn.cellContent(department)
//            }
//        },
//        fields = footerFields,
//        fieldsPerRow = 2,
//        footerBackground = Color(0xFFF8F9FB)
//    )
//}

// ─────────────────────────────────────────────────────────────
// Data Class
// ─────────────────────────────────────────────────────────────

data class DepartmentRequest(
    val name: String,
    val description: String,
    val departmentHead: String
)