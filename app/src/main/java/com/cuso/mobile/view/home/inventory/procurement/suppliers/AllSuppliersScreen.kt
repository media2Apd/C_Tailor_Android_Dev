@file:Suppress("unused")

package com.cuso.mobile.view.home.inventory.procurement.suppliers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.SupplierDto
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.InventoryViewModel

@Composable
fun AllSuppliersScreen(
    onClose: () -> Unit,
    onSupplierClick: (SupplierDto) -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val suppliers by viewModel.suppliers.collectAsState()
    val isLoading by viewModel.isLoadingSuppliers.collectAsState()
    val errorMsg by viewModel.suppliersError.collectAsState()
    val successMsg by viewModel.successMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSupplierToDelete by remember { mutableStateOf<SupplierDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchSuppliers()
    }

    val filteredList = remember(suppliers, searchQuery) {
        if (searchQuery.isBlank()) suppliers
        else {
            suppliers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.supplierCode.contains(searchQuery, ignoreCase = true) ||
                        (it.contact?.contactName?.contains(searchQuery, ignoreCase = true) == true) ||
                        (it.contact?.phone?.contains(searchQuery, ignoreCase = true) == true) ||
                        (it.address?.billing?.city?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Primary_background)) {
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
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Suppliers...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border)

                if (isLoading && suppliers.isEmpty()) {
                    ListSkeleton()
                } else if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No suppliers found",
                            fontSize = tokens.bodyMedium,
                            color = mutedText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(vertical = tokens.extraPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredList, key = { it.id }) { supplier ->
                            SupplierCardItem(
                                supplier = supplier,
                                tokens = tokens,
                                onClick = { onSupplierClick(supplier) },
                                onDeleteClick = { selectedSupplierToDelete = supplier }
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

        DynamicIslandSuccess(message = successMsg, onDismiss = { viewModel.clearAlerts() })
        DynamicIslandError(message = errorMsg?.let { ErrorMapper.map(it) }, onDismiss = { viewModel.clearAlerts() })
    }
}

// ─────────────────────────────────────────────────────────────
// SUPPLIER CARD ITEM
// ─────────────────────────────────────────────────────────────

@Composable
private fun SupplierCardItem(
    supplier: SupplierDto,
    tokens: AppDesignTokens,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isChecked by remember { mutableStateOf(false) }
    val categoryTag = supplier.category.ifBlank { "FABRIC" }.uppercase()
    val code = supplier.supplierCode.ifBlank { "SUP-001" }
    val contactName = supplier.contact?.contactName?.ifBlank { "Contact Person" } ?: "Contact Person"
    val phone = supplier.contact?.phone?.ifBlank { "—" } ?: "—"
    val city = supplier.address?.billing?.city?.ifBlank { "Chennai" } ?: "Chennai"
    val isVerified = supplier.complianceStatus.equals("Verified", true) || supplier.complianceStatus.equals("Completed", true)
    val isActive = supplier.status.equals("active", true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Checkbox, Tags, Code, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )

                Spacer(Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(primary_light)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = categoryTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = code,
                    fontSize = tokens.caption,
                    color = mutedText,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize * 0.9f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Name & Location Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = supplier.name.ifBlank { "Unnamed Supplier" },
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$contactName • $phone",
                        fontSize = tokens.bodySmall,
                        color = textSubdued
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = city,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "Location",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = grey_border.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            // Bottom Badges & View Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniDotBadge(
                        text = if (isVerified) "Verified" else "Pending",
                        bgColor = if (isVerified) greenBg else light_blue,
                        textColor = if (isVerified) greentext else Primary,
                        dotColor = if (isVerified) greentext else Primary
                    )

                    MiniDotBadge(
                        text = if (isActive) "Active" else "Inactive",
                        bgColor = if (isActive) greenBg else redBg,
                        textColor = if (isActive) greentext else redText,
                        dotColor = if (isActive) greentext else redText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Text(
                        text = "View Details",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniDotBadge(
    text: String,
    bgColor: Color,
    textColor: Color,
    dotColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dotColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}