@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead"
)
package com.cuso.mobile.view.home.sales.quotation

// ═════════════════════════════════════════════════════════════════════════
// 📋 Quotation List screen
//    Every row here is rendered with the SAME shared `DataCard` composable
//    from `reusablecomposables/DataAndCardSystem.kt` — nothing custom.
//    We only *describe* the data (title, badge, footer fields, actions);
//    the card layout, divider, and "⋯" menu all come from DataCard itself.
// ═════════════════════════════════════════════════════════════════════════

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.sales.QuotationItemDto
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.viewmodel.CustomerViewModel
import com.cuso.mobile.viewmodel.QuotationDeleteUiState
import com.cuso.mobile.viewmodel.QuotationUiState
import com.cuso.mobile.viewmodel.QuotationViewModel
import com.cuso.mobile.viewmodel.SalesOrderViewModel
import kotlin.time.Duration.Companion.milliseconds

// ── Model for one row in the list ──
data class PricingItem(
    val id: String,
    val title: String,
    val price: String,
    val isActive: Boolean,
    val status: String = "",
    val applicableGarmentLabel: String = "Applicable Garment",
    val applicableGarmentValue: String,
    val fabricLabel: String = "N/A",  // Optional: if you want to show fabric info
    val fabricPrice: Double = 0.0
)
// ── Map API DTO → existing PricingItem UI model ──
// Update the toPricingItem function
private fun QuotationItemDto.toPricingItem(): PricingItem {
    val firstItem = items.firstOrNull()
    return PricingItem(
        id = id,
        title = customerSnapshot?.name?.let { "$it - ${firstItem?.garmentName ?: "Quotation"}" }
            ?: quotationNumber,
        price = "₹${String.format(java.util.Locale.US, "%.2f", grandTotal)}",
        isActive = status.equals("draft", ignoreCase = true).not(),
        status = status.replaceFirstChar { it.uppercase() },
        applicableGarmentValue = firstItem?.garmentName ?: "-",
        // If you want to display fabric info:
        fabricLabel = firstItem?.fabric?.label ?: "N/A",
        fabricPrice = firstItem?.fabric?.price ?: 0.0
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationScreen(
    onClose: () -> Unit = {},
    onAddNe: () -> Unit = {},
    onView: (String) -> Unit = {},
    onEdit: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val customerScreenViewModel: CustomerViewModel = hiltViewModel()
    val salesOrderViewModel: SalesOrderViewModel = hiltViewModel()
    val quotationViewModel: QuotationViewModel = hiltViewModel()   // 👈 add
    val quotationState by quotationViewModel.uiState.collectAsStateWithLifecycle()

    val deleteState by quotationViewModel.deleteState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var quotationToDelete by remember { mutableStateOf<PricingItem?>(null) }

    var isDeleting by remember { mutableStateOf(false) }

    // 👇 replaces samplePricingItems
    val items: List<PricingItem> = when (val state = quotationState) {
        is QuotationUiState.Success -> state.quotations.map { it.toPricingItem() }
        else -> emptyList()
    }

    LaunchedEffect(Unit) {
        customerScreenViewModel.loadCustomers()
        salesOrderViewModel.fetchOrders()
        quotationViewModel.loadQuotations()
    }
    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is QuotationDeleteUiState.Loading -> isDeleting = true
            is QuotationDeleteUiState.Success -> {
                isDeleting = false
                Toast.makeText(context, "Quotation deleted successfully", Toast.LENGTH_SHORT).show()
                quotationViewModel.resetDeleteState()
            }
            is QuotationDeleteUiState.Error -> {
                isDeleting = false
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                quotationViewModel.resetDeleteState()
            }
            else -> Unit
        }
    }
    // 👇 debounce search → refetch from API instead of local filter
    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(400.milliseconds) // simple debounce
        quotationViewModel.searchQuotations(searchQuery)
    }

    FabScaffold(
        fab = FabConfig(
            label = "Add New Quotation",
            icon = Icons.Default.Add,
            onClick = onAddNe,
            bottomPadding = 24.dp
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Quotation List",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onClose() }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // Breadcrumb + Search + Filter
            Column(modifier = Modifier.fillMaxWidth()
                .background(Color(0xFFF8F9FF))
                ) {
                ScreenBreadcrumb(segments = listOf("Sales", "Pricing & Quotations"), onClick = {})
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

            // Content
            when {
                quotationState is QuotationUiState.Loading -> {
                    ListSkeleton()

                }

                quotationState is QuotationUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (quotationState as QuotationUiState.Error).message,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No quotations found", color = Color(0xFF9CA3AF))
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items, key = { it.id }) { item ->
                            // 🔁 Reusing DataCard EXACTLY as defined in reusablecomposables
                            DataCard(
                                item = item,
                                topBadgeText = item.status,
                                topBadgeTextColor = if (item.isActive) Color(0xFF0AB83E) else Color(0xFFF44336),
                                topBadgeBgColor = if (item.isActive) Color(0xFFDBFCE7) else Color(0xFFFEE2E2),
                                topBadgeCornerRadius = 20.dp,
                                topBadgeInline = false,
                                title = item.title,
                                footerFields = listOf(
                                    DataCardField(text = item.price, textColor = Color(0xFF111827)),
                                    DataCardField(text = item.applicableGarmentLabel, textColor = Color(0xFF9CA3AF)),
                                    DataCardField(text = item.applicableGarmentValue, textColor = Color(0xFF111827))
                                ),
                                actions = listOf(
                                    MenuAction("View", Icons.Default.Visibility, onClick = { onView(item.id) }),
                                    MenuAction("Edit", Icons.Default.Edit, onClick = { onEdit(item.id) }),
                                    MenuAction(
                                        "Delete",
                                        Icons.Default.Delete,
                                        tint = Color(0xFFDC2626),
                                        textColor = Color(0xFFDC2626),
                                        onClick = { quotationToDelete = item }
                                    )
                                ),
                                onClick = { onView(item.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(90.dp)) }
                    }
                }
            }
        }
    }

    if (quotationToDelete != null) {
        AlertDialog(
            onDismissRequest = { quotationToDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            title = { Text("Delete Quotation", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
            text = { Text("Are you sure you want to delete this quotation? This action cannot be undone.", color = Color(0xFF6B7280)) },
            confirmButton = {
                Button(
                    onClick = {
                        quotationViewModel.deleteQuotation(quotationToDelete!!.id)
                        quotationToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { quotationToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Cancel", color = Color(0xFF374151)) }
            }
        )
    }
    if (quotationToDelete != null) {
        AlertDialog(
            onDismissRequest = { quotationToDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            title = { Text("Delete Quotation", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
            text = { Text("Are you sure you want to delete this quotation? This action cannot be undone.", color = Color(0xFF6B7280)) },
            confirmButton = {
                Button(
                    onClick = {
                        quotationViewModel.deleteQuotation(quotationToDelete!!.id)
                        quotationToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { quotationToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Cancel", color = Color(0xFF374151)) }
            }
        )
    }
}