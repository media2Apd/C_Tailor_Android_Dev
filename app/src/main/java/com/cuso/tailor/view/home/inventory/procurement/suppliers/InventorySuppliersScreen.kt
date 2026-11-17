@file:Suppress("unused")

package com.cuso.tailor.view.home.inventory.procurement.suppliers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.SupplierDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay

@Composable
fun InventorySuppliersScreen(
    onClose: () -> Unit,
    onSupplierClick: (SupplierDto) -> Unit,
    onAddSupplier: () -> Unit = {},
    onEditSupplier: (SupplierDto) -> Unit = onSupplierClick,
    onBreadCrumbClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val suppliersPagingState by viewModel.suppliersPagingState.collectAsStateWithLifecycle()
    val errorMsg by viewModel.suppliersError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSupplierToDelete by remember { mutableStateOf<SupplierDto?>(null) }

    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (!isInitialized) {
            isInitialized = true
            viewModel.fetchSuppliers()
        } else {
            delay(400)
            viewModel.fetchSuppliers(search = searchQuery.trim().ifBlank { null })
        }
    }

    val filteredList = remember(suppliersPagingState.items, searchQuery) {
        if (searchQuery.isBlank()) suppliersPagingState.items
        else {
            suppliersPagingState.items.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.supplierCode.contains(searchQuery, ignoreCase = true) ||
                        (it.contact?.contactName?.contains(searchQuery, ignoreCase = true) == true) ||
                        (it.contact?.phone?.contains(searchQuery, ignoreCase = true) == true) ||
                        (it.address?.billing?.city?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    val displayPagingState = remember(suppliersPagingState, filteredList) {
        suppliersPagingState.copy(items = filteredList)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "All Suppliers",
                    onClose = onClose
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Spacer(Modifier.padding(top = 10.dp))
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Suppliers...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )
                Spacer(Modifier.padding(top = 10.dp))

                HorizontalDivider(color = grey_border)

                when {
                    errorMsg != null && displayPagingState.items.isEmpty() && !displayPagingState.isInitialLoading -> {
                        AppErrorState(
                            title = "Failed to load suppliers",
                            message = errorMsg?.let { ErrorMapper.map(it) } ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchSuppliers(search = searchQuery.trim().ifBlank { null }) }
                        )
                    }

                    !displayPagingState.isInitialLoading && displayPagingState.items.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No suppliers found" else "No matching suppliers found",
                                fontSize = tokens.bodyMedium,
                                color = mutedText
                            )
                        }
                    }

                    else -> {
                        PaginatedDataList(
                            state = displayPagingState,
                            onLoadMore = { viewModel.loadMoreSuppliers() },
                            modifier = Modifier.fillMaxSize()
                        ) { supplier ->
                            SupplierDataCardItem(
                                supplier = supplier,
                                onClick = { onSupplierClick(supplier) },
                                onEdit = { onEditSupplier(supplier) },
                                onDelete = { selectedSupplierToDelete = supplier }
                            )
                        }
                    }
                }
            }
        }

        selectedSupplierToDelete?.let { supplier ->
            DeleteModel(
                title = "Delete Supplier",
                message = "Are you sure you want to delete '${supplier.name}'? You can restore it within 7 days.",
                onDismiss = { selectedSupplierToDelete = null },
                onDelete = {
                    val idToDelete = supplier.id
                    selectedSupplierToDelete = null
                    viewModel.deleteSupplier(idToDelete)
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable Supplier Card Using DataCard
// ─────────────────────────────────────────────────────────────

@Composable
private fun SupplierDataCardItem(
    supplier: SupplierDto,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val isActive = supplier.status.equals("active", ignoreCase = true)
    val isVerified = supplier.complianceStatus.equals("Verified", ignoreCase = true) ||
            supplier.complianceStatus.equals("Completed", ignoreCase = true)

    val category = supplier.category.ifBlank { "FABRIC" }.uppercase()
    val supplierCode = supplier.supplierCode.ifBlank { "SUP-001" }
    val supplierName = supplier.name.ifBlank { "Unknown Supplier" }
    val contactName = supplier.contact?.contactName.orEmpty()
    val phone = supplier.contact?.phone.orEmpty()
    val city = supplier.address?.billing?.city?.ifBlank { "Chennai" } ?: "Chennai"

    val contactDetails = remember(contactName, phone) {
        when {
            contactName.isNotBlank() && phone.isNotBlank() -> "$contactName • $phone"
            contactName.isNotBlank() -> contactName
            phone.isNotBlank() -> phone
            else -> "—"
        }
    }

    val actions = remember {
        listOf(
            MenuAction(label = "View", icon = Icons.Default.Visibility, onClick = onClick),
            MenuAction(label = "Edit", icon = Icons.Default.Edit, onClick = onEdit),
            MenuAction(label = "Delete", icon = Icons.Default.Delete, textColor = redText, onClick = onDelete)
        )
    }

    DataCard(
        item = supplier,
        onClick = { onClick() },
        showDivider = true,

        // 1. TOP ROW: Category Pill (Left) + Code & 3-Dots (Right)
        headerContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = background_light_purple
                    ) {
                        Text(
                            text = category,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "#$supplierCode",
                        fontSize = tokens.caption,
                        color = mutedText,
                        fontWeight = FontWeight.Normal
                    )
                }

                ActionDropdownMenu(
                    actions = actions,
                    icon = Icons.Default.MoreHoriz
                )
            }
        },
        title = supplierName,
        subtitle = contactDetails,
        // 2. MIDDLE ROW: Left (Supplier Name + Contact) & Right (City + Location)
        footerAsRows = true,
        footerFields = listOf(
//            DataCardField(
//                text = supplierName,
//                label = contactDetails,
//                asColumn = true,
//                textColor = TextPrimary,
//                labelColor = headerGrey,
//                valueFontWeight = FontWeight.Medium
//            ),
            DataCardField(
                text = city,
                label = "Location",
                textColor = title_color,
                labelColor = mutedText,
                valueFontWeight = FontWeight.Medium
            )
        ),

        // 3. BOTTOM ROW: Status Badges (Left) & "View Details →" (Right)
        footerTags = listOf(
            if (isVerified) "Verified" else "Pending",
            if (isActive) "Active" else "Inactive"
        ),
        trailingText = "View Details →"
    )
}