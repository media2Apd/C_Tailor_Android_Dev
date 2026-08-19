@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable", "VariableNeverRead"
)
package com.cuso.mobile.view.home.branch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.disabled
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
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

    // Separate blur states per sheet for independent control
    var addSheetBlur by remember { mutableStateOf(0.dp) }
    var addSheetScrim by remember { mutableFloatStateOf(0f) }
    var editSheetBlur by remember { mutableStateOf(0.dp) }
    var editSheetScrim by remember { mutableFloatStateOf(0f) }

    val isAnySheetOpen = addSheetState != SheetValue.Hidden || editSheetState != SheetValue.Hidden

    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

    val allBranches = (uiState as? BranchUiState.Success)?.branches ?: emptyList()
    val currentBranchesCount = allBranches.size
    val isBranchLimitReached = planLimits?.let { currentBranchesCount >= it.branchLimit } ?: false

    LaunchedEffect(createState) {
        when (val state = createState) {
            is CreateBranchUiState.Success -> {
                addSheetBlur = 0.dp        // ← reset BEFORE the sheet disappears
                addSheetScrim = 0f
                addSheetState = SheetValue.Hidden
                branchViewModel.resetCreateState()
                branchViewModel.loadBranches()
                successMessage = state.message?.takeIf { it.isNotBlank() }
                    ?: "Branch created successfully"
            }
            is CreateBranchUiState.Error -> {
                branchViewModel.resetCreateState()
                if (state.message.contains("limit", true) || state.message.contains("exceed", true)) {
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
            is UpdateBranchUiState.Success -> {
                editSheetBlur = 0.dp       // ← reset BEFORE editingBranch = null destroys the sheet
                editSheetScrim = 0f
                editSheetState = SheetValue.Hidden
                editingBranch = null
                branchViewModel.loadBranches()
                branchViewModel.resetUpdateState()
                successMessage = state.message?.takeIf { it.isNotBlank() }
                    ?: "Branch updated successfully"
            }
            is UpdateBranchUiState.Error -> {
                branchViewModel.resetUpdateState()
                errorMessage = state.message
            }
            else -> Unit
        }
    }

    val isUpdating = updateState is UpdateBranchUiState.Loading
    val isCreating = createState is CreateBranchUiState.Loading

    val filteredBranches = allBranches.filter { b ->
        searchQuery.isBlank() ||
                (b.name?.contains(searchQuery, ignoreCase = true) == true) ||
                (b.branchId?.contains(searchQuery, ignoreCase = true) == true)
    }
    val totalPages = maxOf(1, if (filteredBranches.isNotEmpty()) (filteredBranches.size + itemsPerPage - 1) / itemsPerPage else 1)
    val pagedBranches = filteredBranches.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)

    Box(Modifier.fillMaxSize()) {
        // Using Scaffold topBar slot ensures TitleBar is on top of BottomSheets and Scrims
        Scaffold(
            topBar = {
                TitleBar("Branch Management", onClose = onBack)
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->

            //   OUTER wrapper Box — holds list content, both bottom sheets, and
            //   the islands. Order inside this Box controls draw/z-order in Compose:
            //   whatever is declared LAST is drawn on TOP.
            Box(modifier = Modifier.fillMaxSize()) {

                // ── Layer 1: main list content + plan limit dialog ──────────
                Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

                    FabScaffold(
                        fab = FabConfig(
                            label = "Add Branch",
                            icon = Icons.Default.Add,
                            onClick = {
                                if (isBranchLimitReached) {
                                    showPlanLimitDialog = true
                                } else {
                                    addSheetState = SheetValue.Expanded
                                }
                            }
                        ),
                        fabVisible = !isAnySheetOpen,
                        snackbarHostState = snackbarHostState
                    ) {
                        // Blur is applied only to this Column, not the TitleBar
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .background(Color.Transparent)
                                .blurScrim(addSheetBlur.coerceAtLeast(editSheetBlur))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ScreenBreadcrumb(
                                    segments = listOf("Settings", "Branch Management"),
                                    onClick = {})

                                SearchFilterBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    modifier = Modifier.padding(
                                        horizontal = 20.dp,
                                        vertical = 12.dp
                                    ),
                                    placeholder = "Search Branches...",
                                    accentColor = BluePrimary
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF0F0F0))

                            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                when (uiState) {
                                    is BranchUiState.Loading -> ListSkeleton()
                                    is BranchUiState.Error -> {
                                        Box(
                                            Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    null,
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    "Something went wrong, Please try again later",
                                                    color = Color.Red
                                                )
                                                Spacer(Modifier.height(12.dp))
                                                Button(
                                                    onClick = { branchViewModel.refresh() },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF3B3BF9)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Retry", color = whiteBg)
                                                }
                                            }
                                        }
                                    }

                                    is BranchUiState.Success -> {
                                        if (filteredBranches.isEmpty()) {
                                            Box(
                                                Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        Icons.Default.Business,
                                                        null,
                                                        tint = Color.LightGray,
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(
                                                        if (searchQuery.isNotBlank()) "No matching branches found" else "No branches found",
                                                        color = Color.Gray,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                            }
                                        } else {
                                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                                items(pagedBranches) { branch ->
                                                    val (badgeText, badgeColor) = statusColorsOf(
                                                        branch.status
                                                    )
                                                    DataCard(
                                                        item = branch,
                                                        title = branch.branchId ?: "-",
                                                        titleColor = title_color,
                                                        subtitle = "Employees: not found · Active Orders: not found",
                                                        topBadgeText = badgeText,
                                                        topBadgeTextColor = badgeColor,
                                                        topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                                        topBadgeInline = true,
                                                        footerFields = listOf(
                                                            DataCardField(
                                                                text = branch.name ?: "Unnamed",
                                                                label = "Branch Name",
                                                                asRow = true
                                                            ),
                                                            DataCardField(
                                                                text = locationOf(branch).let { loc ->
                                                                    if (loc.length > 30) loc.take(30) + "…" else loc
                                                                },
                                                                label = "Location",
                                                                asRow = true
                                                            ),
                                                            DataCardField(
                                                                text = branchHeadNameOf(branch),
                                                                label = "Managers",
                                                                asRow = true
                                                            )
                                                        ),
                                                        actions = listOf(
                                                            MenuAction("Edit", Icons.Default.Edit) {
                                                                editingBranch = branch
                                                                editSheetState = SheetValue.Expanded
                                                            }
                                                        )
                                                    )
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
                            onUpgrade = { /* navigate to upgrade */ }
                        )
                    }
                }

                // ── Layer 2: Add Branch BottomSheet ─────────────────────────
                SmoothBottomSheet(
                    state = addSheetState,
                    onStateChange = { newState ->
                        addSheetState = newState
                        if (newState == SheetValue.Hidden) {
                            addSheetBlur = 0.dp
                            addSheetScrim = 0f
                            branchViewModel.resetCreateState()
                        }
                    },
                    peekHeight = 380.dp,
                    topInset = 66.dp,
                    sheetBackgroundColor = whiteBg,
                    collapsedCornerRadius = 24.dp,
                    dragCloseEnabled = true,
                    scrollableContent = true,
                    onDismissRequest = {
                        addSheetBlur = 0.dp
                        addSheetScrim = 0f
                        addSheetState = SheetValue.Hidden
                        branchViewModel.resetCreateState()
                    },
                    onBlurScrimChange = { r, s ->
                        // Ignore stale/late callbacks that fire after we've already
                        // force-reset to 0 on close — prevents the blur getting stuck.
                        if (addSheetState != SheetValue.Hidden) {
                            addSheetBlur = r
                            addSheetScrim = s
                        }
                    }
                ) {
                    AddBranchSheetContent(
                        staffList = staffList,
                        isLoading = isCreating,
                        onDismiss = {
                            addSheetBlur = 0.dp
                            addSheetScrim = 0f
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

                // ── Layer 3: Edit Branch BottomSheet ────────────────────────
                editingBranch?.let { branch ->
                    SmoothBottomSheet(
                        state = editSheetState,
                        onStateChange = { newState ->
                            editSheetState = newState
                            if (newState == SheetValue.Hidden) {
                                editSheetBlur = 0.dp
                                editSheetScrim = 0f
                                editingBranch = null
                                branchViewModel.resetUpdateState()
                            }
                        },
                        peekHeight = 380.dp,
                        topInset = 66.dp,
                        sheetBackgroundColor = whiteBg,
                        collapsedCornerRadius = 24.dp,
                        dragCloseEnabled = true,
                        scrollableContent = true,
                        onDismissRequest = {
                            editSheetBlur = 0.dp
                            editSheetScrim = 0f
                            editSheetState = SheetValue.Hidden
                            editingBranch = null
                            branchViewModel.resetUpdateState()
                        },
                        onBlurScrimChange = { r, s ->
                            // Ignore stale/late callbacks that fire after we've already
                            // force-reset to 0 on close — prevents the blur getting stuck.
                            if (editSheetState != SheetValue.Hidden) {
                                editSheetBlur = r
                                editSheetScrim = s
                            }
                        }
                    ) {
                        EditBranchSheetContent(
                            branch = branch,
                            staffList = staffList,
                            isLoading = isUpdating,
                            onDismiss = {
                                editSheetBlur = 0.dp
                                editSheetScrim = 0f
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
            }
        }

        // ── Layer 4 (TOPMOST): Dynamic Island success/error banners ─
        //   Declared LAST so they always draw above the list, both
        //   bottom sheets, and their blur scrims — never cropped or
        //   hidden behind a closing sheet's blur.
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
// Content Components & Helpers
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Add New Branch", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Create a new branch in your organization", fontSize = 13.sp, color = Color(0xFF6B7280))
        }

        Column {
            FormLabel("Branch Name", isRequired = true)
            FormTextField(value = branchName, onValueChange = { branchName = it; nameError = false }, placeholder = "Enter branch name", isError = nameError, errorMessage = "Branch name is required")
        }

        Column {
            FormLabel("Street Address")
            FormTextField(value = street, onValueChange = { street = it }, placeholder = "Enter street address")
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                FormLabel("City")
                FormTextField(value = city, onValueChange = { city = it }, placeholder = "City")
            }
            Column(modifier = Modifier.weight(1f)) {
                FormLabel("Postal Code")
                FormTextField(value = postalCode, onValueChange = { postalCode = it }, placeholder = "Postal Code", keyboardType = KeyboardType.Number)
            }
        }

        Column {
            FormLabel("Contact Email")
            FormTextField(value = contactEmail, onValueChange = { contactEmail = it }, placeholder = "Enter contact email", keyboardType = KeyboardType.Email)
        }

        Column {
            FormLabel("Contact Mobile")
            FormTextField(value = contactMobile, onValueChange = { contactMobile = it }, placeholder = "Enter contact mobile", keyboardType = KeyboardType.Phone)
        }

        FormDropdown(
            label = "Branch Head",
            value = selectedStaffLabel,
            expanded = staffExpanded,
            onExpandChange = { expanded -> if (staffList.isNotEmpty() || !expanded) staffExpanded = expanded },
            options = staffDisplayList,
            onOptionSelected = { label -> selectedStaff = staffIdMap[label] ?: ""; staffError = false },
            isRequired = true,
            isError = staffError,
            errorMessage = "Please select a branch head"
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniSwitch(checked = isMainBranch, onCheckedChange = { isMainBranch = it })
            Column {
                Text("Set as Main Branch", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                Text("The main branch will be the primary location", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFD1D5DB))) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
            }
            Button(
                onClick = {
                    if (branchName.isBlank()) { nameError = true; return@Button }
                    if (selectedStaff.isEmpty()) { staffError = true; return@Button }
                    onCreate(CreateBranchRequest(branchName, CreateBranchAddress(street, city, postalCode), selectedStaff, contactEmail, contactMobile, isMainBranch))
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = disabled
                ),
                enabled = !isLoading
            ) {
                if (isLoading)
                    CirculerProgressIndicatorSmall()
                else Text("Create Branch", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = whiteBg)
            }
        }
    }
}

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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Edit Branch", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Update branch information", fontSize = 13.sp, color = Color(0xFF6B7280))
        }

        Column {
            FormLabel("Branch Name", isRequired = true)
            FormTextField(value = branchName, onValueChange = { branchName = it }, placeholder = "Enter branch name")
        }

        Column {
            FormLabel("Street Address")
            FormTextField(value = street, onValueChange = { street = it }, placeholder = "Enter street address")
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                FormLabel("City")
                FormTextField(value = city, onValueChange = { city = it }, placeholder = "City")
            }
            Column(modifier = Modifier.weight(1f)) {
                FormLabel("Postal Code")
                FormTextField(value = postalCode, onValueChange = { postalCode = it }, placeholder = "Postal Code", keyboardType = KeyboardType.Number)
            }
        }

        Column {
            FormLabel("Contact Email")
            FormTextField(value = contactEmail, onValueChange = { contactEmail = it }, placeholder = "Enter contact email", keyboardType = KeyboardType.Email)
        }

        FormDropdown(
            label = "Branch Head",
            value = selectedStaffLabel,
            expanded = staffExpanded,
            onExpandChange = { expanded -> if (staffList.isNotEmpty() || !expanded) staffExpanded = expanded },
            options = staffDisplayList,
            onOptionSelected = { label -> selectedStaff = staffIdMap[label] ?: "" },
            isRequired = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFD1D5DB))) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
            }
            Button(
                onClick = { onUpdate(EditBranchRequest(branchName, street, city, state, postalCode, contactEmail, contactMobile, selectedStaff)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                enabled = !isLoading
            ) {
                if (isLoading)
                    CirculerProgressIndicatorSmall()
                else Text("Update Branch", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = whiteBg)
            }
        }
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

data class EditBranchRequest(val name: String, val street: String, val city: String, val state: String, val postalCode: String, val contactEmail: String, val contactMobile: String, val branchHead: String)
data class PlanLimits(val branchLimit: Int, val departmentLimit: Int, val employeeLimit: Int, val orderLimit: Int, val categoryLimit: Int)