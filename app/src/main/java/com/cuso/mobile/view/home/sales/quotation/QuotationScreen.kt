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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.sales.QuotationItemDto
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.redBg
import com.cuso.mobile.ui.theme.redtext
import com.cuso.mobile.view.composable.ActionDropdownMenu
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DeleteModel
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.viewmodel.CustomerViewModel
import com.cuso.mobile.viewmodel.QuotationDeleteUiState
import com.cuso.mobile.viewmodel.QuotationUiState
import com.cuso.mobile.viewmodel.QuotationViewModel
import com.cuso.mobile.viewmodel.SalesOrderViewModel
import kotlin.time.Duration.Companion.milliseconds
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.TitleBar

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
    onEdit: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val customerScreenViewModel: CustomerViewModel = hiltViewModel()
    val salesOrderViewModel: SalesOrderViewModel = hiltViewModel()
    val quotationViewModel: QuotationViewModel = hiltViewModel()
    val quotationState by quotationViewModel.uiState.collectAsStateWithLifecycle()
    val deleteState by quotationViewModel.deleteState.collectAsStateWithLifecycle()

    var quotationToDelete by remember { mutableStateOf<PricingItem?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
            is QuotationDeleteUiState.Success -> {
                successMsg = "Quotation deleted successfully"
                quotationViewModel.resetDeleteState()
            }
            is QuotationDeleteUiState.Error -> {
                errorMsg = state.message
                quotationViewModel.resetDeleteState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(400.milliseconds)
        quotationViewModel.searchQuotations(searchQuery)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    .background(Color.Transparent)
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    TitleBar("Quotation List", onClose = onClose)

                    Spacer(Modifier.height(8.dp))
                }

                // Breadcrumb + Search
                Column(modifier = Modifier.fillMaxWidth()) {
                    ScreenBreadcrumb(segments = listOf("Sales", "Pricing & Quotations"), onClick = { onBreadCrumbClick() })
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Customers...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { /* Filter drawer */ }
                    )
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))


                // Content
                when {
                    quotationState is QuotationUiState.Loading -> ListSkeleton()
                    quotationState is QuotationUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text((quotationState as QuotationUiState.Error).message, color = Color(0xFFDC2626))
                        }
                    }
                    items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No quotations found", color = Color(0xFF9CA3AF))
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items, key = { it.id }) { item ->
                                val menuActions = listOf(
                                    MenuAction("View", Icons.Default.Visibility, onClick = { onView(item.id) }),
                                    MenuAction("Edit", Icons.Default.Edit, onClick = { onEdit(item.id) }),
                                    MenuAction(
                                        "Delete",
                                        Icons.Default.Delete,
                                        tint = Color(0xFFDC2626),
                                        textColor = Color(0xFFDC2626),
                                        onClick = { quotationToDelete = item }
                                    )
                                )

                                DataCard(
                                    item = item,
                                    topBadgeText = item.status,
                                    topBadgeTextColor = if (item.isActive) greentext else redtext,
                                    topBadgeBgColor = if (item.isActive) greenBg else redBg,
                                    topBadgeCornerRadius = 20.dp,
                                    topBadgeInline = true,
                                    topBadgeShowDot = false,
                                    title = item.title,
                                    content = {
                                        val tokens = LocalAppTokens.current
                                        Column {
                                            // Row 1: price + 3-dot menu together
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.price,
                                                    fontSize = tokens.bodySmall,
                                                    color = Color(0xFF111827)
                                                )
                                                ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = menuActions)
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            // Row 2: garment label
                                            Text(
                                                text = item.applicableGarmentLabel,
                                                fontSize = tokens.bodySmall,
                                                color = Color(0xFF9CA3AF)
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            // Row 3: garment value
                                            Text(
                                                text = item.applicableGarmentValue,
                                                fontSize = tokens.bodySmall,
                                                color = Color(0xFF111827)
                                            )
                                        }
                                    },
                                    onClick = { onView(item.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(90.dp)) }
                        }
                    }
                }
            }
        }

        // Delete Alert Dialog
        if (quotationToDelete != null) {
            DeleteModel(
                title = "Delete Quotation",
                message = "Are you sure you want to delete this quotation? This action cannot be undone.",
                onDismiss = {
                    quotationToDelete = null
                },
                onDelete = {
                    quotationViewModel.deleteQuotation(quotationToDelete!!.id)
                    quotationToDelete = null
                }
            )
        }

        // Dynamic Island Overlay
        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMsg,
            onDismiss = { successMsg = null }
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = errorMsg,
            onDismiss = { errorMsg = null }
        )
    }
}