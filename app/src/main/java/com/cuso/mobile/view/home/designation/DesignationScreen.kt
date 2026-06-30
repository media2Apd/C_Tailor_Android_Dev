package com.cuso.mobile.view.home.designation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.model.DesignationItem
import com.cuso.mobile.viewmodel.DesignationCreateState
import com.cuso.mobile.viewmodel.DesignationDeleteState
import com.cuso.mobile.viewmodel.DesignationUiState
import com.cuso.mobile.viewmodel.DesignationUpdateState
import com.cuso.mobile.viewmodel.DesignationViewModel
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// REPLACE in DesignationScreen.kt:
//  1) fun DesignationScreen(...)  -> replace fully with version below
//  2) ADD new composable: DesignationCardItem (new, paste anywhere below DesignationRow)
// Everything else in that file (DesignationRow, dialogs, fields) stays unchanged.
// ─────────────────────────────────────────────────────────────

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

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<DesignationItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf<DesignationItem?>(null) }

    // ✅ View toggle state — true = Table View, false = Card View
    var isListView by remember { mutableStateOf(true) }
    var viewMenuExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ── Load Designations ──
    LaunchedEffect(Unit) { viewModel.loadDesignations() }

    // ── Handle Create Result ──
    LaunchedEffect(createState) {
        when (val state = createState) {
            is DesignationCreateState.Success -> {
                showAddDialog = false
                viewModel.resetCreateState()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Designation created successfully")
                }
            }
            is DesignationCreateState.Error -> {
                viewModel.resetCreateState()
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> Unit
        }
    }

    // ── Handle Update Result ──
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is DesignationUpdateState.Success -> {
                showEditDialog = null
                viewModel.resetUpdateState()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
            is DesignationUpdateState.Error -> {
                viewModel.resetUpdateState()
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> Unit
        }
    }

    // ── Handle Delete Result ──
    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is DesignationDeleteState.Success -> {
                showDeleteDialog = null
                viewModel.resetDeleteState()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
            is DesignationDeleteState.Error -> {
                viewModel.resetDeleteState()
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> Unit
        }
    }

    // ── Column widths ──
    val designationWidth  = 200.dp
    val codeWidth         = 180.dp
    val departmentWidth   = 200.dp
    val employeesWidth    = 130.dp
    val statusWidth       = 120.dp
    val actionWidth       = 80.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // ✅ View Toggle Dropdown
                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .clickable { viewMenuExpanded = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListView) androidx.compose.material.icons.Icons.AutoMirrored.Filled.List else androidx.compose.material.icons.Icons.Default.GridView,
                                contentDescription = "View Toggle",
                                tint = Color(0xFF3B3BF9),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = viewMenuExpanded,
                            onDismissRequest = { viewMenuExpanded = false },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.List, null, tint = Color(0xFF374151), modifier = Modifier.size(16.dp))
                                        Text("Table View", color = Color(0xFF374151))
                                        if (isListView) {
                                            Spacer(Modifier.width(4.dp))
                                            Icon(androidx.compose.material.icons.Icons.Default.Check, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = { isListView = true; viewMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(androidx.compose.material.icons.Icons.Default.GridView, null, tint = Color(0xFF374151), modifier = Modifier.size(16.dp))
                                        Text("Card View", color = Color(0xFF374151))
                                        if (!isListView) {
                                            Spacer(Modifier.width(4.dp))
                                            Icon(androidx.compose.material.icons.Icons.Default.Check, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = { isListView = false; viewMenuExpanded = false }
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Add", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // ── Content ──
            when (val state = uiState) {
                is DesignationUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3B3BF9))
                    }
                }
                is DesignationUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, color = Color.Red)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.loadDesignations() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Retry", color = Color.White) }
                        }
                    }
                }
                is DesignationUiState.Success -> {
                    if (state.items.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No designations found", color = Color.Gray, fontSize = 15.sp)
                        }
                    } else if (isListView) {
                        // ✅ TABLE VIEW
                        val totalWidth = designationWidth + codeWidth + departmentWidth +
                                employeesWidth + statusWidth + actionWidth + 32.dp

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            // ── Table Header ──
                            Row(
                                modifier = Modifier
                                    .width(totalWidth)
                                    .background(Color(0xFFF1F1F1))
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Designation",      modifier = Modifier.width(designationWidth), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text("Designation Code", modifier = Modifier.width(codeWidth),        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text("Department",       modifier = Modifier.width(departmentWidth),  fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text("Employees",        modifier = Modifier.width(employeesWidth),   fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text("Status",           modifier = Modifier.width(statusWidth),      fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text("Action",           modifier = Modifier.width(actionWidth),      fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            }

                            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.width(totalWidth))

                            LazyColumn(
                                modifier = Modifier
                                    .width(totalWidth)
                                    .weight(1f)
                            ) {
                                items(state.items) { item ->
                                    DesignationRow(
                                        item             = item,
                                        designationWidth = designationWidth,
                                        codeWidth        = codeWidth,
                                        departmentWidth  = departmentWidth,
                                        employeesWidth   = employeesWidth,
                                        statusWidth      = statusWidth,
                                        actionWidth      = actionWidth,
                                        onEditClick      = { showEditDialog = it },
                                        onDeleteClick    = { showDeleteDialog = it }
                                    )
                                    HorizontalDivider(color = Color(0xFFF5F5F5))
                                }
                            }
                        }
                    } else {
                        // ✅ CARD VIEW
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.items) { item ->
                                DesignationCardItem(
                                    item = item,
                                    onEditClick = { showEditDialog = it },
                                    onDeleteClick = { showDeleteDialog = it }
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    // ── Add Dialog ──
    if (showAddDialog) {
        AddDesignationDialog(
            isLoading = createState is DesignationCreateState.Loading,
            onDismiss = { showAddDialog = false },
            onCreate  = { name, code, description ->
                viewModel.createDesignation(name, code, description)
            }
        )
    }

    // ── Edit Dialog ──
    showEditDialog?.let { designation ->
        EditDesignationDialog(
            designation = designation,
            isLoading = updateState is DesignationUpdateState.Loading,
            onDismiss = { showEditDialog = null },
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

    // ── Delete Dialog ──
    showDeleteDialog?.let { designation ->
        DeleteConfirmationDialog(
            designation = designation,
            isLoading = deleteState is DesignationDeleteState.Loading,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteDesignation(designation.id)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// DesignationCardItem  (NEW — add anywhere below DesignationRow)
// ─────────────────────────────────────────────────────────────
@Composable
fun DesignationCardItem(
    item: DesignationItem,
    onEditClick: (DesignationItem) -> Unit,
    onDeleteClick: (DesignationItem) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val (badgeText, badgeColor) = if (item.status)
        "Active" to Color(0xFF16A34A)
    else
        "Inactive" to Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .background(Color(0xFF3B3BF9), RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text(item.code, fontSize = 12.sp, color = Color(0xFF3B3BF9))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(badgeText, fontSize = 11.sp, color = badgeColor, fontWeight = FontWeight.Medium)
                }
                Box {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp).clickable { menuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = Color.Black) },
                            onClick = { menuExpanded = false; onEditClick(item) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = { menuExpanded = false; onDeleteClick(item) }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F9FB), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Department", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                Spacer(Modifier.height(2.dp))
                Text("-", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Employees", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                Spacer(Modifier.height(2.dp))
                Text("0", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Bold)
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────
// DesignationRow
// ─────────────────────────────────────────────────────────────

@Composable
fun DesignationRow(
    item: DesignationItem,
    designationWidth: Dp,
    codeWidth: Dp,
    departmentWidth: Dp,
    employeesWidth: Dp,
    statusWidth: Dp,
    actionWidth: Dp,
    onEditClick: (DesignationItem) -> Unit,
    onDeleteClick: (DesignationItem) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Designation Name ──
        Text(
            item.name,
            modifier = Modifier.width(designationWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ── Code ──
        Text(
            item.code,
            modifier = Modifier.width(codeWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ── Department (not in API yet → "-") ──
        Text(
            "-",
            modifier = Modifier.width(departmentWidth),
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF)
        )

        // ── Employees (not in API yet → 0) ──
        Row(
            modifier = Modifier.width(employeesWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.People, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
            Text("0", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
        }

        // ── Status Badge ──
        Box(modifier = Modifier.width(statusWidth)) {
            val (badgeText, badgeColor) = if (item.status)
                "Active" to Color(0xFF16A34A)
            else
                "Inactive" to Color(0xFF6B7280)

            Box(
                modifier = Modifier
                    .border(1.dp, badgeColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(badgeText, fontSize = 12.sp, color = badgeColor, fontWeight = FontWeight.Medium)
            }
        }

        // ── Action Menu ──
        Box(modifier = Modifier.width(actionWidth), contentAlignment = Alignment.Center) {
            Box {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { menuExpanded = true }
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = Color.Black) },
                        onClick = {
                            menuExpanded = false
                            onEditClick(item)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick(item)
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// AddDesignationDialog
// ─────────────────────────────────────────────────────────────

@Composable
fun AddDesignationDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name        by remember { mutableStateOf("") }
    var code        by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add Designation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                DesignationField("Designation Name", name,        { name = it })
                DesignationField("Designation Code", code,        { code = it })
                DesignationField("Description",      description, { description = it })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Cancel", color = Color(0xFF374151)) }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = { onCreate(name, code, description) },
                        enabled = !isLoading && name.isNotBlank() && code.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// EditDesignationDialog
// ─────────────────────────────────────────────────────────────

@Composable
fun EditDesignationDialog(
    designation: DesignationItem,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(designation.name) }
    var code by remember { mutableStateOf(designation.code) }
    var description by remember { mutableStateOf(designation.description) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Designation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                DesignationField("Designation Name", name, { name = it })
                DesignationField("Designation Code", code, { code = it })
                DesignationField("Description", description, { description = it })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Cancel", color = Color(0xFF374151)) }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = {
                            onUpdate(
                                name,
                                code,
                                description.ifEmpty { null }
                            )
                        },
                        enabled = !isLoading && name.isNotBlank() && code.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Update", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DeleteConfirmationDialog
// ─────────────────────────────────────────────────────────────

@Composable
fun DeleteConfirmationDialog(
    designation: DesignationItem,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Delete Designation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        },
        text = {
            Text(
                "Are you sure you want to delete '${designation.name}'?",
                fontSize = 14.sp,
                color = Color(0xFF374151)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Delete", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel", color = Color(0xFF6B7280))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

// ─────────────────────────────────────────────────────────────
// DesignationField
// ─────────────────────────────────────────────────────────────

@Composable
fun DesignationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3B3BF9))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF374151)
            )
        )
    }
}