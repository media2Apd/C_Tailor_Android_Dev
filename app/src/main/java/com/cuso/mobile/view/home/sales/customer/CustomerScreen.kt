@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"

)
package com.cuso.mobile.view.home.sales.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cuso.mobile.model.sales.CustomerItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.DataCardImage
//import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.view.home.reusablecomposables.rememberFilterDrawerState
import com.cuso.mobile.view.home.sales.sales_order.toDisplayDate
import com.cuso.mobile.viewmodel.CustomerUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ✅ NEW — overload for ISO-8601 date strings (e.g. "2004-04-08T00:00:00.000Z")
fun String?.toDisplayDate(): String {
    if (this.isNullOrBlank()) return "—"
    return try {
        val inputFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd"
        )
        var parsedDate: java.util.Date? = null
        for (pattern in inputFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                parsedDate = sdf.parse(this)
                if (parsedDate != null) break
            } catch (_: Exception) {
                // try next pattern
            }
        }
        parsedDate?.let {
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            outputFormat.format(it)
        } ?: "—"
    } catch (_: Exception) {
        "—"
    }
}

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")

@Composable
fun CustomerScreen(
    navController: NavController,
    customerState: CustomerUiState,

    onSearch: (String) -> Unit = {},
    onTypeFilterChange: (String) -> Unit = {},
    onPageChange: (Int) -> Unit = {},
    onItemsPerPageChange: (Int) -> Unit = {},
    onClose: () -> Unit = {},
    onCreateCustomer: () -> Unit = {},
    onView: (CustomerItem) -> Unit = {},
    onEdit: (CustomerItem) -> Unit = {},
    onDelete: (CustomerItem) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
//    var typeFilter by remember { mutableStateOf("all") }
    var page by remember { mutableIntStateOf(1) }
    var itemsPerPage by remember { mutableIntStateOf(10) }
//    var showTypeDropdown by remember { mutableStateOf(false) }
    var showItemsPerPageDropdown by remember { mutableStateOf(false) }
    var customerPendingDelete by remember { mutableStateOf<CustomerItem?>(null) }   // ✅ NEW
    var currentPage by remember { mutableIntStateOf(1) }


    LaunchedEffect(customerState) {
        if (customerState is CustomerUiState.Error) {
            coroutineScope.launch { snackbarHostState.showSnackbar(customerState.message) }
        }
    }

    val isLoading = customerState is CustomerUiState.Loading
    val customers = (customerState as? CustomerUiState.Success)?.customers ?: emptyList()
    val total = (customerState as? CustomerUiState.Success)?.total ?: 0
    val totalPages = (customerState as? CustomerUiState.Success)?.totalPages ?: 1
    val filterDrawerState = rememberFilterDrawerState()

//
//    val typeOptions = listOf(
//        "all" to "All Customers",
//        "individual" to "Individual",
//        "business" to "Business"
//    )

    FabScaffold(
//        fab = FabConfig(
//            label = "Create Customer",
//            icon = Icons.Default.Add,
//            onClick = onCreateCustomer
//        ),
        fab=null,
        snackbarHostState = snackbarHostState
    ) {
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Customers", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onClose() }   // ✅ NEW
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .background(Color(0xFFF8F9FF))
                    .fillMaxWidth()
                    .padding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()
                                 .background(Color(0xFFF8F9FF))
                ) {
                    ScreenBreadcrumb(segments = listOf("Sales", "Customers"), onClick = {})

                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            placeholder = "Search Customers...",
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            onFilterClick = { /* TODO: open filter drawer */ }
                        )

                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // ── Content ──────────────────────────────────────────
            when {
                isLoading -> {
                    ListSkeleton()
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
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    customers.forEach { customer ->
                                        val (badgeText, badgeColor) = when (customer.type?.lowercase()) {
                                            "business" -> "Business" to Color(0xFFD97706)
                                            "regular" -> "Regular" to Color(0xFF16A34A)     // ✅ NEW — handles "regular" type from real API
                                            else -> "Individual" to Color(0xFF3B3BF9)
                                        }

                                        DataCard(
                                            item = customer,
                                            image = DataCardImage(
                                                vector = Icons.Default.Person,
                                                size = 50.dp,
                                                backgroundColor = Color.Transparent,
                                                tint = Color(0xFF9CA3AF)
                                            ),
                                            topBadgeText = badgeText,
                                            topBadgeTextColor = badgeColor,
                                            topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
                                            topBadgeInline = true,                       // ✅ badge next to name, not top row
                                            title = customer.name,
                                            subtitle = customer.dateOfBirth
                                                ?.takeIf { it.isNotBlank() }
                                                ?.let { "Date of Birth  ${it.toDisplayDate()}" }
                                                ?: "Date of Birth  —",   // ✅ CHANGED — dob is often missing in real data, avoid crash/garbage text                                            footerAsRows = true,
                                            footerFields = listOf(
                                                DataCardField(label = "Email", text = customer.email?.ifBlank { "—" } ?: "—"),
                                                DataCardField(label = "Mobile", text = customer.mobile?.ifBlank { "—" } ?: "—"),
                                                DataCardField(label = "Gender", text = customer.gender?.ifBlank { "—" } ?: "—"),
                                                DataCardField(label = "Location", text = customer.location.ifBlank { "—" })
                                            ),
                                            actions = listOf(
                                                MenuAction("View", Icons.Default.Visibility) { onView(customer) },
                                                MenuAction("Edit", Icons.Default.Edit) { onEdit(customer) },
                                                MenuAction("Delete", Icons.Default.Delete, tint = Color(0xFFF44336), textColor = Color(0xFFF44336)) {
                                                    customerPendingDelete = customer
                                                }
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
    }
    customerPendingDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { customerPendingDelete = null },
            title = { Text("Delete Customer") },
            text = { Text("Are you sure you want to delete \"${customer.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(customer)
                        customerPendingDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { customerPendingDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White
        )
    }
}