@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused"
)

package com.cuso.tailor.view.home.services.service_order.production_orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ServiceOrderScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onCreateOrder: () -> Unit = {},
    onViewOrder: (String) -> Unit = {},
    onEditOrder: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {},
    viewModel: ServicesViewModel = hiltViewModel()
) {
    val tokens: AppDesignTokens = LocalAppTokens.current

    // Observe Service Orders state
    val serviceOrders by viewModel.serviceOrdersList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingServiceOrders.collectAsStateWithLifecycle()
    val errorMessage by viewModel.serviceOrdersError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }

    // Fetch orders on search query or filter changes
    LaunchedEffect(searchQuery, statusFilter) {
        viewModel.onServiceOrderSearch(
            query = searchQuery,
            status = statusFilter.takeIf { it != "all" }
        )
    }

    FabScaffold(
        fab = null,
        snackbarHostState = snackbarHostState,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // Top Title Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(
                        title = "Service Orders",
                        onClose = onBack
                    )
                }

                // Search & Filter Component
                Column(modifier = Modifier.fillMaxWidth()) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Customers, Code...",
                        accentColor = Primary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { /* Handle filter sheet */ }
                    )
                }

                HorizontalDivider(color = title_border, thickness = 1.dp)

                // Main Content List Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        isLoading && serviceOrders.isEmpty() -> {
                            ListSkeleton()
                        }

                        errorMessage != null && serviceOrders.isEmpty() -> {
                            AppErrorState(
                                title = "Failed to load service orders",
                                message = errorMessage ?: "Please check your network and try again.",
                                onRetry = { viewModel.fetchServiceOrders() }
                            )
                        }

                        serviceOrders.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(tokens.iconSize * 2.2f)
                                    )
                                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                                    Text(
                                        text = "No service orders found",
                                        color = TextSecondary,
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = tokens.screenPadding)
                            ) {
                                items(serviceOrders, key = { it.id }) { order ->
                                    // 1. Fetch Customer Name & Phone
                                    val customerDisplayName = order.customer?.name?.takeIf { it.isNotBlank() }
                                        ?: order.customerSnapshot?.name?.takeIf { it.isNotBlank() }
                                        ?: order.customerId?.fullName?.takeIf { it.isNotBlank() }
                                        ?: "Unknown Customer"

                                    val customerPhone = order.customer?.phone?.takeIf { it.isNotBlank() }
                                        ?: order.customerSnapshot?.phone?.takeIf { it.isNotBlank() }
                                        ?: order.customerId?.mobileNumber?.takeIf { it.isNotBlank() }
                                        ?: ""

                                    // 2. Fetch Garments / Job Cards summary from production
                                    val totalJobCards = order.production?.totalJobCards ?: 1
                                    val totalPieces = order.production?.totalPieces ?: 1
                                    val completedPieces = order.production?.completedPieces ?: 0

                                    val jobCardTitle = "$totalJobCards Job Cards ($totalPieces pcs)"
                                    val jobCardProgress = "$completedPieces/$totalPieces completed"

                                    // 3. Fetch Delivery / Due Date formatted as '15 Oct 2026'
                                    val deliveryDateText = formatDisplayDate(order.dueDate ?: order.deliveryDate)

                                    // 4. Fetch Status from production.overallStatus
                                    val rawStatus = order.production?.overallStatus
                                        ?: order.status
                                        ?: "Pending_Allocation"
                                    val cleanStatus = rawStatus.replace("_", " ")

                                    val (statusBg, statusText) = when (cleanStatus.lowercase()) {
                                        "completed"                                 -> Pair(greenBg, greentext)
                                        "pending allocation", "pending"             -> Pair(yellowBg, orangeText)
                                        "cutting", "stitching", "in progress"       -> Pair(cyanBg, blueText)
                                        "cancelled", "rejected"                     -> Pair(redBg, redText)
                                        else                                        -> Pair(light_blue, Primary)
                                    }

                                    // 5. Fetch Priority
                                    val priority = order.priority?.takeIf { it.isNotBlank() } ?: "Medium"
                                    val priorityDotColor = when (priority.lowercase()) {
                                        "urgent", "high" -> redText
                                        "low"            -> greentext
                                        else             -> orangeText
                                    }

                                    // 6. Order Code
                                    val orderReferenceCode = order.orderCode?.takeIf { it.isNotBlank() }
                                        ?: order.serviceOrderCode?.takeIf { it.isNotBlank() }
                                        ?: "—"

                                    // 7. Order Type badge (e.g. Sales Order)
                                    val badgeText = order.orderType?.replace("_", " ")
                                        ?: order.primaryCategory?.replace("_", " ")
                                        ?: "Sales Order"

                                    DataCard(
                                        item = order,
                                        eyebrowText = "Order Code: $orderReferenceCode",
                                        eyebrowColor = TextSecondary,
                                        topBadgeText = badgeText,
                                        topBadgeBgColor = primary_light,
                                        topBadgeTextColor = BluePrimary,
                                        topBadgeShowDot = false,
                                        topBadgeInline = false,
                                        showActionsInHeader = true,
                                        title = customerDisplayName,
                                        titleFontWeight = FontWeight.Bold,
                                        titleColor = TextPrimary,
                                        actions = listOf(
                                            MenuAction("View", Icons.Default.Visibility) { onViewOrder(order.id) },
                                            MenuAction("Edit", Icons.Default.Edit) { onEditOrder(order.id) }
                                        ),
                                        onClick = { onViewOrder(order.id) },
                                        content = {
                                            // Customer Phone Subtitle
                                            if (customerPhone.isNotBlank()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Call,
                                                        contentDescription = null,
                                                        tint = mutedText,
                                                        modifier = Modifier.size(tokens.iconSize * 0.75f)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = customerPhone,
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextSecondary
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))
                                            }

                                            // Garments / Items Summary Row
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_tag),
                                                    contentDescription = null,
                                                    tint = mutedText,
                                                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = "$jobCardTitle  •  $jobCardProgress",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Spacer(Modifier.height(6.dp))

                                            // Delivery Date Row
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_calendar),
                                                    contentDescription = null,
                                                    tint = mutedText,
                                                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = "Delivery: $deliveryDateText",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextSecondary
                                                )
                                            }

                                            Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                                            HorizontalDivider(color = light_grey, thickness = 1.dp)
                                            Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                                            // Bottom Row: Priority & Status (Left) | Job Card Code (Right)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Priority Dot & Status Pill
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    // Priority Dot & Label
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(priorityDotColor)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = priority.uppercase(),
                                                            fontSize = tokens.label,
                                                            fontWeight = FontWeight.Medium,
                                                            color = TextPrimary
                                                        )
                                                    }

                                                    // Status Pill
                                                    Surface(
                                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                        color = statusBg
                                                    ) {
                                                        Text(
                                                            text = cleanStatus,
                                                            fontSize = tokens.label,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = statusText,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }

                                                // Right Stage or Primary Job Card Code
                                                val stage = order.production?.currentStages?.firstOrNull()
                                                    ?: order.production?.jobCardCodes?.firstOrNull()
                                                    ?: ""

                                                if (stage.isNotBlank()) {
                                                    Text(
                                                        text = stage,
                                                        fontSize = tokens.label,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                        }
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

/**
 * Formats ISO date string to "15 Oct 2026"
 */
private fun formatDisplayDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "—"

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd"
    )

    for (pattern in patterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.ENGLISH)
            parser.isLenient = true
            val parsed = parser.parse(dateString)
            if (parsed != null) {
                return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(parsed)
            }
        } catch (_: Exception) {}
    }

    return if (dateString.length >= 10) dateString.take(10) else dateString
}