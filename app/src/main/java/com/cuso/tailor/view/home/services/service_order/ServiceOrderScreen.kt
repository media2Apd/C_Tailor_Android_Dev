@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused"
)
package com.cuso.tailor.view.home.services.service_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ServicesViewModel
import kotlinx.coroutines.delay
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
    val tokens = LocalAppTokens.current

    // Observe API states from ServicesViewModel
    val serviceRequests by viewModel.serviceRequestsList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingServiceRequestsList.collectAsStateWithLifecycle()
    val errorMessage by viewModel.serviceRequestsListError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }

    // Debounced search and filter triggering the API
    LaunchedEffect(searchQuery, statusFilter) {
        delay(400)
        viewModel.fetchServiceRequestsList(
            page = 1,
            limit = 20,
            search = searchQuery.takeIf { it.isNotBlank() },
            status = statusFilter.takeIf { it != "all" }
        )
    }

    FabScaffold(
        fab = null,
        snackbarHostState = snackbarHostState,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Primary_background)) {
            Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

                // Top Title Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(title = "Service Orders", onClose = onBack)
                }

                // Search & Filter Component
                Column(modifier = Modifier.fillMaxWidth()) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Customers, Code...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { /* Handle filter drawer */ }
                    )
                }
                HorizontalDivider(color = title_border)

                // Main Content List Area
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when {
                        isLoading && serviceRequests.isEmpty() -> {
                            ListSkeleton()
                        }

                        errorMessage != null && serviceRequests.isEmpty() -> {
                            AppErrorState(
                                title = "Failed to load service requests",
                                message = errorMessage ?: "Please check your network and try again.",
                                onRetry = { viewModel.fetchServiceRequestsList() }
                            )
                        }

                        serviceRequests.isEmpty() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(tokens.iconSize * 2.2f)
                                    )
                                    Spacer(Modifier.height(tokens.extraPadding / 2))
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
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(serviceRequests, key = { it.id }) { request ->
                                    // Extract summary of garments
                                    val garmentNames = request.items?.joinToString(", ") { item ->
                                        val name = item.garmentName?.takeIf { it.isNotBlank() }
                                            ?: item.garmentDescription?.takeIf { it.isNotBlank() }
                                            ?: item.category
                                            ?: "Garment"
                                        "$name (${item.quantity ?: 1})"
                                    }?.ifBlank { "—" } ?: "—"

                                    // Dynamic status color mapping from theme
                                    val cleanStatus = (request.status ?: "Draft").replace("_", " ")
                                    val (statusBg, statusText) = when (cleanStatus.lowercase()) {
                                        "completed", "ready for delivery" -> Pair(greenBg, greentext)
                                        "received in workshop"             -> Pair(cyanBg, chartBlue)
                                        "pending approval"                 -> Pair(yellowBg, orangeText)
                                        else                               -> Pair(cardBgLight, TextSecondary)
                                    }

                                    // Dynamic priority color mapping from theme
                                    val priority = request.priority ?: "Medium"
                                    val priorityDotColor = when (priority.lowercase()) {
                                        "urgent", "high" -> redText
                                        "low"            -> greentext
                                        else             -> orangeText
                                    }

                                    DataCard(
                                        item = request,
                                        eyebrowText = "Request Code: ${request.serviceRequestCode ?: "—"}",
                                        eyebrowColor = TextSecondary,
                                        topBadgeText = request.primaryCategory?.replace("_", " ") ?: "Service",
                                        topBadgeBgColor = primary_light,
                                        topBadgeTextColor = BluePrimary,
                                        topBadgeShowDot = false,
                                        topBadgeInline = false,
                                        showActionsInHeader = true,
                                        title = request.customerSnapshot?.name ?: request.customerId?.fullName ?: "Unknown Customer",
                                        titleFontWeight = FontWeight.Medium,
                                        titleColor = TextPrimary,
                                        actions = listOf(
                                            MenuAction("View", Icons.Default.Visibility) { onViewOrder(request.id) },
                                            MenuAction("Edit", Icons.Default.Edit) { onEditOrder(request.id) }
                                        ),
                                        onClick = { onViewOrder(request.id) },
                                        content = {
                                            // Garments Summary Row
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
                                                    text = garmentNames,
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
                                                    text = "Delivery: ${formatIsoDate(request.deliveryDate)}",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextSecondary
                                                )
                                            }

                                            Spacer(Modifier.height(tokens.extraPadding / 2))
                                            HorizontalDivider(color = light_grey, thickness = 2.dp)
                                            Spacer(Modifier.height(tokens.extraPadding / 2))

                                            // Bottom Row: Priority & Status (Left) | Total & Paid (Right)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                // Left: Priority + Status Pill
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(priorityDotColor)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = "$priority Priority",
                                                            fontSize = tokens.label,
                                                            fontWeight = FontWeight.Medium,
                                                            color = TextPrimary
                                                        )
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f),
                                                        color = statusBg
                                                    ) {
                                                        Text(
                                                            text = cleanStatus,
                                                            fontSize = tokens.label,
                                                            fontWeight = FontWeight.Medium,
                                                            color = statusText,
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }

                                                // Right: Grand Total & Advance Paid
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Total: ₹${(request.grandTotal ?: 0.0).toInt()}",
                                                        fontSize = tokens.label,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextSecondary
                                                    )
                                                    Row(verticalAlignment = Alignment.Bottom) {
                                                        Text(
                                                            text = "₹${(request.advanceAmountPaid ?: 0.0).toInt()}",
                                                            fontSize = tokens.bodyMedium,
                                                            fontWeight = FontWeight.Medium,
                                                            color = TextPrimary
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            text = "Paid",
                                                            fontSize = tokens.label,
                                                            fontWeight = FontWeight.Medium,
                                                            color = mutedText,
                                                            modifier = Modifier.padding(bottom = 1.dp)
                                                        )
                                                    }
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

// Adaptive ISO date formatter
private fun formatIsoDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoString) ?: return isoString.take(10)
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        isoString.take(10)
    }
}