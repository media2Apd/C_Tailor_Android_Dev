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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import com.cuso.tailor.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TimePickerField
import com.cuso.tailor.view.composable.TitleBar
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

private fun alertStyle(type: String?, message: String? = null): AlertStyle {
    val t = (type ?: "").lowercase()
    val m = (message ?: "").lowercase()
    return when {
        // Red: Floor issues, machine breakdown, fabric damage, error
        t.contains("error") || t.contains("issue") || t.contains("critical") ||
                m.contains("floor issue") || m.contains("breakdown") || m.contains("damage") ->
            AlertStyle(roseBg, chartRose, activity_brown, Icons.Outlined.Error)

        // Green: Workflow stage completions
        t.contains("complete") || t.contains("success") ||
                m.contains("completed") || m.contains("ahead of schedule") ->
            AlertStyle(activity_green_bg, complete_button_bg, activity_green, Icons.Outlined.CheckCircle)

        // Amber: High priority orders, delivery overdues, delays, holds
        t.contains("warn") || t.contains("overdue") || t.contains("delay") || t.contains("hold") ||
                m.contains("overdue") || m.contains("priority") || m.contains("delivery") ->
            AlertStyle(yellowBg, chartAmber, activity_brown, Icons.Default.Warning)

        // Blue: Client trial appointments, info updates, confirmations
        else ->
            AlertStyle(activity_purple_bg, chartBlue, darkPurple, Icons.Default.Info)
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
    onVerifyClick: (orderId: String) -> Unit = {},
    // Opens the assignment screen from the 3-dot menu
    onGoToAssignmentClick: (orderId: String, jobCardId: String) -> Unit = { _, _ -> }
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val orderDetail by viewModel.serviceOrderDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingOrderDetail.collectAsStateWithLifecycle()
    val errorMessage by viewModel.orderDetailError.collectAsStateWithLifecycle()

    var selectedJobCardIndex by remember { mutableIntStateOf(0) }

    // Controls the 3-dot dropdown menu in the order header
    var assignmentMenuOpen by remember { mutableStateOf(false) }

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

    // Dynamic Activity & Alert data class
    data class ProductionAlertItem(
        val message: String,
        val time: String,
        val tag: String?,
        val type: String,
        val rawTimestamp: String
    )

    // Pulls all dynamic activities, job card issues, and workflow logs from API response
    val dynamicAlerts = remember(detail, activeJobCard) {
        val items = mutableListOf<ProductionAlertItem>()

        // 1. Order-level activities & alerts from backend
        val apiLogs = detail.extractList("activityLogs", "activities", "activityAlerts", "alerts", "timeline", "auditLogs")
        apiLogs.forEach { log ->
            val msg = log.extractString("message", "title", "description", "action", "text", "event", "note") ?: ""
            val rawTime = log.extractString("createdAt", "timestamp", "date", "time", "updatedAt") ?: ""
            val tag = log.extractString("tag", "stageName", "category", "stage", "label", "source")
            val type = log.extractString("type", "severity", "level", "status", "alertType") ?: "info"

            if (msg.isNotBlank()) {
                items.add(
                    ProductionAlertItem(
                        message = msg,
                        time = formatAlertTime(rawTime),
                        tag = tag,
                        type = type,
                        rawTimestamp = rawTime
                    )
                )
            }
        }

        // 2. Active Job Card reported floor issues from backend
        val jobIssues = activeJobCard.extractList("issues", "jobCardIssues", "floorIssues")
        jobIssues.forEach { issue ->
            val issueType = issue.extractString("issueType", "category", "type") ?: "Floor Alert"
            val desc = issue.extractString("description", "notes", "message") ?: ""
            val rawTime = issue.extractString("createdAt", "timestamp", "date") ?: ""
            val formattedMsg = if (desc.isNotBlank()) "Floor Issue: ${prettyStatus(issueType)} — \"$desc\"" else "Floor Issue: ${prettyStatus(issueType)}"

            items.add(
                ProductionAlertItem(
                    message = formattedMsg,
                    time = formatAlertTime(rawTime),
                    tag = issue.extractString("tag") ?: "Floor Alert",
                    type = "error",
                    rawTimestamp = rawTime
                )
            )
        }

        // 3. Job Card specific activity logs if distinct from order
        val jobCardLogs = activeJobCard.extractList("activityLogs", "activities", "timeline")
        jobCardLogs.forEach { log ->
            val msg = log.extractString("message", "title", "description", "action", "text") ?: ""
            val rawTime = log.extractString("createdAt", "timestamp", "date") ?: ""
            val tag = log.extractString("tag", "stageName", "stage", "category")
            val type = log.extractString("type", "status", "severity") ?: "info"

            if (msg.isNotBlank() && items.none { it.message == msg && it.rawTimestamp == rawTime }) {
                items.add(
                    ProductionAlertItem(
                        message = msg,
                        time = formatAlertTime(rawTime),
                        tag = tag,
                        type = type,
                        rawTimestamp = rawTime
                    )
                )
            }
        }

        items
    }

    val priorityChip = priorityStyle(priority)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Top header ──
        Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {

            // Row 1: back arrow, order code with chips, overflow menu (all vertically centered)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleBar(title = "Production Details", onClose = onBack)
            }

        }

        // ── Scrollable body ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card: order row (code, chips, Verify, menu) and summary row below it
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(whiteBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(12.dp))
            ) {
                // Row 1: order code with chips on the left, Verify button and 3-dot menu at the end
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = orderCode,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
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

                        Spacer(Modifier.height(6.dp))

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

                    Spacer(Modifier.width(8.dp))

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
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
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

                    // 3-dot button with its dropdown anchored to the same Box
                    Box {
                        IconButton(
                            onClick = { assignmentMenuOpen = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = iconMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = assignmentMenuOpen,
                            onDismissRequest = { assignmentMenuOpen = false },
                            // Small gap between the 3-dot button and the popup
                            offset = DpOffset(x = 0.dp, y = 4.dp),
                            // Rounded card look with a thin border and a soft shadow
                            shape = RoundedCornerShape(10.dp),
                            containerColor = whiteBg,
                            tonalElevation = 0.dp,
                            shadowElevation = 4.dp,
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            DropdownMenuItem(
                                // Fixed compact height with small top and bottom padding
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                text = {
                                    Text(
                                        text = "Go to assignment mode",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    // Ruler icon, same as the reference design
                                    Icon(
                                        painter = painterResource(R.drawable.ic_ruler),
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    assignmentMenuOpen = false
                                    onGoToAssignmentClick(orderId, activeJobCardId)
                                }
                            )
                        }
                    }
                }

                // Thin inset divider between the order row and the summary row
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    color = PrimaryBorder
                )

                // Row 2: customer on the left, delivery and garments aligned on the right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Customer name and phone (takes the remaining width)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customerName,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
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
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = deliveryDate,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
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
                        Spacer(Modifier.height(2.dp))
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
                }
            }
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

            // Dynamic Activity & Alerts list matching the screenshots
            if (dynamicAlerts.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activity & Alerts",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )
                        Text(
                            text = "${dynamicAlerts.size} Total",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }

                    dynamicAlerts.forEach { alert ->
                        val style = alertStyle(alert.type, alert.message)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .clip(RoundedCornerShape(8.dp))
                                .background(style.bg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left vertical accent bar
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(style.accent)
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = style.icon,
                                    contentDescription = null,
                                    tint = style.accent,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(verticalArrangement = Arrangement.Center) {
                                    Text(
                                        text = alert.message,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = style.text,
                                        lineHeight = 18.sp
                                    )
                                    if (alert.time.isNotBlank() || !alert.tag.isNullOrBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        val subLabel = buildString {
                                            if (alert.time.isNotBlank()) append(alert.time)
                                            if (!alert.tag.isNullOrBlank()) {
                                                if (isNotEmpty()) append(" • ")
                                                append(alert.tag)
                                            }
                                        }
                                        Text(
                                            text = subLabel,
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = style.accent
                                        )
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
        val stg = targetStage!!
        val stageId = stg.extractString("_id", "id", "stageId") ?: ""
        val stageName = stg.extractString("stageName", "name") ?: "Stage"
        val estimateMins = stg.extractInt("estimatedMinutes", default = 0)
        val estimateLabel = if (estimateMins > 0) formatMinutes(estimateMins) else "0 mins"

        CompleteStageDialog(
            stageName = stageName,
            estimateLabel = estimateLabel,
            jobCardCode = jobCardCode,
            workerName = stageWorkerName(stg),
            totalPieces = quantity,
            onDismiss = { completeModalOpen = false },
            onConfirm = { completedQty, defectiveQty, _ ->
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
            }
        )
    }

    // Modal 2: Put Stage On Hold
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

    // Modal 3: Report Shop-Floor Issue
    if (issueModalOpen && targetStage != null) {
        val stg = targetStage!!
        val stageName = stg.extractString("stageName", "name") ?: "Stage"

        ReportIssueDialog(
            stageName = stageName,
            jobCardCode = jobCardCode,
            workerName = stageWorkerName(stg),
            onDismiss = { issueModalOpen = false },
            onConfirm = { issueType, description ->
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
// Shared dialog building blocks using AppDesignTokens & Theme Colors
// ─────────────────────────────────────────────────────────────

// Extracts the worker full name from stage allocations
private fun stageWorkerName(stage: Any): String {
    val workerObj = stage.extractList("allocations").firstOrNull()?.extractObject("workerId")
    return workerObj?.let {
        "${it.extractString("firstName") ?: ""} ${it.extractString("lastName") ?: ""}".trim()
    }?.takeIf { it.isNotBlank() } ?: "Unassigned"
}

// Rounded dialog container adhering to tokens.cardCornerRadius and whiteBg
@Composable
private fun StageDialogContainer(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                content = content
            )
        }
    }
}

// Header with icon, title, subtitle, and close button
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
            .padding(
                start = tokens.cardPadding,
                end = tokens.extraPadding,
                top = tokens.cardPadding,
                bottom = tokens.extraPadding
            ),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(tokens.iconSize)
        )
        Spacer(Modifier.width(tokens.extraPadding))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = buildAnnotatedString {
                    append(subtitle)
                    if (subtitleHighlight != null) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = textSubdued)) {
                            append(subtitleHighlight)
                        }
                    }
                },
                fontSize = tokens.caption,
                color = headerGrey
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = headerGrey,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    HorizontalDivider(color = grey_border)
}

// Top strip displaying Job Card ID and Assigned Artisan cards
@Composable
private fun JobInfoStrip(jobCardCode: String, workerName: String) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Forces both cards to match the maximum intrinsic height
            .padding(horizontal = tokens.cardPadding, vertical = tokens.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Job Card ID card (fills height and centers content vertically)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(whiteBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "JOB CARD ID",
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = iconMuted
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = jobCardCode.ifBlank { "-" },
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Assigned Worker card (fills height and centers content vertically)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(whiteBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(primary_light),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = workerName.firstOrNull()?.uppercase() ?: "U",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "ASSIGNED",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = iconMuted
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = workerName,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Dialog footer with Cancel button and primary action button
@Composable
private fun StageDialogFooter(
    onCancel: () -> Unit,
    confirmButton: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current

    HorizontalDivider(color = grey_border)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.cardPadding, vertical = 14.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.height(tokens.buttonHeight),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, sectionBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = whiteBg,
                contentColor = textSubdued
            ),
            contentPadding = PaddingValues(horizontal = 18.dp)
        ) {
            Text(
                text = "Cancel",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.width(tokens.extraPadding))
        confirmButton()
    }
}

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
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (completed: Int, defective: Int, dateTime: String) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Quantity states
    var completed by remember { mutableStateOf(totalPieces.toString()) }
    var defective by remember { mutableStateOf("0") }

    // Separate date and time states initialized with current timestamp
    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()))
    }
    var selectedTime by remember {
        mutableStateOf(SimpleDateFormat("hh:mm a", Locale.US).format(Date()))
    }

    // Submission loading state
    var isSubmitting by remember { mutableStateOf(false) }
    val showLoading = isLoading || isSubmitting

    val completedValue = completed.toIntOrNull() ?: 0
    val canConfirm = completedValue > 0 && selectedDate.isNotBlank() && selectedTime.isNotBlank()

    StageDialogContainer(onDismiss = { if (!showLoading) onDismiss() }) {
        // Header
        StageDialogHeader(
            icon = Icons.Outlined.CheckCircle,
            iconTint = darkGreenBg,
            title = "Complete Stage | $stageName",
            subtitle = "Estimated Standard: ",
            subtitleHighlight = estimateLabel,
            onClose = { if (!showLoading) onDismiss() }
        )

        // Job Card & Assigned Worker Info Strip
        JobInfoStrip(jobCardCode = jobCardCode, workerName = workerName)

        Column(
            modifier = Modifier.padding(
                start = tokens.cardPadding,
                end = tokens.cardPadding,
                bottom = tokens.cardPadding
            ),
            verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
        ) {
            // Row 1: Completed Pieces and Defective Pieces inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Completed Pieces box
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Completed Pieces *",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = textSubdued
                    )
                    Spacer(Modifier.height(6.dp))
                    BasicTextField(
                        value = completed,
                        onValueChange = { v -> completed = v.filter { it.isDigit() } },
                        singleLine = true,
                        enabled = !showLoading,
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            fontSize = tokens.bodyLarge,
                            color = TextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .background(whiteBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, sectionBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Total in Job Card: $totalPieces pcs",
                        fontSize = tokens.caption,
                        color = iconMuted
                    )
                }

                // Defective Pieces box
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Defective / Scrap",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = textSubdued
                    )
                    Spacer(Modifier.height(6.dp))
                    BasicTextField(
                        value = defective,
                        onValueChange = { v -> defective = v.filter { it.isDigit() } },
                        singleLine = true,
                        enabled = !showLoading,
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            fontSize = tokens.bodyLarge,
                            color = redText
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .background(whiteBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, sectionBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Damaged/Rework pieces",
                        fontSize = tokens.caption,
                        color = iconMuted
                    )
                }
            }

            // Row 2: Completion Date and Completion Time side-by-side pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Completion Date Field
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Completion Date *",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = textSubdued
                    )
                    Spacer(Modifier.height(6.dp))
                    DatePickerField(
                        value = selectedDate,
                        onDateSelected = { selectedDate = it },
                        enabled = !showLoading
                    )
                }

                // Completion Time Field
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Completion Time *",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = textSubdued
                    )
                    Spacer(Modifier.height(6.dp))
                    TimePickerField(
                        value = selectedTime,
                        onTimeSelected = { selectedTime = it },
                        enabled = !showLoading
                    )
                }
            }
        }

        // Dialog Footer with loader
        StageDialogFooter(onCancel = { if (!showLoading) onDismiss() }) {
            Button(
                onClick = {
                    isSubmitting = true
                    onConfirm(completedValue, defective.toIntOrNull() ?: 0, "$selectedDate $selectedTime")
                },
                enabled = canConfirm && !showLoading,
                modifier = Modifier.height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkGreenBg,
                    contentColor = whiteBg,
                    disabledContainerColor = greenBg,
                    disabledContentColor = whiteBg
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                if (showLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = whiteBg,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = if (showLoading) "Submitting..." else "Confirm Stage Handover",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
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
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var reason by remember { mutableStateOf("") }

    // Submission loading state
    var isSubmitting by remember { mutableStateOf(false) }
    val showLoading = isLoading || isSubmitting

    StageDialogContainer(onDismiss = { if (!showLoading) onDismiss() }) {
        StageDialogHeader(
            icon = Icons.Default.Pause,
            iconTint = yellowText,
            title = "Put Stage On Hold",
            subtitle = "Specify reason for pausing the bench",
            onClose = { if (!showLoading) onDismiss() }
        )

        JobInfoStrip(jobCardCode = jobCardCode, workerName = workerName)

        Column(modifier = Modifier.padding(start = tokens.cardPadding, end = tokens.cardPadding, bottom = tokens.cardPadding)) {
            Text(
                text = "Why is this stage being put on hold? *",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = textSubdued
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                minLines = 4,
                enabled = !showLoading,
                shape = RoundedCornerShape(8.dp),
                placeholder = {
                    Text(
                        text = "E.g. Awaiting client trial measurement clarification, thread shade mismatch...",
                        fontSize = tokens.bodySmall,
                        color = iconMuted
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = whiteBg,
                    unfocusedContainerColor = whiteBg,
                    focusedBorderColor = chartAmber,
                    unfocusedBorderColor = chartAmber
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        StageDialogFooter(onCancel = { if (!showLoading) onDismiss() }) {
            Button(
                onClick = {
                    isSubmitting = true
                    onConfirm(reason)
                },
                enabled = reason.isNotBlank() && !showLoading,
                modifier = Modifier.height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = yellowText,
                    contentColor = whiteBg,
                    disabledContainerColor = orangeBg,
                    disabledContentColor = whiteBg
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                if (showLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = whiteBg,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = if (showLoading) "Holding..." else "Confirm Hold",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
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
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (issueType: String, description: String) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Matches the React chip items and exact backend payload keys
    val categories = listOf(
        "Machine_Breakdown" to "Machine Breakdown",
        "Fabric_Damage" to "Fabric Damage",
        "Wrong_Cutting" to "Cutting Mistake",
        "Customer_Mistake" to "Customer Mistake"
    )
    var selectedType by remember { mutableStateOf(categories.first().first) }
    var description by remember { mutableStateOf("") }

    // Submission loading state
    var isSubmitting by remember { mutableStateOf(false) }
    val showLoading = isLoading || isSubmitting

    StageDialogContainer(onDismiss = { if (!showLoading) onDismiss() }) {
        // Header
        StageDialogHeader(
            icon = Icons.Outlined.Error,
            iconTint = chartRose,
            title = "Report Shop-Floor Issue | $stageName",
            subtitle = "Logged directly to job card issues register",
            onClose = { if (!showLoading) onDismiss() }
        )

        // Job Card & Assigned Worker Info Strip
        JobInfoStrip(jobCardCode = jobCardCode, workerName = workerName)

        Column(
            modifier = Modifier.padding(
                start = tokens.cardPadding,
                end = tokens.cardPadding,
                bottom = tokens.cardPadding
            ),
            verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
        ) {
            // Category Selection (2x2 Grid)
            Column {
                Text(
                    text = "Issue Category *",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = textSubdued
                )
                Spacer(Modifier.height(6.dp))

                categories.chunked(2).forEachIndexed { index, pair ->
                    if (index > 0) Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { (key, label) ->
                            val isSelected = selectedType == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) roseBg else whiteBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) chartRose.copy(alpha = 0.5f) else sectionBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = !showLoading) { selectedType = key }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = label,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) redText else textSubdued,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Description Input
            Column {
                Text(
                    text = "Issue Description *",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = textSubdued
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    minLines = 4,
                    enabled = !showLoading,
                    shape = RoundedCornerShape(8.dp),
                    placeholder = {
                        Text(
                            text = "Detail the issue (e.g. Needle broke on organza)...",
                            fontSize = tokens.bodySmall,
                            color = iconMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = whiteBg,
                        unfocusedContainerColor = whiteBg,
                        focusedBorderColor = sectionBorder,
                        unfocusedBorderColor = sectionBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Footer with loader
        StageDialogFooter(onCancel = { if (!showLoading) onDismiss() }) {
            Button(
                onClick = {
                    isSubmitting = true
                    onConfirm(selectedType, description.trim())
                },
                enabled = description.isNotBlank() && !showLoading,
                modifier = Modifier.height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = chartRose,
                    contentColor = whiteBg,
                    disabledContainerColor = redBg,
                    disabledContentColor = whiteBg
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                if (showLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = whiteBg,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = if (showLoading) "Submitting..." else "Submit Floor Issue",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}