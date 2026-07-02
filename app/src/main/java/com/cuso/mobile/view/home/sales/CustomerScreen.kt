//package com.cuso.mobile.view.home.sales
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.cuso.mobile.model.CustomerItem
//import com.cuso.mobile.view.home.`reusable-composables`.ActionDropdownMenu
//import com.cuso.mobile.view.home.`reusable-composables`.DataCard
//import com.cuso.mobile.view.home.`reusable-composables`.DataColumn
//import com.cuso.mobile.view.home.`reusable-composables`.MenuAction
//import com.cuso.mobile.viewmodel.CustomerUiState
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.*
//
//// ─────────────────────────────────────────────────────────────
//// Screen
//// ─────────────────────────────────────────────────────────────
//
//@Composable
//fun CustomerScreen(
//    navController: NavController,
//    customerState: CustomerUiState,
//    onSearch: (String) -> Unit = {},
//    onTypeFilterChange: (String) -> Unit = {},
//    onPageChange: (Int) -> Unit = {},
//    onItemsPerPageChange: (Int) -> Unit = {},
//    onBack: () -> Unit = {},
//    onCreateCustomer: () -> Unit = {},
//    onView: (CustomerItem) -> Unit = {},
//    onEdit: (CustomerItem) -> Unit = {},
//    onDelete: (CustomerItem) -> Unit = {}
//) {
//    val snackbarHostState = remember { SnackbarHostState() }
//    val coroutineScope = rememberCoroutineScope()
//
//    var searchQuery by remember { mutableStateOf("") }
//    var typeFilter by remember { mutableStateOf("all") }
//    var page by remember { mutableStateOf(1) }
//    var itemsPerPage by remember { mutableStateOf(10) }
//    var showTypeDropdown by remember { mutableStateOf(false) }
//    var showItemsPerPageDropdown by remember { mutableStateOf(false) }
//    var showViewModeDropdown by remember { mutableStateOf(false) }
//
//    // "table" or "card" — this is the toggle you asked for
//    var viewMode by remember { mutableStateOf("table") }
//
//    // ✅ State for selected customers (checkbox) - ONLY for Table View
//    var selectedCustomerIds by remember { mutableStateOf<Set<String>>(emptySet()) }
//
//    LaunchedEffect(customerState) {
//        if (customerState is CustomerUiState.Error) {
//            coroutineScope.launch { snackbarHostState.showSnackbar(customerState.message) }
//        }
//        // ✅ Reset selection when data changes
//        if (customerState is CustomerUiState.Success) {
//            selectedCustomerIds = emptySet()
//        }
//    }
//
//    val isLoading = customerState is CustomerUiState.Loading
//    val customers = (customerState as? CustomerUiState.Success)?.customers ?: emptyList()
//    val total = (customerState as? CustomerUiState.Success)?.total ?: 0
//    val totalPages = (customerState as? CustomerUiState.Success)?.totalPages ?: 1
//
//    val typeOptions = listOf(
//        "all" to "All Customers",
//        "individual" to "Individual",
//        "business" to "Business"
//    )
//
//    // ✅ Function to toggle select all (Table View only)
//    fun toggleSelectAll() {
//        val allIds = customers.map { it.id }.toSet()
//        selectedCustomerIds = if (selectedCustomerIds == allIds) {
//            emptySet()
//        } else {
//            allIds
//        }
//    }
//
//    // ✅ Check if all customers are selected (Table View only)
//    val isAllSelected = customers.isNotEmpty() && selectedCustomerIds == customers.map { it.id }.toSet()
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color(0xFFF9FAFB))
//        ) {
//
//            // ── Top Bar ──────────────────────────────────────────
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color.White)
//                    .padding(horizontal = 16.dp, vertical = 14.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Box {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(6.dp),
//                            modifier = Modifier.clickable { showTypeDropdown = true }
//                        ) {
//                            Icon(
//                                Icons.Default.FilterList,
//                                contentDescription = null,
//                                tint = Color(0xFF111827),
//                                modifier = Modifier.size(18.dp)
//                            )
//                            Text(
//                                typeOptions.find { it.first == typeFilter }?.second ?: "All Customers",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF111827)
//                            )
//                            Icon(
//                                Icons.Default.KeyboardArrowDown,
//                                contentDescription = null,
//                                tint = Color(0xFF111827),
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
//                        DropdownMenu(
//                            expanded = showTypeDropdown,
//                            onDismissRequest = { showTypeDropdown = false },
//                            containerColor = Color.White,
//                            shape = RoundedCornerShape(8.dp)
//                        ) {
//                            typeOptions.forEach { (value, label) ->
//                                DropdownMenuItem(
//                                    text = {
//                                        Row(
//                                            horizontalArrangement = Arrangement.SpaceBetween,
//                                            modifier = Modifier.fillMaxWidth()
//                                        ) {
//                                            Text(label, color = Color(0xFF111827))
//                                            if (value == typeFilter) {
//                                                Icon(Icons.Default.Check, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
//                                            }
//                                        }
//                                    },
//                                    onClick = {
//                                        typeFilter = value
//                                        page = 1
//                                        selectedCustomerIds = emptySet() // ✅ Reset selection
//                                        showTypeDropdown = false
//                                        onTypeFilterChange(value)
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
//
//
//            }
//
//            // ── Search bar ──
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color.White)
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(40.dp)
//                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
//                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        Icon(
//                            Icons.Default.Search,
//                            contentDescription = null,
//                            tint = Color(0xFF9CA3AF),
//                            modifier = Modifier.padding(start = 12.dp, end = 8.dp)
//                        )
//                        BasicTextField(
//                            value = searchQuery,
//                            onValueChange = {
//                                searchQuery = it
//                                page = 1
//                                selectedCustomerIds = emptySet() // ✅ Reset selection
//                                onSearch(it)
//                            },
//                            modifier = Modifier.fillMaxSize().padding(end = 12.dp),
//                            singleLine = true,
//                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF111827)),
//                            cursorBrush = SolidColor(Color(0xFF3B3BF9)),
//                            decorationBox = { innerTextField ->
//                                Box {
//                                    if (searchQuery.isEmpty()) {
//                                        Text("", fontSize = 14.sp, color = Color(0xFF9CA3AF))
//                                    }
//                                    innerTextField()
//                                }
//                            }
//                        )
//                    }
//                }
//
//                // ✅ Show selected count - ONLY in Table View
//                if (viewMode == "table" && selectedCustomerIds.isNotEmpty()) {
//                    Text(
//                        "${selectedCustomerIds.size} selected",
//                        fontSize = 13.sp,
//                        color = Color(0xFF3B3BF9),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    Button(
//                        onClick = { onCreateCustomer() },
//                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
//                        shape = RoundedCornerShape(8.dp),
//                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
//                    ) {
//                        Text("Create Customer", color = Color.White, fontSize = 14.sp)
//                    }
//                }
//            }
//
//            HorizontalDivider(color = Color(0xFFF0F0F0))
//
//            // ── Content ──────────────────────────────────────────
//            when {
//                isLoading -> {
//                    Box(
//                        modifier = Modifier.weight(1f).fillMaxWidth(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator(color = Color(0xFF3B3BF9))
//                    }
//                }
//
//                customerState is CustomerUiState.Error -> {
//                    Box(
//                        modifier = Modifier.weight(1f).fillMaxWidth(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
//                            Spacer(Modifier.height(8.dp))
//                            Text(
//                                customerState.message,
//                                color = Color.Red,
//                                textAlign = TextAlign.Center,
//                                modifier = Modifier.padding(horizontal = 32.dp)
//                            )
//                        }
//                    }
//                }
//
//                customerState is CustomerUiState.Success -> {
//                    if (customers.isEmpty()) {
//                        Box(
//                            modifier = Modifier.weight(1f).fillMaxWidth(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                Icon(Icons.Default.People, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
//                                Spacer(Modifier.height(8.dp))
//                                Text("No customers found", color = Color.Gray, fontSize = 15.sp)
//                            }
//                        }
//                    } else {
//                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
//
//                            Column(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .fillMaxWidth()
//                                    .verticalScroll(rememberScrollState())
//                            ) {
//
//                                    // ✅ Card View WITHOUT Checkbox
//                                    Column(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 16.dp, vertical = 8.dp),
//                                        verticalArrangement = Arrangement.spacedBy(10.dp)
//                                    ) {
//                                        customers.forEach { customer ->
//                                            CustomerCard(
//                                                customer = customer,
//                                                onView = { onView(customer) },
//                                                onEdit = { onEdit(customer) },
//                                                onDelete = { onDelete(customer) }
//                                            )
//                                        }
//                                    }
//
//                            }
//
//                            // ── Pagination ──
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .background(Color.White)
//                                    .padding(horizontal = 16.dp, vertical = 10.dp),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                                ) {
//                                    val from = (page - 1) * itemsPerPage + 1
//                                    val to = minOf(page * itemsPerPage, total)
//                                    Text("Showing $from - $to of $total", fontSize = 13.sp, color = Color(0xFF6B7280))
//
//                                    Box {
//                                        Row(
//                                            modifier = Modifier
//                                                .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
//                                                .clickable { showItemsPerPageDropdown = true }
//                                                .padding(horizontal = 8.dp, vertical = 4.dp),
//                                            verticalAlignment = Alignment.CenterVertically,
//                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
//                                        ) {
//                                            Icon(Icons.Default.Settings, null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
//                                            Text("$itemsPerPage per page", fontSize = 13.sp, color = Color(0xFF374151))
//                                        }
//                                        DropdownMenu(
//                                            expanded = showItemsPerPageDropdown,
//                                            onDismissRequest = { showItemsPerPageDropdown = false },
//                                            containerColor = Color.White,
//                                            shape = RoundedCornerShape(8.dp)
//                                        ) {
//                                            listOf(10, 25, 50, 100).forEach { count ->
//                                                DropdownMenuItem(
//                                                    text = { Text("$count per page", color = Color(0xFF111827)) },
//                                                    onClick = {
//                                                        itemsPerPage = count
//                                                        page = 1
//                                                        selectedCustomerIds = emptySet() // ✅ Reset selection
//                                                        showItemsPerPageDropdown = false
//                                                        onItemsPerPageChange(count)
//                                                    }
//                                                )
//                                            }
//                                        }
//                                    }
//                                }
//
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                                ) {
//                                    IconButton(
//                                        onClick = {
//                                            if (page > 1) {
//                                                page--
//                                                selectedCustomerIds = emptySet() // ✅ Reset selection
//                                                onPageChange(page)
//                                            }
//                                        },
//                                        enabled = page > 1,
//                                        modifier = Modifier.size(32.dp)
//                                    ) {
//                                        Icon(
//                                            Icons.Default.ChevronLeft,
//                                            contentDescription = "Previous",
//                                            tint = if (page > 1) Color(0xFF374151) else Color(0xFFD1D5DB)
//                                        )
//                                    }
//                                    Text("$page - $totalPages", fontSize = 13.sp, color = Color(0xFF6B7280))
//                                    IconButton(
//                                        onClick = {
//                                            if (page < totalPages) {
//                                                page++
//                                                selectedCustomerIds = emptySet() // ✅ Reset selection
//                                                onPageChange(page)
//                                            }
//                                        },
//                                        enabled = page < totalPages,
//                                        modifier = Modifier.size(32.dp)
//                                    ) {
//                                        Icon(
//                                            Icons.Default.ChevronRight,
//                                            contentDescription = "Next",
//                                            tint = if (page < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB)
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        SnackbarHost(
//            hostState = snackbarHostState,
//            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
//        )
//    }
//}
//
//// ─────────────────────────────────────────────────────────────
//// Table view with Select All Checkbox
//// ─────────────────────────────────────────────────────────────
//
//// ─────────────────────────────────────────────────────────────
//// 🔁 ONE column list for customers — reused by table header,
//// table cells AND card footer fields.
//// Needs selection state + callbacks, so it's a function.
//// ─────────────────────────────────────────────────────────────
//private fun customerColumns(
//    selectedIds: Set<String>,
//    onToggleSelect: (String) -> Unit,
//    onToggleSelectAll: () -> Unit,
//    isAllSelected: Boolean,
//    onView: (CustomerItem) -> Unit,
//    onEdit: (CustomerItem) -> Unit,
//    onDelete: (CustomerItem) -> Unit
//): List<DataColumn<CustomerItem>> = listOf(
//    DataColumn(
//        key = "select",
//        label = "",
//        width = 40.dp,
//        cellAlignment = Alignment.Center,
//        headerContent = {
//            Checkbox(
//                checked = isAllSelected,
//                onCheckedChange = { onToggleSelectAll() },
//                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B3BF9), uncheckedColor = Color(0xFF9CA3AF)),
//                modifier = Modifier.size(18.dp)
//            )
//        }
//    ) { customer ->
//        Checkbox(
//            checked = customer.id?.let { selectedIds.contains(it) } ?: false,
//            onCheckedChange = { customer.id?.let { onToggleSelect(it) } },
//            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B3BF9), uncheckedColor = Color(0xFF9CA3AF)),
//            modifier = Modifier.size(18.dp)
//        )
//    },
//    DataColumn("name", "Name", 150.dp) { c ->
//        Text(c.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827),
//            maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("type", "Type", 100.dp) { c ->
//        Box(modifier = Modifier.background(Color(0xFFE8EEFF), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
//            Text(c.type?.replaceFirstChar { it.uppercase() } ?: "—", fontSize = 12.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.Medium)
//        }
//    },
//    DataColumn("email", "Email", 140.dp) { c ->
//        Text(c.email?.ifBlank { "—" } ?: "—", fontSize = 13.sp, color = Color(0xFF374151), maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("mobile", "Mobile", 130.dp) { c ->
//        Text(c.mobile?.ifBlank { "—" } ?: "—", fontSize = 13.sp, color = Color(0xFF374151))
//    },
//    DataColumn("gender", "Gender", 90.dp) { c ->
//        Text(c.gender?.ifBlank { "—" } ?: "—", fontSize = 13.sp, color = Color(0xFF374151))
//    },
//    DataColumn("location", "Location", 110.dp) { c ->
//        Text(c.location, fontSize = 13.sp, color = Color(0xFF374151), maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("dob", "Date of Birth", 120.dp) { c ->
//        Text(c.dateOfBirth.toDisplayDate(), fontSize = 13.sp, color = Color(0xFF374151))
//    },
//    DataColumn("action", "Action", 70.dp, headerBold = true, cellAlignment = Alignment.Center) { c ->
//        ActionDropdownMenu(
//            actions = listOf(
//                MenuAction("View", Icons.Default.Visibility) { onView(c) },
//                MenuAction("Edit", Icons.Default.Edit) { onEdit(c) },
//                MenuAction("Delete", Icons.Default.Delete, tint = Color(0xFFF44336), textColor = Color(0xFFF44336)) { onDelete(c) }
//            )
//        )
//    }
//)
//
//// ── Table view (with select-all checkbox) — same call site as before ──
//
//
//// ── Card view (no checkbox) — same call site as before ──
//@Composable
//fun CustomerCard(
//    customer: CustomerItem,
//    onView: () -> Unit = {},
//    onEdit: () -> Unit = {},
//    onDelete: () -> Unit = {}
//) {
//    // selection args unused for card view — pass safe no-op defaults
//    val columns = customerColumns(
//        selectedIds = emptySet(),
//        onToggleSelect = {},
//        onToggleSelectAll = {},
//        isAllSelected = false,
//        onView = { onView() },
//        onEdit = { onEdit() },
//        onDelete = { onDelete() }
//    )
//    val typeColumn = columns.first { it.key == "type" }
//    val footerFields = columns.filter { it.key in listOf("email", "mobile", "gender", "location", "dob") }
//
//    DataCard(
//        item = customer,
//        leading = {
//            Box(
//                modifier = Modifier.size(42.dp).background(Color(0xFFEEF2FF), RoundedCornerShape(10.dp)),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(customer.name.firstOrNull()?.uppercase() ?: "?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B3BF9))
//            }
//        },
//        title = {
//            Column {
//                Text(customer.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827),
//                    maxLines = 1, overflow = TextOverflow.Ellipsis)
//                typeColumn.cellContent(customer)
//            }
//        },
//        trailing = {
//            ActionDropdownMenu(
//                icon = Icons.Default.MoreVert,
//                actions = listOf(
//                    MenuAction("View", Icons.Default.Visibility) { onView() },
//                    MenuAction("Edit", Icons.Default.Edit) { onEdit() },
//                    MenuAction("Delete", Icons.Default.Delete, tint = Color(0xFFF44336), textColor = Color(0xFFF44336)) { onDelete() }
//                )
//            )
//        },
//        fields = footerFields,
//        fieldsPerRow = 2
//    )
//}
//
//// ─────────────────────────────────────────────────────────────
//// Helpers
//// ─────────────────────────────────────────────────────────────
//
//fun Long?.toDisplayDate(): String {
//    if (this == null) return "—"
//    return runCatching {
//        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this))
//    }.getOrDefault("—")
//}