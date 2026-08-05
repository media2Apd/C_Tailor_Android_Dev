@file:Suppress("UNUSED_PARAMETER",
    "UNUSED",
    "RedundantSuppression",
    "unused_variable",
    "AssignedValueIsNeverRead", "VariableNeverRead"
)
package com.cuso.mobile.view.home.sales.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.sales.GarmentPricingListItemDto
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.title_font
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.viewmodel.GarmentPricingListUiState
import com.cuso.mobile.viewmodel.PricingQuotationViewModel

private val Primary = Color(0xFF3B3BF9)
private val PrimaryLight = Color(0xFFEEF0FF)
  private val TextDark = Color(0xFF111827)
private val CardBorder = Color(0xFFF0F0F0)
private val StripBg = Color(0xFFF9FAFB)

@Composable
fun PricingScreen(
    onClose: () -> Unit,
    onAddNewPricing: () -> Unit = {},
    onCardClick: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit ={}

) {
    val viewModel: PricingQuotationViewModel = hiltViewModel()
    val listState by viewModel.garmentPricingListState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchGarmentPricingList()
    }

    FabScaffold(
        fab = FabConfig(
            label = "Add New Pricing",
            icon = Icons.Default.Add,
            onClick = onAddNewPricing,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TitleBar("Pricing", onClose = onClose)

            }
            HorizontalDivider(color = CardBorder)
            Column(
                Modifier.fillMaxWidth()
            ) {

                // Breadcrumb
                ScreenBreadcrumb(
                    segments = listOf("Sales", "Pricing "),
                    onClick = {onBreadCrumbClick()}
                )

            }
            HorizontalDivider(color = CardBorder)

            when (val state = listState) {
                is GarmentPricingListUiState.Loading -> {
                    ListSkeleton()
                }
                is GarmentPricingListUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, color = Color.Red, fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.fetchGarmentPricingList() }) { Text("Retry") }
                        }
                    }
                }
                is GarmentPricingListUiState.Success -> {
                    val uniqueItems = state.items.distinctBy { it.id }   // ✅ NEW — dedupe by id

                    if (uniqueItems.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pricing records yet", color = mutedText, fontSize = 14.sp)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            PricingDashboard(items = uniqueItems)   // ✅ CHANGED — dashboard stats layum unique items base ah

                            Spacer(Modifier.height(20.dp))

                            uniqueItems.forEach { item ->   // ✅ CHANGED
                                GarmentPricingCard(
                                    item = item,
                                    onClick = { onCardClick(item.id) }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            Spacer(Modifier.height(90.dp)) // space for FAB
                        }
                    }
                }
            }
        }
    }
}
// ── Dashboard — quick stats derived from the loaded list ──
@Composable
private fun PricingDashboard(items: List<GarmentPricingListItemDto>) {
    val totalItems = items.size
    val avgTotalPrice = if (items.isNotEmpty()) items.sumOf { it.totalPrice } / items.size else 0.0
    val avgBasePrice = if (items.isNotEmpty()) items.sumOf { it.basePrice } / items.size else 0.0
    val highestPriced = items.maxOfOrNull { it.totalPrice } ?: 0.0

    Column(Modifier.fillMaxSize()
        .background(whiteBg)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardStatCard(
                label = "Active Quotations",
                value = totalItems.toString(),
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                label = "Avg. Quote Value",
                value = "₹${avgBasePrice.toInt()}",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardStatCard(
                label = "Approval Rate",
                value = "₹${avgTotalPrice.toInt()}",
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                label = "This Month",
                value = "₹${highestPriced.toInt()}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DashboardStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Box(
//                modifier = Modifier
//                    .size(34.dp)
//                    .background(PrimaryLight, CircleShape),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    Icons.Default.BarChart,
//                    contentDescription = null,
//                    tint = Primary,
//                    modifier = Modifier.size(16.dp)
//                )
//            }
//            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, fontSize = 11.sp, color = mutedText)
                Text(value, fontSize = 14.sp, color = TextDark)
                Text(value, fontSize = 10.sp, color = TextDark)
            }
        }
    }
}

// ── Garment Pricing Card — shows Base Price + Total Price ──
@Composable
private fun GarmentPricingCard(item: GarmentPricingListItemDto, onClick: () -> Unit) {
    var clicked by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !clicked) {
                clicked = true
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.itemName.ifBlank { "-" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Garment Pricing",
                            fontSize = 12.sp,
                            color = blackTitle
                        )

                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Total price strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StripBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Base Range ₹",
                        fontSize = 12.sp,
                        color = blackTitle
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${item.basePrice.toInt()}-${item.totalPrice.toInt()}",
                        fontSize = 12.sp,
                        color = blackTitle
                    )
                }
            }
        }
    }
}