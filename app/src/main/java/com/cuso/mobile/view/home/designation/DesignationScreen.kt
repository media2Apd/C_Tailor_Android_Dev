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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.DesignationItem
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.disabled
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DataCardStat
import com.cuso.mobile.view.composable.DataCardStatsRow
import com.cuso.mobile.view.composable.DeleteModel
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.DesignationCreateState
import com.cuso.mobile.viewmodel.DesignationDeleteState
import com.cuso.mobile.viewmodel.DesignationUiState
import com.cuso.mobile.viewmodel.DesignationUpdateState
import com.cuso.mobile.viewmodel.DesignationViewModel

// ─────────────────────────────────────────────────────────────
// DesignationScreen - Updated:
//  - Snackbar removed, replaced with DynamicIsland success/error banners
//  - blur/scrim reset pattern matched to Branch/Department screens
// ─────────────────────────────────────────────────────────────

@Composable
fun DesignationScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
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

    // Independent blur states for sheets
    var addSheetBlur by remember { mutableStateOf(0.dp) }
    var addSheetScrim by remember { mutableFloatStateOf(0f) }
    var editSheetBlur by remember { mutableStateOf(0.dp) }
    var editSheetScrim by remember { mutableFloatStateOf(0f) }

    val isAnySheetOpen = addSheetState != SheetValue.Hidden || editSheetState != SheetValue.Hidden

    //   Dynamic Island messages (replaces snackbar for success/error)
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadDesignations() }

    // Logic for handling Create states
    LaunchedEffect(createState) {
        when (val state = createState) {
            is DesignationCreateState.Success -> {
                addSheetBlur = 0.dp        // ← reset BEFORE the sheet disappears
                addSheetScrim = 0f
                addSheetState = SheetValue.Hidden
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

    // Logic for handling Update states
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is DesignationUpdateState.Success -> {
                editSheetBlur = 0.dp       // ← reset BEFORE editingDesignation = null destroys the sheet
                editSheetScrim = 0f
                editSheetState = SheetValue.Hidden
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

    // Logic for handling Delete states
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

    val allDesignations = (uiState as? DesignationUiState.Success)?.items ?: emptyList()
    val filteredDesignations = allDesignations.filter { d ->
        searchQuery.isBlank() || d.name.contains(searchQuery, ignoreCase = true) || d.code.contains(searchQuery, ignoreCase = true)
    }
    val totalPages = maxOf(1, if (filteredDesignations.isNotEmpty()) (filteredDesignations.size + itemsPerPage - 1) / itemsPerPage else 1)
    val pagedDesignations = filteredDesignations.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)

    // ── OUTER Box — holds Scaffold + topmost Dynamic Island banners ────
    Box(Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                // Header is fixed here and remains visible above sheet scrims
                TitleBar("Designation", onClose = onBack)
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->

            //   OUTER wrapper Box — holds list content + both bottom sheets.
            //   Order inside this Box controls draw/z-order in Compose:
            //   whatever is declared LAST is drawn on TOP.
            Box(modifier = Modifier.fillMaxSize()) {

                // ── Layer 1: main list content ──────────────────────────
                Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
                    FabScaffold(
                        fab = FabConfig(
                            label = "Add Designation",
                            icon = Icons.Default.Add,
                            onClick = { addSheetState = SheetValue.Expanded }
                        ),
                        fabVisible = !isAnySheetOpen
                    ) {
                        // Content area that will blur when a sheet is open
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .background(Color.Transparent)
                                .blurScrim(addSheetBlur.coerceAtLeast(editSheetBlur))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ScreenBreadcrumb(segments = listOf("Settings", "Designation"), onClick = {})
                                SearchFilterBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f),
                                    placeholder = "Search Designations...",
                                    accentColor = BluePrimary,
                                    borderColor = BorderGray,
                                    textSecondaryColor = TextSecondary
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF0F0F0))

                            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                when (uiState) {
                                    is DesignationUiState.Loading -> ListSkeleton()
                                    is DesignationUiState.Error -> {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    null,
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(tokens.iconSize * 2.6f)
                                                )
                                                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                                                Text("Something went wrong", fontSize = tokens.bodyMedium, color = Color.Red)
                                                Button(
                                                    onClick = { viewModel.loadDesignations() },
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                                ) {
                                                    Text("Retry", fontSize = tokens.bodyMedium)
                                                }
                                            }
                                        }
                                    }
                                    is DesignationUiState.Success -> {
                                        if (filteredDesignations.isEmpty()) {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text("No designations found", fontSize = tokens.bodyMedium, color = Color.Gray)
                                            }
                                        } else {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                LazyColumn(modifier = Modifier.weight(1f)) {
                                                    items(pagedDesignations) { item ->
                                                        val (badgeText, badgeColor) = if (item.status) "Active" to Color(0xFF16A34A) else "Inactive" to Color(0xFF6B7280)
                                                        DataCard(
                                                            item = item,
                                                            titleColor = title_color,
                                                            smalltitle = "${item.name}  .  Designation",
                                                            topBadgeText = badgeText,
                                                            topBadgeTextColor = badgeColor,
                                                            topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                                            topBadgeInline = true,
                                                            actions = listOf(
                                                                MenuAction("Edit", Icons.Default.Edit) {
                                                                    editingDesignation = item
                                                                    editSheetState = SheetValue.Expanded
                                                                },
                                                                MenuAction("Delete", Icons.Default.Delete, tint = Color.Red, textColor = Color.Red) {
                                                                    showDeleteDialog = item
                                                                }
                                                            ),
                                                            content = {
                                                                DataCardStatsRow(
                                                                    stats = listOf(
                                                                        DataCardStat(label = "Department", value = "null"),
                                                                        DataCardStat(label = "Employees", value = "null"),
                                                                        DataCardStat(label = "Code", value = item.code)
                                                                    )
                                                                )
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
                }

                // ── Layer 2: Add Designation BottomSheet ─────────────────
                SmoothBottomSheet(
                    state = addSheetState,
                    onStateChange = { newState ->
                        addSheetState = newState
                        if (newState == SheetValue.Hidden) {
                            addSheetBlur = 0.dp
                            addSheetScrim = 0f
                            viewModel.resetCreateState()
                        }
                    },
                    peekHeight = tokens.cardHeight * 3.8f,
                    topInset = 66.dp, // Ensures sheet stops below TitleBar
                    onDismissRequest = {
                        addSheetBlur = 0.dp
                        addSheetScrim = 0f
                        addSheetState = SheetValue.Hidden
                        viewModel.resetCreateState()
                    },
                    onBlurScrimChange = { r, s ->
                        // Ignore stale/late callbacks that fire after we've already
                        // force-reset to 0 on close — prevents the blur getting stuck.
                        if (addSheetState != SheetValue.Hidden) {
                            addSheetBlur = r
                            addSheetScrim = s
                        }
                    },
                    sheetBackgroundColor = whiteBg,
                    maxBlurRadius = 14.dp
                ) {
                    AddDesignationSheetContent(
                        isLoading = createState is DesignationCreateState.Loading,
                        onDismiss = {
                            addSheetBlur = 0.dp
                            addSheetScrim = 0f
                            addSheetState = SheetValue.Hidden
                        },
                        onCreate = { name, code, desc -> viewModel.createDesignation(name, code, desc) }
                    )
                }

                // ── Layer 3: Edit Designation BottomSheet ─────────────────
                editingDesignation?.let { designation ->
                    SmoothBottomSheet(
                        state = editSheetState,
                        onStateChange = { newState ->
                            editSheetState = newState
                            if (newState == SheetValue.Hidden) {
                                editSheetBlur = 0.dp
                                editSheetScrim = 0f
                                editingDesignation = null
                                viewModel.resetUpdateState()
                            }
                        },
                        peekHeight = tokens.cardHeight * 3.8f,
                        topInset = 66.dp,
                        onDismissRequest = {
                            editSheetBlur = 0.dp
                            editSheetScrim = 0f
                            editSheetState = SheetValue.Hidden
                            editingDesignation = null
                            viewModel.resetUpdateState()
                        },
                        onBlurScrimChange = { r, s ->
                            // Ignore stale/late callbacks that fire after we've already
                            // force-reset to 0 on close — prevents the blur getting stuck.
                            if (editSheetState != SheetValue.Hidden) {
                                editSheetBlur = r
                                editSheetScrim = s
                            }
                        },
                        sheetBackgroundColor = whiteBg,
                        maxBlurRadius = 14.dp
                    ) {
                        EditDesignationSheetContent(
                            designation = designation,
                            isLoading = updateState is DesignationUpdateState.Loading,
                            onDismiss = {
                                editSheetBlur = 0.dp
                                editSheetScrim = 0f
                                editSheetState = SheetValue.Hidden
                            },
                            onUpdate = { name, code, desc ->
                                viewModel.updateDesignation(designation.id, name, code, desc)
                            }
                        )
                    }
                }

                // Delete Confirmation Dialog
                showDeleteDialog?.let { designation ->
                    DeleteModel(
                        title = "Delete Designation",
                        message = "Are you sure you want to delete '${designation.name}'?",
                        onDismiss = { showDeleteDialog = null },
                        onDelete = { viewModel.deleteDesignation(designation.id) }
                    )
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

@Composable
fun PaginationFooter(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    itemsPerPage: Int,
    onPageChange: (Int) -> Unit
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(topStart = tokens.cardCornerRadius * 0.8f, topEnd = tokens.cardCornerRadius * 0.8f))
    ) {
        Column {
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val start = if (totalItems == 0) 0 else (currentPage - 1) * itemsPerPage + 1
                val end = minOf(currentPage * itemsPerPage, totalItems)
                Text("Showing $start - $end of $totalItems", fontSize = tokens.bodySmall, color = Color(0xFF6B7280))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onPageChange(currentPage - 1) },
                        enabled = currentPage > 1,
                        modifier = Modifier.size(tokens.iconSize * 1.55f)
                    ) {
                        Icon(Icons.Default.ChevronLeft, "Prev", tint = if (currentPage > 1) Color(0xFF374151) else Color(0xFFD1D5DB))
                    }
                    Text("$currentPage - $totalPages", fontSize = tokens.bodySmall, color = Color(0xFF374151))
                    IconButton(
                        onClick = { onPageChange(currentPage + 1) },
                        enabled = currentPage < totalPages,
                        modifier = Modifier.size(tokens.iconSize * 1.55f)
                    ) {
                        Icon(Icons.Default.ChevronRight, "Next", tint = if (currentPage < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB))
                    }
                }
            }
        }
    }
}

@Composable
fun AddDesignationSheetContent(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    val tokens = LocalAppTokens.current

    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
            .padding(bottom = tokens.extraPadding * 1.6f)
    ) {
        Text("Add Designation", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text("Create a new designation in your organization", fontSize = tokens.bodySmall, color = Color(0xFF6B7280))
        Spacer(Modifier.height(tokens.extraPadding * 1.6f))

        FormLabel("Designation Name", isRequired = true)
        FormTextField(value = name, onValueChange = { name = it; nameError = false }, placeholder = "Enter designation name", isError = nameError)

        Spacer(Modifier.height(tokens.extraPadding * 1.2f))
        FormLabel("Designation Code", isRequired = true)
        FormTextField(value = code, onValueChange = { code = it; codeError = false }, placeholder = "Enter code", isError = codeError)

        Spacer(Modifier.height(tokens.extraPadding * 1.2f))
        FormLabel("Description")
        FormTextField(value = description, onValueChange = { description = it }, placeholder = "Enter description")

        Spacer(Modifier.height(tokens.extraPadding * 2.4f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            ) {
                Text("Cancel", fontSize = tokens.bodyMedium, color = Color(0xFF374151))
            }
            Button(
                onClick = {
                    if (name.isBlank()) nameError = true
                    if (code.isBlank()) codeError = true
                    if (!nameError && !codeError) onCreate(name, code, description)
                },
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = disabled
                ),
                enabled = !isLoading
            ) {
                if (isLoading) CirculerProgressIndicatorSmall() else Text("Create", fontSize = tokens.bodyMedium, color = Color.White)
            }
        }
    }
}

@Composable
fun EditDesignationSheetContent(
    designation: DesignationItem,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String?) -> Unit
) {
    val tokens = LocalAppTokens.current

    var name by remember { mutableStateOf(designation.name) }
    var code by remember { mutableStateOf(designation.code) }
    var description by remember { mutableStateOf(designation.description) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
            .padding(bottom = tokens.extraPadding * 1.6f)
    ) {
        Text("Edit Designation", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text("Update designation information", fontSize = tokens.bodySmall, color = Color(0xFF6B7280))
        Spacer(Modifier.height(tokens.extraPadding * 1.6f))

        FormLabel("Designation Name", isRequired = true)
        FormTextField(value = name, onValueChange = { name = it }, placeholder = "Enter name")

        Spacer(Modifier.height(tokens.extraPadding * 1.2f))
        FormLabel("Designation Code", isRequired = true)
        FormTextField(value = code, onValueChange = { code = it }, placeholder = "Enter code")

        Spacer(Modifier.height(tokens.extraPadding * 1.2f))
        FormLabel("Description")
        FormTextField(value = description, onValueChange = { description = it }, placeholder = "Enter description")

        Spacer(Modifier.height(tokens.extraPadding * 2.4f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            ) {
                Text("Cancel", fontSize = tokens.bodyMedium, color = Color(0xFF374151))
            }
            Button(
                onClick = { onUpdate(name, code, description.ifEmpty { null }) },
                modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isLoading
            ) {
                if (isLoading) CirculerProgressIndicatorSmall() else Text("Update", fontSize = tokens.bodyMedium, color = Color.White)
            }
        }
    }
}