@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.services.service_order.production_work

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.viewmodel.ServicesViewModel

// ─────────────────────────────────────────────────────────────
// Shared Reflection Helpers
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
// Screen Composable
// ─────────────────────────────────────────────────────────────

@Composable
fun QualityDeliveryDispatchScreen(
    orderId: String,
    viewModel: ServicesViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onBackToOrders: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val orderDetail by viewModel.serviceOrderDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingOrderDetail.collectAsStateWithLifecycle()

    var selectedJobCardIndex by remember { mutableIntStateOf(0) }
    var isOrderClosed by remember { mutableStateOf(false) }

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

    val detail = orderDetail
    val orderHeader = detail?.extractObject("orderHeader") ?: detail

    val orderStatus = orderHeader?.extractString("status") ?: ""
    LaunchedEffect(orderStatus) {
        if (orderStatus == "Ready_for_Delivery" || orderStatus == "Delivered") {
            isOrderClosed = true
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Data Extraction (Declared in correct sequential order)
    // ─────────────────────────────────────────────────────────────
    val jobCards = detail?.extractList("jobCards") ?: emptyList()
    val activeJobCard = jobCards.getOrNull(selectedJobCardIndex) ?: jobCards.firstOrNull() ?: detail
    val activeJobCardId = activeJobCard?.extractString("_id", "id", "jobCardId") ?: ""
    val stages = activeJobCard?.extractList("stages", "workflowStages") ?: emptyList()

    val currentStage = stages.firstOrNull {
        it.extractString("status") != "Completed"
    } ?: stages.lastOrNull()
    val currentStageId = currentStage?.extractString("_id", "id", "stageId") ?: ""

    val allJobCardsDone = jobCards.isNotEmpty() && jobCards.all {
        it.extractString("status")?.equals("Completed", ignoreCase = true) == true
    }

    val customerObj = orderHeader?.extractObject("customer", "customerSnapshot")
    val customerName = customerObj?.extractString("name", "customerName") ?: "-"

    val paymentObj = orderHeader?.extractObject("payment")
    val balanceAmount = paymentObj?.extractInt("balanceAmount", default = 0) ?: 0
    val isPaid = balanceAmount <= 0 || paymentObj?.extractString("paymentStatus") == "Paid"

    val isReadyForDispatch = allJobCardsDone && isPaid

    // ─────────────────────────────────────────────────────────────
    // Success View: Order Closed State
    // ─────────────────────────────────────────────────────────────
    if (isOrderClosed) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
                .padding(tokens.screenPadding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 1.5f),
                colors = CardDefaults.cardColors(containerColor = whiteBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(greenBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = greentext,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Order Closed",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Order has been successfully verified & completed.",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )

                    Spacer(Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(greenBg)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChecklistItem(text = "All Garments Production Completed")
                        ChecklistItem(text = "Customer Trial Verified")
                        ChecklistItem(text = "Workshop Rework Cleared")
                        ChecklistItem(text = "Master Quality Check Passed")
                        ChecklistItem(text = "Ready for Customer Dispatch")
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onBackToOrders,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    ) {
                        Text(text = "Back to Orders", color = whiteBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // ─────────────────────────────────────────────────────────────
    // Active Inspection & Dispatch View
    // ─────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text = "QC & Delivery Dispatch",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${jobCards.size} Garment(s) • $customerName",
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }
        }

        // Multi-Garment Switcher
        if (jobCards.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(jobCards) { idx, card ->
                    val isSelected = idx == selectedJobCardIndex
                    val cardGarmentName = card.extractObject("garment")?.extractString("name") ?: "Item ${idx + 1}"
                    val isDone = card.extractString("status")?.equals("Completed", ignoreCase = true) == true

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) primary_light else whiteBg)
                            .border(1.dp, if (isSelected) Primary else title_border, RoundedCornerShape(8.dp))
                            .clickable { selectedJobCardIndex = idx }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isDone) "✓ $cardGarmentName" else cardGarmentName,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Primary else TextPrimary
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quality Check Inspection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = greentext)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Quality Check Inspection", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold)
                    }

                    stages.forEach { stg ->
                        val stageName = stg.extractString("stageName", "name") ?: "Stage"
                        val isStgDone = stg.extractString("status")?.equals("Completed", ignoreCase = true) == true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isStgDone) greenBg else Primary_background)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isStgDone) Icons.Default.CheckCircle else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isStgDone) greentext else mutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(text = stageName, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                text = if (isStgDone) "Passed" else "Pending",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = if (isStgDone) greentext else orangeText
                            )
                        }
                    }
                }
            }

            // Delivery Details & Pre-Delivery Checklist Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Pre-Delivery Checklist", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold)
                    ChecklistItem(
                        text = "All Garments Completed (${jobCards.count { it.extractString("status") == "Completed" }}/${jobCards.size})",
                        checked = allJobCardsDone
                    )
                    ChecklistItem(text = "Customer Trial Stage Verified", checked = true)
                    ChecklistItem(
                        text = "Payment Settled (${if (isPaid) "Fully Paid" else "Balance Due"})",
                        checked = isPaid
                    )
                }
            }

            // Move to Dispatch Button
            Button(
                onClick = {
                    viewModel.updateStageProgress(
                        jobCardId = activeJobCardId,
                        stageId = currentStageId,
                        status = "Completed",
                        notes = "Stage fully completed and verified for delivery",
                        onSuccess = {
                            isOrderClosed = true
                            Toast.makeText(context, "Order Ready for Delivery & Closed!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = isReadyForDispatch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReadyForDispatch) Primary else light_grey,
                    contentColor = if (isReadyForDispatch) whiteBg else mutedText
                ),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text = "Mark Ready for Delivery & Close Order", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChecklistItem(text: String, checked: Boolean = true) {
    val tokens = LocalAppTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (checked) greentext else mutedText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = if (checked) greentext else mutedText
        )
    }
}