package com.cuso.mobile.view.home.inventory.procurement.requisitions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.RequisitionComment
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun RequisitionDetailScreen(
    requisitionId: String?,
    onClose: () -> Unit,
    onEdit: () -> Unit = {},
    onPdfDownload: () -> Unit = {},
    onConvertToPO: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val requisition by viewModel.selectedRequisition.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingRequisitions.collectAsStateWithLifecycle()
    val isSubmittingComment by viewModel.isSubmittingComment.collectAsStateWithLifecycle()
    val successMessage by viewModel.requisitionSuccessMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.requisitionErrorMessage.collectAsStateWithLifecycle()

    var commentInput by remember { mutableStateOf("") }

    LaunchedEffect(requisitionId) {
        if (!requisitionId.isNullOrBlank()) {
            viewModel.fetchRequisitionById(requisitionId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearRequisitionAlerts()
        }
    }

    val prNumber = requisition?.prNumber ?: "PR Number Not Found"
    val status = requisition?.approvalStatus ?: "-"
    val isApproved = status.equals("Approved", ignoreCase = true)
    val department = requisition?.department ?: "Production"
    val creationDate = requisition?.createdAt?.take(10) ?: "—"
    val totalEst = requisition?.estimatedTotal ?: 0.0
    val subtotalEst = requisition?.estimatedSubtotal ?: 0.0
    val taxEst = requisition?.estimatedTax ?: 0.0

    val requestedBy = when (val req = requisition?.requestedBy) {
        is Map<*, *> -> req["name"]?.toString() ?: req["firstName"]?.toString() ?: "Staff"
        is String -> req
        else -> "Staff"
    }

    val priority = requisition?.priority ?: "-"
    val requiredByDate = requisition?.requiredByDate?.take(10) ?: "—"
    val budgetCode = requisition?.budgetCode ?: "N/A"
    val internalRef = requisition?.internalReference ?: "—"
    val warehouseName = requisition?.warehouseDisplayName ?: "-"
    val justificationText = requisition?.justification ?: "No justification provided."
    val items = requisition?.items ?: emptyList()
    val approvalTrail = requisition?.approvalTrail ?: emptyList()
    val comments = requisition?.comments ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                ) {
                    TitleBar(title = "Requisition Details", onClose = onClose)
                    HorizontalDivider(color = title_border)
                }
            }
        ) { paddingValues ->
            if (isLoading && requisition == null) {
                ListSkeleton()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = tokens.screenPadding * 2f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                ) {
                    item {
                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                    }

                    // 1. Header & Actions
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(whiteBg)
                                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prNumber,
                                    fontSize = tokens.h2,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                        .background(
                                            when {
                                                isApproved -> greenBg
                                                status.equals("Rejected", true) -> redBg
                                                else -> yellowBg
                                            }
                                        )
                                        .padding(
                                            horizontal = tokens.extraPadding * 0.8f,
                                            vertical = tokens.extraPadding * 0.3f
                                        )
                                ) {
                                    Text(
                                        text = status,
                                        fontSize = tokens.label,
                                        fontWeight = FontWeight.Medium,
                                        color = when {
                                            isApproved -> darkGreenBg
                                            status.equals("Rejected", true) -> redText
                                            else -> yellowText
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                            Text(
                                text = "Raised on $creationDate   |   Department: $department",
                                fontSize = tokens.caption,
                                color = mutedText
                            )

                            Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedActionButton(
                                    tokens = tokens,
                                    label = "Edit",
                                    icon = Icons.Default.Edit,
                                    onClick = onEdit
                                )
                                OutlinedActionButton(
                                    tokens = tokens,
                                    label = "PDF",
                                    onClick = onPdfDownload
                                )
                                Spacer(Modifier.weight(1f))
                                Button(
                                    onClick = onConvertToPO,
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                    modifier = Modifier.height(tokens.buttonHeight * 0.85f),
                                    contentPadding = PaddingValues(
                                        horizontal = tokens.screenPadding * 0.85f,
                                        vertical = 0.dp
                                    )
                                ) {
                                    Text(
                                        text = "Convert to PO",
                                        fontSize = tokens.caption,
                                        color = whiteBg,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // 2. Total Estimated Amount Card
                    item {
                        Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                colors = CardDefaults.cardColors(containerColor = title_color)
                            ) {
                                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                                    Text(
                                        text = "TOTAL ESTIMATED AMOUNT",
                                        fontSize = tokens.caption,
                                        color = iconMuted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                                    Text(
                                        text = "₹${"%.2f".format(totalEst)}",
                                        fontSize = tokens.h1,
                                        fontWeight = FontWeight.Bold,
                                        color = whiteBg
                                    )

                                    Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Estimated Subtotal", fontSize = tokens.bodySmall, color = light_grey)
                                        Text(
                                            text = "₹${"%.2f".format(subtotalEst)}",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = whiteBg
                                        )
                                    }
                                    Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Estimated Tax", fontSize = tokens.bodySmall, color = light_grey)
                                        Text(
                                            text = "₹${"%.2f".format(taxEst)}",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = whiteBg
                                        )
                                    }
                                    Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Approval Status", fontSize = tokens.bodySmall, color = light_grey)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                                .background(darkGreenBg.copy(alpha = 0.3f))
                                                .padding(horizontal = tokens.extraPadding * 0.6f, vertical = tokens.extraPadding * 0.2f)
                                        ) {
                                            Text(
                                                text = status.uppercase(),
                                                fontSize = tokens.label,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isApproved) complete_button_bg else yellowText
                                            )
                                        }
                                    }

                                    if (approvalTrail.isNotEmpty()) {
                                        Spacer(Modifier.height(tokens.screenPadding))
                                        HorizontalDivider(color = textSubdued)
                                        Spacer(Modifier.height(tokens.screenPadding))

                                        Text(
                                            text = "Approval Workflow",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = whiteBg
                                        )
                                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                                        approvalTrail.forEachIndexed { index, trail ->
                                            val isLast = index == approvalTrail.lastIndex
                                            val stepDone = trail.status.equals("Approved", ignoreCase = true)
                                            WorkflowStepItem(
                                                tokens = tokens,
                                                title = trail.stage ?: "Review",
                                                time = "${trail.status ?: ""} - ${trail.actionedAt?.take(10) ?: ""}",
                                                isDone = stepDone,
                                                isLast = isLast
                                            )
                                        }
                                    }

                                    if (internalRef.isNotBlank()) {
                                        Spacer(Modifier.height(tokens.extraPadding * 1.4f))
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "INTERNAL REFERENCE",
                                                fontSize = tokens.label,
                                                color = iconMuted
                                            )
                                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                            Text(
                                                text = internalRef,
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Bold,
                                                color = light_grey
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Request Summary
                    item {
                        DetailCardContainer(
                            tokens = tokens,
                            icon = R.drawable.ic_clock,
                            title = "Request Summary"
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Requested By", fontSize = tokens.label, color = mutedText)
                                    Text(
                                        text = requestedBy,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = title_color
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Department", fontSize = tokens.label, color = mutedText)
                                    Text(
                                        text = department,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = title_color
                                    )
                                }
                            }
                            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Priority", fontSize = tokens.label, color = mutedText)
                                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                            .background(if (priority.equals("High", true)) redBg else yellowBg)
                                            .padding(horizontal = tokens.extraPadding * 0.6f, vertical = tokens.extraPadding * 0.2f)
                                    ) {
                                        Text(
                                            text = priority.uppercase(),
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Bold,
                                            color = if (priority.equals("High", true)) redText else yellowText
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Required By", fontSize = tokens.label, color = mutedText)
                                    Text(
                                        text = requiredByDate,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = title_color
                                    )
                                }
                            }
                            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
                            Text("Budget Code", fontSize = tokens.label, color = mutedText)
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                    .background(activity_purple_bg)
                                    .padding(horizontal = tokens.extraPadding * 0.8f, vertical = tokens.extraPadding * 0.3f)
                            ) {
                                Text(
                                    text = budgetCode,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Bold,
                                    color = activity_purple
                                )
                            }
                        }
                    }

                    // 4. Delivery Details
                    item {
                        DetailCardContainer(
                            tokens = tokens,
                            icon = R.drawable.truck,
                            title = "Delivery Details"
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(badgeGrey, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .padding(tokens.extraPadding * 1.2f)
                            ) {
                                Column {
                                    Text(
                                        text = "WAREHOUSE LOCATION",
                                        fontSize = tokens.label,
                                        color = mutedText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                    Text(
                                        text = warehouseName,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = title_color
                                    )
                                }
                            }
                        }
                    }

                    // 5. Justification
                    item {
                        DetailCardContainer(
                            tokens = tokens,
                            icon = R.drawable.ic_file,
                            title = "Justification"
                        ) {
                            Text(
                                text = justificationText,
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                lineHeight = tokens.bodyLarge
                            )
                        }
                    }

                    // 6. Requested Items List
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding)
                        ) {
                            Text(
                                text = "Requested Items (${items.size} items)",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                            Spacer(Modifier.height(tokens.extraPadding))
                        }
                    }

                    itemsIndexed(items) { _, item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.55f),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(tokens.extraPadding * 1.2f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = item.itemDisplayName,
                                            fontSize = tokens.bodySmall,
                                            color = title_color,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "SKU: ${item.itemSku}",
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                            .background(greenBg)
                                            .padding(
                                                horizontal = tokens.extraPadding * 0.8f,
                                                vertical = tokens.extraPadding * 0.2f
                                            )
                                    ) {
                                        Text(
                                            text = item.status ?: "Pending",
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Medium,
                                            color = darkGreenBg
                                        )
                                    }
                                }

                                Spacer(Modifier.height(tokens.extraPadding))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Quantity", fontSize = tokens.label, color = mutedText)
                                        Text(
                                            text = "${item.qty.toInt()} ${item.unit ?: "PCS"}",
                                            fontSize = tokens.caption,
                                            color = title_color
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Rate", fontSize = tokens.label, color = mutedText)
                                        Text(
                                            text = "₹${"%.2f".format(item.rate)}",
                                            fontSize = tokens.caption,
                                            color = title_color
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total", fontSize = tokens.label, color = mutedText)
                                        Text(
                                            text = "₹${"%.2f".format(item.total)}",
                                            fontSize = tokens.caption,
                                            color = title_color,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                    }

                    // 7. Activity & Comments
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.55f),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            ActivityAndCommentsSection(
                                tokens = tokens,
                                comments = comments,
                                commentText = commentInput,
                                isSubmitting = isSubmittingComment,
                                onCommentChange = { commentInput = it },
                                onSendClick = {
                                    if (!requisitionId.isNullOrBlank() && commentInput.isNotBlank()) {
                                        viewModel.addRequisitionComment(
                                            requisitionId,
                                            commentInput
                                        ) {
                                            commentInput = ""
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { viewModel.clearRequisitionAlerts() }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { viewModel.clearRequisitionAlerts() }
        )
    }
}

@Composable
private fun ActivityAndCommentsSection(
    tokens: AppDesignTokens,
    comments: List<RequisitionComment>,
    commentText: String,
    isSubmitting: Boolean,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    val inputShape = RoundedCornerShape(tokens.cardCornerRadius * 2f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(tokens.screenPadding) // Provides inner spacing from the Card edges
    ) {
        // Title Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = tokens.extraPadding * 1.2f)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_message),
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(Modifier.width(tokens.extraPadding))
            Text(
                text = "Activity & Comments",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TitleColor
            )
        }

        // Comments List
        if (comments.isEmpty()) {
            Text(
                text = "No comments yet.",
                fontSize = tokens.bodySmall,
                color = iconMuted,
                modifier = Modifier.padding(vertical = tokens.extraPadding)
            )
        } else {
            comments.forEachIndexed { index, comment ->
                CommentCardItem(tokens = tokens, comment = comment, index = index)
                // Gap between each comment card
                Spacer(Modifier.height(tokens.extraPadding * 1.5f))
            }
        }

        // Gap below the last comment and above the input box
        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        // Comment Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.fieldHeight)
                    .clip(inputShape)
                    .background(badgeGrey)
                    .border(1.dp, light_blue_border, inputShape)
                    .padding(horizontal = tokens.screenPadding),
                contentAlignment = Alignment.CenterStart
            ) {
                if (commentText.isEmpty()) {
                    Text(
                        text = "Write a comment...",
                        fontSize = tokens.bodyMedium,
                        color = iconMuted
                    )
                }
                BasicTextField(
                    value = commentText,
                    onValueChange = onCommentChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = tokens.bodyMedium,
                        color = title_color
                    ),
                    cursorBrush = SolidColor(Primary)
                )
            }

            Spacer(Modifier.width(tokens.extraPadding))

            Box(
                modifier = Modifier
                    .size(tokens.fieldHeight)
                    .clip(CircleShape)
                    .background(
                        if (commentText.isNotBlank() && !isSubmitting) Primary else disabled
                    )
                    .clickable(
                        enabled = commentText.isNotBlank() && !isSubmitting,
                        onClick = onSendClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(tokens.iconSize),
                        color = whiteBg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = "Send",
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                    )
                }
            }
        }

        // Gap below the input box inside the card
        Spacer(Modifier.height(tokens.extraPadding * 0.4f))
    }
}

@Composable
private fun CommentCardItem(
    tokens: AppDesignTokens,
    comment: RequisitionComment,
    index: Int
) {
    val avatarBackground = when (index % 2) {
        0 -> sectionBorder
        else -> yellowBg
    }

    val avatarTextColor = when (index % 2) {
        0 -> textSubdued
        else -> yellowText
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = tokens.cardCornerRadius,
        bottomEnd = 0.dp,
        bottomStart = tokens.cardCornerRadius
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // User Profile Picture or Initials Fallback
        Box(
            modifier = Modifier
                .size(tokens.fieldHeight * 0.95f)
                .clip(CircleShape)
                .background(avatarBackground),
            contentAlignment = Alignment.Center
        ) {
            val photoUrl = comment.profilePictureUrl
            if (!photoUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = photoUrl,
                    contentDescription = comment.authorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comment.authorInitial,
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = avatarTextColor
                            )
                        }
                    },
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comment.authorInitial,
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = avatarTextColor
                            )
                        }
                    }
                )
            } else {
                Text(
                    text = comment.authorInitial,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = avatarTextColor
                )
            }
        }

        Spacer(Modifier.width(tokens.extraPadding * 1.2f))

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(bubbleShape)
                .background(light_blue)
                .border(1.dp, light_blue_border, bubbleShape)
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.3f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.authorName,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = formatCommentTimestamp(comment.createdAt),
                        fontSize = tokens.caption,
                        color = iconMuted
                    )
                }
                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                Text(
                    text = comment.content,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    lineHeight = tokens.bodyLarge
                )
            }
        }
    }
}

private fun formatCommentTimestamp(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(timestamp) ?: return timestamp.take(10)
        val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        formatter.format(date)
    } catch (_: Exception) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = parser.parse(timestamp) ?: return timestamp.take(10)
            val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (_: Exception) {
            timestamp.take(10)
        }
    }
}

@Composable
private fun WorkflowStepItem(
    tokens: AppDesignTokens,
    title: String,
    time: String,
    isDone: Boolean,
    isLast: Boolean
) {
    val stepColor = if (isDone) complete_button_bg else textSubdued

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.iconSize * 1.2f)
                    .clip(CircleShape)
                    .background(stepColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_tick_2),
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(tokens.iconSize * 0.7f)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .weight(1f)
                        .padding(vertical = tokens.extraPadding * 0.4f)
                        .background(stepColor, RoundedCornerShape(tokens.cardCornerRadius * 2f))
                )
            }
        }

        Spacer(Modifier.width(tokens.extraPadding * 1.2f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else tokens.screenPadding)
        ) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = whiteBg
            )
            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
            Text(
                text = time,
                fontSize = tokens.caption,
                color = iconMuted
            )
        }
    }
}

@Composable
private fun DetailCardContainer(
    tokens: AppDesignTokens,
    icon: Int,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
            }
            Spacer(Modifier.height(tokens.extraPadding * 1.4f))
            HorizontalDivider(color = grey_border)
            Spacer(Modifier.height(tokens.extraPadding * 1.4f))
            content()
        }
    }
}

@Composable
private fun OutlinedActionButton(
    tokens: AppDesignTokens,
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val boxShape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f)

    Box(
        modifier = Modifier
            .clip(boxShape)
            .border(
                width = 1.dp,
                color = sectionBorder,
                shape = boxShape
            )
            .clickable { onClick() }
            .padding(
                horizontal = tokens.extraPadding * 1.2f,
                vertical = tokens.extraPadding * 0.7f
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = title_color,
                    modifier = Modifier.size(tokens.iconSize * 0.75f)
                )
                Spacer(Modifier.width(tokens.extraPadding * 0.4f))
            }
            Text(
                text = label,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
    }
}