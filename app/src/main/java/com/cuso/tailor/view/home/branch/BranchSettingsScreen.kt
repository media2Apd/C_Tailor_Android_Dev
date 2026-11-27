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

package com.cuso.tailor.view.home.branch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
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
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.StaffDto
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.model.settings.CreateBranchAddress
import com.cuso.tailor.model.settings.CreateBranchRequest
import com.cuso.tailor.model.settings.UpdateBranchAddress
import com.cuso.tailor.model.settings.UpdateBranchRequest
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.formatCurrency
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.PlanLimitDialog
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.CreateBranchUiState
import com.cuso.tailor.viewmodel.PermissionViewModel
import com.cuso.tailor.viewmodel.SalesViewModel
import com.cuso.tailor.viewmodel.UpdateBranchUiState

enum class BranchScreenMode {
    LIST, FORM
}

@Composable
fun BranchSettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onUpgradePlan: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val branchViewModel: BranchViewModel = hiltViewModel()
    val salesViewModel: SalesViewModel = hiltViewModel()
    val permissionViewModel: PermissionViewModel = hiltViewModel()

    val uiState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branchesPagingState by branchViewModel.branchesPagingState.collectAsStateWithLifecycle()
    val updateState by branchViewModel.updateState.collectAsStateWithLifecycle()
    val createState by branchViewModel.createState.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val planLimitsState by permissionViewModel.planLimits.collectAsStateWithLifecycle()

    var currentScreenMode by remember { mutableStateOf(BranchScreenMode.LIST) }
    var editingBranch by remember { mutableStateOf<BranchItem?>(null) }
    var showPlanLimitDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Fetch branches, staff list, and plan limits on screen entry
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        salesViewModel.fetchStaff()
        permissionViewModel.fetchPlanLimitsAndModules()
    }

    val allBranches = (uiState as? BranchUiState.Success)?.branches ?: emptyList()
    val branchLimitResult = remember(planLimitsState, allBranches.size) {
        permissionViewModel.getPlanLimitStatus("branch", fallbackUsedCount = allBranches.size)
    }
    val isBranchLimitReached = branchLimitResult.isExceeded

    // Handle branch creation state transitions
    LaunchedEffect(createState) {
        when (val state = createState) {
            is CreateBranchUiState.Success -> {
                currentScreenMode = BranchScreenMode.LIST
                branchViewModel.resetCreateState()
                branchViewModel.refresh()
                permissionViewModel.fetchPlanLimitsAndModules()
                DynamicIslandManager.showSuccess(
                    state.message?.takeIf { it.isNotBlank() } ?: "Branch created successfully"
                )
            }
            is CreateBranchUiState.Error -> {
                branchViewModel.resetCreateState()
                if (state.message.contains("limit", true) || state.message.contains("exceed", true)) {
                    showPlanLimitDialog = true
                } else {
                    DynamicIslandManager.showError(state.message)
                }
            }
            else -> Unit
        }
    }

    // Handle branch update state transitions
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateBranchUiState.Success -> {
                currentScreenMode = BranchScreenMode.LIST
                editingBranch = null
                branchViewModel.refresh()
                branchViewModel.resetUpdateState()
                DynamicIslandManager.showSuccess(
                    state.message?.takeIf { it.isNotBlank() } ?: "Branch updated successfully"
                )
            }
            is UpdateBranchUiState.Error -> {
                branchViewModel.resetUpdateState()
                DynamicIslandManager.showError(state.message)
            }
            else -> Unit
        }
    }

    val isUpdating = updateState is UpdateBranchUiState.Loading
    val isCreating = createState is CreateBranchUiState.Loading

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
                        TitleBar(title = "Branch Management", onClose = onBack)

                        Column(modifier = Modifier.fillMaxWidth()) {
                            SearchFilterBar(
                                query = searchQuery,
                                onQueryChange = { query ->
                                    searchQuery = query
                                    branchViewModel.onBranchSearchChanged(query)
                                },
                                placeholder = "Search Branches...",
                                accentColor = BluePrimary
                            )
                        }
                        HorizontalDivider(color = title_border)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (uiState is BranchUiState.Error) {
                                AppErrorState(
                                    title = "Failed to load branches",
                                    message = (uiState as BranchUiState.Error).message.ifBlank {
                                        "Something went wrong. Please check your connection and try again."
                                    },
                                    onRetry = { branchViewModel.refresh() }
                                )
                            } else if (!branchesPagingState.isInitialLoading && branchesPagingState.items.isEmpty()) {
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
                                            fontSize = tokens.bodyMedium
                                        )
                                    }
                                }
                            } else {
                                PaginatedDataList(
                                    state = branchesPagingState,
                                    onLoadMore = { branchViewModel.loadMoreBranches() },
                                    modifier = Modifier.fillMaxSize()
                                ) { branch ->
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
                                            val iconColor = headerGrey
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
                                                            text = "${formatCurrency()} Active Orders",
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
                                                            withStyle(
                                                                SpanStyle(
                                                                    color = headerGrey,
                                                                    fontWeight = FontWeight.Normal
                                                                )
                                                            ) {
                                                                append("Manager: ")
                                                            }
                                                            withStyle(
                                                                SpanStyle(
                                                                    color = Color(0xFF0F172A),
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
                    onSubmit = { branchName, flatNo, street, areaZone, city, subdivisionName, pincode, countryName, contactEmail, contactMobile, selectedStaff ->
                        val optionalBranchHead = selectedStaff.ifBlank { null }
                        if (editingBranch != null) {
                            branchViewModel.updateBranch(
                                branchId = editingBranch!!.id,
                                request = UpdateBranchRequest(
                                    name = branchName,
                                    address = UpdateBranchAddress(
                                        flatNo = flatNo,
                                        street = street,
                                        areaZone = areaZone,
                                        city = city,
                                        subdivisionName = subdivisionName,
                                        pincode = pincode,
                                        countryName = countryName
                                    ),
                                    contactEmail = contactEmail,
                                    contactMobile = contactMobile,
                                    status = editingBranch!!.status.ifBlank { "Active" },
                                    branchHead = optionalBranchHead
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
                                    address = CreateBranchAddress(
                                        flatNo = flatNo,
                                        street = street,
                                        areaZone = areaZone,
                                        city = city,
                                        subdivisionName = subdivisionName,
                                        pincode = pincode,
                                        countryName = countryName
                                    ),
                                    branchHead = optionalBranchHead,
                                    contactEmail = contactEmail,
                                    contactMobile = contactMobile,
                                    isMainBranch = false
                                )
                            )
                        }
                    }
                )
            }
        }

        if (showPlanLimitDialog) {
            PlanLimitDialog(
                title = "Branch limit reached",
                message = "This organization has reached the maximum number of branches allowed under your current plan.",
                resourceLabel = "Branches",
                usedCount = branchLimitResult.used,
                maxLimit = branchLimitResult.limit,
                onDismiss = { showPlanLimitDialog = false },
                onUpgradePlan = {
                    showPlanLimitDialog = false
                    onUpgradePlan()
                }
            )
        }
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
        flatNo: String,
        street: String,
        areaZone: String,
        city: String,
        subdivisionName: String,
        pincode: String,
        countryName: String,
        contactEmail: String,
        contactMobile: String,
        selectedStaff: String
    ) -> Unit
) {
    val tokens = LocalAppTokens.current
    val isEditMode = branch != null

    // Form state matching BranchModal.jsx fields
    var branchName by remember(branch) { mutableStateOf(branch?.name.orEmpty()) }
    var flatNo by remember(branch) { mutableStateOf(branch?.address?.flatNo.orEmpty()) }
    var street by remember(branch) { mutableStateOf(branch?.address?.street.orEmpty()) }
    var areaZone by remember(branch) { mutableStateOf(branch?.address?.areaZone.orEmpty()) }
    var city by remember(branch) { mutableStateOf(branch?.address?.city.orEmpty()) }
    var subdivisionName by remember(branch) {
        mutableStateOf(branch?.address?.subdivisionName ?: branch?.address?.state.orEmpty())
    }
    var pincode by remember(branch) {
        mutableStateOf(branch?.address?.pincode ?: branch?.address?.postalCode.orEmpty())
    }
    var countryName by remember(branch) {
        mutableStateOf(branch?.address?.countryName ?: branch?.address?.country.orEmpty())
    }
    var contactEmail by remember(branch) { mutableStateOf(branch?.contactEmail.orEmpty()) }
    var contactMobile by remember(branch) { mutableStateOf(branch?.contactMobile.orEmpty()) }
    var selectedStaff by remember(branch) { mutableStateOf(branch?.branchHead?.id.orEmpty()) }
    var staffExpanded by remember { mutableStateOf(false) }

    // Inline validation error flags for required fields
    var nameError by remember { mutableStateOf(false) }
    var streetError by remember { mutableStateOf(false) }
    var cityError by remember { mutableStateOf(false) }
    var pincodeError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }

    // Include an explicit optional reset entry so Branch Head remains optional
    val optionalStaffPlaceholder = "Select Branch Head (Optional)"
    val staffDisplayList = remember(staffList) {
        listOf(optionalStaffPlaceholder) + staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    }
    val staffIdMap = remember(staffList) {
        staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == selectedStaff }?.key ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar(
                    title = if (isEditMode) "Edit Branch" else "Add New Branch",
                    onClose = onBack
                )
                HorizontalDivider(color = title_border)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding)
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // 1. Branch Name (Required)
                Column {
                    FormLabel("Branch Name", isRequired = true)
                    FormTextField(
                        value = branchName,
                        onValueChange = {
                            branchName = it
                            nameError = false
                        },
                        placeholder = "e.g. Chennai Main Branch",
                        isError = nameError,
                        errorMessage = if (nameError) "Branch name is required" else null
                    )
                }

                // 2. Flat / Building No
                Column {
                    FormLabel("Flat / Building No")
                    FormTextField(
                        value = flatNo,
                        onValueChange = { flatNo = it },
                        placeholder = "e.g. Suite 402, Tower B"
                    )
                }

                // 3. Street Address (Required)
                Column {
                    FormLabel("Street Address", isRequired = true)
                    FormTextField(
                        value = street,
                        onValueChange = {
                            street = it
                            streetError = false
                        },
                        placeholder = "e.g. 12 Anna Salai, Guindy",
                        isError = streetError,
                        errorMessage = if (streetError) "Street address is required" else null
                    )
                }

                // 4. Area / Landmark
                Column {
                    FormLabel("Area / Landmark")
                    FormTextField(
                        value = areaZone,
                        onValueChange = { areaZone = it },
                        placeholder = "e.g. Near Metro Station"
                    )
                }

                // 5. City (Required) & State / Province
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("City", isRequired = true)
                        FormTextField(
                            value = city,
                            onValueChange = {
                                city = it
                                cityError = false
                            },
                            placeholder = "e.g. Chennai",
                            isError = cityError,
                            errorMessage = if (cityError) "City is required" else null
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("State / Province")
                        FormTextField(
                            value = subdivisionName,
                            onValueChange = { subdivisionName = it },
                            placeholder = "e.g. Tamil Nadu"
                        )
                    }
                }

                // 6. Pincode (Required) & Country
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Pincode", isRequired = true)
                        FormTextField(
                            value = pincode,
                            onValueChange = { input ->
                                // Strip spaces and cap at 10 characters (matches BranchModal.jsx)
                                pincode = input.filter { !it.isWhitespace() }.take(10)
                                pincodeError = false
                            },
                            placeholder = "e.g. 600032",
                            keyboardType = KeyboardType.Text,
                            isError = pincodeError,
                            errorMessage = if (pincodeError) "Pincode is required" else null
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Country")
                        FormTextField(
                            value = countryName,
                            onValueChange = { countryName = it },
                            placeholder = "e.g. India"
                        )
                    }
                }

                // 7. Contact Email (Required)
                Column {
                    FormLabel("Contact Email", isRequired = true)
                    FormTextField(
                        value = contactEmail,
                        onValueChange = {
                            contactEmail = it
                            emailError = false
                        },
                        placeholder = "branch@company.com",
                        keyboardType = KeyboardType.Email,
                        isError = emailError,
                        errorMessage = if (emailError) "Valid contact email is required" else null
                    )
                }

                // 8. Contact Mobile (Required)
                Column {
                    FormLabel("Contact Mobile", isRequired = true)
                    FormTextField(
                        value = contactMobile,
                        onValueChange = { input ->
                            // Allow digits only and cap at 10 digits (matches BranchModal.jsx)
                            contactMobile = input.filter { it.isDigit() }.take(10)
                            mobileError = false
                        },
                        placeholder = "10-digit mobile number",
                        keyboardType = KeyboardType.Phone,
                        isError = mobileError,
                        errorMessage = if (mobileError) "10-digit mobile number is required" else null
                    )
                }

                // 9. Branch Head (Optional)
                Column {
                    FormLabel("Branch Head")
                    FormDropdown(
                        value = selectedStaffLabel.ifBlank { optionalStaffPlaceholder },
                        expanded = staffExpanded,
                        onExpandChange = { expanded ->
                            if (staffList.isNotEmpty() || !expanded) staffExpanded = expanded
                        },
                        options = staffDisplayList,
                        onOptionSelected = { label ->
                            selectedStaff = if (label == optionalStaffPlaceholder) {
                                ""
                            } else {
                                staffIdMap[label].orEmpty()
                            }
                        },
                        isRequired = false
                    )
                }
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
                label = if (isEditMode) "Update Branch" else "Create Branch",
                onClick = {
                    val isNameMissing = branchName.isBlank()
                    val isStreetMissing = street.isBlank()
                    val isCityMissing = city.isBlank()
                    val isPincodeMissing = pincode.isBlank()
                    val isEmailInvalid = contactEmail.isBlank() || !contactEmail.contains("@")
                    val isMobileInvalid = contactMobile.length < 10

                    nameError = isNameMissing
                    streetError = isStreetMissing
                    cityError = isCityMissing
                    pincodeError = isPincodeMissing
                    emailError = isEmailInvalid
                    mobileError = isMobileInvalid

                    if (isNameMissing || isStreetMissing || isCityMissing || isPincodeMissing || isEmailInvalid || isMobileInvalid) {
                        return@Update
                    }

                    onSubmit(
                        branchName.trim(),
                        flatNo.trim(),
                        street.trim(),
                        areaZone.trim(),
                        city.trim(),
                        subdivisionName.trim(),
                        pincode.trim(),
                        countryName.trim(),
                        contactEmail.trim(),
                        contactMobile.trim(),
                        selectedStaff
                    )
                }
            )
        )
    }
}

private fun locationOf(branch: BranchItem): String =
    listOfNotNull(
        branch.address.flatNo,
        branch.address.street,
        branch.address.areaZone,
        branch.address.city,
        branch.address.subdivisionName ?: branch.address.state,
        branch.address.countryName ?: branch.address.country,
        branch.address.pincode ?: branch.address.postalCode
    )
        .filter { it.isNotBlank() }
        .joinToString(", ")
        .ifEmpty { "—" }

private fun branchHeadNameOf(branch: BranchItem): String =
    branch.branchHead?.let { "${it.firstName} ${it.lastName}" } ?: "-"