package com.cuso.mobile.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.cuso.mobile.model.inventory.PoItemOverviewDto
import com.cuso.mobile.model.inventory.PoReceiveSummaryDto
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel

@Composable
fun PurchaseDetailScreen(
    poId: String = "6a8e9577d6326a46b6364822",
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onPreviewPdfClick: () -> Unit = {},
    onConvertToBillSuccess: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val historyData by viewModel.poHistory.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var selectedReceiveId by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(poId) {
        if (poId.isNotBlank()) {
            viewModel.fetchReceiveHistoryByPo(poId)
        }
    }

    LaunchedEffect(historyData) {
        historyData?.receives?.firstOrNull()?.let {
            selectedReceiveId = it.id
        }
    }

    val poHeader = historyData?.po
    val itemsOverview = historyData?.itemsOverview ?: emptyList()
    val receives = historyData?.receives ?: emptyList()

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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── PO Header Section ──
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
                                text = poHeader?.poNumber ?: "PO Detail",
                                fontSize = 20.sp,
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
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }

                        IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Purchase Orders / ${poHeader?.poNumber.orEmpty()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Action Buttons Row ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg, contentColor = title_color),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp), tint = title_color)
                            Spacer(Modifier.width(6.dp))
                            Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = title_color)
                        }

                        OutlinedButton(
                            onClick = onPreviewPdfClick,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg, contentColor = title_color),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Preview PDF", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = title_color)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp), tint = title_color)
                        }

                        OutlinedButton(
                            onClick = { /* Download */ },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg, contentColor = title_color),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = title_color, modifier = Modifier.size(17.dp))
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
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(38.dp).weight(1f)
                        ) {
                            Text("Convert to Bill", fontSize = 13.sp, color = whiteBg, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // ── PURCHASE DETAILS Section ──
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Text("PURCHASE DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Receive Date", fontSize = 11.sp, color = mutedText)
                            Text(poHeader?.poDate?.take(10) ?: "—", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Warehouse", fontSize = 11.sp, color = mutedText)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text(poHeader?.warehouse ?: "Main Warehouse", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // ── Items Overview Section ──
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Items Overview", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${itemsOverview.size} Products", fontSize = 11.sp, color = mutedText)
                    }

                    Spacer(Modifier.height(12.dp))

                    itemsOverview.forEach { item ->
                        ItemProgressCard(item = item)
                        Spacer(Modifier.height(10.dp))
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // ── Receive History Section ──
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Receive History", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text("${receives.size} Shipments recorded", fontSize = 11.sp, color = mutedText)
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
                    modifier = Modifier.size(36.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("SKU: ${item.sku}", fontSize = 11.sp, color = iconMuted)
                }

                Text("₹${item.rate}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${item.totalReceivedQty} / ${item.orderedQty} Received", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Medium)
                Text("${item.percent.toInt()}%", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (item.percent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Primary,
                trackColor = grey_border
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Status", fontSize = 11.sp, color = mutedText)
                Text(item.receiveStatus, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = darkGreenBg)
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
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
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
            Text(item.receiveNumber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Primary)
            Text(item.receiveDate.take(10), fontSize = 11.sp, color = mutedText)
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
            Text("₹${item.grandTotal}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("${item.totalQty} Units", fontSize = 11.sp, color = mutedText)
        }
    }
}