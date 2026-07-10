package com.cuso.mobile.view.home.sales.quotation

// ═════════════════════════════════════════════════════════════════════════
// 📋 Quotation List screen
//    Every row here is rendered with the SAME shared `DataCard` composable
//    from `reusablecomposables/DataAndCardSystem.kt` — nothing custom.
//    We only *describe* the data (title, badge, footer fields, actions);
//    the card layout, divider, and "⋯" menu all come from DataCard itself.
// ═════════════════════════════════════════════════════════════════════════

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.QuotationItemDto
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.viewmodel.CustomerViewModel
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
    onCardClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val customerScreenViewModel: CustomerViewModel = hiltViewModel()
    val salesOrderViewModel: SalesOrderViewModel = hiltViewModel()
    val quotationViewModel: QuotationViewModel = hiltViewModel()   // 👈 add
    val quotationState by quotationViewModel.uiState.collectAsStateWithLifecycle()

    // 👇 replaces samplePricingItems
    val items: List<PricingItem> = when (val state = quotationState) {
        is QuotationUiState.Success -> state.quotations.map { it.toPricingItem() }
        else -> emptyList()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column {
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

                Column(
                    modifier = Modifier
                        .background(Color(0xFFF8F9FF))
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sales", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Pricing & Quotations",
                            fontSize = 13.sp,
                            color = Color(0xFF3B3BF9),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(Color.White, RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Search Customers...",
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White, RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.FilterList,
                                "Filter",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNe,
                containerColor = Color(0xFF3B3BF9),
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add New Pricing", fontWeight = FontWeight.Medium) }
            )
        }
    ) { padding ->
        LaunchedEffect(Unit) {
            customerScreenViewModel.loadCustomers()
            salesOrderViewModel.fetchOrders()
            quotationViewModel.loadQuotations()

        }
        // 👇 debounce search → refetch from API instead of local filter
        LaunchedEffect(searchQuery) {
            kotlinx.coroutines.delay(400.milliseconds) // simple debounce
            quotationViewModel.searchQuotations(searchQuery)
        }
        when {
            quotationState is QuotationUiState.Loading && items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            quotationState is QuotationUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
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
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No quotations found", color = Color(0xFF9CA3AF))
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    items(items, key = { it.id }) { item ->

                        // 🔁 Reusing DataCard EXACTLY as defined in reusablecomposables
                        DataCard(
                            item = item,

                            // top-right status badge — "Active" / "Inactive"
                            topBadgeText = if (item.isActive) "Active" else "Inactive",
                            topBadgeTextColor = if (item.isActive) Color(0xFF0AB83E) else Color(
                                0xFFC10007
                            ),
                            topBadgeBgColor = if (item.isActive) Color(0xFFDBFCE7) else Color(
                                0xFFFEE2E2
                            ),
                            topBadgeCornerRadius = 20.dp,
                            topBadgeInline = false,

                            title = item.title,

                            // price + "Applicable Garment" + value — all via footerFields
                            footerFields = listOf(
                                DataCardField(
                                    text = item.price,
                                    textColor = Color(0xFF111827)
                                ),
                                DataCardField(
                                    text = item.applicableGarmentLabel,
                                    textColor = Color(0xFF9CA3AF)
                                ),
                                DataCardField(
                                    text = item.applicableGarmentValue,
                                    textColor = Color(0xFF111827)
                                )
                            ),

                            // "⋯" action menu
                            actions = listOf(
                                MenuAction(
                                    "View",
                                    Icons.Default.Visibility,
                                    onClick = { onCardClick(item.id) }),
                                MenuAction(
                                    "Edit",
                                    Icons.Default.Edit,
                                    onClick = { onCardClick(item.id) }),
                                MenuAction(
                                    "Delete",
                                    Icons.Default.Delete,
                                    tint = Color(0xFFDC2626),
                                    textColor = Color(0xFFDC2626),
                                    onClick = {  }
                                )
                            ),
                            onClick = { onCardClick(item.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(90.dp)) }
                }
            }
        }

    }
}