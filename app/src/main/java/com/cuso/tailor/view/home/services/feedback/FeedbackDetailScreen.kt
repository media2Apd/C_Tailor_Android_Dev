@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION"
)

package com.cuso.tailor.view.home.services.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FeedbackDetailScreen(
    feedbackId: String,
    viewModel: ServicesViewModel,
    onDismiss: () -> Unit = {}
) {
    val tokens: AppDesignTokens = LocalAppTokens.current
    val detail by viewModel.feedbackDetail.collectAsState()
    val isLoading by viewModel.isLoadingFeedbackDetail.collectAsState()
    val errorMessage by viewModel.feedbackDetailError.collectAsState()

    var selectedClassification by remember { mutableStateOf("") }

    LaunchedEffect(feedbackId) {
        if (feedbackId.isNotBlank()) {
            viewModel.fetchFeedbackDetail(feedbackId)
        }
    }

    // Resolving dynamic fields from fetched API data
    val displayFeedbackCode = detail?.feedbackCode ?: "-"
    val displayOrderCode = detail?.salesOrder?.orderCode ?: "-"
    val customerName = detail?.customerSnapshot?.name
        ?: detail?.customer?.fullName
        ?: "-"
    val customerPhone = detail?.customerSnapshot?.phone
        ?: detail?.customer?.mobileNumber
        ?: "-"
    val firstItem = detail?.salesOrder?.items?.firstOrNull()
    val garmentTitle = firstItem?.customGarment?.garmentName
        ?: firstItem?.customGarment?.categoryDisplayName
        ?: firstItem?.itemDescription
        ?: "-"
    val feedbackDate = formatDisplayDate(detail?.createdAt)
    val orderDeliveryDate = formatDisplayDate(detail?.salesOrder?.orderDate)
    val orderStatus = detail?.salesOrder?.status ?: ""

    val overallScore = detail?.overallRating ?: 5.0
    val npsCategory = detail?.npsCategory ?: ""
    val npsScore = detail?.npsScore ?: 10

    val fittingScore = detail?.fittingRating ?: 5.0
    val stitchingScore = detail?.stitchingQualityRating ?: 5.0
    val deliveryScore = detail?.deliveryTimelinessRating ?: 4.0
    val staffScore = detail?.staffBehaviorRating ?: 5.0

    val reviewTitle = detail?.reviewTitle ?: ""
    val commentsText = detail?.comments.orEmpty()
    val positiveAspects = detail?.positiveAspects ?: listOf(
        ""
    )

    val collectedChannel = detail?.collectedChannel?.replace("_", " ") ?: ""
    val formattedAuditTime = formatAuditTime(detail?.createdAt)

    val classificationOptions = listOf(
        "Product Quality",
        "Delivery Speed",
        "Fitting Accuracy",
        "Staff Behavior",
        "Fabric Quality"
    )

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Feedback Details", onClose = onDismiss
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            ListSkeleton()
        } else if (!errorMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Error loading feedback details",
                    color = redText,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.1f)
            ) {
                // ─────────────────────────────────────────────
                // 1. CUSTOMER DETAILS CARD (2-COLUMN GRID)
                // ─────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
                        Text(
                            text = "Customer Details",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                        // Row 1: Feedback ID & Order/Ticket ID
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItemColumn(
                                label = "Feedback ID",
                                value = displayFeedbackCode,
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                            DetailItemColumn(
                                label = "Order / Ticket ID",
                                value = displayOrderCode,
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Row 2: Customer Name & Garment
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItemColumn(
                                label = "Customer Name",
                                value = customerName,
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                            DetailItemColumn(
                                label = "Garment",
                                value = garmentTitle,
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Row 3: Feedback Date
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItemColumn(
                                label = "Feedback Date",
                                value = feedbackDate,
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 2. CUSTOMER INFORMATION CARD (LINE ITEMS)
                // ─────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
                        Text(
                            text = "Customer Information",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(top = tokens.extraPadding * 0.8f, bottom = tokens.extraPadding),
                            color = BorderGray,
                            thickness = 1.dp
                        )

                        InfoRowStacked(label = "Full Name", value = customerName, tokens = tokens)
                        InfoRowStacked(label = "Contact Number", value = customerPhone, tokens = tokens)
                        InfoRowStacked(label = "Order Reference", value = displayOrderCode, tokens = tokens, isLink = true)
                        InfoRowStacked(label = "Garment Type", value = garmentTitle, tokens = tokens)
                        InfoRowStacked(label = "Delivery Date", value = orderDeliveryDate, tokens = tokens)

                        // Order Status Row
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = tokens.extraPadding * 0.35f)
                        ) {
                            Text(
                                text = "Order Status",
                                fontSize = tokens.caption,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .background(greenBg)
                                    .border(1.dp, greentext.copy(alpha = 0.3f), RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .padding(horizontal = tokens.extraPadding, vertical = 3.dp)
                            ) {
                                Text(
                                    text = orderStatus,
                                    fontSize = tokens.caption,
                                    color = greentext,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 3. FEEDBACK DETAILS CARD
                // ─────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
                        Text(
                            text = "Feedback Details",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(top = tokens.extraPadding * 0.8f, bottom = tokens.extraPadding),
                            color = BorderGray,
                            thickness = 1.dp
                        )

                        Text(
                            text = "Overall Satisfaction",
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                        // Star ratings & NPS pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Row {
                                repeat(5) { index ->
                                    val isFilled = index < overallScore.toInt()
                                    Icon(
                                        imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = chartAmber,
                                        modifier = Modifier.size(tokens.iconSize * 1.15f)
                                    )
                                }
                            }

                            Text(
                                text = "%.1f / 5.0".format(overallScore),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )

                            // NPS pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                    .background(greenBg)
                                    .padding(horizontal = tokens.extraPadding * 0.7f, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "NPS: $npsCategory ($npsScore/10)",
                                    fontSize = tokens.label,
                                    color = greentext,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                        // 4-Score Metric Grid (Fitting, Stitching, Delivery, Staff)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            ScoreMetricBox(
                                label = "FITTING",
                                score = "${fittingScore.toInt()} / 5 ★",
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                            ScoreMetricBox(
                                label = "STITCHING",
                                score = "${stitchingScore.toInt()} / 5 ★",
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            ScoreMetricBox(
                                label = "DELIVERY",
                                score = "${deliveryScore.toInt()} / 5 ★",
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                            ScoreMetricBox(
                                label = "STAFF",
                                score = "${staffScore.toInt()} / 5 ★",
                                modifier = Modifier.weight(1f),
                                tokens = tokens
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                        // Customer Comments Callout Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            color = light_blue
                        ) {
                            Column(modifier = Modifier.padding(tokens.extraPadding)) {
                                Text(
                                    text = "Customer Comments",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                                Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                                Text(
                                    text = if (commentsText.isNotBlank()) "\"$commentsText\"" else "\"No additional comments provided.\"",
                                    fontSize = tokens.bodySmall,
                                    color = textSubdued,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.1f))

                        // Appreciated FlowRow (Wraps tags cleanly onto multiple lines matching the image)
                        OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                        ) {
                            Box(
                                modifier = Modifier.padding(top = 3.dp)
                            ) {
                                Text(
                                    text = "APPRECIATED:",
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.Medium,
                                    color = greentext
                                )
                            }

                            positiveAspects.forEach { aspect ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                        .background(greenBg)
                                        .padding(horizontal = tokens.extraPadding * 0.7f, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "✓ $aspect",
                                        fontSize = tokens.label,
                                        color = greentext,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                        // Helpful & Share Internal Action Buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                border = BorderStroke(1.dp, BorderGray),
                                color = whiteBg,
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { /* Helpful toggle action */ }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = tokens.extraPadding * 1.1f, vertical = tokens.extraPadding * 0.55f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ThumbUp,
                                        contentDescription = "Helpful",
                                        tint = title_color,
                                        modifier = Modifier.size(tokens.iconSize * 0.85f)
                                    )
                                    Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                                    Text(
                                        text = "Helpful",
                                        fontSize = tokens.bodySmall,
                                        color = title_color,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                border = BorderStroke(1.dp, BorderGray),
                                color = whiteBg,
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { /* Share Internal action */ }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = tokens.extraPadding * 1.1f, vertical = tokens.extraPadding * 0.55f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Internal",
                                        tint = title_color,
                                        modifier = Modifier.size(tokens.iconSize * 0.85f)
                                    )
                                    Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                                    Text(
                                        text = "Share Internal",
                                        fontSize = tokens.bodySmall,
                                        color = title_color,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 4. FEEDBACK CATEGORY CARD
                // ─────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
                        Text(
                            text = "Feedback Category",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(top = tokens.extraPadding * 0.8f, bottom = tokens.extraPadding),
                            color = BorderGray,
                            thickness = 1.dp
                        )

                        // Highlighted Review Title Pill
                        if (reviewTitle.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .background(Primary)
                                    .padding(horizontal = tokens.extraPadding * 1.2f, vertical = tokens.extraPadding * 0.6f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = whiteBg,
                                        modifier = Modifier.size(tokens.iconSize * 0.85f)
                                    )
                                    Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                                    Text(
                                        text = reviewTitle,
                                        fontSize = tokens.bodySmall,
                                        color = whiteBg,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
                        }

                        Text(
                            text = "REVIEW CLASSIFICATION",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )

                        Spacer(Modifier.height(tokens.extraPadding * 0.7f))

                        // Classification Radio-Style Options
                        classificationOptions.forEach { classification ->
                            val isSelected = classification == selectedClassification
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { selectedClassification = classification }
                                    .padding(vertical = tokens.extraPadding * 0.45f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) Primary else BorderGray,
                                            shape = CircleShape
                                        )
                                        .background(if (isSelected) Primary else Color.Transparent)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                                Text(
                                    text = classification,
                                    fontSize = tokens.bodySmall,
                                    color = title_color,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 5. FEEDBACK TIMELINE CARD
                // ─────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
                        Text(
                            text = "Feedback Timeline",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(top = tokens.extraPadding * 0.8f, bottom = tokens.extraPadding),
                            color = BorderGray,
                            thickness = 1.dp
                        )

                        // Single Timeline Item matching image
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(1.5.dp)
                                        .height(35.dp)
                                        .background(BorderGray)
                                )
                            }

                            Spacer(Modifier.width(tokens.extraPadding))

                            Column {
                                Text(
                                    text = "Feedback recorded in system",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = formattedAuditTime,
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = "Collected via $collectedChannel.",
                                    fontSize = tokens.caption,
                                    color = textSubdued,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                        // View Full Audit Trail Button
                        Surface(
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                            color = light_blue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { /* Full Audit Action */ }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "View Full Audit Trail",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding))
            }
        }
    }
}

// ─────────────────────────────────────────────
// COMPONENT HELPERS
// ─────────────────────────────────────────────

@Composable
private fun DetailItemColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tokens: AppDesignTokens
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = mutedText,
            fontWeight = FontWeight.Normal
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            color = title_color,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoRowStacked(
    label: String,
    value: String,
    tokens: AppDesignTokens,
    isLink: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding * 0.35f)
    ) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = mutedText,
            fontWeight = FontWeight.Normal
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            color = if (isLink) Primary else title_color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ScoreMetricBox(
    label: String,
    score: String,
    modifier: Modifier = Modifier,
    tokens: AppDesignTokens
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
        color = Primary_background
    ) {
        Column(
            modifier = Modifier.padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.7f)
        ) {
            Text(
                text = label,
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = score,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
    }
}

private fun formatDisplayDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "-"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoDate)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        date?.let { formatter.format(it) } ?: isoDate
    } catch (_: Exception) {
        isoDate
    }
}

private fun formatAuditTime(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "-"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoDate)
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        date?.let { formatter.format(it) } ?: isoDate
    } catch (_: Exception) {
        isoDate
    }
}