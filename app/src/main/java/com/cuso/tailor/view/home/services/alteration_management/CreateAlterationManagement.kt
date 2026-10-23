@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

package com.cuso.tailor.view.home.services.alteration_management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ViewAlterationManagementScreen(
    alterationId: String,
    viewModel: ServicesViewModel,
    onClose: () -> Unit
) {
    val tokens: AppDesignTokens = LocalAppTokens.current
    val alterationData by viewModel.alterationDetail.collectAsState()
    val isLoading by viewModel.isLoadingAlterationDetail.collectAsState()
    val errorMessage by viewModel.alterationDetailError.collectAsState()
    val isDelivering by viewModel.isDelivering.collectAsState()
    val isSendingComment by viewModel.isSendingComment.collectAsState()

    var auditComment by remember { mutableStateOf("") }

    LaunchedEffect(alterationId) {
        if (alterationId.isNotBlank()) {
            viewModel.fetchAlterationDetail(alterationId)
        }
    }

    val request = alterationData?.serviceRequest
    val firstItem = request?.items?.firstOrNull()

    val ticketCode = request?.serviceRequestCode ?: "-—"
    val garmentSource = request?.garmentSource?.replace("_", " ") ?: ""
    val status = request?.status ?: ""
    val priority = request?.priority ?: ""
    val customerName = request?.customerSnapshot?.name
        ?: request?.customer?.fullName
        ?: ""
    val customerPhone = request?.customerSnapshot?.phone
        ?: request?.customer?.mobileNumber
        ?: "—"

    val garmentTitle: String = firstItem?.garmentName?.takeIf { it.isNotBlank() }
        ?: firstItem?.garmentDescription?.takeIf { it.isNotBlank() }
        ?: "—"

    val categoryName: String = firstItem?.garmentCategoryName?.takeIf { it.isNotBlank() }
        ?: firstItem?.segmentName?.takeIf { it.isNotBlank() }
        ?: "—"

    val issueInstructions: String = firstItem?.issueSummary?.takeIf { it.isNotBlank() }
        ?: request?.internalNotes?.takeIf { it.isNotBlank() }
        ?: "—"

    val quantity: Int = firstItem?.quantity ?: 1

    val deliveryDate: String = formatIsoDate(request?.deliveryDate)
    val attachments = request?.attachments.orEmpty()

    val serviceTotal: Double = firstItem?.serviceCharge ?: 0.0
    val advancePaid: Double = request?.advanceAmountPaid ?: 0.0
    val balanceAmount: Double = request?.balanceAmount ?: 0.0
    val paymentMode: String = request?.paymentMode ?: "N/A"
    val paymentStatus: String = request?.paymentStatus?.replace("_", " ") ?: "Not Applicable"

    val createdDateFormatted: String = formatAuditDate(request?.createdAt)
    val creatorName: String = "${request?.createdBy?.firstName.orEmpty()} ${request?.createdBy?.lastName.orEmpty()}".trim()
        .ifEmpty { "" }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(title ="Alteration Detail", onClose = onClose)
        }
    ) { padding ->
        if (isLoading) {
            ListSkeleton()
        } else if (!errorMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Failed to load",
                    color = redText,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // ─────────────────────────────────────────────
                // 1. TOP HEADER SUMMARY CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ticketCode,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.6f))

                            Text(
                                text = garmentSource,
                                fontSize = tokens.bodySmall,
                                color = headerGrey,
                                fontWeight = FontWeight.Normal
                            )

                            Spacer(Modifier.weight(1f))

                            //Status Pill
                            StatusBadge(
                                text = status,
                                textColor = if (status.equals("Cancelled", true)) BluePrimary else greentext,
                                backgroundColor = if (status.equals("Cancelled", true)) light_blue else greenBg,
                            )

                            Spacer(Modifier.width(tokens.extraPadding * 0.5f))

                            // Priority Pill
                            StatusBadge(
                                text = priority,
                                textColor = redText,
                                backgroundColor = redBg
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.7f))

                        // Customer Line
                        Row(
                            modifier = Modifier.padding(start = tokens.buttonHeight * 0.8f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Customer: ",
                                fontSize = tokens.bodySmall,
                                color = headerGrey
                            )
                            Text(
                                text = customerName,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            Text(
                                text = " ($customerPhone)",
                                fontSize = tokens.bodySmall,
                                color = headerGrey
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Full-width Mark as Delivered Button
                        val isAlreadyDelivered = status.equals("Delivered", ignoreCase = true)

                        Button(
                            onClick = {
                                if (!isAlreadyDelivered && !isDelivering) {
                                    viewModel.markAlterationAsDelivered(
                                        id = alterationId,
                                        onSuccess = { /* Status updated & UI refreshed automatically */ },
                                        onError = { /* Handle error toast or message */ }
                                    )
                                }
                            },
                            enabled = !isAlreadyDelivered && !isDelivering,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAlreadyDelivered) greentext else Primary,
                                disabledContainerColor = if (isAlreadyDelivered) greentext else disabled
                            )
                        ) {
                            if (isDelivering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(tokens.iconSize),
                                    color = whiteBg,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.7f))
                                Text(
                                    text = if (isAlreadyDelivered) "Delivered" else "Mark as Delivered",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 2. LIVE WORKFLOW PROGRESS CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIVE ALTERATION WORKFLOW PROGRESS",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        Spacer(Modifier.height(tokens.screenPadding))

                        // Two-step horizontal line stepper
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step 1: Requested (Checked Green)
                            Box(
                                modifier = Modifier
                                    .size(tokens.iconSize * 1.5f)
                                    .clip(CircleShape)
                                    .background(greentext),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                                )
                            }

                            // Connecting line
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = BorderGray,
                                thickness = 2.dp
                            )

                            // Step 2: Delivered
                            Box(
                                modifier = Modifier
                                    .size(tokens.iconSize * 1.5f)
                                    .clip(CircleShape)
                                    .border(1.5.dp, BorderGray, CircleShape)
                                    .background(whiteBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "2",
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.7f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.extraPadding * 0.8f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Requested",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            Text(
                                text = "Delivered",
                                fontSize = tokens.bodySmall,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 3. GARMENT & ALTERATION NOTES CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "GARMENT #1 & ALTERATION NOTES",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                            Text(
                                text = "Qty: $quantity pc(s)",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = headerGrey
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = tokens.extraPadding),
                            color = dividerColor
                        )

                        Text(
                            text = "GARMENT TITLE",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )
                        Text(
                            text = garmentTitle,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        Text(
                            text = "CATEGORY / CUT PROFILE",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )
                        Text(
                            text = categoryName,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        Text(
                            text = "ISSUE / MASTER INSTRUCTIONS",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(light_blue)
                                .padding(tokens.extraPadding)
                        ) {
                            Text(
                                text = "\"$issueInstructions\"",
                                fontSize = tokens.bodySmall,
                                color = title_color,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 4. MEASUREMENT ADJUSTMENTS CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MEASUREMENT ADJUSTMENTS (GARMENT #1)",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("POINT", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.weight(1.3f))
                            Text("ORIGINAL", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("ALTERED", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("DIFF / ACTION", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = tokens.extraPadding * 0.7f),
                            color = dividerColor
                        )

                        val alterations = firstItem?.alterations.orEmpty()
                        if (alterations.isEmpty()) {
                            Text(
                                text = "No adjustments specified",
                                fontSize = tokens.bodySmall,
                                color = mutedText,
                                modifier = Modifier.padding(vertical = tokens.extraPadding * 0.4f)
                            )
                        } else {
                            alterations.forEach { alt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = tokens.extraPadding * 0.4f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alt.targetArea.replace("_", " "),
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = title_color,
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    Text(
                                        text = "${alt.currentValue ?: "—"} ${alt.unit.orEmpty()}",
                                        fontSize = tokens.bodySmall,
                                        color = title_color,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "${alt.targetValue ?: "—"} ${alt.unit.orEmpty()}",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Primary,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "${alt.action ?: "Adjust"} (${alt.value ?: "—"} ${alt.unit.orEmpty()})",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = redText,
                                        modifier = Modifier.weight(1.3f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 5. WORKSHOP STAGE ARTISANS CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "WORKSHOP STAGE ARTISANS",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                            Text(
                                text = "0 Stages",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = tokens.extraPadding),
                            color = dividerColor
                        )

                        Text(
                            text = "No workshop stages allocated.",
                            fontSize = tokens.bodySmall,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // ─────────────────────────────────────────────
                // 6. PROMISED DELIVERY & REFERENCE EVIDENCE CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PROMISED DELIVERY & REFERENCE EVIDENCE",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Promised Delivery Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(tokens.extraPadding)
                        ) {
                            Column {
                                Text(
                                    text = "PROMISED DELIVERY DATE",
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.Medium,
                                    color = mutedText
                                )
                                Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                                Text(
                                    text = deliveryDate,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                        }

                        if (attachments.isNotEmpty()) {
                            Spacer(Modifier.height(tokens.extraPadding))
                            Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                                attachments.forEach { evidence ->
                                    Box(
                                        modifier = Modifier
                                            .size(tokens.imageDp * 1.3f)
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    ) {
                                        AsyncImage(
                                            model = evidence.url,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 7. FINANCIAL SETTLEMENT CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "FINANCIAL SETTLEMENT",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.3f),
                                color = redBg
                            ) {
                                Text(
                                    text = paymentStatus,
                                    fontSize = tokens.label,
                                    color = redText,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = tokens.extraPadding * 0.6f, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        FinancialRow(label = "Service Total:", value = "₹ ${serviceTotal.toInt()}", tokens = tokens)
                        Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                        FinancialRow(
                            label = "Advance Paid:",
                            value = "₹ ${advancePaid.toInt()}",
                            valueColor = greentext,
                            tokens = tokens
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                        FinancialRow(label = "Payment Mode:", value = paymentMode, tokens = tokens)

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Highlighted Balance at Delivery Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .border(1.dp, orangeBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(yellowBg)
                                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Balance at Delivery:",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = yellowText
                                )
                                Text(
                                    text = "₹ ${balanceAmount.toInt()}",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = yellowText
                                )
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 8. LIVE AUDIT LOG & NOTES CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        // Section Header with Clock Icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIVE AUDIT LOG & NOTES",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = tokens.extraPadding),
                            color = dividerColor
                        )

                        // Audit Event Item with Bullet Dot
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .size(tokens.iconSize * 0.45f)
                                    .clip(CircleShape)
                                    .background(Primary)
                            )
                            Spacer(Modifier.width(tokens.extraPadding))
                            Column {
                                Text(
                                    text = "Service Request Initialized",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Alteration request logged for $quantity garment(s)",
                                    fontSize = tokens.caption,
                                    color = textSubdued,
                                    fontWeight = FontWeight.Normal
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "$creatorName • $createdDateFormatted",
                                    fontSize = tokens.label,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        // Section Divider above the input area
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = tokens.extraPadding),
                            color = dividerColor
                        )
                        FormTextArea(
                            value = auditComment,
                            onValueChange = { auditComment = it }
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Action Footer: Instruction text and Send Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Press Send to audit",
                                fontSize = tokens.caption,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )

                            val canSendComment = auditComment.isNotBlank() && !isSendingComment

                            Button(
                                onClick = {
                                    if (canSendComment) {
                                        viewModel.addAlterationComment(
                                            id = alterationId,
                                            commentText = auditComment,
                                            onSuccess = {
                                                auditComment = ""
                                            },
                                            onError = { /* Handle error toast or message */ }
                                        )
                                    }
                                },
                                enabled = canSendComment,
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary,
                                    disabledContainerColor = disabled
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = tokens.screenPadding,
                                    vertical = tokens.extraPadding * 0.6f
                                )
                            ) {
                                if (isSendingComment) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(tokens.iconSize * 0.8f),
                                        color = whiteBg,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = whiteBg,
                                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                                    )
                                }
                                Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                                Text(
                                    text = "Send",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding * 1.5f))
            }
        }
    }
}

@Composable
private fun FinancialRow(
    label: String,
    value: String,
    valueColor: Color = title_color,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = headerGrey,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

private fun formatIsoDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoDate)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        date?.let { formatter.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun formatAuditDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "Recently"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoDate)
        val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        date?.let { formatter.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}