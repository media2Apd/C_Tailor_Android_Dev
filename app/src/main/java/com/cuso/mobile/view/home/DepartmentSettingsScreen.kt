package com.cuso.mobile.view.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.cuso.mobile.model.BranchItem
import com.cuso.mobile.model.DepartmentItem
import com.cuso.mobile.model.StaffDto
import com.cuso.mobile.viewmodel.DepartmentUiState
import com.cuso.mobile.viewmodel.DepartmentViewModel
import com.cuso.mobile.viewmodel.SalesViewModel

@Composable
fun DepartmentSettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val departmentViewModel: DepartmentViewModel = hiltViewModel()
    val salesViewModel: SalesViewModel = hiltViewModel()

    val uiState by departmentViewModel.uiState.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        departmentViewModel.loadDepartments()
        salesViewModel.fetchStaff()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
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
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onBack() },
                    tint = Color(0xFF111827)
                )
                Text(
                    "Department",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Button(
                onClick = { /* TODO: Add Department */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(Modifier.width(4.dp))
                Text("Add", color = Color.White, fontSize = 14.sp)
            }
        }

        HorizontalDivider(color = Color(0xFFF0F0F0))

        when (val state = uiState) {
            is DepartmentUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is DepartmentUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = Color.Red)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { departmentViewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Retry", color = Color.White) }
                    }
                }
            }

            is DepartmentUiState.Success -> {
                if (state.departments.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("No departments found", color = Color.Gray, fontSize = 15.sp)
                        }
                    }
                } else {
                    DepartmentTable(
                        departments = state.departments,
                        staffList = staffList,
                        onEditClick = { /* TODO: open edit dialog */ },
                        onDeleteClick = { /* TODO: call delete */ },
                        onViewClick = { /* TODO: navigate to detail */ }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DepartmentTable
// ─────────────────────────────────────────────────────────────

@Composable
fun DepartmentTable(
    departments: List<DepartmentItem>,
    staffList: List<StaffDto>,
    onEditClick: (DepartmentItem) -> Unit,
    onDeleteClick: (DepartmentItem) -> Unit,
    onViewClick: (DepartmentItem) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    val departmentWidth = 220.dp
    val headWidth        = 180.dp
    val descriptionWidth = 260.dp
    val employeesWidth   = 150.dp
    val teamsWidth        = 100.dp
    val statusWidth       = 100.dp
    val actionWidth        = 70.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
    ) {
        // ── Table Header ──
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Department",      modifier = Modifier.width(departmentWidth), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Text("Department Head", modifier = Modifier.width(headWidth),        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Text("Description",     modifier = Modifier.width(descriptionWidth), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Text("Total Employees", modifier = Modifier.width(employeesWidth),   fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Text("Teams",           modifier = Modifier.width(teamsWidth),       fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Text("Status",          modifier = Modifier.width(statusWidth),      fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Text("Action",          modifier = Modifier.width(actionWidth),      fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
        }

        HorizontalDivider(color = Color(0xFFF0F0F0))

        LazyColumn {
            items(departments) { department ->
                DepartmentTableRow(
                    department        = department,
                    staffList         = staffList,
                    departmentWidth   = departmentWidth,
                    headWidth         = headWidth,
                    descriptionWidth  = descriptionWidth,
                    employeesWidth    = employeesWidth,
                    teamsWidth        = teamsWidth,
                    statusWidth       = statusWidth,
                    actionWidth       = actionWidth,
                    onEditClick       = onEditClick,
                    onDeleteClick     = onDeleteClick,
                    onViewClick       = onViewClick
                )
                HorizontalDivider(color = Color(0xFFF5F5F5))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DepartmentTableRow
// ─────────────────────────────────────────────────────────────

@Composable
fun DepartmentTableRow(
    department: DepartmentItem,
    staffList: List<StaffDto>,
    departmentWidth: Dp,
    headWidth: Dp,
    descriptionWidth: Dp,
    employeesWidth: Dp,
    teamsWidth: Dp,
    statusWidth: Dp,
    actionWidth: Dp,
    onEditClick: (DepartmentItem) -> Unit,
    onDeleteClick: (DepartmentItem) -> Unit,
    onViewClick: (DepartmentItem) -> Unit
) {
    var actionMenuExpanded by remember { mutableStateOf(false) }

    // departmentHeadId is just an id in the response — resolve a display name from staffList
    val headName = remember(department.departmentHeadId, staffList) {
        department.departmentHeadId
            ?.let { headId -> staffList.firstOrNull { it.id == headId } }
            ?.let { "${it.firstName} ${it.lastName}" }
            ?: "-"
    }

    Row(
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Department ──
        Row(
            modifier = Modifier.width(departmentWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                department.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ── Department Head ──
        Row(
            modifier = Modifier.width(headWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                headName,
                fontSize = 13.sp,
                color = Color(0xFF374151),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ── Description ──
        Text(
            department.description ?: "-",
            modifier = Modifier.width(descriptionWidth),
            fontSize = 13.sp,
            color = Color(0xFF6B7280),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ── Total Employees ──
        Row(
            modifier = Modifier.width(employeesWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
            Text(
                department.totalEmployees.toString(),
                fontSize = 13.sp,
                color = Color(0xFFD97706),
                fontWeight = FontWeight.SemiBold
            )
        }

        // ── Teams ── (not in API response yet; static placeholder like Branch's Employees/Active Orders)
        Text(
            "0 Teams",
            modifier = Modifier.width(teamsWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151)
        )

        // ── Status Badge ──
        Box(modifier = Modifier.width(statusWidth)) {
            val (badgeText, bgColor, textColor) = if (department.status) {
                Triple("active", Color(0xFFD1FAE5), Color(0xFF059669))
            } else {
                Triple("inactive", Color(0xFFF3F4F6), Color(0xFF6B7280))
            }
            Box(
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(badgeText, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
            }
        }

        // ── Action Menu ──
        Box(modifier = Modifier.width(actionWidth), contentAlignment = Alignment.Center) {
            Box {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = "More",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { actionMenuExpanded = true }
                )
                DropdownMenu(
                    expanded = actionMenuExpanded,
                    onDismissRequest = { actionMenuExpanded = false },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(10.dp)
                ) {

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Edit", color = Color.Black)
                            }
                        },
                        onClick = {
                            actionMenuExpanded = false
                            onEditClick(department)
                        }
                    )
                    Spacer(Modifier.padding(top = 5.dp))
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("View Teams", color = Color.Black)
                            }
                        },
                        onClick = {
                            actionMenuExpanded = false
                            onViewClick(department)
                        }
                    )

                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Add Department Dialog - UI Design from image
// ─────────────────────────────────────────────────────────────

@Composable
fun AddDepartmentDialog(
    onDismiss: () -> Unit,
    onCreate: (DepartmentRequest) -> Unit,
    isLoading: Boolean = false,
    staffList: List<StaffDto> = emptyList()
) {
    // Form states
    var departmentName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedStaff by remember { mutableStateOf("") }
    var staffExpanded by remember { mutableStateOf(false) }
    var departmentHeadError by remember { mutableStateOf(false) }

    // Staff dropdown data
    val staffDisplayList = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == selectedStaff }?.key ?: ""

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Header ──
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Add New Department",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        "v1.0",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // ── Form Fields ──

                // Department Name
                DepartmentField(
                    label = "Department Name",
                    value = departmentName,
                    onValueChange = { departmentName = it },
                    placeholder = "Enter department name"
                )

                // Description
                DepartmentField(
                    label = "Description",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Enter description",
                    isDescription = true
                )

                // Department Head Dropdown
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Department Head",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFF3F4F6),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = if (departmentHeadError) 1.dp else 0.dp,
                                    color = Color.Red,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (staffList.isNotEmpty()) {
                                        staffExpanded = true
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedStaffLabel.ifEmpty {
                                    if (staffList.isEmpty()) "Loading..." else "Select an option"
                                },
                                fontSize = 14.sp,
                                color = if (selectedStaffLabel.isNotEmpty()) {
                                    Color(0xFF111827)
                                } else {
                                    Color(0xFF9CA3AF)
                                }
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Dropdown Menu
                        DropdownMenu(
                            expanded = staffExpanded,
                            onDismissRequest = { staffExpanded = false },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.White)
                        ) {
                            if (staffList.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "No staff available",
                                            color = Color(0xFF9CA3AF)
                                        )
                                    },
                                    onClick = { staffExpanded = false }
                                )
                            } else {
                                staffDisplayList.forEach { label ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                label,
                                                color = Color(0xFF111827),
                                                fontSize = 14.sp
                                            )
                                        },
                                        onClick = {
                                            selectedStaff = staffIdMap[label] ?: ""
                                            departmentHeadError = false
                                            staffExpanded = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (selectedStaff == staffIdMap[label]) {
                                                    Color(0xFFF3F4F6)
                                                } else {
                                                    Color.White
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // Error message for department head
                    if (departmentHeadError) {
                        Text(
                            "Please select a department head",
                            fontSize = 12.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Action Buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF374151)
                        ),
                        modifier = Modifier
                            .weight(0.4f)
                            .height(48.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Create Button
                    Button(
                        onClick = {
                            // Validate
                            if (selectedStaff.isEmpty()) {
                                departmentHeadError = true
                                return@Button
                            }
                            if (departmentName.isBlank()) {
                                // You can add similar validation for department name
                                return@Button
                            }

                            onCreate(
                                DepartmentRequest(
                                    name = departmentName,
                                    description = description,
                                    departmentHead = selectedStaff
                                )
                            )
                        },
                        enabled = !isLoading && departmentName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B3BF9),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFD1D5DB)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(0.6f)
                            .height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Create",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Department Field Component - FIXED
// ─────────────────────────────────────────────────────────────

@Composable
fun DepartmentField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDescription: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )

        // Use Box with BasicTextField for placeholder support
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                singleLine = !isDescription,
                maxLines = if (isDescription) 3 else 1,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                ),
                cursorBrush = SolidColor(Color(0xFF3B3BF9))
            )

            // Placeholder
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Data Class for Department Request
// ─────────────────────────────────────────────────────────────

data class DepartmentRequest(
    val name: String,
    val description: String,
    val departmentHead: String
)