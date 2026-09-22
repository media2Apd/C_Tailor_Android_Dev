package com.cuso.tailor.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.model.inventory.PoHistoryHeaderDto
import com.cuso.tailor.model.inventory.PoItemOverviewDto
import com.cuso.tailor.model.inventory.PoReceiveSummaryDto
import com.cuso.tailor.model.inventory.PurchaseReceiveItem
import com.cuso.tailor.model.inventory.ReceiveHistoryByPoResponse
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.view.composable.ActionRowButtons
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.util.Locale

@Composable
fun PurchaseDetailScreen(
    poId: String,
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onPreviewPdfClick: () -> Unit = {},
    onConvertToBillSuccess: () -> Unit = {}
) {
    val historyData: ReceiveHistoryByPoResponse? by viewModel.poHistory.collectAsStateWithLifecycle()
    val allReceives: List<PurchaseReceiveItem> by viewModel.allReceives.collectAsStateWithLifecycle()
    val isLoadingHistory: Boolean by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingReceives: Boolean by viewModel.isLoadingReceives.collectAsStateWithLifecycle()
    val isLoadingMultiple: Boolean by viewModel.isLoadingMultipleReceives.collectAsStateWithLifecycle()
    val errorMessage: String? by viewModel.errorMessage.collectAsStateWithLifecycle()

    var selectedReceiveId by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val effectivePoId = poId.ifBlank { historyData?.po?.poId.orEmpty() }

    LaunchedEffect(effectivePoId) {
        if (effectivePoId.isNotBlank()) {
            viewModel.fetchReceiveHistoryByPo(effectivePoId)
            viewModel.fetchAllReceives()
        }
    }

    val poHeader: PoHistoryHeaderDto? = historyData?.po

    val itemsOverview: List<PoItemOverviewDto> = remember(historyData?.itemsOverview) {
        historyData?.itemsOverview ?: emptyList()
    }

    val receives: List<PoReceiveSummaryDto> = remember(historyData?.receives, allReceives, effectivePoId) {
        if (!historyData?.receives.isNullOrEmpty()) {
            historyData!!.receives
        } else {
            allReceives.filter { it.poId?.id == effectivePoId || it.poId?.poNumber == poHeader?.poNumber }
                .map { rec ->
                    PoReceiveSummaryDto(
                        id = rec.id,
                        receiveNumber = rec.receiveNumber.ifBlank { "REC-${rec.id.takeLast(5)}" },
                        receiveDate = rec.receiveDate,
                        totalQty = rec.items.sumOf { it.qtyReceived.toDouble() },
                        grandTotal = rec.grandTotal,
                        billingStatus = rec.billingStatus,
                        receivedBy = null
                    )
                }
        }
    }

    LaunchedEffect(receives) {
        if (selectedReceiveId.isBlank() && receives.isNotEmpty()) {
            selectedReceiveId = receives.first().id
        }
    }

    val isInitialLoading = (isLoadingHistory || isLoadingReceives) && historyData == null && itemsOverview.isEmpty()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar("Purchase Detail", onClose = onClose)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        if (isInitialLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp).background(Color.Transparent))

                // PO Header Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = poHeader?.poNumber?.takeIf { it.isNotBlank() } ?: "PO-88995",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E2238)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8FBF4),
                                border = BorderStroke(1.dp, Color(0xFFA3EEDB))
                            ) {
                                Text(
                                    text = "PAID",
                                    color = Color(0xFF00B074),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Purchase Orders / ${poHeader?.poNumber?.takeIf { it.isNotBlank() }?.let { "Bill-$it" } ?: "Bill-88995"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8B95A5)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Buttons with Convert Action calling View Multiple API
                    ActionRowButtons(
                        editText = "Edit",
                        previewPdfText = "Preview PDF",
                        downloadDescription = "Download",
                        convertToBillText = if (isLoadingMultiple) "Converting..." else "Convert to Bill",
                        isConvertToBillEnabled = selectedReceiveId.isNotBlank() && !isLoadingMultiple,
                        onEditClick = onEditClick,
                        onPreviewPdfClick = onPreviewPdfClick,
                        onDownloadClick = { },
                        onConvertToBillClick = {
                            if (selectedReceiveId.isNotBlank()) {
                                viewModel.fetchMultipleReceives(listOf(selectedReceiveId)) {
                                    onConvertToBillSuccess()
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp).background(Color.Transparent))

                // Purchase Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "PURCHASE DETAILS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2238)
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(text = "Vendor", fontSize = 13.sp, color = Color(0xFF8C95A6))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = poHeader?.supplierName?.takeIf { it.isNotBlank() } ?: "—",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1E2238)
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Receive Date", fontSize = 13.sp, color = Color(0xFF8C95A6))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatDisplayDate(poHeader?.poDate).ifBlank { "—" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF1E2238)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Warehouse", fontSize = 13.sp, color = Color(0xFF8C95A6))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF5B4DFF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = poHeader?.warehouse?.takeIf { it.isNotBlank() } ?: "—",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF1E2238)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp).background(Color.Transparent))

                // Items Overview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Items Overview",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2238)
                        )
                        Text(
                            text = "${itemsOverview.size} Products",
                            fontSize = 13.sp,
                            color = Color(0xFF8C95A6)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    itemsOverview.forEachIndexed { index, item ->
                        ItemProgressCard(item = item, isFirst = index == 0)
                        if (index != itemsOverview.lastIndex) {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp).background(Color.Transparent))

                // Receive History
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF4338CA),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Receive History",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E2238)
                            )
                        }
                        Text(
                            text = "${receives.size} Shipments recorded",
                            fontSize = 13.sp,
                            color = Color(0xFF8C95A6)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    receives.forEachIndexed { index, receive ->
                        ShipmentHistoryRow(
                            item = receive,
                            isSelected = selectedReceiveId == receive.id,
                            onSelect = { selectedReceiveId = receive.id }
                        )
                        if (index != receives.lastIndex) {
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp).background(Color.Transparent))
            }
        }

        DynamicIslandSuccess(message = successMsg, onDismiss = { successMsg = null })
        DynamicIslandError(message = errorMessage, onDismiss = { viewModel.clearErrors() })
    }
}

@Composable
fun ItemProgressCard(item: PoItemOverviewDto, isFirst: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEEF2F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFirst) Icons.Outlined.DesktopWindows else Icons.Outlined.Checkroom,
                    contentDescription = null,
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E2238)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "SKU: ${item.sku}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Text(
                text = String.format(Locale.US, "₹%,.2f", item.rate),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E2238)
            )
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8FAFD))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.totalReceivedQty.toInt()} / ${item.orderedQty.toInt()} Received",
                        fontSize = 13.sp,
                        color = Color(0xFF4338CA),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${item.percent.toInt()}%",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (item.percent / 100f).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF4338CA),
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "In Stock",
                        fontSize = 13.sp,
                        color = headerGrey
                    )
                    Text(
                        text = item.receiveStatus.takeIf { it.isNotBlank() } ?: item.totalReceivedQty.toInt().toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}

@Composable
fun ShipmentHistoryRow(
    item: PoReceiveSummaryDto,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isBilled = item.billingStatus.equals("Billed", ignoreCase = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFFF6F8FF) else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4338CA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.receiveNumber,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4338CA)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.receiveDate.take(10),
                fontSize = 13.sp,
                color = Color(0xFF8C95A6)
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isBilled) Color(0xFFE8FBF4) else Color(0xFFEEF0FF)
            ) {
                Text(
                    text = item.billingStatus,
                    color = if (isBilled) Color(0xFF00B074) else Color(0xFF4338CA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format(Locale.US, "₹%,.2f", item.grandTotal),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2238)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${item.totalQty.toInt().toString().padStart(2, '0')} Units",
                fontSize = 13.sp,
                color = Color(0xFF8C95A6)
            )
        }
    }
}

private fun formatDisplayDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return ""
    return try {
        val clean = rawDate.take(10)
        val parts = clean.split("-")
        if (parts.size == 3) {
            val months = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            val monthIdx = (parts[1].toIntOrNull() ?: 1) - 1
            val monthName = months.getOrElse(monthIdx) { "Aug" }
            val day = parts[2].toIntOrNull() ?: parts[2]
            "$day $monthName ${parts[0]}"
        } else rawDate
    } catch (_: Exception) {
        rawDate
    }
}