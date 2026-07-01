package com.cuso.mobile.view.home.sales

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.model.MeasurementItem
import com.cuso.mobile.viewmodel.MeasurementsUiState
import com.cuso.mobile.viewmodel.MeasurementsViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────
// Measurements Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun MeasurementsScreen(
    navController: NavController,
    viewModel: MeasurementsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCreateOrder: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("all") }
    var page by remember { mutableStateOf(1) }
    var itemsPerPage by remember { mutableStateOf(10) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showItemsPerPageDropdown by remember { mutableStateOf(false) }

    // ✅ View toggle state — true = Table View, false = Card View
    var isListView by remember { mutableStateOf(true) }

    val typeOptions = listOf(
        "all" to "All Customers",
        "individual" to "Individual",
        "corporate" to "Corporate"
    )

    val measurementItems = (uiState as? MeasurementsUiState.Success)?.items ?: emptyList()
    val isLoading = uiState is MeasurementsUiState.Loading

    // ── Filter items based on search ──
    val filteredItems = if (searchQuery.isNotBlank()) {
        measurementItems.filter {
            it.customerName.contains(searchQuery, ignoreCase = true) ||
                    it.contact.contains(searchQuery, ignoreCase = true)
        }
    } else {
        measurementItems
    }

    // ── Paginate ──
    val startIndex = (page - 1) * itemsPerPage
    val endIndex = minOf(startIndex + itemsPerPage, filteredItems.size)
    val pagedItems = if (filteredItems.isNotEmpty()) {
        filteredItems.subList(startIndex, endIndex)
    } else {
        emptyList()
    }
    val totalFiltered = filteredItems.size
    val totalPagesFiltered = if (totalFiltered > 0) (totalFiltered + itemsPerPage - 1) / itemsPerPage else 1

    Box(modifier = Modifier.fillMaxSize()) {
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
                    // Back Button
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onBack() },
                        tint = Color(0xFF111827)
                    )

                    Text(
                        "Measurements",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                // ── New Order Button ──
                Button(
                    onClick = onCreateOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("New Order", color = Color.White, fontSize = 14.sp)
                }
            }

            // ── Filter Row ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Filter Dropdown
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                            .clickable { showTypeDropdown = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            typeOptions.find { it.first == typeFilter }?.second ?: "All Customers",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
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
                                }
                            )
                        }
                    }
                }

                // Search Box
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                            },
                            modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF111827)),
                            cursorBrush = SolidColor(Color(0xFF3B3BF9)),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search customers…", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                // ── View Toggle ──
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isListView) Color.White else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { isListView = true }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Table view",
                            tint = if (isListView) Color(0xFF3B3BF9) else Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (!isListView) Color.White else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { isListView = false }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = "Card view",
                            tint = if (!isListView) Color(0xFF3B3BF9) else Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // ── Content ──────────────────────────────────────────
            when (val state = uiState) {
                is MeasurementsUiState.Loading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF3B3BF9))
                            Spacer(Modifier.height(8.dp))
                            Text("Loading measurements...", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }

                is MeasurementsUiState.Error -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.message,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.loadMeasurements() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9))
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }

                is MeasurementsUiState.Success -> {
                    val items = state.items

                    val filteredList = if (searchQuery.isNotBlank()) {
                        items.filter {
                            it.customerName.contains(searchQuery, ignoreCase = true) ||
                                    it.contact.contains(searchQuery, ignoreCase = true)
                        }
                    } else {
                        items
                    }

                    val startIdx = (page - 1) * itemsPerPage
                    val endIdx = minOf(startIdx + itemsPerPage, filteredList.size)
                    val pagedList = if (filteredList.isNotEmpty()) {
                        filteredList.subList(startIdx, endIdx)
                    } else {
                        emptyList()
                    }
                    val totalFilteredCount = filteredList.size
                    val totalPagesFilteredCount = if (totalFilteredCount > 0) (totalFilteredCount + itemsPerPage - 1) / itemsPerPage else 1

                    // ✅ Empty state check
                    if (pagedList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color(0xFFF3F4F6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.List,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }

                                Text(
                                    text = "No Measurements Found",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )

                                Text(
                                    text = if (searchQuery.isNotBlank()) {
                                        "No customers match your search criteria"
                                    } else {
                                        "No measurement records available"
                                    },
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )

                                if (searchQuery.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            searchQuery = ""
                                            page = 1
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF3B3BF9)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF3B3BF9)
                                        ),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Clear Search", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // ✅ Data with Table or Card View
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            // ── Content Container ──
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (isListView) {
                                    // ✅ Table View
                                    MeasurementsTable(items = pagedList)
                                } else {
                                    // ✅ Card View
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        pagedList.forEach { item ->
                                            MeasurementCard(item = item)
                                        }
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
                                    val to = minOf(page * itemsPerPage, totalFilteredCount)
                                    Text("Showing $from - $to of $totalFilteredCount", fontSize = 13.sp, color = Color(0xFF6B7280))

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
                                        onClick = { if (page > 1) page-- },
                                        enabled = page > 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronLeft,
                                            contentDescription = "Previous",
                                            tint = if (page > 1) Color(0xFF374151) else Color(0xFFD1D5DB)
                                        )
                                    }
                                    Text("$page - $totalPagesFilteredCount", fontSize = 13.sp, color = Color(0xFF6B7280))
                                    IconButton(
                                        onClick = { if (page < totalPagesFilteredCount) page++ },
                                        enabled = page < totalPagesFilteredCount,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Next",
                                            tint = if (page < totalPagesFilteredCount) Color(0xFF374151) else Color(0xFFD1D5DB)
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

// ─────────────────────────────────────────────────────────────
// Measurements Table
// ─────────────────────────────────────────────────────────────

@Composable
fun MeasurementsTable(
    items: List<MeasurementItem>
) {
    val horizontalScrollState = rememberScrollState()

    val customerWidth = 150.dp
    val contactWidth = 130.dp
    val typeWidth = 100.dp
    val garmentsWidth = 120.dp
    val pendingWidth = 100.dp
    val spendWidth = 120.dp
    val dateWidth = 120.dp
    val actionWidth = 70.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .background(Color(0xFFF9FAFB))
                .border(BorderStroke(1.dp, Color(0xFFE5E7EB)))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MeasurementsHeaderCell("Customer", customerWidth)
            MeasurementsHeaderCell("Contact", contactWidth)
            MeasurementsHeaderCell("Type", typeWidth)
            MeasurementsHeaderCell("Garments", garmentsWidth)
            MeasurementsHeaderCell("Pending", pendingWidth)
            MeasurementsHeaderCell("Total Spend", spendWidth)
            MeasurementsHeaderCell("Last Updated", dateWidth)
            MeasurementsHeaderCell("Action", actionWidth, bold = true)
        }

        HorizontalDivider(color = Color(0xFFE5E7EB))

        // ── Body ──
        Column {
            items.forEach { item ->
                MeasurementsRow(
                    item = item,
                    customerWidth = customerWidth,
                    contactWidth = contactWidth,
                    typeWidth = typeWidth,
                    garmentsWidth = garmentsWidth,
                    pendingWidth = pendingWidth,
                    spendWidth = spendWidth,
                    dateWidth = dateWidth,
                    actionWidth = actionWidth
                )
                HorizontalDivider(color = Color(0xFFF3F4F6))
            }
        }
    }
}

@Composable
private fun MeasurementsHeaderCell(text: String, width: Dp, bold: Boolean = false) {
    Text(
        text,
        modifier = Modifier.width(width),
        fontSize = 13.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
        color = if (bold) Color(0xFF111827) else Color(0xFF6B7280)
    )
}

@Composable
fun MeasurementsRow(
    item: MeasurementItem,
    customerWidth: Dp,
    contactWidth: Dp,
    typeWidth: Dp,
    garmentsWidth: Dp,
    pendingWidth: Dp,
    spendWidth: Dp,
    dateWidth: Dp,
    actionWidth: Dp
) {
    var actionMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Customer Name
        Text(
            text = item.customerName,
            modifier = Modifier.width(customerWidth),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Contact
        Text(
            text = item.contact,
            modifier = Modifier.width(contactWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Type with badge
        Box(modifier = Modifier.width(typeWidth)) {
            Box(
                modifier = Modifier
                    .background(
                        if (item.type == "Corporate") Color(0xFFFEF3C7) else Color(0xFFE8EEFF),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    item.type,
                    fontSize = 12.sp,
                    color = if (item.type == "Corporate") Color(0xFFD97706) else Color(0xFF3B3BF9),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Garments
        Text(
            text = item.garments,
            modifier = Modifier.width(garmentsWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Pending
        Text(
            text = item.pending,
            modifier = Modifier.width(pendingWidth),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (item.pending == "₹0") Color(0xFF22C55E) else Color(0xFFEF4444)
        )

        // Total Spend
        Text(
            text = item.totalSpend,
            modifier = Modifier.width(spendWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151),
            fontWeight = FontWeight.Medium
        )

        // Last Updated
        Text(
            text = item.lastUpdated,
            modifier = Modifier.width(dateWidth),
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )

        // Action
        Box(modifier = Modifier.width(actionWidth), contentAlignment = Alignment.Center) {
            Box {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = "Actions",
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
                                Icon(Icons.Default.Visibility, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                Text("View", color = Color(0xFF111827))
                            }
                        },
                        onClick = { actionMenuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Edit, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                Text("Edit", color = Color(0xFF111827))
                            }
                        },
                        onClick = { actionMenuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                                Text("Delete", color = Color(0xFFF44336))
                            }
                        },
                        onClick = { actionMenuExpanded = false }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Measurements Card View
// ─────────────────────────────────────────────────────────────

@Composable
fun MeasurementCard(
    item: MeasurementItem
) {
    var actionMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Top: Avatar + Name + Type + Menu ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.customerName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B3BF9)
                        )
                    }
                    Column {
                        Text(
                            item.customerName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (item.type == "Corporate") Color(0xFFFEF3C7) else Color(0xFFE8EEFF),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                item.type,
                                fontSize = 11.sp,
                                color = if (item.type == "Corporate") Color(0xFFD97706) else Color(0xFF3B3BF9),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Box {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Actions",
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
                                    Icon(Icons.Default.Visibility, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                    Text("View", color = Color(0xFF111827))
                                }
                            },
                            onClick = { actionMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Edit, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                    Text("Edit", color = Color(0xFF111827))
                                }
                            },
                            onClick = { actionMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                                    Text("Delete", color = Color(0xFFF44336))
                                }
                            },
                            onClick = { actionMenuExpanded = false }
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))

            // ── Middle: Contact + Garments ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Contact", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(
                        item.contact,
                        fontSize = 13.sp,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Garments", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(
                        item.garments,
                        fontSize = 13.sp,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))

            // ── Bottom: Pending + Total Spend + Last Updated ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Pending", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(
                        item.pending,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.pending == "₹0") Color(0xFF22C55E) else Color(0xFFEF4444)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Spend", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(
                        item.totalSpend,
                        fontSize = 13.sp,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Last Updated", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(
                        item.lastUpdated,
                        fontSize = 13.sp,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}