@file:Suppress("unused")

package com.cuso.tailor.view.home.logistics.cod

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.CodTimelineItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.toTitleCase
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.LogisticsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.core.net.toUri

@Composable
fun CodOrderOverviewScreen(
    codOrderId: String,
    viewModel: LogisticsViewModel,
    onBack: () -> Unit,
    onViewTrackingHistory: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val data by viewModel.codDetail.collectAsState()
    val isLoading by viewModel.isLoadingCodDetail.collectAsState()
    val isMarkingSaving by viewModel.isMarkingCodCollected.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var settlementIdInput by remember { mutableStateOf("") }
    var copiedTracking by remember { mutableStateOf(false) }

    // Fetch dynamic COD order details on screen launch
    LaunchedEffect(codOrderId) {
        viewModel.fetchCodOrderDetail(codOrderId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCodDetail()
        }
    }

    val isCollected = data?.status.equals("Collected", ignoreCase = true)
    val statusBg = if (isCollected) greenBg else redBg
    val statusText = if (isCollected) greentext else redText

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            // Reusing TitleBar component with trailingContent slot for dynamic status badge
            TitleBar(
                title = "Order #${data?.codNumber?.ifBlank { data?.orderCode } ?: ""}",
                onClose = onBack,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = (data?.status ?: "Pending").toTitleCase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusText
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading && data == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (data == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "COD order details not found",
                    fontSize = tokens.bodyMedium,
                    color = mutedText
                )
            }
        } else {
            val order = data!!
            val customer = order.customerInfo
            val payment = order.paymentDetails
            val courier = order.courierDetails
            val timeline = order.activityTimeline
            val hasTracking = !courier?.trackingId.isNullOrBlank() && courier.trackingId != "--"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dynamic Last Updated Timestamp calculation
                val latestTimestamp = timeline.firstOrNull()?.timestamp
                if (!latestTimestamp.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = mutedText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Last updated ${formatTimeAgo(latestTimestamp)}",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                }

                // ─── 1. COD Amount & Action Button Card ───
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding)
                    ) {
                        Text(
                            text = "COD Amount",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "₹${formatIndianNumber(order.codAmount)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { showConfirmDialog = true },
                            enabled = !isCollected && !isMarkingSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight * 1.1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                disabledContainerColor = disabled
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Payments,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isCollected) "Collected" else "Mark as Collected",
                                color = whiteBg,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // ─── 2. Customer Information Card ───
                OverviewCard(
                    icon = Icons.Outlined.Person,
                    title = "Customer Information"
                ) {
                    Text(
                        text = "Customer Name",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Text(
                        text = customer?.name?.ifBlank { "—" } ?: "—",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )

                    Spacer(Modifier.height(10.dp))

                    // Call & Email Dynamic Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                customer?.phone?.let {
                                    val intent = Intent(Intent.ACTION_DIAL, "tel:$it".toUri())
                                    context.startActivity(intent)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BorderGray),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = cardBgLight),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Call",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                customer?.email?.let {
                                    val intent = Intent(Intent.ACTION_SENDTO, "mailto:$it".toUri())
                                    context.startActivity(intent)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BorderGray),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = cardBgLight),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Email",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Delivery Address",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = customer?.deliveryAddress?.ifBlank { "—" } ?: "—",
                        fontSize = tokens.bodySmall,
                        color = title_color,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(14.dp))

                    // Delivery Date & Coordinates Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cardBgLight)
                                .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Delivery Date",
                                        fontSize = tokens.caption,
                                        color = mutedText
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = formatIsoToDisplayDate(customer?.deliveryDate),
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                        }

                        if (!customer?.coordinates.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(cardBgLight)
                                    .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Map,
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Coordinates",
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = customer.coordinates,
                                        fontSize = tokens.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Medium,
                                        color = title_color
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── 3. Payment Details Card ───
                OverviewCard(
                    icon = Icons.Outlined.Receipt,
                    title = "Payment Details",
                    trailingBadge = {
                        val isPaid = payment?.status.equals("Collected", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isPaid) greenBg else redBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = payment?.status ?: "Not Collected",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isPaid) greentext else redText
                            )
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentInfoCell(
                            title = "Payment Type",
                            value = payment?.paymentType ?: "COD",
                            modifier = Modifier.weight(1f)
                        )
                        PaymentInfoCell(
                            title = "Collection Date",
                            value = if (!payment?.collectionDate.isNullOrBlank()) {
                                formatIsoToDisplayDate(payment.collectionDate)
                            } else "Payment not collected yet",
                            modifier = Modifier.weight(1.3f)
                        )
                        PaymentInfoCell(
                            title = "Settlement ID",
                            value = payment?.settlementId?.ifBlank { "--" } ?: "--",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ─── 4. Courier & Fulfillment Card ───
                OverviewCard(
                    icon = Icons.Outlined.LocalShipping,
                    title = "Courier & Fulfillment"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(light_grey),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = courier?.courierCode?.ifBlank { "BD" } ?: "BD",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Courier Partner",
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                                Text(
                                    text = courier?.courierName?.ifBlank { "—" } ?: "—",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Tracking ID",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                            Spacer(Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(cardBgLight)
                                        .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = courier?.trackingId ?: "--",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = title_color
                                    )
                                }
                                if (hasTracking) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (copiedTracking) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy Tracking",
                                        tint = if (copiedTracking) greenBg else Primary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(courier.trackingId))
                                                copiedTracking = true
                                                Toast.makeText(context, "Copied tracking ID", Toast.LENGTH_SHORT).show()
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── 5. Activity Timeline Card ───
                OverviewCard(
                    icon = Icons.Outlined.History,
                    title = "Activity Timeline"
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        timeline.forEachIndexed { index, step ->
                            TimelineItemRow(
                                item = step,
                                index = index,
                                isLast = index == timeline.lastIndex
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                courier?.trackingId?.let { trackingId ->
                                    onViewTrackingHistory(trackingId)
                                }
                            },
                            enabled = hasTracking,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (hasTracking) Color(0xFF3F37C9) else BorderGray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = "View Full Tracking History",
                                color = if (hasTracking) Color(0xFF3F37C9) else mutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // ─── Confirmation Modal for "Mark as Collected" ───
        if (showConfirmDialog) {
            Dialog(onDismissRequest = { showConfirmDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Mark as Collected",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Confirm that ₹${formatIndianNumber(data?.codAmount ?: 0.0)} has been collected for ${data?.codNumber}. The order will be marked as paid.",
                            fontSize = tokens.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = "Settlement ID (optional)",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Spacer(Modifier.height(4.dp))

                        FormTextField(
                            value = settlementIdInput,
                            onValueChange = { settlementIdInput = it },
                            placeholder = "Auto-generated if empty"
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showConfirmDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = title_color,
                                    fontSize = tokens.bodySmall
                                )
                            }

                            Button(
                                onClick = {
                                    showConfirmDialog = false
                                    viewModel.markCodOrderCollected(
                                        id = codOrderId,
                                        settlementId = settlementIdInput,
                                        onSuccess = { DynamicIslandManager.showSuccess(it) },
                                        onError = { DynamicIslandManager.showError(it) }
                                    )
                                },
                                enabled = !isMarkingSaving,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text(
                                    text = if (isMarkingSaving) "Updating..." else "Confirm",
                                    color = whiteBg,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Sub-Components ───

@Composable
private fun OverviewCard(
    icon: ImageVector,
    title: String,
    trailingBadge: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(primary_light),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                }

                trailingBadge?.invoke()
            }

            content()
        }
    }
}

@Composable
private fun PaymentInfoCell(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(cardBgLight)
            .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 10.sp,
                color = mutedText
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
    }
}

@Composable
private fun TimelineItemRow(
    item: CodTimelineItemDto,
    index: Int,
    isLast: Boolean
) {
    val tokens = LocalAppTokens.current
    val circleSize = 32.dp

    // Dynamic icon resolution: step 0 gets checkmark, intermediate gets truck, last gets box
    val stepIcon = when {
        index == 0 -> Icons.Outlined.CheckCircle
        isLast -> Icons.Outlined.Inventory2
        else -> Icons.Outlined.LocalShipping
    }

    val isStepCompleted = item.isCompleted && !item.timestamp.isNullOrBlank()

    val circleBg = if (isStepCompleted) Color(0xFF3F37C9) else Color(0xFFF1F5F9)
    val circleTint = if (isStepCompleted) Color.White else Color(0xFF64748B)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical step node + connector line aligned directly underneath
        Box(
            modifier = Modifier.width(circleSize),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = circleSize)
                        .width(1.5.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFE2E8F0))
                )
            }

            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(circleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stepIcon,
                    contentDescription = null,
                    tint = circleTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        // Right side: Title, formatted date/Pending, and description pill
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 4.dp else 24.dp)
        ) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.isCompleted) Color(0xFF1E293B) else Color(0xFF64748B)
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = formatTimelineDateTime(item.timestamp),
                fontSize = 12.sp,
                color = Color(0xFF8D99AE)
            )

            if (!item.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ─── Format Helpers ───

private fun formatIsoToDisplayDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "--"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoString)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        date?.let { formatter.format(it) } ?: isoString
    } catch (_: Exception) {
        isoString
    }
}

private fun formatTimelineDateTime(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "Pending"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoString)
        val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        date?.let { formatter.format(it) } ?: "Pending"
    } catch (_: Exception) {
        "Pending"
    }
}

private fun formatTimeAgo(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoString) ?: return ""
        val diffMins = (Date().time - date.time) / (1000 * 60)
        when {
            diffMins < 60 -> "$diffMins minutes ago"
            diffMins < 1440 -> "${diffMins / 60} hours ago"
            else -> "${diffMins / 1440} days ago"
        }
    } catch (_: Exception) {
        ""
    }
}