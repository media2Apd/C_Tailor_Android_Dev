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
import com.cuso.mobile.model.StaffDto
import com.cuso.mobile.model.UpdateBranchAddress
import com.cuso.mobile.model.UpdateBranchRequest
import com.cuso.mobile.viewmodel.BranchUiState
import com.cuso.mobile.viewmodel.BranchViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import com.cuso.mobile.viewmodel.UpdateBranchUiState
import kotlinx.coroutines.launch

@Composable
fun BranchSettingsScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val branchViewModel: BranchViewModel = hiltViewModel()
    val salesViewModel: SalesViewModel = hiltViewModel()

    val uiState by branchViewModel.uiState.collectAsState()
    val updateState by branchViewModel.updateState.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()

    var editingBranch by remember { mutableStateOf<BranchItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        salesViewModel.fetchStaff()
    }

    // ── React to update result ──
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateBranchUiState.Success -> {
                editingBranch = null
                branchViewModel.refresh()
                branchViewModel.resetUpdateState()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Branch updated successfully")
                }
            }
            is UpdateBranchUiState.Error -> {
                branchViewModel.resetUpdateState()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
            else -> Unit
        }
    }

    val isUpdating = updateState is UpdateBranchUiState.Loading

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
                        "Branches",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Button(
                    onClick = { /* TODO: Add Branch */ },
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
                is BranchUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is BranchUiState.Error -> {
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
                                onClick = { branchViewModel.refresh() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Retry", color = Color.White) }
                        }
                    }
                }

                is BranchUiState.Success -> {
                    if (state.branches.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("No branches found", color = Color.Gray, fontSize = 15.sp)
                            }
                        }
                    } else {
                        BranchTable(
                            branches = state.branches,
                            onEditClick = { branch -> editingBranch = branch }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    // ── Edit Dialog ──
    editingBranch?.let { branch ->
        EditBranchDialog(
            branch = branch,
            staffList = staffList,
            isLoading = isUpdating,
            onDismiss = { editingBranch = null },
            onUpdate = { request ->
                branchViewModel.updateBranch(
                    branchId = branch.id, // ← real Mongo _id, NOT branch.branchId
                    request = UpdateBranchRequest(
                        name = request.name,
                        address = UpdateBranchAddress(
                            street = request.street,
                            city = request.city,
                            postalCode = request.postalCode
                        ),
                        contactEmail = request.contactEmail,
                        contactMobile = request.contactMobile,
                        status = branch.status, // not editable in this dialog yet
                        branchHead = request.branchHead
                    )
                )
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// BranchTable
// ─────────────────────────────────────────────────────────────

@Composable
fun BranchTable(
    branches: List<BranchItem>,
    onEditClick: (BranchItem) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    val branchIdWidth    = 180.dp
    val branchNameWidth  = 220.dp
    val locationWidth    = 220.dp
    val employeesWidth   = 110.dp
    val activeOrderWidth = 130.dp
    val managersWidth    = 150.dp
    val statusWidth      = 100.dp
    val actionWidth      = 80.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
    ) {
        // ── Table Header ──
        Row(
            modifier = Modifier
                .background(Color(0xFFF1F1F1))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Branch ID",     modifier = Modifier.width(branchIdWidth),    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Branch Name",   modifier = Modifier.width(branchNameWidth),  fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Location",      modifier = Modifier.width(locationWidth),    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Employees",     modifier = Modifier.width(employeesWidth),   fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Active Orders", modifier = Modifier.width(activeOrderWidth), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Managers",      modifier = Modifier.width(managersWidth),    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Status",        modifier = Modifier.width(statusWidth),      fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Action",        modifier = Modifier.width(actionWidth),      fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }

        HorizontalDivider(color = Color(0xFFF0F0F0))

        LazyColumn {
            items(branches) { branch ->
                BranchTableRow(
                    branch           = branch,
                    branchIdWidth    = branchIdWidth,
                    branchNameWidth  = branchNameWidth,
                    locationWidth    = locationWidth,
                    employeesWidth   = employeesWidth,
                    activeOrderWidth = activeOrderWidth,
                    managersWidth    = managersWidth,
                    statusWidth      = statusWidth,
                    actionWidth      = actionWidth,
                    onEditClick      = onEditClick
                )
                HorizontalDivider(color = Color(0xFFF5F5F5))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// BranchTableRow
// ─────────────────────────────────────────────────────────────

@Composable
fun BranchTableRow(
    branch: BranchItem,
    branchIdWidth: Dp,
    branchNameWidth: Dp,
    locationWidth: Dp,
    employeesWidth: Dp,
    activeOrderWidth: Dp,
    managersWidth: Dp,
    statusWidth: Dp,
    actionWidth: Dp,
    onEditClick: (BranchItem) -> Unit
) {
    var actionMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Branch ID ──
        Text(
            branch.branchId,
            modifier = Modifier.width(branchIdWidth),
            fontSize = 13.sp,
            color = Color(0xFF3B3BF9),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ── Branch Name ──
        Row(
            modifier = Modifier.width(branchNameWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                branch.name,
                fontSize = 13.sp,
                color = Color(0xFF3B3BF9),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (branch.isMainBranch) {
                Text("⭐", fontSize = 13.sp)
            }
        }

        // ── Location ──
        val locationText = listOfNotNull(branch.address.city, branch.address.state)
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifEmpty { "—" }
        Text(
            locationText,
            modifier = Modifier.width(locationWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ── Employees ──
        Row(
            modifier = Modifier.width(employeesWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.People, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
            Text("0", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
        }

        // ── Active Orders ──
        Box(modifier = Modifier.width(activeOrderWidth)) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.ShoppingBag, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                    Text("0", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ── Managers (Branch Head) ──
        val branchHeadName = branch.branchHead?.let { "${it.firstName} ${it.lastName}" } ?: "-"
        Text(
            branchHeadName,
            modifier = Modifier.width(managersWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ── Status Badge ──
        Box(modifier = Modifier.width(statusWidth)) {
            val (badgeText, badgeColor) = when (branch.status.lowercase()) {
                "active"   -> "Active"   to Color(0xFF16A34A)
                "inactive" -> "Inactive" to Color(0xFF6B7280)
                else       -> branch.status to Color(0xFF9CA3AF)
            }
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
                            onEditClick(branch)
                        }
                    )

                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// EditBranchDialog - FIXED with null safety for ALL fields
// ─────────────────────────────────────────────────────────────

@Composable
fun EditBranchDialog(
    branch: BranchItem,
    staffList: List<StaffDto>,
    onDismiss: () -> Unit,
    onUpdate: (EditBranchRequest) -> Unit,
    isLoading: Boolean = false
) {
    // Initialize with branch data - using null safety for all fields
    var branchName by remember { mutableStateOf(branch.name ) }
    var street by remember { mutableStateOf(branch.address.street ) }
    var city by remember { mutableStateOf(branch.address.city ) }
    var state by remember { mutableStateOf(branch.address.state ) }
    var postalCode by remember { mutableStateOf(branch.address.postalCode ) }
    var contactEmail by remember { mutableStateOf(branch.contactEmail ) }
    var contactMobile by remember { mutableStateOf(branch.contactMobile ) }
    var selectedStaff by remember { mutableStateOf(branch.branchHead?.id ?: "") }
    var staffExpanded by remember { mutableStateOf(false) }

    val staffDisplayList = staffList.map { "${it.firstName} ${it.lastName} - ${it.memberId}" }
    val staffIdMap = staffList.associate { "${it.firstName} ${it.lastName} - ${it.memberId}" to it.id }
    val selectedStaffLabel = staffIdMap.entries.firstOrNull { it.value == selectedStaff }?.key ?: ""

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Branch", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                EditBranchField("Branch Name", branchName, { branchName = it })
                EditBranchField("Street Address", street, { street = it })
                EditBranchField("City", city, { city = it })
                EditBranchField("State", state, { state = it })
                EditBranchField("Postal Code", postalCode, { postalCode = it }, KeyboardType.Number)
                EditBranchField("Contact Email", contactEmail, { contactEmail = it }, KeyboardType.Email)
                EditBranchField("Contact Mobile", contactMobile, { contactMobile = it }, KeyboardType.Phone)

                // ── Branch Head Dropdown ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Branch Head", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3B3BF9))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .clickable { staffExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedStaffLabel.ifEmpty {
                                    if (staffList.isEmpty()) "Loading..." else "Select Branch Head"
                                },
                                fontSize = 14.sp,
                                color = if (selectedStaffLabel.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF374151),
                                fontWeight = if (selectedStaffLabel.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = staffExpanded,
                            onDismissRequest = { staffExpanded = false },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            staffDisplayList.forEach { label ->
                                DropdownMenuItem(
                                    text = { Text(label, color = Color(0xFF374151)) },
                                    onClick = {
                                        selectedStaff = staffIdMap[label] ?: ""
                                        staffExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Text("Cancel", color = Color(0xFF374151))
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onUpdate(
                                EditBranchRequest(
                                    name = branchName,
                                    street = street?:"",
                                    city = city?:"",
                                    state = state?:"",
                                    postalCode = postalCode?:"",
                                    contactEmail = contactEmail,
                                    contactMobile = contactMobile,
                                    branchHead = selectedStaff
                                )
                            )
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
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
// EditBranchField - FIXED to accept nullable values
// ─────────────────────────────────────────────────────────────

@Composable
fun EditBranchField(
    label: String,
    value: String?,  // ← Made nullable
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3B3BF9))
        BasicTextField(
            value = value ?: "",  // ← Null safety with default
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color(0xFF374151))
        )
    }
}

// ── Request model ──
data class EditBranchRequest(
    val name: String,
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val contactEmail: String,
    val contactMobile: String,
    val branchHead: String
)