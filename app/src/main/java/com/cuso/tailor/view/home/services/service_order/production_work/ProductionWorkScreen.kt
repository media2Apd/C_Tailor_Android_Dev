@file:Suppress("unused", "Unused_Variable")

package com.cuso.tailor.view.home.services.service_order.production_work

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.ServiceOrderItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.blurScrim
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun ProductionWorksScreen(
    onClose: () -> Unit = {},
    onOrderClick: (orderId: String) -> Unit = {},
    viewModel: ServicesViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val orders by viewModel.serviceOrdersList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingServiceOrders.collectAsStateWithLifecycle()
    val errorMessage by viewModel.serviceOrdersError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var blurRadius by remember { mutableStateOf(0.dp) }

    val filterState = rememberFilterDrawerState()

    // Filter sections must stay stable so the drawer keeps its selection state
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Production Status",
                options = listOf(
                    FilterOption("Pending_Allocation", "Pending Allocation"),
                    FilterOption("In_Progress", "In Progress"),
                    FilterOption("Completed", "Completed")
                ),
                isMultiSelect = false
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchServiceOrders()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .blurScrim(radius = blurRadius)
        ) {
            TitleBar(title = "Production Works", onClose = onClose)

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.onServiceOrderSearch(it, selectedStatus)
                },
                placeholder = "Search SO number, customer...",
                filterCount = if (selectedStatus != null) 1 else 0,
                onFilterClick = { filterState.open() },
                height = tokens.buttonHeight
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading && orders.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && orders.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = errorMessage ?: "Failed to load production orders",
                                    fontSize = tokens.bodySmall,
                                    color = redText
                                )
                                Spacer(Modifier.height(tokens.extraPadding))
                                Button(
                                    onClick = {
                                        viewModel.fetchServiceOrders(
                                            search = searchQuery.takeIf { it.isNotBlank() },
                                            status = selectedStatus
                                        )
                                    },
                                    modifier = Modifier.height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Text(
                                        text = "Retry",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = whiteBg
                                    )
                                }
                            }
                        }
                    }

                    orders.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No production orders found",
                                fontSize = tokens.bodySmall,
                                color = mutedText
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(orders, key = { it.id }) { order ->
                                ProductionOrderCard(order = order, onClick = onOrderClick)
                            }
                        }
                    }
                }
            }
        }

        // Status filter drawer
        FilterDrawer(
            state = filterState,
            title = "Filters",
            sections = filterSections,
            onApply = { applied ->
                val status = applied
                    .firstOrNull()
                    ?.options
                    ?.firstOrNull { it.isSelected }
                    ?.id
                selectedStatus = status
                viewModel.fetchServiceOrders(
                    search = searchQuery.takeIf { it.isNotBlank() },
                    status = status
                )
            },
            onClearAll = {},
            onBackgroundBlurChange = { blurRadius = it }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Order card built on the reusable DataCard
// ─────────────────────────────────────────────────────────────

@Composable
private fun ProductionOrderCard(
    order: ServiceOrderItemDto,
    onClick: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(tokens.cardCornerRadius)
    val status = resolveStatusStyle(order.production?.overallStatus)

    DataCard(
        item = order,
        title = order.customer?.name ?: order.customerSnapshot?.name ?: "-",
        titleFontWeight = FontWeight.Medium,
        titleColor = TextPrimary,
        subtitle = order.orderCode ?: order.serviceOrderCode ?: "-",
        topBadgeText = status.label,
        topBadgeTextColor = status.textColor,
        topBadgeBgColor = status.bgColor,
        topBadgeShowDot = true,
        topBadgeInline = true,
        actions = listOf(
            MenuAction(
                label = "View Details",
                icon = Icons.Default.Visibility,
                tint = mutedText,
                textColor = TextPrimary,
                onClick = {
                    // Safe click check for 3-dot menu action
                    val validId = order.id.takeIf { it.isNotBlank() }
                    if (validId != null) {
                        onClick(validId)
                    }
                }
            )
        ),
        onClick = {
            // Safe click check for card body click
            val validId = it.id.takeIf { id -> id.isNotBlank() }
            if (validId != null) {
                onClick(validId)
            }
        },
        content = { OrderCardContent(order) }
    )
}

@Composable
private fun OrderCardContent(order: ServiceOrderItemDto) {
    val tokens = LocalAppTokens.current
    val boxShape = RoundedCornerShape(tokens.cardCornerRadius)
    val production = order.production

    val phone = order.customer?.phone ?: order.customerSnapshot?.phone ?: "-"
    val totalCards = production?.totalJobCards ?: 0
    val totalPieces = production?.totalPieces ?: 0
    val completedPieces = production?.completedPieces ?: 0

    val garmentLine = "$totalCards Garment${if (totalCards == 1) "" else "s"}($totalPieces pcs)"
    val progressLine = if (totalPieces > 0 && completedPieces >= totalPieces) {
        "Complete"
    } else {
        "$completedPieces/$totalPieces pcs done"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Contact number row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Contact number", fontSize = tokens.bodySmall, color = mutedText)
            Text(text = phone, fontSize = tokens.bodySmall, color = TextPrimary)
        }

        Spacer(Modifier.height(tokens.extraPadding))

        // Progress, target and priority summary box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(boxShape)
                .background(Primary_background, boxShape)
                .border(1.dp, title_border, boxShape)
                .padding(horizontal = 8.dp, vertical = tokens.extraPadding),
            verticalAlignment = Alignment.Top
        ) {
            SummaryColumn(
                label = "Garment Progress",
                lines = listOf(garmentLine, progressLine),
                modifier = Modifier.weight(1.3f)
            )
            SummaryColumn(
                label = "Target",
                lines = listOf(formatDate(order.dueDate ?: order.deliveryDate)),
                modifier = Modifier.weight(1f)
            )
            SummaryColumn(
                label = "Priority",
                lines = listOf(order.priority ?: "-"),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    lines: List<String>,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = mutedText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        lines.forEach {
            Text(
                text = it,
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────

private data class OrderStatusStyle(
    val label: String,
    val textColor: Color,
    val bgColor: Color
)

private fun resolveStatusStyle(raw: String?): OrderStatusStyle = when (raw) {
    "Completed" -> OrderStatusStyle("Order Closed", greentext, greenBg)
    "In_Progress" -> OrderStatusStyle("In Progress", yellowText, yellowBg)
    "Pending_Allocation" -> OrderStatusStyle("Pending Allocation", orangeText, orangeBg)
    null -> OrderStatusStyle("-", TextSecondary, badgeGrey)
    else -> OrderStatusStyle(raw.replace("_", " "), TextSecondary, badgeGrey)
}

/** Formats an ISO date such as 2026-10-15T00:00:00.000Z as 15 Oct 2026. */
private fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(raw) ?: return raw.take(10)
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}