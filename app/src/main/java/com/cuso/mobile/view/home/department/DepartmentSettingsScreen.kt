package com.cuso.mobile.view.home.department

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.model.DepartmentItem
import com.cuso.mobile.model.StaffDto
import com.cuso.mobile.view.home.branch.PlanLimits
import com.cuso.mobile.view.home.reusablecomposables.ActionDropdownMenu
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardBadge
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.shadow
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
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

            Column(modifier = Modifier.background(Color(0xFFF8F9FF)).fillMaxWidth().padding(vertical = 10.dp, horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Settings", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                    Text("Department", fontSize = 13.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp)).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it; currentPage = 1 },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) Text("Search departments...", fontSize = 14.sp, color = Color.Black)
                            inner()
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (val state = uiState) {
                    is DepartmentUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is DepartmentUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(state.message, color = Color.Red)
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
            DepartmentPlanLimitDialog(
                title = "Plan Limit Reached",
                message = "Department limit exceeded ($currentDepartments/${planLimits?.departmentLimit ?: 0}). Upgrade your plan to add more departments.",
                currentCount = currentDepartments,
                maxLimit = planLimits?.departmentLimit ?: 0,
                onDismiss = { showPlanLimitDialog = false },
                onUpgrade = { showPlanLimitDialog = false; navController.navigate("subscription") }
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
// Plan Limit Dialog - Slides from Top
// ─────────────────────────────────────────────────────────────

@Composable
fun DepartmentPlanLimitDialog(
    title: String,
    message: String,
    currentCount: Int,
    maxLimit: Int,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { /* Prevent click through */ },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = message,
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Limit reached",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF374151)
                                ),
                                modifier = Modifier.weight(0.4f)
                            ) {
                                Text("Close", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                            }

                            Button(
                                onClick = onUpgrade,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B3BF9),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(0.6f)
                            ) {
                                Text("Upgrade Plan", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DepartmentTable
// ─────────────────────────────────────────────────────────────



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
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == selectedStaff }?.key ?: ""

    Dialog(onDismissRequest = onDismiss) {
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

                DepartmentField(label = "Department Name", value = departmentName, onValueChange = { departmentName = it }, placeholder = "Enter department name")
                DepartmentField(label = "Description", value = description, onValueChange = { description = it }, placeholder = "Enter description", isDescription = true)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Department Head", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .clickable { if (staffList.isNotEmpty()) staffExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedStaffLabel.ifEmpty { if (staffList.isEmpty()) "Loading..." else "Select department head" },
                                fontSize = 14.sp,
                                color = if (selectedStaffLabel.isNotEmpty()) Color(0xFF111827) else Color(0xFF9CA3AF)
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = staffExpanded,
                            onDismissRequest = { staffExpanded = false },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            staffDisplayList.forEach { label ->
                                DropdownMenuItem(
                                    text = { Text(label, color = Color(0xFF111827), fontSize = 14.sp) },
                                    onClick = {
                                        selectedStaff = staffIdMap[label] ?: ""
                                        staffExpanded = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (selectedStaff == staffIdMap[label]) Color(0xFFF3F4F6) else Color.White)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Switch(
                        checked = status,
                        onCheckedChange = { status = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF3B3BF9),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                    Column {
                        Text(if (status) "Active" else "Inactive", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Text(if (status) "Department is active" else "Department is inactive", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        modifier = Modifier.weight(0.4f).height(48.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp, color = Color(0xFF374151))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onUpdate(departmentName, description.ifEmpty { null }, selectedStaff.ifEmpty { null }, status)
                        },
                        enabled = !isLoading && departmentName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9), disabledContainerColor = Color(0xFFD1D5DB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.6f).height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Update", fontSize = 14.sp, color = Color.White)
                        }
                    }
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
    onDismiss: () -> Unit,
    onCreate: (DepartmentRequest) -> Unit,
    isLoading: Boolean = false,
    staffList: List<StaffDto> = emptyList()
) {
    var departmentName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedStaff by remember { mutableStateOf("") }
    var staffExpanded by remember { mutableStateOf(false) }
    var departmentHeadError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    val staffDisplayList = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == selectedStaff }?.key ?: ""

    Dialog(onDismissRequest = onDismiss) {
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
                    Text("Add New Department", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text("Create a new department in your organization", fontSize = 13.sp, color = Color(0xFF6B7280))
                }

                DepartmentField(
                    label = "Department Name",
                    value = departmentName,
                    onValueChange = { departmentName = it; nameError = false },
                    placeholder = "Enter department name",
                    isError = nameError,
                    errorMessage = "Department name is required"
                )

                DepartmentField(
                    label = "Description",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Enter description",
                    isDescription = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Department Head", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (departmentHeadError) Color(0xFFFFF3F3) else Color(0xFFF3F4F6),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = if (departmentHeadError) 1.dp else 0.dp,
                                    color = Color.Red,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { if (staffList.isNotEmpty()) staffExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedStaffLabel.ifEmpty { if (staffList.isEmpty()) "Loading..." else "Select department head" },
                                fontSize = 14.sp,
                                color = if (selectedStaffLabel.isNotEmpty()) Color(0xFF111827) else Color(0xFF9CA3AF)
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = staffExpanded,
                            onDismissRequest = { staffExpanded = false },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
                        ) {
                            if (staffList.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No staff available", color = Color(0xFF9CA3AF)) },
                                    onClick = { staffExpanded = false }
                                )
                            } else {
                                staffDisplayList.forEach { label ->
                                    DropdownMenuItem(
                                        text = { Text(label, color = Color(0xFF111827), fontSize = 14.sp) },
                                        onClick = {
                                            selectedStaff = staffIdMap[label] ?: ""
                                            departmentHeadError = false
                                            staffExpanded = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (selectedStaff == staffIdMap[label]) Color(0xFFF3F4F6) else Color.White)
                                    )
                                }
                            }
                        }
                    }
                    if (departmentHeadError) {
                        Text("Please select a department head", fontSize = 12.sp, color = Color.Red, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        modifier = Modifier.weight(0.4f).height(48.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp, color = Color(0xFF374151))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            var hasError = false
                            if (departmentName.isBlank()) { nameError = true; hasError = true }
                            if (selectedStaff.isEmpty()) { departmentHeadError = true; hasError = true }
                            if (hasError) return@Button
                            onCreate(DepartmentRequest(name = departmentName, description = description, departmentHead = selectedStaff))
                        },
                        enabled = !isLoading && departmentName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9), disabledContainerColor = Color(0xFFD1D5DB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.6f).height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Create", fontSize = 14.sp, color = Color.White)
                        }
                    }
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