@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "unusedvariable"
)
package com.cuso.mobile.view.home.designation

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.model.DesignationItem
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.MenuAction
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.DeleteModel
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.viewmodel.DesignationCreateState
import com.cuso.mobile.viewmodel.DesignationDeleteState
import com.cuso.mobile.viewmodel.DesignationUiState
import com.cuso.mobile.viewmodel.DesignationUpdateState
import com.cuso.mobile.viewmodel.DesignationViewModel
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// DesignationScreen with SmoothBottomSheet and FabScaffold
// ─────────────────────────────────────────────────────────────

@Suppress("UNUSED_PARAMETER")
@Composable
fun DesignationScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val viewModel: DesignationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()

    var addSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var editingDesignation by remember { mutableStateOf<DesignationItem?>(null) }
    var editSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var showDeleteDialog by remember { mutableStateOf<DesignationItem?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Blur states
    var addSheetBlur by remember { mutableStateOf(0.dp) }
    var editSheetBlur by remember { mutableStateOf(0.dp) }

    val isAnySheetOpen = addSheetState != SheetValue.Hidden || editSheetState != SheetValue.Hidden
    val currentBlur = when {
        addSheetState != SheetValue.Hidden -> addSheetBlur
        editSheetState != SheetValue.Hidden -> editSheetBlur
        else -> 0.dp
    }

    LaunchedEffect(Unit) { viewModel.loadDesignations() }

    LaunchedEffect(createState) {
        when (val state = createState) {
            is DesignationCreateState.Success -> {
                addSheetState = SheetValue.Hidden
                viewModel.resetCreateState()
                viewModel.loadDesignations()
                coroutineScope.launch { snackbarHostState.showSnackbar("Designation created successfully") }
            }
            is DesignationCreateState.Error -> {
                viewModel.resetCreateState()
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> Unit
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is DesignationUpdateState.Success -> {
                editSheetState = SheetValue.Hidden
                editingDesignation = null
                viewModel.resetUpdateState()
                viewModel.loadDesignations()
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            is DesignationUpdateState.Error -> {
                viewModel.resetUpdateState()
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
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
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            is DesignationDeleteState.Error -> {
                viewModel.resetDeleteState()
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> Unit
        }
    }

    val allDesignations = (uiState as? DesignationUiState.Success)?.items ?: emptyList()
    val filteredDesignations = allDesignations.filter { d ->
        searchQuery.isBlank() || d.name.contains(searchQuery, ignoreCase = true) || d.code.contains(searchQuery, ignoreCase = true)
    }
    val totalPages = maxOf(1, if (filteredDesignations.isNotEmpty()) (filteredDesignations.size + itemsPerPage - 1) / itemsPerPage else 1)
    val pagedDesignations = filteredDesignations.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)

    // FAB configuration
    val fabConfig = FabConfig(
        label = "Add Designation",
        icon = Icons.Default.Add,
        onClick = { addSheetState = SheetValue.Expanded },
        endPadding = 10.dp,
        bottomPadding = 50.dp,
        draggable = true
    )

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = fabConfig,
        snackbarHostState = snackbarHostState
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

            // ── HEADER - Always solid (NO BLUR) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(22.dp).clickable { onBack() },
                            tint = Color(0xFF111827)
                        )
                        Text(
                            "Designation",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                    }
                }
            }

            // ── MAIN CONTENT with blur ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blurScrim(
                        if (isAnySheetOpen) currentBlur else 0.dp
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ScreenBreadcrumb(segments = listOf("Settings", "Designation"), onClick = {})
                        Spacer(Modifier.height(12.dp))
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            placeholder = "Search Designations...",
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            onFilterClick = { /* TODO: open filter drawer */ }
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        when (val state = uiState) {
                            is DesignationUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                ListSkeleton()
                            }
                            is DesignationUiState.Error -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Something went wrong, Please try again later", color = Color.Red)
                                        Spacer(Modifier.height(12.dp))
                                        Button(
                                            onClick = { viewModel.loadDesignations() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Retry", color = whiteBg)
                                        }
                                    }
                                }
                            }
                            is DesignationUiState.Success -> {
                                if (filteredDesignations.isEmpty()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            if (searchQuery.isNotBlank()) "No matching designations found" else "No designations found",
                                            color = Color.Gray,
                                            fontSize = 15.sp
                                        )
                                    }
                                } else {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxWidth().weight(1f)
                                        ) {
                                            items(pagedDesignations) { item ->
                                                val (badgeText, badgeColor) = if (item.status) "Active" to Color(0xFF16A34A) else "Inactive" to Color(0xFF6B7280)
                                                DataCard(
                                                    item = item,
                                                    topBadgeText = badgeText,
                                                    topBadgeTextColor = badgeColor,
                                                    topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                                    title = item.name,
                                                    subtitle = item.code,
                                                    footerFields = listOf(
                                                        DataCardField(icon = Icons.Default.People, text = "0 Employees")
                                                    ),
                                                    actions = listOf(
                                                        MenuAction("Edit", Icons.Default.Edit) {
                                                            editingDesignation = item
                                                            editSheetState = SheetValue.Expanded
                                                        },
                                                        MenuAction("Delete", Icons.Default.Delete, tint = Color.Red, textColor = Color.Red) {
                                                            showDeleteDialog = item
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                        Box(modifier = Modifier.fillMaxWidth().background(whiteBg, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))) {
                                            Column {
                                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        "Showing ${if (filteredDesignations.isEmpty()) 0 else (currentPage - 1) * itemsPerPage + 1} - ${minOf(currentPage * itemsPerPage, filteredDesignations.size)} of ${filteredDesignations.size}",
                                                        fontSize = 13.sp, color = Color(0xFF6B7280)
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        IconButton(
                                                            onClick = { if (currentPage > 1) currentPage-- },
                                                            enabled = currentPage > 1,
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.ChevronLeft,
                                                                "Previous",
                                                                tint = if (currentPage > 1) Color(0xFF374151) else Color(0xFFD1D5DB)
                                                            )
                                                        }
                                                        Text("$currentPage - $totalPages", fontSize = 13.sp, color = Color(0xFF374151))
                                                        IconButton(
                                                            onClick = { if (currentPage < totalPages) currentPage++ },
                                                            enabled = currentPage < totalPages,
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.ChevronRight,
                                                                "Next",
                                                                tint = if (currentPage < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB)
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
                    }
                }
            }
        }

        // ── SmoothBottomSheet for Add Designation ──
        SmoothBottomSheet(
            state = addSheetState,
            onStateChange = {
                addSheetState = it
                if (it == SheetValue.Hidden) {
                    viewModel.resetCreateState()
                }
            },
            peekHeight = 380.dp,
            topInset = 60.dp,
            onDismissRequest = {
                addSheetState = SheetValue.Hidden
                viewModel.resetCreateState()
            },
            onBlurScrimChange = { blur, _ ->
                addSheetBlur = blur
            },
            sheetBackgroundColor = whiteBg,
            maxScrimAlpha = 0.4f,
            maxBlurRadius = 14.dp
        ) {
            AddDesignationSheetContent(
                isLoading = createState is DesignationCreateState.Loading,
                onDismiss = {
                    addSheetState = SheetValue.Hidden
                    viewModel.resetCreateState()
                },
                onCreate = { name, code, description ->
                    viewModel.createDesignation(name, code, description)
                }
            )
        }

        // ── SmoothBottomSheet for Edit Designation ──
        editingDesignation?.let { designation ->
            SmoothBottomSheet(
                state = editSheetState,
                onStateChange = {
                    editSheetState = it
                    if (it == SheetValue.Hidden) {
                        editingDesignation = null
                        viewModel.resetUpdateState()
                    }
                },
                peekHeight = 380.dp,
                topInset = 60.dp,
                onDismissRequest = {
                    editSheetState = SheetValue.Hidden
                    editingDesignation = null
                    viewModel.resetUpdateState()
                },
                onBlurScrimChange = { blur, _ ->
                    editSheetBlur = blur
                },
                sheetBackgroundColor = whiteBg,
                maxScrimAlpha = 0.4f,
                maxBlurRadius = 14.dp
            ) {
                EditDesignationSheetContent(
                    designation = designation,
                    isLoading = updateState is DesignationUpdateState.Loading,
                    onDismiss = {
                        editSheetState = SheetValue.Hidden
                        editingDesignation = null
                        viewModel.resetUpdateState()
                    },
                    onUpdate = { name, code, description ->
                        viewModel.updateDesignation(
                            id = designation.id,
                            name = name,
                            code = code,
                            description = description
                        )
                    }
                )
            }
        }

        // ── Delete Confirmation Dialog ──
        showDeleteDialog?.let { designation ->
            DeleteModel(
                title = "Delete Designation",
                message = "Are you sure you want to delete '${designation.name}'?\nThis action cannot be undone.",
                onDismiss = { showDeleteDialog = null },
                onDelete = {
                    viewModel.deleteDesignation(designation.id)
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// AddDesignationSheetContent
// ─────────────────────────────────────────────────────────────

@Composable
fun AddDesignationSheetContent(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            "Add Designation",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Create a new designation in your organization",
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(Modifier.height(16.dp))

        Column {
            FormLabel("Designation Name", isRequired = true)
            FormTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                placeholder = "Enter designation name",
                isError = nameError,
                errorMessage = "Designation name is required"
            )
        }
        Spacer(Modifier.height(12.dp))

        Column {
            FormLabel("Designation Code", isRequired = true)
            FormTextField(
                value = code,
                onValueChange = { code = it; codeError = false },
                placeholder = "Enter designation code",
                isError = codeError,
                errorMessage = "Designation code is required"
            )
        }
        Spacer(Modifier.height(12.dp))

        Column {
            FormLabel("Description")
            FormTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Enter description"
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF374151)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Button(
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
                    if (hasError) return@Button
                    onCreate(name, code, description)
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CirculerProgressIndicatorReuse()
                } else {
                    Text("Create", color = whiteBg, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// EditDesignationSheetContent
// ─────────────────────────────────────────────────────────────

@Composable
fun EditDesignationSheetContent(
    designation: DesignationItem,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(designation.name) }
    var code by remember { mutableStateOf(designation.code) }
    var description by remember { mutableStateOf(designation.description) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            "Edit Designation",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Update designation information",
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(Modifier.height(16.dp))

        Column {
            FormLabel("Designation Name", isRequired = true)
            FormTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Enter designation name"
            )
        }
        Spacer(Modifier.height(12.dp))

        Column {
            FormLabel("Designation Code", isRequired = true)
            FormTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = "Enter designation code"
            )
        }
        Spacer(Modifier.height(12.dp))

        Column {
            FormLabel("Description")
            FormTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Enter description"
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF374151)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = {
                    onUpdate(name, code, description.ifEmpty { null })
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CirculerProgressIndicatorReuse()
                } else {
                    Text("Update", color = whiteBg, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

