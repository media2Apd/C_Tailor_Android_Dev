@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "unusedVariable",
    "VariableNeverRead"
)
package com.cuso.mobile.view.home.designation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.settings.DesignationItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.view.composable.AppErrorState
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DeleteModel
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.viewmodel.DepartmentViewModel
import com.cuso.mobile.viewmodel.DesignationCreateState
import com.cuso.mobile.viewmodel.DesignationDeleteState
import com.cuso.mobile.viewmodel.DesignationUiState
import com.cuso.mobile.viewmodel.DesignationUpdateState
import com.cuso.mobile.viewmodel.DesignationViewModel

enum class DesignationScreenMode {
    LIST, FORM
}

@Composable
fun DesignationScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val viewModel: DesignationViewModel = hiltViewModel()
    val departmentViewModel: DepartmentViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val departmentUiState by departmentViewModel.uiState.collectAsStateWithLifecycle()

    var currentScreenMode by remember { mutableStateOf(DesignationScreenMode.LIST) }
    var editingDesignation by remember { mutableStateOf<DesignationItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf<DesignationItem?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadDesignations()
        departmentViewModel.loadDepartments()
    }

    LaunchedEffect(createState) {
        when (val state = createState) {
            is DesignationCreateState.Success -> {
                currentScreenMode = DesignationScreenMode.LIST
                viewModel.resetCreateState()
                viewModel.loadDesignations()
                successMessage = "Designation created successfully"
            }
            is DesignationCreateState.Error -> {
                viewModel.resetCreateState()
                errorMessage = state.message
            }
            else -> Unit
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is DesignationUpdateState.Success -> {
                currentScreenMode = DesignationScreenMode.LIST
                editingDesignation = null
                viewModel.resetUpdateState()
                viewModel.loadDesignations()
                successMessage = state.message
            }
            is DesignationUpdateState.Error -> {
                viewModel.resetUpdateState()
                errorMessage = state.message
            }
            else -> Unit
        }
    }

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is DesignationDeleteState.Success -> {
                showDeleteDialog = null
                viewModel.resetDeleteState()
                viewModel.loadDesignations()
                successMessage = state.message
            }
            is DesignationDeleteState.Error -> {
                viewModel.resetDeleteState()
                errorMessage = state.message
            }
            else -> Unit
        }
    }

    val isCreating = createState is DesignationCreateState.Loading
    val isUpdating = updateState is DesignationUpdateState.Loading

    val allDesignations = (uiState as? DesignationUiState.Success)?.items ?: emptyList()
    val departments = (departmentUiState as? com.cuso.mobile.viewmodel.DepartmentUiState.Success)?.departments ?: emptyList()

    val filteredDesignations = allDesignations.filter { d ->
        searchQuery.isBlank() || d.name.contains(searchQuery, ignoreCase = true) || d.code.contains(searchQuery, ignoreCase = true)
    }
    val pagedDesignations = filteredDesignations.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)

    Box(Modifier.fillMaxSize()) {
        when (currentScreenMode) {
            DesignationScreenMode.LIST -> {
                FabScaffold(
                    fab = FabConfig(
                        label = "Add Designation",
                        icon = Icons.Default.Add,
                        onClick = {
                            editingDesignation = null
                            currentScreenMode = DesignationScreenMode.FORM
                        }
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        TitleBar("Designation", onClose = onBack)

                        Column(modifier = Modifier.fillMaxWidth()) {
                            SearchFilterBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = "Search Designations...",
                                accentColor = BluePrimary,
                                borderColor = BorderGray,
                                textSecondaryColor = TextSecondary
                            )
                        }
                        HorizontalDivider(color = title_border)

                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            when (uiState) {
                                is DesignationUiState.Loading -> ListSkeleton()
                                is DesignationUiState.Error -> {
                                    AppErrorState(
                                        title = "Failed to load designation",
                                        message = "Something went wrong. Please check your connection and try again.",
                                        onRetry = { viewModel.loadDesignations() }
                                    )
                                }
                                is DesignationUiState.Success -> {
                                    if (filteredDesignations.isEmpty()) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("No designations found", fontSize = tokens.bodyMedium, color = Color.Gray)
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(bottom = 80.dp)
                                        ) {
                                            items(pagedDesignations) { item ->
                                                val (badgeText, badgeColor) = if (item.status) "Active" to Color(0xFF16A34A) else "Inactive" to Color(0xFF6B7280)
                                                DataCard(
                                                    item = item,
                                                    title = item.name,
                                                    titleColor = Color(0xFF0F172A),
                                                    subtitle = item.code,
                                                    topBadgeText = badgeText,
                                                    topBadgeTextColor = badgeColor,
                                                    topBadgeBgColor = if (item.status) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                                    topBadgeShowDot = false,
                                                    topBadgeInline = true,
                                                    showHeaderDivider = true,
                                                    actions = listOf(
                                                        MenuAction("Edit", Icons.Default.Edit) {
                                                            editingDesignation = item
                                                            currentScreenMode = DesignationScreenMode.FORM
                                                        },
                                                        MenuAction("Delete", Icons.Default.Delete, tint = Color.Red, textColor = Color.Red) {
                                                            showDeleteDialog = item
                                                        }
                                                    ),
                                                    content = {
                                                        val iconColor = Color(0xFF64748B)
                                                        val textBodyColor = Color(0xFF334155)

                                                        Column(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            // Dept Row
                                                            Row(
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
                                                                    text = buildAnnotatedString {
                                                                        withStyle(SpanStyle(color = Color(0xFF64748B), fontWeight = FontWeight.Normal)) {
                                                                            append("Dept: ")
                                                                        }
                                                                        withStyle(SpanStyle(color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)) {
                                                                            append("Inventory not found")
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
                                                                    text =  "Not Found",
                                                                    fontSize = 14.sp,
                                                                    color = textBodyColor,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }

                                                            // Employees Row
                                                            Row(
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
                                                                    text = "$ - Employees",
                                                                    fontSize = 14.sp,
                                                                    color = textBodyColor
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

            DesignationScreenMode.FORM -> {
                AddDesignationPage(
                    designation = editingDesignation,
                    departmentList = departments,
                    isLoading = if (editingDesignation != null) isUpdating else isCreating,
                    onBack = {
                        currentScreenMode = DesignationScreenMode.LIST
                        editingDesignation = null
                        viewModel.resetCreateState()
                        viewModel.resetUpdateState()
                    },
                    onSubmit = { name, code, departmentId, description ->
                        if (editingDesignation != null) {
                            viewModel.updateDesignation(editingDesignation!!.id, name, code, description)
                        } else {
                            viewModel.createDesignation(name, code, description)
                        }
                    }
                )
            }
        }

        showDeleteDialog?.let { designation ->
            DeleteModel(
                title = "Delete Designation",
                message = "Are you sure you want to delete '${designation.name}'?",
                onDismiss = { showDeleteDialog = null },
                onDelete = { viewModel.deleteDesignation(designation.id) }
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
fun AddDesignationPage(
    designation: DesignationItem? = null,
    departmentList: List<com.cuso.mobile.model.settings.DepartmentItem> = emptyList(),
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onSubmit: (name: String, code: String, departmentId: String, description: String?) -> Unit
) {
    val isEditMode = designation != null

    var name by remember(designation) { mutableStateOf(designation?.name ?: "") }
    var code by remember(designation) { mutableStateOf(designation?.code ?: "") }
    var selectedDepartment by remember(designation) { mutableStateOf(designation?.description?: "") }
    var description by remember(designation) { mutableStateOf(designation?.description ?: "") }

    var departmentExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    val departmentDisplayList = departmentList.map { it.name }
    val departmentIdMap = departmentList.associate { it.name to it._id }
    val selectedDepartmentLabel = departmentIdMap.entries.firstOrNull { it.value == selectedDepartment }?.key ?: ""

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
                    TitleBar(if (isEditMode) "Edit Designation" else "Add New Designation", onClose = onBack)
                }
                HorizontalDivider(color = title_border)


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Designation Name
                Column {
                    FormLabel("Designation Name", isRequired = true)
                    FormTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        placeholder = "Enter designation name",
                        isError = nameError,
                        errorMessage = if (nameError) "Designation name is required" else null
                    )
                }

                // 2. Designation Code
                Column {
                    FormLabel("Designation Code")
                    FormTextField(
                        value = code,
                        onValueChange = {
                            code = it
                            codeError = false
                        },
                        placeholder = "Enter code",
                        isError = codeError,
                        errorMessage = if (codeError) "Designation code is required" else null
                    )
                }

                // 3. Department Dropdown
                Column {
                    FormLabel("Department")
                    FormDropdown(
                        value = selectedDepartmentLabel,
                        expanded = departmentExpanded,
                        onExpandChange = { expanded ->
                            if (departmentList.isNotEmpty() || !expanded) departmentExpanded = expanded
                        },
                        options = departmentDisplayList,
                        onOptionSelected = { label ->
                            selectedDepartment = departmentIdMap[label] ?: ""
                        },
                        isRequired = false
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
                label = if (isEditMode) "Update Designation" else "Add Designation",
                onClick = {
                    var hasError = false
                    if (name.isBlank()) {
                        nameError = true
                        hasError = true
                    }
                    if (code.isBlank()) {
                        codeError = true
                        hasError = true
                    }
                    if (hasError) return@Update

                    onSubmit(name, code, selectedDepartment, description.ifBlank { null })
                }
            )
        )
    }
}