package com.cuso.tailor.view.home.inventory.approval

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.ApprovalCommentItem
import com.cuso.tailor.model.inventory.ApprovalItemDoc
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.R

@Composable
fun ApprovalDetailScreen(
    approvalId: String,
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    onDownloadPdf: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var commentInput by remember { mutableStateOf("") }

    // API state observers
    val detail by viewModel.selectedApprovalDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingApprovalDetail.collectAsStateWithLifecycle()
    val errorMessage by viewModel.approvalDetailError.collectAsStateWithLifecycle()
    val isSubmittingComment by viewModel.isSubmittingComment.collectAsStateWithLifecycle()
    val requisitionError by viewModel.requisitionErrorMessage.collectAsStateWithLifecycle()

    // Display error when comment submission fails
    LaunchedEffect(requisitionError) {
        requisitionError?.let {
            DynamicIslandManager.showError(it)
            viewModel.clearRequisitionAlerts()
        }
    }

    // Fetch view-one approval details on screen entry
    LaunchedEffect(approvalId) {
        viewModel.fetchApprovalDetail(approvalId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Primary_background,
        topBar = {
            TitleBar(title = "Approval Detail", onClose = onBack)
        }
    ) { innerPadding ->
        when {
            isLoading && detail == null -> {
                ListSkeleton()
            }
            errorMessage != null && detail == null -> {
                AppErrorState(
                    title = "Failed to load approval detail",
                    message = errorMessage ?: "Unexpected error occurred",
                    onRetry = { viewModel.fetchApprovalDetail(approvalId) }
                )
            }
            detail != null -> {
                val data = detail!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header title row
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = data.prNumber,
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(text = data.approvalStatus, textColor = greentext, bgColor = greenBg)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null,
                                        tint = iconMuted,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Raised on ${formatIsoDate(data.createdAt ?: "")} | Department ${data.department ?: "N/A"}",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }

                    // Card 1: Request Summary
                    item {
                        ApprovalCardContainer(tokens = tokens) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(tokens.iconSize))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Request Summary", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = BluePrimary)
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    LabelValueField(label = "Requested By", value = data.requestedBy?.fullName ?: "Unknown", modifier = Modifier.weight(1f))
                                    LabelValueField(label = "Department", value = data.department ?: "N/A", modifier = Modifier.weight(1f))
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Priority", fontSize = tokens.caption, color = dataCardField)
                                        Spacer(Modifier.height(4.dp))
                                        StatusBadge(
                                            text = data.priority ?: "Normal",
                                            textColor = if (data.priority.equals("High", true)) redText else orangeText,
                                            bgColor = if (data.priority.equals("High", true)) redBg else orangeBg
                                        )
                                    }
                                    LabelValueField(label = "Required By", value = formatIsoDate(data.requiredByDate ?: ""), modifier = Modifier.weight(1f))
                                }

                                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Budget Code", fontSize = tokens.caption, color = TextSecondary)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(primary_light)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = data.budgetCode ?: "N/A", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = Primary)
                                    }
                                }
                            }
                        }
                    }

                    // Card 2: Total Estimated Amount
                    item {
                        ApprovalCardContainer(tokens = tokens) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Total Estimated Amount", fontSize = tokens.caption, color = headerGrey)
                                Text("₹${data.estimatedTotal.toInt()}", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Medium, color = title_color)

                                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                TwoColumnRow(label = "Estimated Subtotal", value = "₹${data.estimatedSubtotal.toInt()}", tokens = tokens)
                                TwoColumnRow(label = "GST Amount", value = "₹${data.estimatedTax.toInt()}", tokens = tokens)

                                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Approval Status", fontSize = tokens.caption, color = TextSecondary)
                                    StatusBadge(text = data.approvalStatus, textColor = greentext, bgColor = greenBg)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Converted to PO", fontSize = tokens.caption, color = TextSecondary)
                                    StatusBadge(text = data.conversionStatus ?: "Not Converted", textColor = TextSecondary, bgColor = badgeGrey)
                                }
                            }
                        }
                    }

                    // Card 3: Requested Items List
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Requested Items (${data.items.size} items)", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = BluePrimary)

                                Button(
                                    onClick = onDownloadPdf,
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Download", fontSize = tokens.caption, color = whiteBg, fontWeight = FontWeight.Medium)
                                }
                            }

                            data.items.forEach { itemDoc ->
                                RequestedItemApiCard(item = itemDoc, tokens = tokens)
                            }
                        }
                    }

                    // Card 4: Justification
                    item {
                        ApprovalCardContainer(tokens = tokens) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Description, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(tokens.iconSize))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Justification", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = BluePrimary)
                                }
                                Text(
                                    text = data.justification?.ifBlank { "No justification provided." } ?: "No justification provided.",
                                    fontSize = tokens.caption,
                                    color = textSubdued,
                                    lineHeight = tokens.bodyMedium
                                )
                            }
                        }
                    }

                    // Card 5: Delivery Details
                    item {
                        ApprovalCardContainer(tokens = tokens) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(tokens.iconSize))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Delivery Details", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = BluePrimary)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeGrey)
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text("Warehouse Location", fontSize = tokens.caption, color = dataCardField)
                                        Spacer(Modifier.height(2.dp))
                                        Text(data.warehouseId?.name ?: "Main Warehouse", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                                        val addr = data.warehouseId?.address
                                        Text("${addr?.state ?: ""}, ${addr?.country ?: ""}", fontSize = tokens.caption, color = TextSecondary)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(primary_light),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("Contact Person", fontSize = tokens.caption, color = dataCardField)
                                        Text(data.requestedBy?.fullName ?: "Warehouse Supervisor", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                                    }
                                }
                            }
                        }
                    }

                    // Card 6: Activity & Comments (Uses addRequisitionComment API)
                    item {
                        ApprovalCardContainer(tokens = tokens) {
                            ActivityAndCommentsSection(
                                tokens = tokens,
                                comments = data.comments,
                                commentText = commentInput,
                                isSubmitting = isSubmittingComment,
                                onCommentChange = { commentInput = it },
                                onSendClick = {
                                    if (commentInput.isNotBlank()) {
                                        // Trigger the requested addRequisitionComment API
                                        viewModel.addRequisitionComment(
                                            requisitionId = approvalId,
                                            commentText = commentInput
                                        ) {
                                            commentInput = ""
                                            // Reload latest approval details to show the newly added comment
                                            viewModel.fetchApprovalDetail(approvalId)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RequestedItemApiCard(item: ApprovalItemDoc, tokens: AppDesignTokens) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(whiteBg)
            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.itemId?.name ?: "Item", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                StatusBadge(text = item.status, textColor = greentext, bgColor = greenBg)
            }
            Text(text = "SKU: ${item.itemId?.sku ?: "N/A"}", fontSize = tokens.caption, color = dataCardField)

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Quantity", fontSize = tokens.caption, color = dataCardField)
                    Text("${item.qty.toInt()} ${item.unit ?: "Meters"}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Unit Cost", fontSize = tokens.caption, color = dataCardField)
                    Text("₹${item.rate.toInt()}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", fontSize = tokens.caption, color = dataCardField)
                    Text("₹${item.total.toInt()}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                }
            }
        }
    }
}

@Composable
private fun ActivityAndCommentsSection(
    tokens: AppDesignTokens,
    comments: List<ApprovalCommentItem>,
    commentText: String,
    isSubmitting: Boolean,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    val inputShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = tokens.cardCornerRadius * 2f,
        bottomStart = tokens.cardCornerRadius * 2f,
        bottomEnd = 0.dp
    )

    val sendShape = RoundedCornerShape( tokens.cardCornerRadius * 2f)


    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = tokens.extraPadding * 1.2f)
        ) {
            Icon(Icons.Outlined.QuestionAnswer, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
            Spacer(Modifier.width(tokens.extraPadding))
            Text("Activity & Comments", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TitleColor)
        }

        if (comments.isEmpty()) {
            Text("No comments yet.", fontSize = tokens.bodySmall, color = iconMuted, modifier = Modifier.padding(vertical = tokens.extraPadding))
        } else {
            comments.forEachIndexed { index, comment ->
                CommentCardItem(tokens = tokens, comment = comment, index = index)
                Spacer(Modifier.height(tokens.extraPadding * 1.5f))
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.fieldHeight)
                    .clip(sendShape)
                    .background(badgeGrey)
                    .border(1.dp, light_blue_border, sendShape)
                    .padding(horizontal = tokens.screenPadding),
                contentAlignment = Alignment.CenterStart
            ) {
                if (commentText.isEmpty()) {
                    Text("Write a comment...", fontSize = tokens.bodyMedium, color = iconMuted)
                }
                BasicTextField(
                    value = commentText,
                    onValueChange = onCommentChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = tokens.bodyMedium, color = title_color),
                    cursorBrush = SolidColor(Primary)
                )
            }

            Spacer(Modifier.width(tokens.extraPadding))

            Box(
                modifier = Modifier
                    .size(tokens.fieldHeight)
                    .clip(CircleShape)
                    .background(if (commentText.isNotBlank() && !isSubmitting) Primary else disabled)
                    .clickable(enabled = commentText.isNotBlank() && !isSubmitting, onClick = onSendClick),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(tokens.iconSize), color = whiteBg, strokeWidth = 2.dp)
                } else {
                    Icon(painter = painterResource(R.drawable.ic_send), contentDescription = "Send", tint = whiteBg, modifier = Modifier.size(tokens.iconSize * 0.8f))
                }
            }
        }
    }
}

@Composable
private fun CommentCardItem(
    tokens: AppDesignTokens,
    comment: ApprovalCommentItem,
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

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(tokens.fieldHeight * 0.95f)
                .clip(CircleShape)
                .background(avatarBackground),
            contentAlignment = Alignment.Center
        ) {
            val photoUrl = comment.commentedBy?.profilePicture
            if (!photoUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = photoUrl,
                    contentDescription = comment.commentedBy.fullName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    error = {
                        Text(comment.commentedBy.initials, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Medium, color = avatarTextColor)
                    }
                )
            } else {
                Text(comment.commentedBy?.initials ?: "U", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Medium, color = avatarTextColor)
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
                    Text(comment.commentedBy?.fullName ?: "Unknown", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                    Text(formatIsoDateTime(comment.createdAt), fontSize = tokens.caption, color = iconMuted)
                }
                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                Text(comment.text, fontSize = tokens.bodySmall, color = TextSecondary, lineHeight = tokens.bodyLarge)
            }
        }
    }
}

@Composable
private fun ApprovalCardContainer(tokens: AppDesignTokens, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun LabelValueField(label: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier) {
        Text(text = label, fontSize = tokens.caption, color = dataCardField)
        Spacer(Modifier.height(2.dp))
        Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
    }
}

@Composable
private fun TwoColumnRow(label: String, value: String, tokens: AppDesignTokens) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.caption, color = TextSecondary)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
    }
}

@Composable
private fun StatusBadge(text: String, textColor: Color, bgColor: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = textColor)
    }
}
