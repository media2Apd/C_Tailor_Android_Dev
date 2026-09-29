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
import com.cuso.tailor.model.service.ServiceRequestData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ServiceDetailUiState
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
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()

    LaunchedEffect(requestId) {
        viewModel.loadServiceRequestById(requestId)
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                TitleBar(title ="Service Details", onClose = onBack)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = detailState) {
                is ServiceDetailUiState.Loading -> {
                    ListSkeleton()
                }
                is ServiceDetailUiState.Error -> {
                    AppErrorState(
                        title = "Error Loading Details",
                        message = state.message,
                        onRetry = { viewModel.loadServiceRequestById(requestId) }
                    )
                }
                is ServiceDetailUiState.Success -> {
                    ServiceRequestDetailContent(
                        service = state.request,
                        onViewFullOrderHistory = onViewFullOrderHistory
                    )
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun ServiceRequestDetailContent(
    service: ServiceRequestData,
    onViewFullOrderHistory: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val item = service.items.firstOrNull()

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
                        text = service.status.replace("_", " ").orDash(),
                        bg = yellowBg,
                        fg = yellowText
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
                        "Request Date",
                        formatDate(service.receivedDate ?: service.createdAt),
                        Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(tokens.fieldHeight * 0.8f)
                            .background(dividerColor)
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                    LabeledValue(
                        "Priority",
                        service.priority.orDash(),
                        Modifier.weight(1f),
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
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            LabeledValueRow("Preferred Completion Date", formatDate(service.deliveryDate))
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            LabeledValueRow("Priority Level", service.priority.orDash())
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            LabeledValueRow("Service Type", service.primaryCategory.orDash())
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

        // ── Section 2: Customer Details ──
        SectionHeader(
            title = "Customer Details",
            iconRes = R.drawable.ic_person
        )
        DetailSectionCard {
            LabeledValueRow("Customer Name", service.customerId?.fullName.orDash())
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            LabeledValueRow("Phone Number", service.customerId?.mobileNumber.orDash())
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            LabeledValueRow("Email Address", service.customerId?.email.orDash())
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            LabeledValueRow("Shipping Address", service.placeOfSupply.orDash())
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                val attachmentCount = if (service.attachments.isNotEmpty()) service.attachments.size else 3
                repeat(attachmentCount) {
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
                    Text(
                        text = if (service.subtotal > 0) String.format(Locale.US, "%.2f", service.subtotal) else "0.00",
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
                Text(
                    text = item?.internalNotes.orDash(),
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
        Text(text = value.orDash(), color = valueColor, fontSize = tokens.bodySmall, fontWeight = FontWeight.Normal)
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
        Text(text = value.orDash(), color = TitleColor, fontSize = tokens.bodySmall, fontWeight = FontWeight.Normal)
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
        Text(text = text.orDash(), color = fg, fontSize = tokens.caption, fontWeight = FontWeight.Medium)
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