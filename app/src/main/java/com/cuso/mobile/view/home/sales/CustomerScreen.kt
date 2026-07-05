package com.cuso.mobile.view.home.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cuso.mobile.model.CustomerItem
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardBadge
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.viewmodel.CustomerUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun CustomerScreen(
    navController: NavController,
    customerState: CustomerUiState,
    onSearch: (String) -> Unit = {},
    onTypeFilterChange: (String) -> Unit = {},
    onPageChange: (Int) -> Unit = {},
    onItemsPerPageChange: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    onCreateCustomer: () -> Unit = {},
    onView: (CustomerItem) -> Unit = {},
    onEdit: (CustomerItem) -> Unit = {},
    onDelete: (CustomerItem) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("all") }
    var page by remember { mutableStateOf(1) }
    var itemsPerPage by remember { mutableStateOf(10) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showItemsPerPageDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(customerState) {
        if (customerState is CustomerUiState.Error) {
            coroutineScope.launch { snackbarHostState.showSnackbar(customerState.message) }
        }
    }

    val isLoading = customerState is CustomerUiState.Loading
    val customers = (customerState as? CustomerUiState.Success)?.customers ?: emptyList()
    val total = (customerState as? CustomerUiState.Success)?.total ?: 0
    val totalPages = (customerState as? CustomerUiState.Success)?.totalPages ?: 1

    val typeOptions = listOf(
        "all" to "All Customers",
        "individual" to "Individual",
        "business" to "Business"
    )

    Box(modifier = Modifier.fillMaxSize()) {   // ✅ NEW — wraps everything so FAB overlays content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
        ) {

            // ── Top Bar ──────────────────────────────────────────
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
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable { showTypeDropdown = true }
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                typeOptions.find { it.first == typeFilter }?.second ?: "All Customers",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showTypeDropdown,
                            onDismissRequest = { showTypeDropdown = false },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            typeOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(label, color = Color(0xFF111827))
                                            if (value == typeFilter) {
                                                Icon(Icons.Default.Check, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        typeFilter = value
                                        page = 1
                                        showTypeDropdown = false
                                        onTypeFilterChange(value)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Search bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)    // ✅ now takes full width since button is removed
                        .height(40.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                page = 1
                                onSearch(it)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 12.dp),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF111827)),
                            cursorBrush = SolidColor(Color(0xFF3B3BF9)),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text("", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
                // ✅ REMOVED — "Create Customer" Button moved out to fixed FAB below
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // ── Content ──────────────────────────────────────────
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3B3BF9))
                    }
                }

                customerState is CustomerUiState.Error -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                customerState.message,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }

                customerState is CustomerUiState.Success -> {
                    if (customers.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.People, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No customers found", color = Color.Gray, fontSize = 15.sp)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // ✅ Card View — using DataCard directly (Designation pattern)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    customers.forEach { customer ->
                                        val (badgeText, badgeColor) = if (customer.type == "business")
                                            "Business" to Color(0xFFD97706)
                                        else
                                            "Individual" to Color(0xFF3B3BF9)

                                        DataCard(
                                            item = customer,
                                            badge = DataCardBadge(
                                                text = badgeText,
                                                color = badgeColor
                                            ),
                                            title = customer.name,
                                            subtitle = customer.email?.ifBlank { "—" } ?: "—",
                                            footerFields = listOf(
                                                DataCardField(icon = Icons.Default.Phone, text = customer.mobile?.ifBlank { "—" } ?: "—"),
                                                DataCardField(icon = Icons.Default.Person, text = customer.gender?.ifBlank { "—" } ?: "—"),
                                                DataCardField(icon = Icons.Default.LocationOn, text = customer.location),
                                                DataCardField(
                                                    icon = Icons.Default.CalendarMonth,
                                                    text = customer.dateOfBirth.toDisplayDate()
                                                )
                                            ),
                                            actions = listOf(
                                                MenuAction(
                                                    "View",
                                                    Icons.Default.Visibility
                                                ) { onView(customer) },
                                                MenuAction("Edit", Icons.Default.Edit) { onEdit(customer) },
                                                MenuAction("Delete", Icons.Default.Delete, tint = Color(0xFFF44336), textColor = Color(0xFFF44336)) { onDelete(customer) }
                                            )
                                        )
                                    }
                                }
                            }

                            // ── Pagination ──
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val from = (page - 1) * itemsPerPage + 1
                                    val to = minOf(page * itemsPerPage, total)
                                    Text("Showing $from - $to of $total", fontSize = 13.sp, color = Color(0xFF6B7280))

                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                                                .clickable { showItemsPerPageDropdown = true }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Settings, null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                                            Text("$itemsPerPage per page", fontSize = 13.sp, color = Color(0xFF374151))
                                        }
                                        DropdownMenu(
                                            expanded = showItemsPerPageDropdown,
                                            onDismissRequest = { showItemsPerPageDropdown = false },
                                            containerColor = Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            listOf(10, 25, 50, 100).forEach { count ->
                                                DropdownMenuItem(
                                                    text = { Text("$count per page", color = Color(0xFF111827)) },
                                                    onClick = {
                                                        itemsPerPage = count
                                                        page = 1
                                                        showItemsPerPageDropdown = false
                                                        onItemsPerPageChange(count)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (page > 1) {
                                                page--
                                                onPageChange(page)
                                            }
                                        },
                                        enabled = page > 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronLeft,
                                            contentDescription = "Previous",
                                            tint = if (page > 1) Color(0xFF374151) else Color(0xFFD1D5DB)
                                        )
                                    }
                                    Text("$page - $totalPages", fontSize = 13.sp, color = Color(0xFF6B7280))
                                    IconButton(
                                        onClick = {
                                            if (page < totalPages) {
                                                page++
                                                onPageChange(page)
                                            }
                                        },
                                        enabled = page < totalPages,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Next",
                                            tint = if (page < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB)
                                        )
                                    }
                                }
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

        // ✅ NEW — Fixed FAB "Create Customer" Button (Branch pattern)
        Button(
            onClick = { onCreateCustomer() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Create Customer", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
