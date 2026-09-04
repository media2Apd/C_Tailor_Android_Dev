@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "VariableNeverRead"
)
package com.cuso.mobile.view.home.branch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.model.settings.BranchItem
import com.cuso.mobile.model.settings.CreateBranchAddress
import com.cuso.mobile.model.settings.CreateBranchRequest
import com.cuso.mobile.model.settings.UpdateBranchAddress
import com.cuso.mobile.model.settings.UpdateBranchRequest
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.close_color
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AppErrorState
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
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
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.BranchUiState
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.CreateBranchUiState
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.cuso.mobile.viewmodel.UpdateBranchUiState

enum class BranchScreenMode {
    LIST, FORM
}

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

    var currentScreenMode by remember { mutableStateOf(BranchScreenMode.LIST) }
    var editingBranch by remember { mutableStateOf<BranchItem?>(null) }
    var showPlanLimitDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10

    val snackbarHostState = remember { SnackbarHostState() }

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
                currentScreenMode = BranchScreenMode.LIST
                branchViewModel.resetCreateState()
                branchViewModel.loadBranches()
                successMessage = state.message?.takeIf { it.isNotBlank() } ?: "Branch created successfully"
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
                currentScreenMode = BranchScreenMode.LIST
                editingBranch = null
                branchViewModel.loadBranches()
                branchViewModel.resetUpdateState()
                successMessage = state.message?.takeIf { it.isNotBlank() } ?: "Branch updated successfully"
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
    val pagedBranches = filteredBranches.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)

    Box(Modifier.fillMaxSize()) {
        when (currentScreenMode) {
            BranchScreenMode.LIST -> {
                FabScaffold(
                    fab = FabConfig(
                        label = "Add Branch",
                        icon = Icons.Default.Add,
                        onClick = {
                            if (isBranchLimitReached) {
                                showPlanLimitDialog = true
                            } else {
                                editingBranch = null
                                currentScreenMode = BranchScreenMode.FORM
                            }
                        }
                    ),
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        TitleBar("Branch Management", onClose = onBack)

                        Column(modifier = Modifier.fillMaxWidth()) {
                            SearchFilterBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = "Search Branches...",
                                accentColor = BluePrimary
                            )
                        }
                        HorizontalDivider(color = title_border)

                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            when (uiState) {
                                is BranchUiState.Loading -> ListSkeleton()
                                is BranchUiState.Error -> {
                                    AppErrorState(
                                        title = "Failed to load branch",
                                        message = "Something went wrong. Please check your connection and try again.",
                                        onRetry = { branchViewModel.refresh() }
                                    )
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
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(bottom = 80.dp)
                                        ) {
                                            items(pagedBranches) { branch ->
                                                DataCard(
                                                    item = branch,
                                                    title = branch.name ?: "-",
                                                    titleColor = Color(0xFF0F172A),
                                                    subtitle = branch.branchId ?: "-",
                                                    topBadgeText = branch.status,
                                                    topBadgeTextColor = Color(0xFF16A34A),
                                                    topBadgeBgColor = Color(0xFFDCFCE7),
                                                    topBadgeShowDot = false,
                                                    topBadgeInline = true,
                                                    showHeaderDivider = true,
                                                    actions = listOf(
                                                        MenuAction("Edit", Icons.Default.Edit) {
                                                            editingBranch = branch
                                                            currentScreenMode = BranchScreenMode.FORM
                                                        }
                                                    ),
                                                    content = {
                                                        val iconColor = Color(0xFF64748B)
                                                        val textBodyColor = close_color

                                                        Column(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
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
                                                                    text = locationOf(branch),
                                                                    fontSize = 14.sp,
                                                                    color = textBodyColor,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }

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
                                                                        text = "Employees",
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
                                                                        text = "₹ Active Orders",
                                                                        fontSize = 14.sp,
                                                                        color = textBodyColor
                                                                    )
                                                                }
                                                            }

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
                                                                        withStyle(SpanStyle(color = Color(0xFF64748B), fontWeight = FontWeight.Normal)) {
                                                                            append("Manager: ")
                                                                        }
                                                                        withStyle(
                                                                            SpanStyle(
                                                                                color = Color(
                                                                                    0xFF0F172A
                                                                                ),
                                                                                fontWeight = FontWeight.SemiBold
                                                                            )
                                                                        ) {
                                                                            append(branchHeadNameOf(branch))
                                                                        }
                                                                    },
                                                                    fontSize = 14.sp
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

            BranchScreenMode.FORM -> {
                AddBranchPage(
                    branch = editingBranch,
                    staffList = staffList,
                    isLoading = if (editingBranch != null) isUpdating else isCreating,
                    onBack = {
                        currentScreenMode = BranchScreenMode.LIST
                        editingBranch = null
                        branchViewModel.resetCreateState()
                        branchViewModel.resetUpdateState()
                    },
                    onSubmit = { branchName, street, city, postalCode, contactEmail, contactMobile, selectedStaff, isMainBranch ->
                        if (editingBranch != null) {
                            branchViewModel.updateBranch(
                                branchId = editingBranch!!.id,
                                request = UpdateBranchRequest(
                                    name = branchName,
                                    address = UpdateBranchAddress(
                                        street = street,
                                        city = city,
                                        postalCode = postalCode
                                    ),
                                    contactEmail = contactEmail,
                                    contactMobile = contactMobile,
                                    status = editingBranch!!.status,
                                    branchHead = selectedStaff
                                )
                            )
                        } else {
                            if (isBranchLimitReached) {
                                showPlanLimitDialog = true
                                return@AddBranchPage
                            }
                            branchViewModel.createBranch(
                                CreateBranchRequest(
                                    name = branchName,
                                    address = CreateBranchAddress(street = street, city = city, postalCode = postalCode),
                                    branchHead = selectedStaff,
                                    contactEmail = contactEmail,
                                    contactMobile = contactMobile,
                                    isMainBranch = isMainBranch
                                )
                            )
                        }
                    }
                )
            }
        }

        if (showPlanLimitDialog) {
            PlanLimitDialog(
                title = "Plan Limit Reached",
                message = "Branch limit exceeded ($currentBranchesCount/${planLimits?.branchLimit ?: 0}). Upgrade your plan to add more branches.",
                onDismiss = { showPlanLimitDialog = false },
                onUpgrade = { }
            )
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

@Composable
fun AddBranchPage(
    branch: BranchItem? = null,
    staffList: List<StaffDto>,
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onSubmit: (
        name: String,
        street: String,
        city: String,
        postalCode: String,
        contactEmail: String,
        contactMobile: String,
        selectedStaff: String,
        isMainBranch: Boolean
    ) -> Unit
) {
    val isEditMode = branch != null

    var branchName by remember(branch) { mutableStateOf(branch?.name ?: "") }
    var street by remember(branch) { mutableStateOf(branch?.address?.street ?: "") }
    var city by remember(branch) { mutableStateOf(branch?.address?.city ?: "") }
    var postalCode by remember(branch) { mutableStateOf(branch?.address?.postalCode ?: "") }
    var contactEmail by remember(branch) { mutableStateOf(branch?.contactEmail ?: "") }
    var contactMobile by remember(branch) { mutableStateOf(branch?.contactMobile ?: "") }
    var selectedStaff by remember(branch) { mutableStateOf(branch?.branchHead?.id ?: "") }
    var staffExpanded by remember { mutableStateOf(false) }
    var isMainBranch by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var staffError by remember { mutableStateOf(false) }

    val staffDisplayList = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == selectedStaff }?.key ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar(if (isEditMode) "Edit Branch" else "Add New Branch", onClose = onBack)
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
                Column {
                    FormLabel("Branch Name", isRequired = true)
                    FormTextField(
                        value = branchName,
                        onValueChange = {
                            branchName = it
                            nameError = false
                        },
                        placeholder = "Enter branch name",
                        isError = nameError,
                        errorMessage = if (nameError) "Branch name is required" else null
                    )
                }

                Column {
                    FormLabel("Address / Location")
                    FormTextField(
                        value = street,
                        onValueChange = { street = it },
                        placeholder = "Enter address or location"
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
                    FormLabel("Contact Email")
                    FormTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        placeholder = "Enter contact email",
                        keyboardType = KeyboardType.Email
                    )
                }

//                Column {
//                    FormLabel("Contact Mobile")
//                    FormTextField(
//                        value = contactMobile,
//                        onValueChange = { contactMobile = it },
//                        placeholder = "Enter contact mobile",
//                        keyboardType = KeyboardType.Phone
//                    )
//                }

                Column {
                    FormLabel("Branch Manager", isRequired = true)
                    FormDropdown(
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
                        errorMessage = if (staffError) "Please select a branch head" else null
                    )
                }

//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    modifier = Modifier.padding(top = 4.dp)
//                ) {
//                    MiniSwitch(
//                        checked = isMainBranch,
//                        onCheckedChange = { isMainBranch = it }
//                    )
//                    Column {
//                        Text(
//                            text = "Set as Main Branch",
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color(0xFF374151)
//                        )
//                        Text(
//                            text = "The main branch will be the primary location",
//                            fontSize = 12.sp,
//                            color = Color(0xFF9CA3AF)
//                        )
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
                label = if (isEditMode) "Update Branch" else "Add Branch",
                onClick = {
                    if (branchName.isBlank()) {
                        nameError = true
                        return@Update
                    }
                    if (selectedStaff.isEmpty()) {
                        staffError = true
                        return@Update
                    }
                    onSubmit(
                        branchName,
                        street,
                        city,
                        postalCode,
                        contactEmail,
                        contactMobile,
                        selectedStaff,
                        isMainBranch
                    )
                }
            )
        )
    }
}

private fun locationOf(branch: BranchItem): String =
    listOf(branch.address.city ?: "", branch.address.state ?: "", branch.address.street ?: "")
        .filter { it.isNotBlank() }.joinToString(", ").ifEmpty { "—" }

private fun branchHeadNameOf(branch: BranchItem): String =
    branch.branchHead?.let { "${it.firstName} ${it.lastName}" } ?: "-"

data class PlanLimits(
    val branchLimit: Int,
    val departmentLimit: Int,
    val employeeLimit: Int,
    val orderLimit: Int,
    val categoryLimit: Int
)