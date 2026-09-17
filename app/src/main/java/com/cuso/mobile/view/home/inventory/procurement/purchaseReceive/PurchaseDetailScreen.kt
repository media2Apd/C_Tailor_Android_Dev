package com.cuso.mobile.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.PoHistoryHeaderDto
import com.cuso.mobile.model.inventory.PoItemOverviewDto
import com.cuso.mobile.model.inventory.PoReceiveSummaryDto
import com.cuso.mobile.model.inventory.ReceiveHistoryByPoResponse
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextPrimary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.darkGreenBg
import com.cuso.mobile.ui.theme.dividerColor
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.iconMuted
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel

@Composable
fun PurchaseDetailScreen(
    poId: String,
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onPreviewPdfClick: () -> Unit = {},
    onConvertToBillSuccess: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val historyData: ReceiveHistoryByPoResponse? by viewModel.poHistory.collectAsStateWithLifecycle()
    val isLoading: Boolean by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage: String? by viewModel.errorMessage.collectAsStateWithLifecycle()

    var selectedReceiveId by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(poId) {
        if (poId.isNotBlank()) {
            viewModel.fetchReceiveHistoryByPo(poId)
        }
    }

    LaunchedEffect(historyData) {
        if (selectedReceiveId.isBlank()) {
            historyData?.receives?.firstOrNull()?.let { firstReceive ->
                selectedReceiveId = firstReceive.id
            }
        }
    }

    val poHeader: PoHistoryHeaderDto? = historyData?.po
    val itemsOverview: List<PoItemOverviewDto> = historyData?.itemsOverview ?: emptyList()
    val receives: List<PoReceiveSummaryDto> = historyData?.receives ?: emptyList()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "Purchase Detail",
                onClose = onClose
            )
        }
    ) { padding ->
        if (isLoading && historyData == null) {
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
                // ── PO Header Summary Card ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = poHeader?.poNumber?.takeIf { it.isNotBlank() } ?: "PO Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFE6F9F0),
                                border = BorderStroke(1.dp, Color(0xFFB4F2D6))
                            ) {
                                Text(
                                    text = "ACTIVE",
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
                        text = "Supplier: ${poHeader?.supplierName.orEmpty()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Action Buttons ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = whiteBg,
                                contentColor = title_color
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = title_color
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        OutlinedButton(
                            onClick = onPreviewPdfClick,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = whiteBg,
                                contentColor = title_color
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Preview PDF", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = title_color
                            )
                        }

                        Button(
                            onClick = {
                                if (selectedReceiveId.isNotBlank()) {
                                    viewModel.convertReceiveToBill(selectedReceiveId) {
                                        successMsg = "Converted to bill successfully"
                                        onConvertToBillSuccess()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            enabled = selectedReceiveId.isNotBlank(),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = "Convert to Bill",
                                fontSize = 13.sp,
                                color = whiteBg,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // ── Detailed Metadata Grid ──
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Text(
                        text = "PURCHASE DETAILS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Receive Date", fontSize = 11.sp, color = mutedText)
                            Text(
                                text = poHeader?.poDate?.take(10) ?: "—",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Warehouse", fontSize = 11.sp, color = mutedText)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = poHeader?.warehouse?.takeIf { it.isNotBlank() } ?: "—",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    if (!poHeader?.transportName.isNullOrBlank() || !poHeader?.vehicleNumber.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Transport", fontSize = 11.sp, color = mutedText)
                                Text(
                                    text = poHeader?.transportName ?: "—",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Vehicle No.", fontSize = 11.sp, color = mutedText)
                                Text(
                                    text = poHeader?.vehicleNumber ?: "—",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // ── Line Items Fulfillment Overview ──
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Items Overview",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${itemsOverview.size} Products",
                            fontSize = 11.sp,
                            color = mutedText
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    itemsOverview.forEach { item ->
                        ItemProgressCard(item = item)
                        Spacer(Modifier.height(10.dp))
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // ── Associated Shipment / Receive Logs ──
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Receive History",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${receives.size} Shipments recorded",
                            fontSize = 11.sp,
                            color = mutedText
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    receives.forEach { receive ->
                        ShipmentHistoryRow(
                            item = receive,
                            isSelected = selectedReceiveId == receive.id,
                            onSelect = { selectedReceiveId = receive.id }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }

        DynamicIslandSuccess(message = successMsg, onDismiss = { successMsg = null })
        DynamicIslandError(message = errorMessage, onDismiss = { viewModel.clearErrors() })
    }
}

@Composable
fun ItemProgressCard(item: PoItemOverviewDto) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, grey_border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "SKU: ${item.sku}",
                        fontSize = 11.sp,
                        color = iconMuted
                    )
                }

                Text(
                    text = "₹${item.rate}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.totalReceivedQty.toInt()} / ${item.orderedQty.toInt()} Received",
                    fontSize = 11.sp,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${item.percent.toInt()}%",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (item.percent / 100f).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Primary,
                trackColor = grey_border
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status",
                    fontSize = 11.sp,
                    color = mutedText
                )
                Text(
                    text = item.receiveStatus,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkGreenBg
                )
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
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = Primary),
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.receiveNumber,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )
            Text(
                text = item.receiveDate.take(10),
                fontSize = 11.sp,
                color = mutedText
            )
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isBilled) greenBg else Color(0xFFEFF6FF)
            ) {
                Text(
                    text = item.billingStatus,
                    color = if (isBilled) darkGreenBg else Primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${item.grandTotal}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${item.totalQty.toInt()} Units",
                fontSize = 11.sp,
                color = mutedText
            )
        }
    }
}