@file:Suppress("UNUSED_VALUE", "unused")

package com.cuso.tailor.view.home.services.service_request

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.ServiceRequestDetailData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ServiceRequestDetailsScreen(
    requestId: String,
    onBack: () -> Unit = {},
    onViewFullOrderHistory: () -> Unit = {},
    viewModel: ServicesViewModel = hiltViewModel()
) {
    // Collect ViewModel states
    val detailData by viewModel.serviceRequestDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingServiceRequestDetail.collectAsStateWithLifecycle()
    val errorMsg by viewModel.serviceRequestDetailError.collectAsStateWithLifecycle()

    // Load single service request detail on launch or requestId change
    LaunchedEffect(requestId) {
        viewModel.fetchServiceRequestDetail(requestId)
    }

    // Clear state when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearServiceRequestDetail()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg
            ) {
                TitleBar(
                    title = "Service Details",
                    onClose = onBack
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    ListSkeleton()
                }
                errorMsg != null -> {
                    AppErrorState(
                        title = "Error Loading Details",
                        message = errorMsg ?: "Unable to fetch details",
                        onRetry = { viewModel.fetchServiceRequestDetail(requestId) }
                    )
                }
                detailData != null -> {
                    ServiceRequestDetailContent(
                        service = detailData!!,
                        onViewFullOrderHistory = onViewFullOrderHistory
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceRequestDetailContent(
    service: ServiceRequestDetailData,
    onViewFullOrderHistory: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val item = service.items?.firstOrNull()

    // Determine status styling
    val isCompleted = service.status.equals("Completed", ignoreCase = true)
    val statusBg = if (isCompleted) greenBg else yellowBg
    val statusFg = if (isCompleted) greentext else yellowText

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = tokens.buttonHeight * 2)
    ) {
        Spacer(Modifier.height(tokens.extraPadding * 0.5f))

        // ── Top Summary Header Card ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = whiteBg,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.serviceRequestCode.orDash(),
                        fontWeight = FontWeight.Medium,
                        fontSize = tokens.bodyMedium,
                        color = TitleColor
                    )
                    StatusChip(
                        text = service.status?.replace("_", " ").orDash(),
                        bg = statusBg,
                        fg = statusFg
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabeledValue("Service", service.primaryCategory.orDash(), Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(tokens.fieldHeight * 0.8f)
                            .background(dividerColor)
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                    LabeledValue(
                        label = "Request Date",
                        value = formatDate(service.receivedDate ?: service.createdAt),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(tokens.fieldHeight * 0.8f)
                            .background(dividerColor)
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                    LabeledValue(
                        label = "Priority",
                        value = service.priority.orDash(),
                        modifier = Modifier.weight(1f),
                        valueColor = if (service.priority.equals("High", true) || service.priority.equals("Urgent", true)) redText else TitleColor
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        // ── Section 1: Requested Service ──
        SectionHeader(
            title = "Requested Service",
            iconRes = R.drawable.ic_dotted_pencil
        )
        DetailSectionCard {
            LabeledValueRow("Service Category", item?.category.orDash())
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.6f))
            LabeledValueRow("Preferred Completion Date", formatDate(service.deliveryDate ?: service.preferredDate))
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.6f))
            LabeledValueRow("Priority Level", service.priority.orDash())
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.6f))
            LabeledValueRow("Service Type", service.primaryCategory.orDash())
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        // ── Section 2: Customer Details ──
        SectionHeader(
            title = "Customer Details",
            iconRes = R.drawable.ic_person
        )
        DetailSectionCard {
            val customerName = service.customerId?.fullName
                ?: service.customerSnapshot?.name
            val phone = service.customerId?.mobileNumber
                ?: service.customerSnapshot?.phone
            val email = service.customerId?.email
                ?: service.customerSnapshot?.email
            val address = service.placeOfSupply
                ?: service.customerId?.billingAddress?.city
                ?: service.customerSnapshot?.billingAddress?.city

            LabeledValueRow("Customer Name", customerName.orDash())
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.6f))
            LabeledValueRow("Phone Number", phone.orDash())
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.6f))
            LabeledValueRow("Email Address", email.orDash())
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.6f))
            LabeledValueRow("Shipping Address", address.orDash())
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        // ── Section 3: Original Order Details ──
        SectionHeader(
            title = "Original Order Details",
            iconRes = R.drawable.ic_shopping_bag
        )
        DetailSectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order ID", color = mutedText, fontSize = tokens.caption)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (!service.originalSalesOrderId.isNullOrBlank()) "#${service.originalSalesOrderId}" else "-",
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = tokens.bodySmall
                    )
                }
                StatusChip(
                    text = "Completed",
                    bg = greenBg,
                    fg = greentext
                )
            }

            Spacer(Modifier.height(tokens.extraPadding))
            LabeledValueRow("Garment Item", item?.garmentDescription.orDash())
            Spacer(Modifier.height(tokens.extraPadding))

            Row(modifier = Modifier.fillMaxWidth()) {
                LabeledValue("Order Date", formatDate(service.createdAt), Modifier.weight(1f))
                LabeledValue("Delivery Date", formatDate(service.deliveryDate), Modifier.weight(1f))
            }

            Spacer(Modifier.height(tokens.extraPadding))

            OutlinedButton(
                onClick = onViewFullOrderHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                border = BorderStroke(1.dp, Primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Text(
                    text = "View Full Order History",
                    color = Primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = tokens.bodySmall
                )
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        // ── Section 4: Issue Description ──
        SectionHeader(
            title = "Issue Description",
            iconRes = R.drawable.ic_document
        )
        DetailSectionCard {
            Text(
                text = item?.issueSummary.orDash(),
                color = TextPrimary,
                fontSize = tokens.bodySmall,
                lineHeight = tokens.bodyMedium.value.dp.value.sp
            )
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        // ── Section 5: Attachments ──
        SectionHeader(
            title = "Attachments",
            iconRes = R.drawable.ic_upload_cloud
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            val attachmentsList = service.attachments.orEmpty()
            val count = if (attachmentsList.isNotEmpty()) attachmentsList.size else 3

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                repeat(count) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                            .background(whiteBg)
                            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = mutedText,
                            modifier = Modifier.size(tokens.iconSize * 1.3f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        // ── Section 6: Charges ──
        SectionHeader(
            title = "Charges",
            iconRes = null
        )
        DetailSectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Service", color = mutedText, fontSize = tokens.bodySmall)

                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(tokens.fieldHeight * 0.8f)
                        .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    val amount = service.subtotal ?: item?.serviceCharge ?: 0.0
                    Text(
                        text = if (amount > 0) String.format(Locale.US, "%.2f", amount) else "0.00",
                        fontSize = tokens.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.5f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clippad_tick),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Add Field +",
                    color = Primary,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        // ── Section 7: Internal Notes ──
        SectionHeader(
            title = "Internal Notes",
            iconRes = R.drawable.ic_horiz_3_lines
        )
        DetailSectionCard {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                    .padding(tokens.screenPadding * 0.8f)
            ) {
                val notes = service.internalNotes ?: item?.internalNotes
                Text(
                    text = notes.orDash(),
                    color = TextPrimary,
                    fontSize = tokens.bodySmall,
                    lineHeight = tokens.bodyMedium.value.dp.value.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable Layout Components
// ─────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    iconRes: Int? = null
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = whiteBg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
            }
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = tokens.bodyMedium,
                color = TitleColor
            )
        }
    }
}

@Composable
private fun DetailSectionCard(content: @Composable ColumnScope.() -> Unit) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            content = content
        )
    }
}

@Composable
private fun LabeledValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier) {
        Text(text = label, color = mutedText, fontSize = tokens.caption)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value.orDash(),
            color = valueColor,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun LabeledValueRow(
    label: String,
    value: String
) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = mutedText, fontSize = tokens.caption)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value.orDash(),
            color = TitleColor,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun StatusChip(text: String, bg: Color, fg: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(bg)
            .padding(horizontal = tokens.extraPadding * 0.75f, vertical = 4.dp)
    ) {
        Text(
            text = text.orDash(),
            color = fg,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Format Helpers
// ─────────────────────────────────────────────────────────────

private fun String?.orDash(): String =
    if (this.isNullOrBlank()) "-" else this

private fun formatDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = inputFormat.parse(rawDate)
        if (date != null) outputFormat.format(date) else rawDate.take(10)
    } catch (_: Exception) {
        rawDate.take(10)
    }
}