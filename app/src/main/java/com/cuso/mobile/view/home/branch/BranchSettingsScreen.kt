@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)
package com.cuso.mobile.view.home.branch

import CreateBranchAddress
import CreateBranchRequest
import android.util.Log
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
import com.cuso.mobile.model.BranchItem
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.model.UpdateBranchAddress
import com.cuso.mobile.model.UpdateBranchRequest
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
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
import com.cuso.mobile.viewmodel.BranchUiState
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.CreateBranchUiState
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.cuso.mobile.viewmodel.UpdateBranchUiState
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// REPLACE in BranchSettingsScreen.kt:
//  1) fun BranchSettingsScreen(...)  -> replace fully with version below
//  2) fun BranchTable(...)           -> replace fully with version below
//  3) ADD new composable: BranchCardItem (new, paste anywhere below BranchTableRow)
// Everything else in that file (BranchTableRow, dialogs, fields, data classes) stays unchanged.
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")

@Composable
fun BranchSettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val branchViewModel: BranchViewModel = hiltViewModel()
    val salesViewModel: SalesViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

    val uiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val updateState by branchViewModel.updateState.collectAsStateWithLifecycle()
    val createState by branchViewModel.createState.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    var editingBranch by remember { mutableStateOf<BranchItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPlanLimitDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
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
    Log.d("PLAN", "Plan Response = $planLimits")
    LaunchedEffect(createState) {
        when (val state = createState) {
            is CreateBranchUiState.Success -> {
                showAddDialog = false
                branchViewModel.resetCreateState()
                branchViewModel.loadBranches()
                coroutineScope.launch { snackbarHostState.showSnackbar("Branch created successfully") }
            }
            is CreateBranchUiState.Error -> {
                branchViewModel.resetCreateState()
                if (state.message.contains("limit", true) || state.message.contains("exceed", true)) {
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
            is UpdateBranchUiState.Success -> {
                editingBranch = null
                branchViewModel.loadBranches()
                branchViewModel.resetUpdateState()
                coroutineScope.launch { snackbarHostState.showSnackbar("Branch updated successfully") }
            }
            is UpdateBranchUiState.Error -> {
                branchViewModel.resetUpdateState()
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> Unit
        }
    }

    val isUpdating = updateState is UpdateBranchUiState.Loading
    val isCreating = createState is CreateBranchUiState.Loading

    val allBranches = (uiState as? BranchUiState.Success)?.branches ?: emptyList()
    val currentBranchesCount = allBranches.size
    val isBranchLimitReached = planLimits?.let { currentBranchesCount >= it.branchLimit } ?: false

    val filteredBranches = allBranches.filter { b ->
        searchQuery.isBlank() ||
                (b.name?.contains(searchQuery, ignoreCase = true) == true) ||
                (b.branchId?.contains(searchQuery, ignoreCase = true) == true)
    }
    val totalPages = maxOf(1, if (filteredBranches.isNotEmpty()) (filteredBranches.size + itemsPerPage - 1) / itemsPerPage else 1)
    val pagedBranches = filteredBranches.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)

    FabScaffold(
        fab = FabConfig(
            label = "Add Branch",
            icon = Icons.Default.Add,
            onClick = {
                if (isBranchLimitReached) showPlanLimitDialog = true else showAddDialog = true
            }
        ),
        snackbarHostState = snackbarHostState
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

            // ── FIXED TOP HEADER ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(22.dp).clickable { onBack() },
                            tint = Color(0xFF111827)
                        )
                        Text("Branches", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    }
                    if (planLimits != null) {
                        Text(
                            text = "$currentBranchesCount/${planLimits.branchLimit}",
                            fontSize = 13.sp,
                            color = if (isBranchLimitReached) Color.Red else Color(0xFF6B7280)
                        )
                    }
                }
            }

            // ── Breadcrumb + Search ──
            Column(modifier = Modifier.fillMaxWidth()) {
                ScreenBreadcrumb(
                    segments = listOf("Settings", "Branches"),
                    onClick = { /* TODO: hook to modules panel if needed, else {} */ }
                )
                SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        placeholder = "Search Branches...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { /* TODO: open filter drawer */ }
                )
            }

            // ── Content ──
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (val state = uiState) {
                    is BranchUiState.Loading -> {
                        ListSkeleton()
                    }
                    is BranchUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(state.message, color = Color.Red)
                                Spacer(Modifier.height(12.dp))
                                Button(onClick = { branchViewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)), shape = RoundedCornerShape(8.dp)) {
                                    Text("Retry", color = Color.White)
                                }
                            }
                        }
                    }
                    is BranchUiState.Success -> {
                        if (filteredBranches.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Business, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(if (searchQuery.isNotBlank()) "No matching branches found" else "No branches found", color = Color.Gray, fontSize = 15.sp)
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                ) {
                                    items(pagedBranches) { branch ->
                                        val (badgeText, badgeColor) = statusColorsOf(branch.status)
                                        DataCard(
                                            item = branch,
                                            topBadgeText = badgeText,
                                            topBadgeTextColor = badgeColor,
                                            topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                            title = (branch.name ?: "Unnamed") + if (branch.isMainBranch) " ⭐" else "",
                                            subtitle = branch.branchId ?: "-",
                                            footerFields = listOf(
                                                DataCardField(icon = Icons.Default.LocationOn, text = locationOf(branch)),
                                                DataCardField(icon = Icons.Default.Person, text = branchHeadNameOf(branch))
                                            ),
                                            actions = listOf(
                                                MenuAction("Edit", Icons.Default.Edit) { editingBranch = branch }
                                            )
                                        )
                                    }
                                }
                                // ── Pagination Footer ──
                                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))) {
                                    Column {
                                        HorizontalDivider(color = Color(0xFFF0F0F0))
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "Showing ${if (filteredBranches.isEmpty()) 0 else (currentPage - 1) * itemsPerPage + 1} - ${minOf(currentPage * itemsPerPage, filteredBranches.size)} of ${filteredBranches.size}",
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
                message = "Branch limit exceeded ($currentBranchesCount/${planLimits?.branchLimit ?: 0}). Upgrade your plan to add more branches.",
                onDismiss = { showPlanLimitDialog = false },
                onUpgrade = { showPlanLimitDialog = false }
            )
        }
    }

    if (showAddDialog) {
        AddBranchDialog(
            staffList = staffList,
            isLoading = isCreating,
            onDismiss = { showAddDialog = false; branchViewModel.resetCreateState() },
            onCreate = { request ->
                if (isBranchLimitReached) { showPlanLimitDialog = true; return@AddBranchDialog }
                branchViewModel.createBranch(request)
            }
        )
    }

    editingBranch?.let { branch ->
        EditBranchDialog(
            branch = branch,
            staffList = staffList,
            isLoading = isUpdating,
            onDismiss = { editingBranch = null },
            onUpdate = { request ->
                branchViewModel.updateBranch(
                    branchId = branch.id,
                    request = UpdateBranchRequest(
                        name = request.name,
                        address = UpdateBranchAddress(street = request.street, city = request.city, postalCode = request.postalCode),
                        contactEmail = request.contactEmail,
                        contactMobile = request.contactMobile,
                        status = branch.status,
                        branchHead = request.branchHead
                    )
                )
            }
        )
    }
}


// ── Plan Limits Data Class ──
data class PlanLimits(
    val branchLimit: Int,
    val departmentLimit: Int,
    val employeeLimit: Int,
    val orderLimit: Int,
    val categoryLimit: Int
)



// ─────────────────────────────────────────────────────────────
// AddBranchDialog
// ─────────────────────────────────────────────────────────────

@Composable
fun AddBranchDialog(
    staffList: List<StaffDto>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onCreate: (CreateBranchRequest) -> Unit
) {
    // Form states
    var branchName by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactMobile by remember { mutableStateOf("") }
    var selectedStaff by remember { mutableStateOf("") }
    var staffExpanded by remember { mutableStateOf(false) }
    var isMainBranch by remember { mutableStateOf(false) }

    // Validation errors
    var nameError by remember { mutableStateOf(false) }
    var staffError by remember { mutableStateOf(false) }

    // Staff dropdown data
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
                .padding(horizontal = 16.dp),
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
                    Text(
                        "Add New Branch",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        "Create a new branch in your organization",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                // ── Branch Name ──
                Column {
                    FormLabel("Branch Name", isRequired = true)
                    FormTextField(
                        value = branchName,
                        onValueChange = { branchName = it; nameError = false },
                        placeholder = "Enter branch name",
                        isError = nameError,
                        errorMessage = "Branch name is required"
                    )
                }

                // ── Street Address ──
                Column {
                    FormLabel("Street Address")
                    FormTextField(
                        value = street,
                        onValueChange = { street = it },
                        placeholder = "Enter street address"
                    )
                }

                // ── City ──
                Column {
                    FormLabel("City")
                    FormTextField(
                        value = city,
                        onValueChange = { city = it },
                        placeholder = "Enter city"
                    )
                }

                // ── Postal Code ──
                Column {
                    FormLabel("Postal Code")
                    FormTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it },
                        placeholder = "Enter postal code",
                        keyboardType = KeyboardType.Number
                    )
                }

                // ── Contact Email ──
                Column {
                    FormLabel("Contact Email")
                    FormTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        placeholder = "Enter contact email",
                        keyboardType = KeyboardType.Email
                    )
                }

                // ── Contact Mobile ──
                Column {
                    FormLabel("Contact Mobile")
                    FormTextField(
                        value = contactMobile,
                        onValueChange = { contactMobile = it },
                        placeholder = "Enter contact mobile",
                        keyboardType = KeyboardType.Phone
                    )
                }

                // ── Branch Head Dropdown ──
                FormDropdown(
                    label = "Branch Head",
                    value = selectedStaffLabel,
                    expanded = staffExpanded,
                    onExpandChange = { expanded ->
                        if (staffList.isNotEmpty() || !expanded) staffExpanded = expanded
                    },
                    options = staffDisplayList,
                    onOptionSelected = { label ->
                        selectedStaff = staffIdMap[label] ?: ""
                        staffError = false
                    },
                    isRequired = true,
                    isError = staffError,
                    errorMessage = "Please select a branch head"
                )

                // ── Is Main Branch Toggle ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniSwitch(
                        checked = isMainBranch,
                        onCheckedChange = { isMainBranch = it }
                    )
                    Column {
                        Text(
                            "Set as Main Branch",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Text(
                            "The main branch will be the primary location",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Action Buttons — StepNavigationFab design, inline (not fixed) ──
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
                            enabled = branchName.isNotBlank(),
                            onClick = {
                                var hasError = false
                                if (branchName.isBlank()) {
                                    nameError = true
                                    hasError = true
                                }
                                if (selectedStaff.isEmpty()) {
                                    staffError = true
                                    hasError = true
                                }
                                if (hasError) return@Update

                                onCreate(
                                    CreateBranchRequest(
                                        name = branchName,
                                        address = CreateBranchAddress(
                                            street = street,
                                            city = city,
                                            postalCode = postalCode
                                        ),
                                        branchHead = selectedStaff,
                                        contactEmail = contactEmail,
                                        contactMobile = contactMobile,
                                        isMainBranch = isMainBranch
                                    )
                                )
                            }
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// BranchField Component
// ─────────────────────────────────────────────────────────────

@Composable
fun BranchField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) Color.Red else Color(0xFF374151)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    if (isError) Color(0xFFFFF3F3) else Color(0xFFF3F4F6),
                    RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isError) 1.dp else 0.dp,
                    color = Color.Red,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                ),
                cursorBrush = SolidColor(Color(0xFF3B3BF9))
            )

            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 16.dp)
                )
            }
        }

        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = Color.Red,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// EditBranchDialog
// ─────────────────────────────────────────────────────────────

@Composable
fun EditBranchDialog(
    branch: BranchItem,
    staffList: List<StaffDto>,
    onDismiss: () -> Unit,
    onUpdate: (EditBranchRequest) -> Unit,
    isLoading: Boolean = false
) {
    var branchName by remember { mutableStateOf(branch.name ?: "") }
    var street by remember { mutableStateOf(branch.address.street ?: "") }
    var city by remember { mutableStateOf(branch.address.city ?: "") }
    var state by remember { mutableStateOf(branch.address.state ?: "") }
    var postalCode by remember { mutableStateOf(branch.address.postalCode ?: "") }
    var contactEmail by remember { mutableStateOf(branch.contactEmail) }
    var contactMobile by remember { mutableStateOf(branch.contactMobile) }
    var selectedStaff by remember { mutableStateOf(branch.branchHead?.id ?: "") }
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
                Text(
                    "Edit Branch",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Column {
                    FormLabel("Branch Name")
                    FormTextField(value = branchName, onValueChange = { branchName = it }, placeholder = "Enter branch name")
                }
                Column {
                    FormLabel("Street Address")
                    FormTextField(value = street, onValueChange = { street = it }, placeholder = "Enter street address")
                }
                Column {
                    FormLabel("City")
                    FormTextField(value = city, onValueChange = { city = it }, placeholder = "Enter city")
                }
                Column {
                    FormLabel("State")
                    FormTextField(value = state, onValueChange = { state = it }, placeholder = "Enter state")
                }
                Column {
                    FormLabel("Postal Code")
                    FormTextField(value = postalCode, onValueChange = { postalCode = it }, placeholder = "Enter postal code", keyboardType = KeyboardType.Number)
                }
                Column {
                    FormLabel("Contact Email")
                    FormTextField(value = contactEmail, onValueChange = { contactEmail = it }, placeholder = "Enter contact email", keyboardType = KeyboardType.Email)
                }
                Column {
                    FormLabel("Contact Mobile")
                    FormTextField(value = contactMobile, onValueChange = { contactMobile = it }, placeholder = "Enter contact mobile", keyboardType = KeyboardType.Phone)
                }

                // ── Branch Head Dropdown ──
                FormDropdown(
                    label = "Branch Head",
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

                Spacer(modifier = Modifier.height(4.dp))

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
                            onClick = {
                                onUpdate(
                                    EditBranchRequest(
                                        name = branchName,
                                        street = street,
                                        city = city,
                                        state = state,
                                        postalCode = postalCode,
                                        contactEmail = contactEmail,
                                        contactMobile = contactMobile,
                                        branchHead = selectedStaff
                                    )
                                )
                            }
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// EditBranchField
// ─────────────────────────────────────────────────────────────

@Composable
fun EditBranchField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF111827)
            ),
            cursorBrush = SolidColor(Color(0xFF3B3BF9))
        )
    }
}


private fun locationOf(branch: BranchItem): String =
    listOf(branch.address.city ?: "", branch.address.state ?: "", branch.address.street ?: "")
        .filter { it.isNotBlank() }.joinToString(", ").ifEmpty { "—" }

private fun branchHeadNameOf(branch: BranchItem): String =
    branch.branchHead?.let { "${it.firstName} ${it.lastName}" } ?: "-"

private fun statusColorsOf(status: String): Pair<String, Color> = when (status.lowercase()) {
    "active" -> "Active" to Color(0xFF16A34A)
    "inactive" -> "Inactive" to Color(0xFF6B7280)
    else -> "Unknown" to Color(0xFF9CA3AF)
}

// ─────────────────────────────────────────────────────────────
// 🔁 ONE column list for branches — table header, table cells and
// card fields all reuse this.
// ─────────────────────────────────────────────────────────────
//private fun branchColumns(onEditClick: (BranchItem) -> Unit): List<DataColumn<BranchItem>> = listOf(
//    DataColumn("branchId", "Branch ID", 180.dp) { b ->
//        Text(b.branchId ?: "-", fontSize = 13.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.Medium,
//            maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("name", "Branch Name", 220.dp) { b ->
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//            Text(b.name ?: "Unnamed", fontSize = 13.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.Medium,
//                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
//            if (b.isMainBranch) Text("⭐", fontSize = 13.sp)
//        }
//    },
//    DataColumn("location", "Location", 220.dp) { b ->
//        Text(
//            locationOf(b),
//            fontSize = 13.sp,
//            color = Color(0xFF374151),
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//    },
//    DataColumn("employees", "Employees", 110.dp) {
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//            Icon(Icons.Default.People, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
//            Text("0", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
//        }
//    },
//    DataColumn("activeOrders", "Active Orders", 130.dp) {
//        Box(modifier = Modifier.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                Icon(Icons.Default.ShoppingBag, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
//                Text("0", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
//            }
//        }
//    },
//    DataColumn("managers", "Managers", 150.dp) { b ->
//        Text(branchHeadNameOf(b), fontSize = 13.sp, color = Color(0xFF374151), maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("status", "Status", 100.dp) { b ->
//        val (badgeText, badgeColor) = statusColorsOf(b.status)
//        Box(modifier = Modifier.border(1.dp, badgeColor, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
//            Text(badgeText, fontSize = 12.sp, color = badgeColor, fontWeight = FontWeight.Medium)
//        }
//    },
//    DataColumn("action", "Action", 80.dp, cellAlignment = Alignment.Center) { b ->
//        ActionDropdownMenu(
//            icon = Icons.Default.MoreVert,
//            actions = listOf(MenuAction("Edit", Icons.Default.Edit) { onEditClick(b) })
//        )
//    }
//)
//
//// ── Card view — same call site as before ──
//@Composable
//fun BranchCardItem(branch: BranchItem, onEditClick: (BranchItem) -> Unit) {
//    val columns = branchColumns(onEditClick)
//    val statusColumn = columns.first { it.key == "status" }
//    val locationColumn = columns.first { it.key == "location" }
//    val footerFields = columns.filter { it.key in listOf("managers", "employees", "activeOrders") }
//
//    DataCard(
//        item = branch,
//        leading = { Box(modifier = Modifier.width(4.dp).height(18.dp).background(Color(0xFF3B3BF9), RoundedCornerShape(2.dp))) },
//        title = {
//            Column {
//                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                    Text(branch.name ?: "Unnamed", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
//                    if (branch.isMainBranch) Text("⭐", fontSize = 13.sp)
//                }
//                Text(branch.branchId ?: "-", fontSize = 12.sp, color = Color(0xFF3B3BF9))
//            }
//        },
//        trailing = {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                statusColumn.cellContent(branch)
//                ActionDropdownMenu(
//                    icon = Icons.Default.MoreVert,
//                    actions = listOf(MenuAction("Edit", Icons.Default.Edit) { onEditClick(branch) })
//                )
//            }
//        },
//        middleContent = {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
//                Spacer(Modifier.width(6.dp))
//                locationColumn.cellContent(branch)
//            }
//        },
//        fields = footerFields,
//        fieldsPerRow = 3,
//        footerBackground = Color(0xFFF8F9FB)
//    )
//}

// ── Request models ──
data class EditBranchRequest(
    val name: String,
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val contactEmail: String,
    val contactMobile: String,
    val branchHead: String
)