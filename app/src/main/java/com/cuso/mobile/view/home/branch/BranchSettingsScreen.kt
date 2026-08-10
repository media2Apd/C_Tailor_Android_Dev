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


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.model.BranchItem
import com.cuso.mobile.model.CreateBranchAddress
import com.cuso.mobile.model.CreateBranchRequest
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.model.UpdateBranchAddress
import com.cuso.mobile.model.UpdateBranchRequest
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.TitleColor
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
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
import com.cuso.mobile.viewmodel.BranchUiState
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.CreateBranchUiState
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.cuso.mobile.viewmodel.UpdateBranchUiState
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.blurScrim
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// BranchSettingsScreen - Updated with SmoothBottomSheet for both Add and Edit
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
    var addSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var editSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var showPlanLimitDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Blur states - same pattern as DesignationScreen, driven by SmoothBottomSheet's own callback
    var addSheetBlur by remember { mutableStateOf(0.dp) }
    var editSheetBlur by remember { mutableStateOf(0.dp) }

    // True whenever either sheet is not hidden, used to decide if content should blur
    val isAnySheetOpen = addSheetState != SheetValue.Hidden || editSheetState != SheetValue.Hidden
    val currentBlur = when {
        addSheetState != SheetValue.Hidden -> addSheetBlur
        editSheetState != SheetValue.Hidden -> editSheetBlur
        else -> 0.dp
    }

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

    LaunchedEffect(createState) {
        when (val state = createState) {
            is CreateBranchUiState.Success -> {
                addSheetState = SheetValue.Hidden
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
                editSheetState = SheetValue.Hidden
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

    // Removed Scaffold's topBar slot for TitleBar - it lived in a separate layout
    // region from the sheet's scrim, which let the scrim bleed into it.
    // TitleBar is now placed directly inside FabScaffold's body, outside the blurred Box,
    // exactly like DesignationScreen. This guarantees it never blurs or dims.
    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Add Branch",
            icon = Icons.Default.Add,
            onClick = {
                if (isBranchLimitReached) {
                    showPlanLimitDialog = true
                } else {
                    addSheetState = SheetValue.Expanded
                }
            },
            endPadding = 16.dp,
            bottomPadding = 16.dp,
            draggable = true
        ),
        snackbarHostState = snackbarHostState
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

            // ── HEADER - Always solid, never blurred or dimmed ──
            Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                TitleBar("Branches", onClose = onBack)
            }

            // ── MAIN CONTENT - blur applied only here while a sheet is open ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blurScrim(if (isAnySheetOpen) currentBlur else 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ScreenBreadcrumb(
                        segments = listOf("Settings", "Branches"),
                        onClick = { /* TODO: hook to modules panel */ }
                    )

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search Branches...",
                            onFilterClick = { /* TODO: open filter drawer */ },
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                        )
                    }

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
                                        Text("Something went wrong, Please try again later", color = Color.Red)
                                        Spacer(Modifier.height(12.dp))
                                        Button(
                                            onClick = { branchViewModel.refresh() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Retry", color = whiteBg)
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
                                            Text(
                                                if (searchQuery.isNotBlank()) "No matching branches found" else "No branches found",
                                                color = Color.Gray,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 80.dp)
                                    ) {
                                        items(pagedBranches) { branch ->
                                            val (badgeText, badgeColor) = statusColorsOf(branch.status)
                                            DataCard(
                                                item = branch,
                                                topBadgeText = badgeText,
                                                topBadgeTextColor = badgeColor,
                                                topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                                topBadgeInline = false,
                                                title = (branch.name ?: "Unnamed") + if (branch.isMainBranch) " ⭐" else "",
                                                titleFontWeight = FontWeight.Bold,
                                                titleColor = Color(0xFF111827),
                                                subtitle = branch.branchId ?: "-",
                                                dateText = branch.branchId ?: "-",
                                                showDateIcon = false,
                                                footerFields = listOf(
                                                    DataCardField(
                                                        icon = Icons.Default.LocationOn,
                                                        text = locationOf(branch),
                                                        iconTint = Color(0xFF9CA3AF),
                                                        textColor = Color(0xFF374151),
                                                        label = "Location",
                                                        asRow = true,
                                                        labelColor = Color(0xFF9CA3AF)
                                                    ),
                                                    DataCardField(
                                                        icon = Icons.Default.Person,
                                                        text = branchHeadNameOf(branch),
                                                        iconTint = Color(0xFF9CA3AF),
                                                        textColor = Color(0xFF374151),
                                                        label = "Managers",
                                                        asRow = true,
                                                        labelColor = Color(0xFF9CA3AF)
                                                    )
                                                ),
                                                footerAsRows = true,
                                                actions = listOf(
                                                    MenuAction("Edit", Icons.Default.Edit) {
                                                        editingBranch = branch
                                                        editSheetState = SheetValue.Expanded
                                                    }
                                                )
                                            )
                                        }
                                        item { Spacer(Modifier.height(8.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── SmoothBottomSheet for Add Branch ──
        // peekHeight defines the initial half-open height; user can drag up to expand to full screen.
        SmoothBottomSheet(
            state = addSheetState,
            onStateChange = { newState ->
                addSheetState = newState
                if (newState == SheetValue.Hidden) {
                    branchViewModel.resetCreateState()
                }
            },
            peekHeight = 380.dp,
            topInset = 60.dp,
            maxBlurRadius = 14.dp,
            maxScrimAlpha = 0.35f,
            sheetBackgroundColor = whiteBg,
            collapsedCornerRadius = 24.dp,
            dragCloseEnabled = true,
            scrollableContent = true,
            onDismissRequest = {
                addSheetState = SheetValue.Hidden
                branchViewModel.resetCreateState()
            },
            onBlurScrimChange = { blur, _ ->
                addSheetBlur = blur
            }
        ) {
            AddBranchSheetContent(
                staffList = staffList,
                isLoading = isCreating,
                onDismiss = {
                    addSheetState = SheetValue.Hidden
                    branchViewModel.resetCreateState()
                },
                onCreate = { request ->
                    if (isBranchLimitReached) {
                        showPlanLimitDialog = true
                        return@AddBranchSheetContent
                    }
                    branchViewModel.createBranch(request)
                }
            )
        }

        // ── SmoothBottomSheet for Edit Branch ──
        // Moved inside FabScaffold's body (was outside the Scaffold entirely before),
        // so it shares the same layout context and blur wiring as the Add sheet.
        editingBranch?.let { branch ->
            SmoothBottomSheet(
                state = editSheetState,
                onStateChange = { newState ->
                    editSheetState = newState
                    if (newState == SheetValue.Hidden) {
                        editingBranch = null
                        branchViewModel.resetUpdateState()
                    }
                },
                peekHeight = 380.dp,
                topInset = 60.dp,
                maxBlurRadius = 14.dp,
                maxScrimAlpha = 0.35f,
                sheetBackgroundColor = whiteBg,
                collapsedCornerRadius = 24.dp,
                dragCloseEnabled = true,
                scrollableContent = true,
                onDismissRequest = {
                    editSheetState = SheetValue.Hidden
                    editingBranch = null
                    branchViewModel.resetUpdateState()
                },
                onBlurScrimChange = { blur, _ ->
                    editSheetBlur = blur
                }
            ) {
                EditBranchSheetContent(
                    branch = branch,
                    staffList = staffList,
                    isLoading = isUpdating,
                    onDismiss = {
                        editSheetState = SheetValue.Hidden
                        editingBranch = null
                        branchViewModel.resetUpdateState()
                    },
                    onUpdate = { request ->
                        branchViewModel.updateBranch(
                            branchId = branch.id,
                            request = UpdateBranchRequest(
                                name = request.name,
                                address = UpdateBranchAddress(
                                    street = request.street,
                                    city = request.city,
                                    postalCode = request.postalCode
                                ),
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

        // ── Plan Limit Dialog ──
        if (showPlanLimitDialog) {
            PlanLimitDialog(
                title = "Plan Limit Reached",
                message = "Branch limit exceeded ($currentBranchesCount/${planLimits?.branchLimit ?: 0}). Upgrade your plan to add more branches.",
                onDismiss = { showPlanLimitDialog = false },
                onUpgrade = { showPlanLimitDialog = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// AddBranchSheetContent
// ─────────────────────────────────────────────────────────────

@Composable
fun AddBranchSheetContent(
    staffList: List<StaffDto>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onCreate: (CreateBranchRequest) -> Unit
) {
    var branchName by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactMobile by remember { mutableStateOf("") }
    var selectedStaff by remember { mutableStateOf("") }
    var staffExpanded by remember { mutableStateOf(false) }
    var isMainBranch by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var staffError by remember { mutableStateOf(false) }

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
                "Add New Branch",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                "Create a new branch in your organization",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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

        Column {
            FormLabel("Street Address")
            FormTextField(
                value = street,
                onValueChange = { street = it },
                placeholder = "Enter street address"
            )
        }

        Column {
            FormLabel("City")
            FormTextField(
                value = city,
                onValueChange = { city = it },
                placeholder = "Enter city"
            )
        }

        Column {
            FormLabel("Postal Code")
            FormTextField(
                value = postalCode,
                onValueChange = { postalCode = it },
                placeholder = "Enter postal code",
                keyboardType = KeyboardType.Number
            )
        }

        Column {
            FormLabel("Contact Email")
            FormTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                placeholder = "Enter contact email",
                keyboardType = KeyboardType.Email
            )
        }

        Column {
            FormLabel("Contact Mobile")
            FormTextField(
                value = contactMobile,
                onValueChange = { contactMobile = it },
                placeholder = "Enter contact mobile",
                keyboardType = KeyboardType.Phone
            )
        }

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
                    if (branchName.isBlank()) {
                        nameError = true
                        hasError = true
                    }
                    if (selectedStaff.isEmpty()) {
                        staffError = true
                        hasError = true
                    }
                    if (hasError) return@Button

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
                        color = whiteBg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Branch", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = whiteBg)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// EditBranchSheetContent
// ─────────────────────────────────────────────────────────────

@Composable
fun EditBranchSheetContent(
    branch: BranchItem,
    staffList: List<StaffDto>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onUpdate: (EditBranchRequest) -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Edit Branch",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                "Update branch information",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column {
            FormLabel("Branch Name", isRequired = true)
            FormTextField(
                value = branchName,
                onValueChange = { branchName = it },
                placeholder = "Enter branch name"
            )
        }

        Column {
            FormLabel("Street Address")
            FormTextField(
                value = street,
                onValueChange = { street = it },
                placeholder = "Enter street address"
            )
        }

        Column {
            FormLabel("City")
            FormTextField(
                value = city,
                onValueChange = { city = it },
                placeholder = "Enter city"
            )
        }

        Column {
            FormLabel("State")
            FormTextField(
                value = state,
                onValueChange = { state = it },
                placeholder = "Enter state"
            )
        }

        Column {
            FormLabel("Postal Code")
            FormTextField(
                value = postalCode,
                onValueChange = { postalCode = it },
                placeholder = "Enter postal code",
                keyboardType = KeyboardType.Number
            )
        }

        Column {
            FormLabel("Contact Email")
            FormTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                placeholder = "Enter contact email",
                keyboardType = KeyboardType.Email
            )
        }

        Column {
            FormLabel("Contact Mobile")
            FormTextField(
                value = contactMobile,
                onValueChange = { contactMobile = it },
                placeholder = "Enter contact mobile",
                keyboardType = KeyboardType.Phone
            )
        }

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
            },
            isRequired = true
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
                        color = whiteBg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Update Branch", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = whiteBg)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// Helper Functions
// ─────────────────────────────────────────────────────────────

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

data class PlanLimits(
    val branchLimit: Int,
    val departmentLimit: Int,
    val employeeLimit: Int,
    val orderLimit: Int,
    val categoryLimit: Int
)