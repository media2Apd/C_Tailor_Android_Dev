@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.services.service_order.production_work

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────
// Dynamic API to Object Reflection Helpers
// ─────────────────────────────────────────────────────────────

private fun Any.extractString(vararg names: String): String? {
    for (name in names) {
        try {
            val field = this::class.java.declaredFields.find { it.name.equals(name, ignoreCase = true) }
            if (field != null) {
                field.isAccessible = true
                val v = field.get(this)
                if (v is String && v.isNotBlank()) return v
                if (v != null && v !is Collection<*> && v !is Map<*, *>) return v.toString()
            }
            val getter = this::class.java.declaredMethods.find {
                it.name.equals("get${name.replaceFirstChar { c -> c.uppercase() }}", ignoreCase = true) ||
                        it.name.equals(name, ignoreCase = true)
            }
            if (getter != null && getter.parameterTypes.isEmpty()) {
                getter.isAccessible = true
                val v = getter.invoke(this)
                if (v is String && v.isNotBlank()) return v
                if (v != null && v !is Collection<*> && v !is Map<*, *>) return v.toString()
            }
        } catch (_: Exception) {}
    }
    return null
}

private fun Any.extractInt(vararg names: String, default: Int = 0): Int {
    for (name in names) {
        try {
            val field = this::class.java.declaredFields.find { it.name.equals(name, ignoreCase = true) }
            if (field != null) {
                field.isAccessible = true
                val v = field.get(this)
                if (v is Number) return v.toInt()
            }
            val getter = this::class.java.declaredMethods.find {
                it.name.equals("get${name.replaceFirstChar { c -> c.uppercase() }}", ignoreCase = true) ||
                        it.name.equals(name, ignoreCase = true)
            }
            if (getter != null && getter.parameterTypes.isEmpty()) {
                getter.isAccessible = true
                val v = getter.invoke(this)
                if (v is Number) return v.toInt()
            }
        } catch (_: Exception) {}
    }
    return default
}

private fun Any.extractObject(vararg names: String): Any? {
    for (name in names) {
        try {
            val field = this::class.java.declaredFields.find { it.name.equals(name, ignoreCase = true) }
            if (field != null) {
                field.isAccessible = true
                val v = field.get(this)
                if (v != null) return v
            }
            val getter = this::class.java.declaredMethods.find {
                it.name.equals("get${name.replaceFirstChar { c -> c.uppercase() }}", ignoreCase = true) ||
                        it.name.equals(name, ignoreCase = true)
            }
            if (getter != null && getter.parameterTypes.isEmpty()) {
                getter.isAccessible = true
                val v = getter.invoke(this)
                if (v != null) return v
            }
        } catch (_: Exception) {}
    }
    return null
}

private fun Any.extractList(vararg names: String): List<Any> {
    for (name in names) {
        try {
            val field = this::class.java.declaredFields.find { it.name.equals(name, ignoreCase = true) }
            if (field != null) {
                field.isAccessible = true
                val v = field.get(this)
                if (v is List<*>) return v.filterNotNull()
            }
            val getter = this::class.java.declaredMethods.find {
                it.name.equals("get${name.replaceFirstChar { c -> c.uppercase() }}", ignoreCase = true) ||
                        it.name.equals(name, ignoreCase = true)
            }
            if (getter != null && getter.parameterTypes.isEmpty()) {
                getter.isAccessible = true
                val v = getter.invoke(this)
                if (v is List<*>) return v.filterNotNull()
            }
        } catch (_: Exception) {}
    }
    return emptyList()
}

// ─────────────────────────────────────────────────────────────
// Formatting helpers
// ─────────────────────────────────────────────────────────────

private fun formatPipelineDate(raw: String?): String {
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

// Formats an ISO timestamp as "Oct 13, 05:30 AM" in the device time zone
private fun formatAlertTime(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(raw) ?: return raw.take(10)
        SimpleDateFormat("MMM dd, hh:mm a", Locale.ENGLISH).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}

// Converts minutes to a compact label such as "1h 30m"
private fun formatMinutes(mins: Int): String {
    val h = mins / 60
    val m = mins % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}

private fun prettyStatus(raw: String): String = raw.replace("_", " ")

private fun stageIcon(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        n.contains("cut") -> Icons.Default.ContentCut
        n.contains("embroid") -> Icons.Default.AutoAwesome
        n.contains("stitch") -> Icons.Default.Checkroom
        n.contains("quality") -> Icons.Default.VerifiedUser
        n.contains("trial") -> Icons.Default.Person
        n.contains("iron") || n.contains("press") -> Icons.Default.Iron
        n.contains("pack") -> Icons.Default.Inventory2
        else -> Icons.Default.Settings
    }
}

// ─────────────────────────────────────────────────────────────
// Style holders
// ─────────────────────────────────────────────────────────────

private data class ChipStyle(val bg: Color, val fg: Color, val border: Color)

private data class AlertStyle(
    val bg: Color,
    val accent: Color,
    val text: Color,
    val icon: ImageVector
)

private fun priorityStyle(priority: String): ChipStyle = when (priority.lowercase()) {
    "high", "urgent" -> ChipStyle(redBg, redText, redBg)
    "low" -> ChipStyle(greenBg, greentext, greenBg)
    else -> ChipStyle(yellowBg, yellowText, orangeBg)
}

private fun stageBadgeStyle(status: String): ChipStyle = when (status.lowercase()) {
    "completed" -> ChipStyle(greenBg, greentext, greenBg)
    "on_hold" -> ChipStyle(orangeBg, orangeText, orangeBg)
    "rework", "in_progress" -> ChipStyle(activity_purple_bg, blueText, light_blue_border)
    else -> ChipStyle(badgeGrey, mutedText, sectionBorder)
}

private fun alertStyle(type: String?): AlertStyle {
    val t = type?.lowercase() ?: ""
    return when {
        t.contains("error") || t.contains("critical") || t.contains("high") || t.contains("danger") ->
            AlertStyle(roseBg, redText, redText, Icons.Default.Error)
        t.contains("warn") || t.contains("overdue") || t.contains("delay") ->
            AlertStyle(yellowBg, chartAmber, activity_brown, Icons.Default.Warning)
        else ->
            AlertStyle(activity_purple_bg, blueText, darkPurple, Icons.Default.Info)
    }
}

// ─────────────────────────────────────────────────────────────
// Screen Composable
// ─────────────────────────────────────────────────────────────

@Composable
fun OrderProductionPipelineScreen(
    orderId: String,
    viewModel: ServicesViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onVerifyClick: (orderId: String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val orderDetail by viewModel.serviceOrderDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingOrderDetail.collectAsStateWithLifecycle()
    val errorMessage by viewModel.orderDetailError.collectAsStateWithLifecycle()

    var selectedJobCardIndex by remember { mutableIntStateOf(0) }

    // Modal state
    var startModalOpen by remember { mutableStateOf(false) }
    var completeModalOpen by remember { mutableStateOf(false) }
    var holdModalOpen by remember { mutableStateOf(false) }
    var issueModalOpen by remember { mutableStateOf(false) }
    var wastageModalOpen by remember { mutableStateOf(false) }
    var targetStage by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(orderId) {
        if (orderId.isNotBlank()) {
            viewModel.fetchServiceOrderDetail(orderId)
        }
    }

    if (isLoading && orderDetail == null) {
        Box(modifier = Modifier.fillMaxSize().background(Primary_background)) {
            ListSkeleton()
        }
        return
    }

    if (errorMessage != null && orderDetail == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Primary_background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = errorMessage ?: "Failed to load order", color = redText, fontSize = tokens.bodySmall)
                Spacer(Modifier.height(tokens.extraPadding))
                Button(
                    onClick = { viewModel.fetchServiceOrderDetail(orderId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(text = "Retry", color = whiteBg)
                }
            }
        }
        return
    }

    val detail = orderDetail ?: return

    // Order header details
    val orderHeader = detail.extractObject("orderHeader") ?: detail
    val orderCode = orderHeader.extractString("orderCode", "serviceOrderCode", "code") ?: orderId
    val priority = orderHeader.extractString("priority") ?: "Medium"
    val status = orderHeader.extractString("status", "overallStatus") ?: "In Production"

    val customerObj = orderHeader.extractObject("customer", "customerSnapshot")
    val customerName = customerObj?.extractString("name", "customerName") ?: "-"
    val customerPhone = customerObj?.extractString("phone", "mobileNumber", "contact") ?: "-"

    val deliveryDate = formatPipelineDate(orderHeader.extractString("dueDate", "deliveryDate"))

    // Job cards
    val jobCards = detail.extractList("jobCards")
    val activeJobCard = jobCards.getOrNull(selectedJobCardIndex) ?: jobCards.firstOrNull() ?: detail
    val activeJobCardId = activeJobCard.extractString("_id", "id", "jobCardId") ?: ""
    val garmentObj = activeJobCard.extractObject("garment")
    val garmentName = garmentObj?.extractString("name") ?: activeJobCard.extractString("garmentName") ?: "Garment"
    val isStoreProvided = garmentObj?.extractString("fabricSource") == "Store_Provided"
    val totalGarments = jobCards.size.coerceAtLeast(1)
    val quantity = activeJobCard.extractInt("quantity", "qty", default = 1)
    val jobCardStatus = activeJobCard.extractString("status") ?: "In_Production"
    val jobCardCode = activeJobCard.extractString("jobCardNumber", "jobCardCode", "jobNumber", "code")
        ?: activeJobCardId

    // Stages
    val stages = activeJobCard.extractList("stages", "workflowStages")
    val isAllStagesCompleted = stages.isNotEmpty() && stages.all {
        it.extractString("status")?.equals("Completed", ignoreCase = true) == true
    }
    val activeStageIndex = stages.indexOfFirst {
        it.extractString("status")?.equals("Completed", ignoreCase = true) != true
    }
    val currentStageName = stages.getOrNull(activeStageIndex)
        ?.extractString("stageName", "name") ?: "Completed"

    val assignedGarmentsCount = jobCards.count {
        it.extractString("status") != "Pending_Allocation"
    }

    // Activity and alerts
    val alerts = detail.extractList("alerts", "activityAlerts", "activities", "activityLogs")

    val priorityChip = priorityStyle(priority)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Top header ──
        // ── Top header ──
        Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {

            // Row 1: back arrow, order code with chips, overflow menu (all vertically centered)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Back",
                        tint = headerGrey,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = orderCode,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Spacer(Modifier.width(8.dp))
                        // Priority chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(priorityChip.bg)
                                .border(1.dp, priorityChip.border, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = priority,
                                fontSize = tokens.caption,
                                color = priorityChip.fg,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Order status chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(greenBg)
                            .border(1.dp, chartEmerald.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = prettyStatus(status),
                            fontSize = tokens.caption,
                            color = greentext,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                IconButton(onClick = onMenuClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = iconMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Thin inset divider between the title area and the summary row
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = tokens.screenPadding),
                color = PrimaryBorder
            )

            // Row 2: customer, delivery, garments and verify button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer name and phone
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customerName,
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = customerPhone,
                        fontSize = tokens.caption,
                        color = iconMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Delivery date (right aligned under its label)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DELIVERY",
                        fontSize = tokens.caption,
                        color = iconMuted,
                        maxLines = 1
                    )
                    Text(
                        text = deliveryDate,
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Garments count with a rounded Primary underline as wide as the text
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GARMENTS",
                        fontSize = tokens.caption,
                        color = iconMuted,
                        maxLines = 1
                    )
                    Text(
                        text = "$assignedGarmentsCount / $totalGarments assigned",
                        fontSize = tokens.bodySmall,
                        color = headerGrey,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Primary)
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Verify checklist button (grey until every stage is completed)
                Button(
                    onClick = { onVerifyClick(orderId) },
                    enabled = isAllStagesCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAllStagesCompleted) greenBg else grey_border,
                        contentColor = if (isAllStagesCompleted) greentext else textSubdued,
                        disabledContainerColor = grey_border,
                        disabledContentColor = textSubdued
                    ),
                    border = BorderStroke(1.dp, sectionBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Verify",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        HorizontalDivider(color = title_border)

        // ── Scrollable body ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Item card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primary_light)
                    .border(1.dp, background_light_purple, RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(Primary)
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ITEM ${selectedJobCardIndex + 1}: ${garmentName.uppercase()}",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Qty: ",
                                fontSize = tokens.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "$quantity",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(iconMuted, CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${prettyStatus(jobCardStatus)} ($currentStageName)",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(background_light_purple)
                            .clickable(enabled = jobCards.size > 1) {
                                // Cycle through the garments when there is more than one
                                selectedJobCardIndex = (selectedJobCardIndex + 1) % jobCards.size
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next item",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Activity and alerts
            if (alerts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVITY & ALERTS",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryTextColor
                        )
                        Text(
                            text = "${alerts.size} Total",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }

                    alerts.forEach { alert ->
                        val message = alert.extractString("message", "title", "description", "text") ?: ""
                        val time = formatAlertTime(alert.extractString("createdAt", "date", "timestamp", "time"))
                        val tag = alert.extractString("tag", "category", "label")
                        val style = alertStyle(alert.extractString("type", "severity", "level"))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .clip(RoundedCornerShape(10.dp))
                                .background(style.bg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(style.accent)
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = style.icon,
                                    contentDescription = null,
                                    tint = style.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = message,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = style.text
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = time,
                                            fontSize = tokens.caption,
                                            color = style.accent
                                        )
                                        if (!tag.isNullOrBlank()) {
                                            Spacer(Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(style.accent, CircleShape)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = tag,
                                                fontSize = tokens.caption,
                                                color = style.accent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pipeline header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRODUCTION PIPELINE: ${garmentName.uppercase()}",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (stages.isNotEmpty()) {
                        Text(
                            text = "Step ${(activeStageIndex.takeIf { it >= 0 } ?: (stages.size - 1)) + 1} of ${stages.size}",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                    if (isStoreProvided) {
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { wastageModalOpen = true },
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, chartAmber),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = chartAmber)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(text = "Scrap", fontSize = tokens.caption, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Timeline
            if (stages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No stages configured for this garment.",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }
            } else {
                Column {
                    stages.forEachIndexed { idx, stg ->
                        val stageId = stg.extractString("_id", "id", "stageId") ?: "$idx"
                        val stageName = stg.extractString("stageName", "name") ?: "Stage ${idx + 1}"
                        val stageStatus = stg.extractString("status") ?: "Assigned"
                        val workerObj = stg.extractList("allocations").firstOrNull()?.extractObject("workerId")
                        val workerName = workerObj?.let {
                            "${it.extractString("firstName") ?: ""} ${it.extractString("lastName") ?: ""}".trim()
                        }?.takeIf { it.isNotBlank() } ?: "Unassigned"
                        val estimateMins = stg.extractInt("estimatedMinutes", default = 0)

                        val isCompleted = stageStatus.equals("Completed", ignoreCase = true)
                        val isOnHold = stageStatus.equals("On_Hold", ignoreCase = true)
                        val isInProgress = stageStatus.equals("In_Progress", ignoreCase = true) ||
                                stageStatus.equals("Rework", ignoreCase = true)
                        val isAssigned = stageStatus.equals("Assigned", ignoreCase = true)
                        val isActive = idx == activeStageIndex
                        val isLast = idx == stages.lastIndex
                        val badge = stageBadgeStyle(stageStatus)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            // Timeline rail
                            Column(
                                modifier = Modifier
                                    .width(24.dp)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(Modifier.height(18.dp))
                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Primary, CircleShape)
                                            .border(3.dp, disabled, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = whiteBg,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                } else if (isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(greentext, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = whiteBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(whiteBg, CircleShape)
                                            .border(2.dp, sectionBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(sectionBorder, CircleShape)
                                        )
                                    }
                                }
                                if (!isLast) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .weight(1f)
                                            .background(sectionBorder)
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            // Stage card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = if (isLast) 0.dp else 12.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                                    border = BorderStroke(
                                        if (isActive) 1.5.dp else 1.dp,
                                        if (isActive) chartBlue else sectionBorder
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        // Row 1: icon, stage name (single line) and worker chip
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(primary_light),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = stageIcon(stageName),
                                                    contentDescription = null,
                                                    tint = Primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            Text(
                                                text = stageName,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            // Worker chip
                                            Row(
                                                modifier = Modifier
                                                    .widthIn(max = 120.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(yellowBg)
                                                    .border(1.dp, orangeBg, RoundedCornerShape(50))
                                                    .padding(start = 3.dp, end = 8.dp, top = 3.dp, bottom = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .background(chartAmber, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = workerName.first().uppercase(),
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Bold,
                                                        color = whiteBg,
                                                        maxLines = 1
                                                    )
                                                }
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = workerName,
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = activity_brown,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // Row 2: estimated time on the left, status badge on the right
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccessTime,
                                                    contentDescription = null,
                                                    tint = iconMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                if (estimateMins > 0) {
                                                    Text(
                                                        text = "Est. Time: ",
                                                        fontSize = tokens.caption,
                                                        color = mutedText,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = formatMinutes(estimateMins),
                                                        fontSize = tokens.caption,
                                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isActive) TextPrimary else mutedText,
                                                        maxLines = 1
                                                    )
                                                } else {
                                                    Text(
                                                        text = "No estimation set",
                                                        fontSize = tokens.caption,
                                                        color = mutedText,
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(badge.bg)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = prettyStatus(stageStatus).uppercase(),
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = badge.fg,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        // Action buttons only on the active stage
                                        if (isActive && !isCompleted) {
                                            Spacer(Modifier.height(10.dp))
                                            HorizontalDivider(color = title_border)
                                            Spacer(Modifier.height(10.dp))

                                            if (isAssigned) {
                                                Button(
                                                    onClick = {
                                                        targetStage = stg
                                                        startModalOpen = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = chartGreenDeep),
                                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = whiteBg,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = "Start Work",
                                                        color = whiteBg,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            if (isInProgress) {
                                                Button(
                                                    onClick = {
                                                        targetStage = stg
                                                        completeModalOpen = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = chartGreenDeep),
                                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = whiteBg,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = "Mark Complete",
                                                        color = whiteBg,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            if (isOnHold) {
                                                Button(
                                                    onClick = {
                                                        viewModel.updateStageProgress(
                                                            jobCardId = activeJobCardId,
                                                            stageId = stageId,
                                                            status = "In_Progress",
                                                            notes = "Work resumed on floor",
                                                            onSuccess = {
                                                                Toast.makeText(context, "Work resumed!", Toast.LENGTH_SHORT).show()
                                                                viewModel.fetchServiceOrderDetail(orderId)
                                                            }
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = chartAmber),
                                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = whiteBg,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = "Resume Work",
                                                        color = whiteBg,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (isInProgress) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            targetStage = stg
                                                            holdModalOpen = true
                                                        },
                                                        modifier = Modifier.weight(1f).height(40.dp),
                                                        shape = RoundedCornerShape(10.dp),
                                                        border = BorderStroke(1.dp, sectionBorder),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            containerColor = whiteBg,
                                                            contentColor = TextPrimary
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Pause,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = "Put On Hold",
                                                            fontSize = tokens.caption,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        targetStage = stg
                                                        issueModalOpen = true
                                                    },
                                                    modifier = Modifier.weight(1f).height(40.dp),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, redBg),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        containerColor = roseBg,
                                                        contentColor = redText
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = "Report Issue",
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Medium
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

            Spacer(Modifier.height(16.dp))
        }
    }

    // Modal 0: Start Work
    if (startModalOpen && targetStage != null) {
        var notes by remember { mutableStateOf("") }
        val stageId = targetStage!!.extractString("_id", "id", "stageId") ?: ""
        val stageName = targetStage!!.extractString("stageName", "name") ?: "Stage"

        AlertDialog(
            onDismissRequest = { startModalOpen = false },
            containerColor = whiteBg,
            title = { Text(text = "Start Work: $stageName", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Commence bench operations on this stage?", fontSize = tokens.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Starting Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStageProgress(
                            jobCardId = activeJobCardId,
                            stageId = stageId,
                            status = "In_Progress",
                            notes = notes.ifBlank { "Artisan started work on bench" },
                            onSuccess = {
                                Toast.makeText(context, "Work started!", Toast.LENGTH_SHORT).show()
                                startModalOpen = false
                                viewModel.fetchServiceOrderDetail(orderId)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = chartGreenDeep)
                ) {
                    Text(text = "Start", color = whiteBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { startModalOpen = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    // Modal 1: Mark Stage Complete
    if (completeModalOpen && targetStage != null) {
        var completedQty by remember { mutableIntStateOf(1) }
        var defectiveQty by remember { mutableIntStateOf(0) }
        val stageId = targetStage!!.extractString("_id", "id", "stageId") ?: ""
        val stageName = targetStage!!.extractString("stageName", "name") ?: "Stage"

        AlertDialog(
            onDismissRequest = { completeModalOpen = false },
            title = { Text(text = "Complete Stage: $stageName", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = completedQty.toString(),
                        onValueChange = { completedQty = it.toIntOrNull() ?: 1 },
                        label = { Text("Completed Pieces *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = defectiveQty.toString(),
                        onValueChange = { defectiveQty = it.toIntOrNull() ?: 0 },
                        label = { Text("Defective Pieces") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Generate UTC ISO-8601 timestamp (e.g. 2026-12-10T17:07:00.000Z)
                        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date())

                        viewModel.updateStageProgress(
                            jobCardId = activeJobCardId,
                            stageId = stageId,
                            status = "Completed",
                            completedQuantity = completedQty,
                            defectiveQuantity = defectiveQty,
                            completedAt = nowIso,
                            notes = "Stage fully completed and verified",
                            onSuccess = {
                                Toast.makeText(context, "Stage marked completed!", Toast.LENGTH_SHORT).show()
                                completeModalOpen = false
                                viewModel.fetchServiceOrderDetail(orderId)
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = greentext)
                ) {
                    Text(text = "Confirm Handover", color = whiteBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { completeModalOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Modal 2: Put On Hold
    if (holdModalOpen && targetStage != null) {
        val stg = targetStage!!
        val stageId = stg.extractString("_id", "id", "stageId") ?: ""

        HoldStageDialog(
            jobCardCode = jobCardCode,
            workerName = stageWorkerName(stg),
            onDismiss = { holdModalOpen = false },
            onConfirm = { reason ->
                viewModel.updateStageProgress(
                    jobCardId = activeJobCardId,
                    stageId = stageId,
                    status = "On_Hold",
                    notes = "HOLD REASON: ${reason.trim()}",
                    onSuccess = {
                        Toast.makeText(context, "Stage put on hold", Toast.LENGTH_SHORT).show()
                        holdModalOpen = false
                        viewModel.fetchServiceOrderDetail(orderId)
                    }
                )
            }
        )
    }

    // Modal 3: Report Floor Issue
    if (issueModalOpen && targetStage != null) {
        var issueType by remember { mutableStateOf("Customer_Mistake") }
        var description by remember { mutableStateOf("") }

        val issueOptions = listOf(
            "Customer_Mistake" to "Customer Mistake",
            "Machine_Breakdown" to "Machine Breakdown",
            "Fabric_Damage" to "Fabric Damage",
            "Wrong_Cutting" to "Wrong Cutting"
        )

        AlertDialog(
            onDismissRequest = { issueModalOpen = false },
            title = { Text(text = "Report Floor Issue", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Category:", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold)

                    // Category Selection Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        issueOptions.take(2).forEach { (id, label) ->
                            FilterChip(
                                selected = issueType == id,
                                onClick = { issueType = id },
                                label = { Text(label, fontSize = tokens.caption) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        issueOptions.drop(2).forEach { (id, label) ->
                            FilterChip(
                                selected = issueType == id,
                                onClick = { issueType = id },
                                label = { Text(label, fontSize = tokens.caption) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Issue Description *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (description.isBlank()) return@Button

                        viewModel.logIssueToJobCard(
                            jobCardId = activeJobCardId,
                            issueType = issueType,
                            description = description,
                            affectedQuantity = 1,
                            onSuccess = {
                                Toast.makeText(context, "Issue logged successfully!", Toast.LENGTH_SHORT).show()
                                issueModalOpen = false
                                viewModel.fetchServiceOrderDetail(orderId)
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = redText)
                ) {
                    Text(text = "Submit Issue", color = whiteBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { issueModalOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Modal 4: Record Fabric Scrap
    if (wastageModalOpen) {
        var totalIssued by remember { mutableStateOf("2.5") }
        var actualUsed by remember { mutableStateOf("2.1") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { wastageModalOpen = false },
            containerColor = whiteBg,
            title = { Text(text = "Log Fabric Scrap & Yield", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalIssued,
                        onValueChange = { totalIssued = it },
                        label = { Text("Total Issued Meters *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = actualUsed,
                        onValueChange = { actualUsed = it },
                        label = { Text("Actual Used Meters *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Cutting Layout Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val issued = totalIssued.toDoubleOrNull() ?: 0.0
                        val used = actualUsed.toDoubleOrNull() ?: 0.0
                        viewModel.logFabricWastage(
                            jobCardId = activeJobCardId,
                            totalIssuedMeters = issued,
                            actualGarmentMeters = used,
                            scrapType = "Reusable_Offcut",
                            notes = notes,
                            onSuccess = {
                                Toast.makeText(context, "Scrap recorded!", Toast.LENGTH_SHORT).show()
                                wastageModalOpen = false
                                viewModel.fetchServiceOrderDetail(orderId)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = chartAmber)
                ) {
                    Text(text = "Save Yield", color = whiteBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { wastageModalOpen = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}


// ─────────────────────────────────────────────────────────────
// Shared dialog building blocks
// ─────────────────────────────────────────────────────────────

// Returns the name of the first worker allocated to a stage
private fun stageWorkerName(stage: Any): String {
    val workerObj = stage.extractList("allocations").firstOrNull()?.extractObject("workerId")
    return workerObj?.let {
        "${it.extractString("firstName") ?: ""} ${it.extractString("lastName") ?: ""}".trim()
    }?.takeIf { it.isNotBlank() } ?: "Unassigned"
}

// Rounded white container used by every stage dialog
@Composable
private fun StageDialogContainer(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = whiteBg
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                content = content
            )
        }
    }
}

// Title row with icon, title, subtitle and a close button
@Composable
private fun StageDialogHeader(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    subtitleHighlight: String? = null,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.padding(top = 2.dp).size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = buildAnnotatedString {
                    append(subtitle)
                    if (subtitleHighlight != null) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textSubdued)) {
                            append(subtitleHighlight)
                        }
                    }
                },
                fontSize = tokens.caption,
                color = headerGrey
            )
        }
        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Grey strip with the job card id and the assigned worker
@Composable
private fun JobInfoStrip(jobCardCode: String, workerName: String) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary_background)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(whiteBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "JOB CARD ID",
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryTextColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = jobCardCode.ifBlank { "-" },
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(whiteBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "ASSIGNED",
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryTextColor
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(background_light_purple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = workerName.first().uppercase(),
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = workerName,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Footer with a Cancel button and a custom confirm button
@Composable
private fun StageDialogFooter(
    onCancel: () -> Unit,
    confirmButton: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current

    HorizontalDivider(color = title_border)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.height(40.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, sectionBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = whiteBg,
                contentColor = TextPrimary
            )
        ) {
            Text(text = "Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.width(10.dp))
        confirmButton()
    }
}

// Colors shared by all dialog text fields
@Composable
private fun dialogFieldColors(
    container: Color = badgeGrey,
    border: Color = sectionBorder,
    focusedBorder: Color = Primary,
    text: Color = TextPrimary
) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = container,
    unfocusedContainerColor = container,
    focusedBorderColor = focusedBorder,
    unfocusedBorderColor = border,
    focusedTextColor = text,
    unfocusedTextColor = text,
    cursorColor = focusedBorder
)

// ─────────────────────────────────────────────────────────────
// Dialog 1: Complete Stage
// ─────────────────────────────────────────────────────────────

@Composable
private fun CompleteStageDialog(
    stageName: String,
    estimateLabel: String,
    jobCardCode: String,
    workerName: String,
    totalPieces: Int,
    onDismiss: () -> Unit,
    onConfirm: (completed: Int, defective: Int, dateTime: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var completed by remember { mutableStateOf(totalPieces.toString()) }
    var defective by remember { mutableStateOf("0") }
    val dateTime = remember {
        SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH).format(Date())
    }
    val completedValue = completed.toIntOrNull() ?: 0
    val canConfirm = completedValue > 0

    StageDialogContainer(onDismiss = onDismiss) {
        StageDialogHeader(
            icon = Icons.Outlined.CheckCircle,
            iconTint = greentext,
            title = "Complete Stage | $stageName",
            subtitle = "Estimated Standard: ",
            subtitleHighlight = estimateLabel,
            onClose = onDismiss
        )

        JobInfoStrip(jobCardCode = jobCardCode, workerName = workerName)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Completed Pieces *",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = textSubdued
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = completed,
                        onValueChange = { v -> completed = v.filter { it.isDigit() } },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = dialogFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Total in Job Card: $totalPieces pcs",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Defective / Scrap Pieces",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = textSubdued,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = defective,
                        onValueChange = { v -> defective = v.filter { it.isDigit() } },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = dialogFieldColors(text = redText),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Damaged/Rework pieces",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }

            Column {
                Text(
                    text = "Completion Date & Time *",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textSubdued
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = dateTime,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = dialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        StageDialogFooter(onCancel = onDismiss) {
            Button(
                onClick = {
                    onConfirm(completedValue, defective.toIntOrNull() ?: 0, dateTime)
                },
                enabled = canConfirm,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = chartGreenDeep,
                    contentColor = whiteBg,
                    disabledContainerColor = disabled,
                    disabledContentColor = whiteBg
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Confirm Stage Handover",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Dialog 2: Put Stage On Hold
// ─────────────────────────────────────────────────────────────

@Composable
private fun HoldStageDialog(
    jobCardCode: String,
    workerName: String,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var reason by remember { mutableStateOf("") }

    StageDialogContainer(onDismiss = onDismiss) {
        StageDialogHeader(
            icon = Icons.Outlined.PauseCircle,
            iconTint = yellowText,
            title = "Put Stage On Hold",
            subtitle = "Specify reason for pausing the bench",
            onClose = onDismiss
        )

        JobInfoStrip(jobCardCode = jobCardCode, workerName = workerName)

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = "Why is this stage being put on hold? *",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = textSubdued
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                minLines = 3,
                shape = RoundedCornerShape(10.dp),
                placeholder = {
                    Text(
                        text = "E.g. Awaiting client trial measurement clarification, thread shade mismatch...",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                },
                colors = dialogFieldColors(
                    container = yellowBg,
                    border = chartAmber.copy(alpha = 0.4f),
                    focusedBorder = chartAmber
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        StageDialogFooter(onCancel = onDismiss) {
            Button(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank(),
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = yellowText,
                    contentColor = whiteBg,
                    disabledContainerColor = orangeBg,
                    disabledContentColor = whiteBg
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Confirm Hold",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Dialog 3: Report Shop-Floor Issue
// ─────────────────────────────────────────────────────────────

@Composable
private fun ReportIssueDialog(
    stageName: String,
    jobCardCode: String,
    workerName: String,
    onDismiss: () -> Unit,
    onConfirm: (issueType: String, description: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val categories = listOf(
        "Machine_Breakdown",
        "Fabric_Damage",
        "Cutting_Mistake",
        "Customer_Mistake"
    )
    var selectedType by remember { mutableStateOf(categories.first()) }
    var description by remember { mutableStateOf("") }

    StageDialogContainer(onDismiss = onDismiss) {
        StageDialogHeader(
            icon = Icons.Outlined.Error,
            iconTint = chartRose,
            title = "Report Shop-Floor Issue | $stageName",
            subtitle = "Logged directly to job card issues register",
            onClose = onDismiss
        )

        JobInfoStrip(jobCardCode = jobCardCode, workerName = workerName)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Issue Category *",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textSubdued
                )
                Spacer(Modifier.height(8.dp))

                // Two-column grid of selectable categories
                categories.chunked(2).forEachIndexed { rowIdx, rowItems ->
                    if (rowIdx > 0) Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { type ->
                            val isSelected = type == selectedType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) roseBg else whiteBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) chartRose.copy(alpha = 0.6f) else sectionBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedType = type }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = prettyStatus(type),
                                    fontSize = tokens.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) redText else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Column {
                Text(
                    text = "Issue Description *",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textSubdued
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    minLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    placeholder = {
                        Text(
                            text = "Detail the issue (e.g. Needle broke on organza)...",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    },
                    colors = dialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        StageDialogFooter(onCancel = onDismiss) {
            Button(
                onClick = { onConfirm(selectedType, description.trim()) },
                enabled = description.isNotBlank(),
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = chartRose,
                    contentColor = whiteBg,
                    disabledContainerColor = redBg,
                    disabledContentColor = whiteBg
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Submit Floor Issue",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}