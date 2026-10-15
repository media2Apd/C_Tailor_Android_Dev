@file:Suppress("unused","unusedVariable","AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.inventory.inventory.packages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.PackageStatusHistoryDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun PackageDetailScreen(
    packageId: String,
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEditClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Observe View-One package details from ViewModel
    val packageDetail by viewModel.packageDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingPackageDetail.collectAsStateWithLifecycle()
    val errorMessage by viewModel.packageDetailError.collectAsStateWithLifecycle()
    val isDeleting by viewModel.isDeletingPackage.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Fetch View-One API on screen launch
    LaunchedEffect(packageId) {
        if (packageId.isNotBlank()) {
            viewModel.fetchPackageDetail(packageId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearPackageDetail()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                TitleBar(
                    title = "Package Details",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading && packageDetail == null -> {
                    ListSkeleton()
                }

                errorMessage != null && packageDetail == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Failed to load package details",
                            color = redText,
                            fontSize = tokens.bodyMedium
                        )
                    }
                }

                packageDetail != null -> {
                    val pkg = packageDetail!!
                    val isDelivered = pkg.status.equals("Delivered", ignoreCase = true)
                    val isShipped = pkg.status.equals("Shipped", ignoreCase = true)

                    val (statusBg, statusTextColor) = when {
                        isDelivered -> Pair(greenBg, greentext)
                        isShipped -> Pair(light_blue, Primary)
                        else -> Pair(orangeBg, orangeText)
                    }

                    val createdDateFormatted = remember(pkg.createdAt) {
                        formatDetailDate(pkg.createdAt)
                    }

                    val picklistCode = pkg.picklistIds.firstOrNull()?.picklistNumber ?: "-"

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = tokens.screenPadding,
                            vertical = tokens.extraPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // ── 1. Top Card: Package Code, Status Badge & Actions ──


                        // ── 2. Customer Header Card ──
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                color = whiteBg,
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Customer",
                                        fontSize = tokens.bodySmall,
                                        color = close_color
                                    )
                                    Text(
                                        text = pkg.customerDisplay,
                                        fontSize = tokens.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = blackTitle
                                    )
                                    pkg.customerId?.mobileNumber?.let { phone ->
                                        if (phone.isNotBlank()) {
                                            Text(
                                                text = phone,
                                                fontSize = tokens.bodySmall,
                                                color = close_color
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── 3. 2x2 Metric Cards Grid (2 Per Row) ──
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Row 1: Created On & Sales Order
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    DetailMetricCard(
                                        title = "Created On",
                                        value = createdDateFormatted,
                                        modifier = Modifier.weight(1f)
                                    )
                                    DetailMetricCard(
                                        title = "Sales Order",
                                        value = pkg.salesOrderCode,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Row 2: Picklist & Location
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    DetailMetricCard(
                                        title = "Picklist",
                                        value = picklistCode,
                                        modifier = Modifier.weight(1f)
                                    )
                                    DetailMetricCard(
                                        title = "Location",
                                        value = pkg.warehouseName,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // ── 4. Items Card (Mobile-friendly Cards) ──
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                color = whiteBg,
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ITEM",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.SemiBold,
                                            color = close_color
                                        )
                                        Text(
                                            text = "QTY",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.SemiBold,
                                            color = close_color
                                        )
                                    }

                                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                    val itemsList = pkg.items
                                    itemsList.forEachIndexed { index, innerItem ->
                                        // Mobile Line Item Card
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Primary_background)
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = innerItem.itemName ?: "Custom Garment",
                                                    fontSize = tokens.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = if (innerItem.requiresStock) "Stock required" else "No stock required",
                                                    fontSize = tokens.caption,
                                                    color = close_color
                                                )
                                            }

                                            // Quantity Capsule Pill
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(whiteBg)
                                                    .border(1.dp, light_blue_border, RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${innerItem.qty}",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Primary
                                                )
                                            }
                                        }

                                        if (index < itemsList.lastIndex) {
                                            Spacer(Modifier.height(8.dp))
                                        }
                                    }

                                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                    Text(
                                        text = "Total Qty: ${pkg.totalQty.toInt()}",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = close_color,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        // ── 5. Status History Card ──
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                color = whiteBg,
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Status History",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = blackTitle
                                    )

                                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                    val historyList = pkg.statusHistory.ifEmpty {
                                        listOf(
                                            PackageStatusHistoryDto(
                                                toStatus = pkg.status,
                                                message = "Package created",
                                                changedAt = pkg.createdAt
                                            )
                                        )
                                    }

                                    historyList.forEach { history ->
                                        val formattedTime = formatHistoryDate(history.changedAt)

                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 6.dp)
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(chartAmber)
                                            )

                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = history.toStatus ?: "Ready for Shipment",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TextPrimary
                                                    )
                                                    history.message?.let { msg ->
                                                        Text(
                                                            text = "— $msg",
                                                            fontSize = tokens.bodySmall,
                                                            color = close_color
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = formattedTime,
                                                    fontSize = tokens.caption,
                                                    color = close_color
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
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Package", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this package? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deletePackage(packageId) {
                            Toast.makeText(context, "Package deleted successfully", Toast.LENGTH_SHORT).show()
                            onClose()
                        }
                    }
                ) {
                    Text("Delete", color = redText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            }
        )
    }
}

/**
 * Reusable Metric Card for 2x2 Grid
 */
@Composable
private fun DetailMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = tokens.caption,
                color = close_color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = blackTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatDetailDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "-"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val parsed = parser.parse(isoDate)
        if (parsed != null) formatter.format(parsed) else isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun formatHistoryDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val parsed = parser.parse(isoDate)
        if (parsed != null) formatter.format(parsed) else isoDate
    } catch (_: Exception) {
        isoDate
    }
}