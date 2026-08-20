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
package com.cuso.mobile.view.home.role

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.blurScrim
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// RoleSettingsScreen - Scaffold with TitleBar in topBar slot,
// FabScaffold (draggable "Add Role" FAB) wrapping the DataCard
// list, SmoothBottomSheet for Add/Edit Role
// ─────────────────────────────────────────────────────────────

@Composable
fun RoleSettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val roleList = remember {
        mutableStateListOf(
            RoleItem("1", "Role Name", "Sales manager", "Managers - Men", "Mt-01", "Active", "Org Manager", 8),
            RoleItem("2", "Role Name", "Sales manager", "Managers - Men", "Mt-01", "Active", "Org Manager", 8),
            RoleItem("3", "Role Name", "Sales manager", "Managers - Men", "Mt-01", "Active", "Org Manager", 8),
            RoleItem("4", "Role Name", "Sales manager", "Managers - Men", "Mt-01", "Active", "Org Manager", 8),
            RoleItem("5", "Role Name", "Sales manager", "Managers - Men", "Mt-01", "Active", "Org Manager", 8)
        )
    }

    val reportingOptions = remember {
        listOf("Org Manager", "Regional Manager", "Branch Manager", "HR Manager")
    }

    var editingRole by remember { mutableStateOf<RoleItem?>(null) }
    var addSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var editSheetState by remember { mutableStateOf(SheetValue.Hidden) }

    var searchQuery by remember { mutableStateOf("") }

//    val snackbarHostState = remember { SnackbarHostState() }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Blur states driven by SmoothBottomSheet's own callback
    var addSheetBlur by remember { mutableStateOf(0.dp) }
    var editSheetBlur by remember { mutableStateOf(0.dp) }

    val isAnySheetOpen = addSheetState != SheetValue.Hidden || editSheetState != SheetValue.Hidden
    val currentBlur = when {
        addSheetState != SheetValue.Hidden -> addSheetBlur
        editSheetState != SheetValue.Hidden -> editSheetBlur
        else -> 0.dp
    }

    val filteredRoles = roleList.filter { r ->
        searchQuery.isBlank() ||
                r.title.contains(searchQuery, ignoreCase = true) ||
                r.roleId.contains(searchQuery, ignoreCase = true)
    }
    Box(
        Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                    TitleBar("Role Management", onClose = onBack)
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent
            // NOTE: floatingActionButton slot removed - FabScaffold below now owns the "Add Role" FAB
        ) { paddingValues ->

            FabScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                fab = FabConfig(
                    label = "Add Role",
                    icon = Icons.Default.Add,
                    onClick = { addSheetState = SheetValue.Expanded },
                    endPadding = 16.dp,
                    bottomPadding = 16.dp,
                    draggable = true
                ),
                fabVisible = !isAnySheetOpen, // NEW: FAB fades/scales out while add or edit sheet is open
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blurScrim(if (isAnySheetOpen) currentBlur else 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScreenBreadcrumb(
                            segments = listOf("Settings", "Role Management"),
                            onClick = { }
                        )

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            SearchFilterBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = "Search Role...",
                                onFilterClick = { }
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))

                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            if (filteredRoles.isEmpty()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Badge,
                                            null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            if (searchQuery.isNotBlank()) "No matching roles found" else "No roles found",
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
                                    items(filteredRoles) { role ->
                                        val (badgeText, badgeColor) = statusColorsOfRole(role.status)
                                        DataCard(
                                            item = role,
                                            smalltitle = role.title,
                                            titleColor = title_color,
                                            subtitle = null,
                                            topBadgeText = badgeText,
                                            topBadgeTextColor = badgeColor,
                                            topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                            topBadgeInline = true,
                                            footerFields = listOf(
                                                DataCardField(
                                                    text = role.reportingTo,
                                                    textColor = title_color,
                                                    label = "Reporting To",
                                                    asRow = true,
                                                    labelColor = mutedText
                                                ),
                                                DataCardField(
                                                    text = role.category,
                                                    textColor = title_color,
                                                    label = "Category",
                                                    asRow = true,
                                                    labelColor = mutedText
                                                ),
                                                DataCardField(
                                                    text = role.employeeCount.toString(),
                                                    textColor = title_color,
                                                    label = "Employees",
                                                    asRow = true,
                                                    labelColor = mutedText
                                                ),
                                                DataCardField(
                                                    text = role.roleId,
                                                    textColor = title_color,
                                                    label = "Role ID",
                                                    asRow = true,
                                                    labelColor = mutedText
                                                )
                                            ),
                                            actions = listOf(
                                                MenuAction("Edit", Icons.Default.Edit) {
                                                    editingRole = role
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

                // ── SmoothBottomSheet for Add Role ──
                SmoothBottomSheet(
                    state = addSheetState,
                    onStateChange = { newState -> addSheetState = newState },
                    peekHeight = 380.dp,
                    topInset = 66.dp,
                    maxBlurRadius = 14.dp,
                    maxScrimAlpha = 0.35f,
                    sheetBackgroundColor = whiteBg,
                    collapsedCornerRadius = 24.dp,
                    dragCloseEnabled = true,
                    scrollableContent = true,
                    onDismissRequest = { addSheetState = SheetValue.Hidden },
                    onBlurScrimChange = { blur, _ ->
                        addSheetBlur = blur
                    }
                ) {
                    AddRoleSheetContent(
                        reportingOptions = reportingOptions,
                        onDismiss = { addSheetState = SheetValue.Hidden },
                        onCreate = { request ->
                            roleList.add(
                                RoleItem(
                                    id = (roleList.size + 1).toString(),
                                    title = "Role Name",
                                    name = request.name,
                                    category = request.category,
                                    roleId = "Mt-0${roleList.size + 1}",
                                    status = request.status,
                                    reportingTo = request.reportingTo,
                                    employeeCount = 0
                                )
                            )
                            addSheetState = SheetValue.Hidden
                            successMessage = "Role Created Successfully"
                        }
                    )
                }

                // ── SmoothBottomSheet for Edit Role ──
                editingRole?.let { role ->
                    SmoothBottomSheet(
                        state = editSheetState,
                        onStateChange = { newState ->
                            editSheetState = newState
                            if (newState == SheetValue.Hidden) {
                                editingRole = null
                            }
                        },
                        peekHeight = 380.dp,
                        topInset = 66.dp,
                        maxBlurRadius = 14.dp,
                        maxScrimAlpha = 0.35f,
                        sheetBackgroundColor = whiteBg,
                        collapsedCornerRadius = 24.dp,
                        dragCloseEnabled = true,
                        scrollableContent = true,
                        onDismissRequest = {
                            editSheetState = SheetValue.Hidden
                            editingRole = null
                        },
                        onBlurScrimChange = { blur, _ ->
                            editSheetBlur = blur
                        }
                    ) {
                        EditRoleSheetContent(
                            role = role,
                            reportingOptions = reportingOptions,
                            onDismiss = {
                                editSheetState = SheetValue.Hidden
                                editingRole = null
                            },
                            onUpdate = { request ->
                                val index = roleList.indexOfFirst { it.id == role.id }
                                if (index != -1) {
                                    roleList[index] = roleList[index].copy(
                                        name = request.name,
                                        category = request.category,
                                        status = request.status,
                                        reportingTo = request.reportingTo
                                    )
                                }
                                editSheetState = SheetValue.Hidden
                                editingRole = null
                                successMessage = "Role Updated Successfully"
                            }
                        )
                    }
                }
            }
        }
        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// AddRoleSheetContent
// ─────────────────────────────────────────────────────────────

@Composable
fun AddRoleSheetContent(
    reportingOptions: List<String>,
    onDismiss: () -> Unit,
    onCreate: (CreateRoleRequest) -> Unit
) {
    var roleName by remember { mutableStateOf("") }
    var roleCategory by remember { mutableStateOf("") }
    var roleStatus by remember { mutableStateOf("") }
    var statusExpanded by remember { mutableStateOf(false) }
    var reportingTo by remember { mutableStateOf("") }
    var reportingExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var reportingError by remember { mutableStateOf(false) }

    val statusOptions = listOf("Active", "Inactive")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Add New Role",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                "Create a new role",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column {
            FormLabel("Role Name", isRequired = true)
            FormTextField(
                value = roleName,
                onValueChange = { roleName = it; nameError = false },
                placeholder = "Enter role name",
                isError = nameError,
                errorMessage = "Role name is required"
            )
        }

        Column {
            FormLabel("Role Category")
            FormTextField(
                value = roleCategory,
                onValueChange = { roleCategory = it },
                placeholder = "Enter role category"
            )
        }

        Column {
            FormLabel("Role ID (Auto generated)")
            FormTextField(
                value = "",
                onValueChange = { },
                placeholder = "Auto generated",
                enabled = false
            )
        }

        FormDropdown(
            label = "Role Status",
            value = roleStatus.ifBlank { "Select an option" },
            expanded = statusExpanded,
            onExpandChange = { statusExpanded = it },
            options = statusOptions,
            onOptionSelected = { roleStatus = it }
        )

        FormDropdown(
            label = "Reporting to",
            value = reportingTo.ifBlank { "Select an option" },
            expanded = reportingExpanded,
            onExpandChange = { reportingExpanded = it },
            options = reportingOptions,
            onOptionSelected = { reportingTo = it; reportingError = false },
            isRequired = true,
            isError = reportingError,
            errorMessage = "Please select a reporting manager"
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
                    if (roleName.isBlank()) {
                        nameError = true
                        hasError = true
                    }
                    if (reportingTo.isBlank()) {
                        reportingError = true
                        hasError = true
                    }
                    if (hasError) return@Button

                    onCreate(
                        CreateRoleRequest(
                            name = roleName,
                            category = roleCategory,
                            status = roleStatus.ifBlank { "Active" },
                            reportingTo = reportingTo
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text("Add Role", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = whiteBg)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// EditRoleSheetContent
// ─────────────────────────────────────────────────────────────

@Composable
fun EditRoleSheetContent(
    role: RoleItem,
    reportingOptions: List<String>,
    onDismiss: () -> Unit,
    onUpdate: (EditRoleRequest) -> Unit
) {
    var roleName by remember { mutableStateOf(role.name) }
    var roleCategory by remember { mutableStateOf(role.category) }
    var roleStatus by remember { mutableStateOf(role.status) }
    var statusExpanded by remember { mutableStateOf(false) }
    var reportingTo by remember { mutableStateOf(role.reportingTo) }
    var reportingExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf("Active", "Inactive")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Edit Role",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                "Update role information",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column {
            FormLabel("Role Name", isRequired = true)
            FormTextField(
                value = roleName,
                onValueChange = { roleName = it },
                placeholder = "Enter role name"
            )
        }

        Column {
            FormLabel("Role Category")
            FormTextField(
                value = roleCategory,
                onValueChange = { roleCategory = it },
                placeholder = "Enter role category"
            )
        }

        Column {
            FormLabel("Role ID")
            FormTextField(
                value = role.roleId,
                onValueChange = { },
                placeholder = "Role ID",
                enabled = false
            )
        }

        FormDropdown(
            label = "Role Status",
            value = roleStatus.ifBlank { "Select an option" },
            expanded = statusExpanded,
            onExpandChange = { statusExpanded = it },
            options = statusOptions,
            onOptionSelected = { roleStatus = it }
        )

        FormDropdown(
            label = "Reporting to",
            value = reportingTo.ifBlank { "Select an option" },
            expanded = reportingExpanded,
            onExpandChange = { reportingExpanded = it },
            options = reportingOptions,
            onOptionSelected = { reportingTo = it },
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
                        EditRoleRequest(
                            name = roleName,
                            category = roleCategory,
                            status = roleStatus,
                            reportingTo = reportingTo
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text("Update Role", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = whiteBg)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// Helper Functions
// ─────────────────────────────────────────────────────────────

private fun statusColorsOfRole(status: String): Pair<String, Color> = when (status.lowercase()) {
    "active" -> "Active" to Color(0xFF16A34A)
    "inactive" -> "Inactive" to Color(0xFF6B7280)
    else -> "Unknown" to Color(0xFF9CA3AF)
}

// ─────────────────────────────────────────────────────────────
// Models
// ─────────────────────────────────────────────────────────────

data class RoleItem(
    val id: String,
    val title: String,
    val name: String,
    val category: String,
    val roleId: String,
    val status: String,
    val reportingTo: String,
    val employeeCount: Int
)

data class CreateRoleRequest(
    val name: String,
    val category: String,
    val status: String,
    val reportingTo: String
)

data class EditRoleRequest(
    val name: String,
    val category: String,
    val status: String,
    val reportingTo: String
)