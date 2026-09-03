@file:Suppress("UNUSED_PARAMETER",
    "UNUSED",
    "RedundantSuppression",
    "unused_variable",
    "AssignedValueIsNeverRead", "VariableNeverRead"
)
package com.cuso.mobile.view.home.sales.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.sales.GarmentPricingListItemDto
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AppErrorState
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.viewmodel.GarmentPricingListUiState
import com.cuso.mobile.viewmodel.PricingQuotationViewModel

private val Primary = Color(0xFF3B3BF9)
private val PrimaryLight = Color(0xFFEEF0FF)
private val TextDark = Color(0xFF111827)
private val CardBorder = title_border
private val StripBg = Color(0xFFF9FAFB)
private val StatCardBorder = Color(0xFFEDEDF2)
private val StatLabelColor = Color(0xFF9CA3AF)
private val StatValueColor = Color(0xFF1C1C1E)
private val StatTrendGreen = Color(0xFF16A34A)

@Composable
fun PricingScreen(
    onClose: () -> Unit,
    onAddNewPricing: () -> Unit = {},
    onCardClick: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
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
            bottomPadding = tokens.screenPadding * 1.5f
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TitleBar("Pricing", onClose = onClose)
            }

            HorizontalDivider(color = CardBorder)

            when (val state = listState) {
                is GarmentPricingListUiState.Loading -> {
                    ListSkeleton()
                }
                is GarmentPricingListUiState.Error -> {
                    AppErrorState(
                        title = "Failed to load garment pricing",
                        message = "Something went wrong. Please check your connection and try again.",
                        onRetry = { viewModel.fetchGarmentPricingList() }
                    )
                }
                is GarmentPricingListUiState.Success -> {
                    val uniqueItems = state.items.distinctBy { it.id }

                    if (uniqueItems.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pricing records yet", color = mutedText, fontSize = tokens.bodyMedium)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            PricingDashboard(items = uniqueItems)
                            Column(
                                Modifier.fillMaxWidth()
                                    .padding(tokens.screenPadding)

                            ) {

                                Spacer(Modifier.height(tokens.screenPadding * 1.25f))

                                uniqueItems.forEach { item ->
                                    GarmentPricingCard(
                                        item = item,
                                        onClick = { onCardClick(item.id) }
                                    )
                                    Spacer(Modifier.height(tokens.extraPadding * 0.75f))
                                }
                                Spacer(Modifier.height(tokens.screenPadding * 5.6f)) // space for FAB
                            }
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
    val tokens = LocalAppTokens.current
    val totalItems = items.size
    val avgTotalPrice = if (items.isNotEmpty()) items.sumOf { it.totalPrice } / items.size else 0.0
    val avgBasePrice = if (items.isNotEmpty()) items.sumOf { it.basePrice } / items.size else 0.0
    val highestPriced = items.maxOfOrNull { it.totalPrice } ?: 0.0

    Column(
        Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(tokens.screenPadding)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.65f)
        ) {
            DashboardStatCard(
                label = "Active Quotations",
                value = totalItems.toString(),
                trend = "+12% from last month",
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                label = "Avg. Quote Value",
                value = "₹${avgBasePrice.toInt()}",
                trend = "+8% from last month",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(tokens.extraPadding * 0.65f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.65f)
        ) {
            DashboardStatCard(
                label = "Approval Rate",
                value = "78%",
                trend = "+5% from last month",
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                label = "This Month",
                value = "₹${highestPriced.toInt()}",
                trend = "+15% from last month",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DashboardStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    trend: String? = null
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, StatCardBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(Color.White, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.cardPadding * 0.7f)
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = StatLabelColor
        )
        Spacer(Modifier.height(tokens.extraPadding * 0.4f))
        Text(
            text = value,
            fontSize = tokens.bodyMedium,
            color = StatValueColor
        )
        if (trend != null) {
            Spacer(Modifier.height(tokens.extraPadding * 0.4f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = StatTrendGreen,
                    modifier = Modifier.size(tokens.iconSize * 0.6f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = trend,
                    fontSize = tokens.label,
                    color = StatTrendGreen
                )
            }
        }
    }
}

// ── Garment Pricing Card — shows Base Price + Total Price ──
@Composable
private fun GarmentPricingCard(item: GarmentPricingListItemDto, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    var clicked by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !clicked) {
                clicked = true
                onClick()
            },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding * 0.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.itemName.ifBlank { "-" },
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Garment Pricing",
                            fontSize = tokens.bodySmall,
                            color = blackTitle
                        )
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            // Total price strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StripBg)
                    .padding(horizontal = tokens.cardPadding * 0.5f, vertical = tokens.extraPadding * 0.5f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Base Range ₹",
                        fontSize = tokens.bodySmall,
                        color = blackTitle
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${item.basePrice.toInt()}-${item.totalPrice.toInt()}",
                        fontSize = tokens.bodySmall,
                        color = blackTitle
                    )
                }
            }
        }
    }
}