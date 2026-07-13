package com.cuso.mobile.view.home.designation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.model.DesignationItem
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
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

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<DesignationItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf<DesignationItem?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadDesignations() }

    LaunchedEffect(createState) {
        when (val state = createState) {
            is DesignationCreateState.Success -> {
                showAddDialog = false
                viewModel.resetCreateState()
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
                showEditDialog = null
                viewModel.resetUpdateState()
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

            Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(22.dp).clickable { onBack() }, tint = Color(0xFF111827))
                        Text("Designation", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    }
                }
            }

            Column(modifier = Modifier.background(Color(0xFFF8F9FF)).fillMaxWidth().padding(vertical = 10.dp, horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Settings", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                    Text("Designation", fontSize = 13.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp)).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it; currentPage = 1 },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) Text("Search designations...", fontSize = 14.sp, color = Color.Black)
                            inner()
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (val state = uiState) {
                    is DesignationUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CirculerProgressIndicatorReuse()
                    }
                    is DesignationUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(state.message, color = Color.Red)
                                Spacer(Modifier.height(12.dp))
                                Button(onClick = { viewModel.loadDesignations() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)), shape = RoundedCornerShape(8.dp)) {
                                    Text("Retry", color = Color.White)
                                }
                            }
                        }
                    }
                    is DesignationUiState.Success -> {
                        if (filteredDesignations.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(if (searchQuery.isNotBlank()) "No matching designations found" else "No designations found", color = Color.Gray, fontSize = 15.sp)
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
                                                MenuAction("Edit", Icons.Default.Edit) { showEditDialog = item },
                                                MenuAction("Delete", Icons.Default.Delete, tint = Color.Red, textColor = Color.Red) { showDeleteDialog = item }
                                            )
                                        )
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))) {
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

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))

        Button(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Add Designation", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showAddDialog) {
        AddDesignationDialog(
            isLoading = createState is DesignationCreateState.Loading,
            onDismiss = { showAddDialog = false },
            onCreate = { name, code, description -> viewModel.createDesignation(name, code, description) }
        )
    }

    showEditDialog?.let { designation ->
        EditDesignationDialog(
            designation = designation,
            isLoading = updateState is DesignationUpdateState.Loading,
            onDismiss = { showEditDialog = null },
            onUpdate = { name, code, description ->
                viewModel.updateDesignation(id = designation.id, name = name, code = code, description = description)
            }
        )
    }

    showDeleteDialog?.let { designation ->
        DeleteConfirmationDialog(
            designation = designation,
            isLoading = deleteState is DesignationDeleteState.Loading,
            onDismiss = { showDeleteDialog = null },
            onConfirm = { viewModel.deleteDesignation(designation.id) }
        )
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
                            CirculerProgressIndicatorReuse()

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
                            CirculerProgressIndicatorReuse()

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
                    CirculerProgressIndicatorReuse()

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


// ─────────────────────────────────────────────────────────────
// 🔁 ONE column list for designations. This REPLACES the table
// header Row + LazyColumn that used to be written inline inside
// DesignationScreen's `is DesignationUiState.Success ->` branch.
// ─────────────────────────────────────────────────────────────
//private fun designationColumns(
//    onEditClick: (DesignationItem) -> Unit,
//    onDeleteClick: (DesignationItem) -> Unit
//): List<DataColumn<DesignationItem>> = listOf(
//    DataColumn("name", "Designation", 200.dp) { item ->
//        Text(item.name, fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium,
//            maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("code", "Designation Code", 180.dp) { item ->
//        Text(item.code, fontSize = 13.sp, color = Color(0xFF374151), maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("department", "Department", 200.dp) { Text("-", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
//    DataColumn("employees", "Employees", 130.dp) {
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//            Icon(Icons.Default.People, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
//            Text("0", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
//        }
//    },
//    DataColumn("status", "Status", 120.dp) { item ->
//        val (badgeText, badgeColor) = if (item.status) "Active" to Color(0xFF16A34A) else "Inactive" to Color(0xFF6B7280)
//        Box(modifier = Modifier.border(1.dp, badgeColor, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
//            Text(badgeText, fontSize = 12.sp, color = badgeColor, fontWeight = FontWeight.Medium)
//        }
//    },
//    DataColumn("action", "Action", 80.dp, cellAlignment = Alignment.Center) { item ->
//        ActionDropdownMenu(
//            actions = listOf(
//                MenuAction("Edit", Icons.Default.Edit) { onEditClick(item) },
//                MenuAction("Delete", Icons.Default.Delete, tint = Color.Red, textColor = Color.Red) { onDeleteClick(item) }
//            )
//        )
//    }
//)
//
//
//// ── Card view — same call site as before ──
//@Composable
//fun DesignationCardItem(
//    item: DesignationItem,
//    onEditClick: (DesignationItem) -> Unit,
//    onDeleteClick: (DesignationItem) -> Unit
//) {
//    val columns = designationColumns(onEditClick, onDeleteClick)
//    val footerFields = columns.filter { it.key in listOf("department", "employees") }
//
//    DataCard(
//        item = item,
//        leading = { Box(modifier = Modifier.width(4.dp).height(18.dp).background(Color(0xFF3B3BF9), RoundedCornerShape(2.dp))) },
//        title = {
//            Column {
//                Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
//                Text(item.code, fontSize = 12.sp, color = Color(0xFF3B3BF9))
//            }
//        },
//        trailing = {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                val (badgeText, badgeColor) = if (item.status) "Active" to Color(0xFF16A34A) else "Inactive" to Color(0xFF6B7280)
//                Box(modifier = Modifier.background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
//                    Text(badgeText, fontSize = 11.sp, color = badgeColor, fontWeight = FontWeight.Medium)
//                }
//                ActionDropdownMenu(
//                    icon = Icons.Default.MoreVert,
//                    actions = listOf(
//                        MenuAction("Edit", Icons.Default.Edit) { onEditClick(item) },
//                        MenuAction("Delete", Icons.Default.Delete, tint = Color.Red, textColor = Color.Red) { onDeleteClick(item) }
//                    )
//                )
//            }
//        },
//        fields = footerFields,
//        fieldsPerRow = 2,
//        footerBackground = Color(0xFFF8F9FB)
//    )
//}