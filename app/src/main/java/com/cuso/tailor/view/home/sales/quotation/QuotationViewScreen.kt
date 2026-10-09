@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter"
)

package com.cuso.tailor.view.home.sales.quotation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.QuotationItemDto
import com.cuso.tailor.model.sales.QuotationLineItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ─────────────────────────────────────────────────────────────
// ENTRY POINT
// ─────────────────────────────────────────────────────────────

@Composable
fun QuotationViewScreen(
    quotationId: String,
    onClose: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    val quotationViewModel: QuotationViewModel = hiltViewModel()
    val detailState by quotationViewModel.detailState.collectAsStateWithLifecycle()

    LaunchedEffect(quotationId) {
        if (quotationId.isNotBlank()) quotationViewModel.fetchQuotationById(quotationId)
    }

    Column(modifier = Modifier.fillMaxSize().background(Primary_background)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth()) {
            TitleBar(title = "Quotation Details", onClose = onClose)
        }

        when (val state = detailState) {
            is QuotationDetailUiState.Success -> {
                if (state.quotation.id == quotationId) {
                    QuotationViewContent(
                        dto = state.quotation,
                        onEdit = onEdit,
                        onClose = onClose
                    )
                } else {
                    ListSkeleton()
                }
            }
            is QuotationDetailUiState.Error -> {
                AppErrorState(
                    title = "Failed to load quotation",
                    message = "Something went wrong. Please check your connection and try again.",
                    onRetry = { quotationViewModel.fetchQuotationById(quotationId) }
                )
            }
            else -> ListSkeleton()
        }
    }
}

// ─────────────────────────────────────────────────────────────
// CONTENT
// ─────────────────────────────────────────────────────────────

@Composable
private fun QuotationViewContent(
    dto: QuotationItemDto,
    onEdit: () -> Unit,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Split the flat line list into main lines and add-on lines
    val mainLines = dto.items.filter { it.lineType != "Garment_Addon" }
    val addonLines = dto.items.filter { it.lineType == "Garment_Addon" }
    val mainIds = mainLines.mapNotNull { it.id }.toSet()
    val orphanAddons = addonLines.filter { it.parentLineId.isNullOrBlank() || it.parentLineId !in mainIds }

    // Aggregates
    val garmentTotal = mainLines.filter { it.lineType == "Custom_Garment" }.sumOf { it.lineAmount() }
    val addonTotal = addonLines.sumOf { it.lineAmount() }
    val materialTotal = mainLines.filter { it.lineType != "Custom_Garment" }.sumOf { it.lineAmount() }
    val totalPieces = mainLines
        .filter { it.lineType == "Custom_Garment" || it.lineType == "Retail_Product" }
        .sumOf { it.quantity }
        .toInt()

    val customerName = dto.customerSnapshot?.name?.takeIf { it.isNotBlank() }
        ?: dto.customerId?.fullName?.takeIf { it.isNotBlank() }
        ?: dto.leadId?.fullName?.takeIf { it.isNotBlank() }
        ?: "-"

    val primaryGarment = mainLines.firstOrNull { it.lineType == "Custom_Garment" }?.displayTitle()
        ?: mainLines.firstOrNull()?.displayTitle()
        ?: "-"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header card
        HeaderCard(
            dto = dto,
            customerName = customerName,
            primaryGarment = primaryGarment,
            totalPieces = totalPieces
        )

        // 2. Breakdown
        Text(
            text = "Item Breakdown",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )

        mainLines.forEachIndexed { index, line ->
            val children = addonLines.filter { !it.parentLineId.isNullOrBlank() && it.parentLineId == line.id }
            BreakdownCard(index = index + 1, line = line, addons = children)
        }

        if (orphanAddons.isNotEmpty()) {
            AdditionalWorkCard(addons = orphanAddons)
        }

        // 3. Aggregate summary
        AggregateSummaryCard(
            totalPieces = totalPieces,
            garmentTotal = garmentTotal,
            addonTotal = addonTotal,
            materialTotal = materialTotal,
            tax = dto.taxAmount,
            discount = dto.discountAmount,
            delivery = dto.deliveryCharge,
            grandTotal = dto.grandTotal
        )

        // 4. Notes
        if (!dto.notes.isNullOrBlank()) {
            NotesCard(notes = dto.notes)
        }

        // 5. Activity trail
        ActivityTrailCard(dto = dto, customerName = customerName, itemCount = mainLines.size)

        // 6. Actions
        if (dto.status.equals("draft", ignoreCase = true)) {
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = whiteBg)
            ) {
                Text(
                    text = "Edit Quotation",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = whiteBg
                )
            }
        }

        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Primary),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg, contentColor = Primary)
        ) {
            Text(
                text = "Back to Quotations",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }

        Spacer(Modifier.height(tokens.buttonHeight))
    }
}

// ─────────────────────────────────────────────────────────────
// HEADER CARD
// ─────────────────────────────────────────────────────────────

@Composable
private fun HeaderCard(
    dto: QuotationItemDto,
    customerName: String,
    primaryGarment: String,
    totalPieces: Int
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dto.quotationNumber?.takeIf { it.isNotBlank() } ?: "Quotation",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Created on ${formatDateTime(dto.createdAt ?: dto.quotationDate)}",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                }
                StatusChip(status = dto.status)
            }

            Spacer(Modifier.height(10.dp))

            // Info chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(text = if (dto.leadId != null) "Lead" else "Customer")
                if (!dto.expiryDate.isNullOrBlank()) {
                    InfoChip(text = "Valid till ${formatDateOnly(dto.expiryDate)}")
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = BorderGray)
            Spacer(Modifier.height(12.dp))

            HeaderField(label = "CUSTOMER", value = customerName)
            Spacer(Modifier.height(10.dp))
            HeaderField(label = "PRIMARY GARMENT", value = primaryGarment)
            Spacer(Modifier.height(10.dp))
            HeaderField(label = "TOTAL PIECES", value = "$totalPieces Pieces")
            Spacer(Modifier.height(10.dp))
            HeaderField(label = "QUOTATION VALUE", value = viewFormatPrice(dto.grandTotal), valueColor = Primary)
        }
    }
}

@Composable
private fun HeaderField(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Column {
        Text(text = label, fontSize = tokens.caption, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun InfoChip(text: String) {
    val tokens = LocalAppTokens.current
    Surface(shape = RoundedCornerShape(20.dp), color = primary_light) {
        Text(
            text = text,
            fontSize = tokens.caption,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val tokens = LocalAppTokens.current
    val s = status.lowercase(Locale.US)
    val (bg, fg) = when {
        s.contains("draft") -> yellowBg to yellowText
        s.contains("accept") || s.contains("approv") || s.contains("convert") -> greenBg to greentext
        s.contains("reject") || s.contains("expire") || s.contains("cancel") -> redBg to redText
        else -> primary_light to Primary
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            text = status.replace("_", " "),
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// BREAKDOWN CARDS
// ─────────────────────────────────────────────────────────────

@Composable
private fun BreakdownCard(
    index: Int,
    line: QuotationLineItemDto,
    addons: List<QuotationLineItemDto>
) {
    val tokens = LocalAppTokens.current
    val garment = line.customGarment

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = index.toString().padStart(2, '0'),
                    fontSize = tokens.bodySmall,
                    color = mutedText
                )
                Text(
                    text = line.displayTitle(),
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            when (line.lineType) {
                "Custom_Garment" -> {
                    garment?.garmentName?.takeIf { it.isNotBlank() }?.let { ViewKeyValueRow("Garment", it) }
                    garment?.stitchingType?.takeIf { it.isNotBlank() }?.let { ViewKeyValueRow("Stitching Type", it) }
                    garment?.designName?.takeIf { it.isNotBlank() }?.let { ViewKeyValueRow("Design", it) }
                    garment?.fabricNotes?.takeIf { it.isNotBlank() }?.let { ViewKeyValueRow("Fabric Notes", it) }
                    garment?.specialInstructions?.takeIf { it.isNotBlank() }?.let { ViewKeyValueRow("Instructions", it) }
                    ViewKeyValueRow("Stitching Charges", viewFormatPrice(line.unitPrice))
                    if (addons.isNotEmpty()) {
                        addons.forEach { addon ->
                            ViewKeyValueRow(
                                label = addon.addonWork?.workType ?: addon.itemDescription ?: "Add-on",
                                value = viewFormatPrice(addon.lineAmount())
                            )
                        }
                    }
                }
                "Garment_Material" -> {
                    ViewKeyValueRow("Type", "Store Fabric")
                    ViewKeyValueRow("Unit", line.unit ?: "-")
                }
                else -> {
                    ViewKeyValueRow("Type", "Accessory")
                    ViewKeyValueRow("Unit", line.unit ?: "-")
                }
            }

            ViewKeyValueRow("Quantity", viewFormatQty(line.quantity))
            ViewKeyValueRow("Unit Price", viewFormatPrice(line.unitPrice))
            if (line.taxAmount > 0.0) {
                ViewKeyValueRow("Tax", viewFormatPrice(line.taxAmount))
            }

            // Line total includes the add-ons attached to this garment line
            val lineTotal = line.lineAmount() + addons.sumOf { it.lineAmount() }
            ViewKeyValueRow("Line Total", viewFormatPrice(lineTotal))
        }
    }
}

// Shown only when an add-on cannot be matched to a parent line
@Composable
private fun AdditionalWorkCard(addons: List<QuotationLineItemDto>) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Additional Work",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            addons.forEach { addon ->
                ViewKeyValueRow(
                    label = addon.addonWork?.workType ?: addon.itemDescription ?: "Add-on",
                    value = viewFormatPrice(addon.lineAmount())
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// AGGREGATE SUMMARY
// ─────────────────────────────────────────────────────────────

@Composable
private fun AggregateSummaryCard(
    totalPieces: Int,
    garmentTotal: Double,
    addonTotal: Double,
    materialTotal: Double,
    tax: Double,
    discount: Double,
    delivery: Double,
    grandTotal: Double
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = modelBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aggregate Summary",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(text = "$totalPieces pcs", fontSize = tokens.bodySmall, color = mutedText)
            }
            Spacer(Modifier.height(8.dp))

            ViewKeyValueRow("Stitching Charges", viewFormatPrice(garmentTotal))
            ViewKeyValueRow("Additional Work", viewFormatPrice(addonTotal))
            if (materialTotal > 0.0) ViewKeyValueRow("Fabrics & Items", viewFormatPrice(materialTotal))
            ViewKeyValueRow("Tax", viewFormatPrice(tax))
            if (discount > 0.0) ViewKeyValueRow("Discount", "-${viewFormatPrice(discount)}")
            if (delivery > 0.0) ViewKeyValueRow("Delivery Charge", viewFormatPrice(delivery))

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quotation Total",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = viewFormatPrice(grandTotal),
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Terms & Notes",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(text = notes, fontSize = tokens.bodySmall, color = TextSecondary)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ACTIVITY TRAIL
// ─────────────────────────────────────────────────────────────

private data class TrailEvent(
    val title: String,
    val time: String,
    val description: String,
    val isCurrent: Boolean = false,
    val trailing: String? = null
)

@Composable
private fun ActivityTrailCard(
    dto: QuotationItemDto,
    customerName: String,
    itemCount: Int
) {
    val tokens = LocalAppTokens.current

    val events = buildList {
        add(
            TrailEvent(
                title = "Quotation Created",
                time = formatDateTime(dto.createdAt ?: dto.quotationDate),
                description = "Quotation drafted for $customerName with $itemCount item(s)."
            )
        )
        if (!dto.updatedAt.isNullOrBlank() && dto.updatedAt != dto.createdAt) {
            add(
                TrailEvent(
                    title = "Quotation Updated",
                    time = formatDateTime(dto.updatedAt),
                    description = "Quotation details were modified."
                )
            )
        }
        if (!dto.expiryDate.isNullOrBlank()) {
            add(
                TrailEvent(
                    title = "Valid Until",
                    time = formatDateOnly(dto.expiryDate),
                    description = "Quotation prices are valid until this date."
                )
            )
        }
        add(
            TrailEvent(
                title = dto.status.replace("_", " "),
                time = "",
                description = "",
                isCurrent = true,
                trailing = "Current Status"
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quotation Activity Trail",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(14.dp))

            events.forEachIndexed { index, event ->
                TrailRow(event = event, isLast = index == events.lastIndex)
            }
        }
    }
}

@Composable
private fun TrailRow(event: TrailEvent, isLast: Boolean) {
    val tokens = LocalAppTokens.current
    val dotColor = if (event.isCurrent) Primary else complete_button_bg

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier.width(20.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(BorderGray)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (event.isCurrent) Primary else TextPrimary
                )
                if (event.trailing != null) {
                    Text(text = event.trailing, fontSize = tokens.caption, color = mutedText)
                }
            }
            if (event.time.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(text = event.time, fontSize = tokens.caption, color = mutedText)
            }
            if (event.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(text = event.description, fontSize = tokens.bodySmall, color = TextSecondary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SMALL HELPERS
// ─────────────────────────────────────────────────────────────

@Composable
private fun ViewKeyValueRow(
    label: String,
    value: String
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = mutedText)
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

// Best display title for a line
private fun QuotationLineItemDto.displayTitle(): String {
    val garment = customGarment
    return garment?.categoryDisplayName?.takeIf { it.isNotBlank() }
        ?: garment?.garmentName?.takeIf { it.isNotBlank() }
        ?: itemDescription?.takeIf { it.isNotBlank() }
        ?: "Item"
}

// Line amount: backend lineTotal, or unit price x quantity when the backend value is empty
private fun QuotationLineItemDto.lineAmount(): Double =
    if (totalPrice > 0.0) totalPrice else unitPrice * quantity

private fun viewFormatPrice(amount: Double): String {
    val pattern = if (amount % 1.0 == 0.0) "%,.0f" else "%,.2f"
    return "₹" + String.format(Locale.US, pattern, amount)
}

private fun viewFormatQty(qty: Double): String =
    if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()

// ISO UTC string -> local "6 May 2025 • 10:30 AM"
private fun formatDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = input.parse(raw.take(19)) ?: return raw
        SimpleDateFormat("d MMM yyyy • hh:mm a", Locale.US).format(date)
    } catch (_: Exception) {
        raw
    }
}

// ISO UTC string -> "6 May 2025"
private fun formatDateOnly(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = input.parse(raw.take(10)) ?: return raw
        SimpleDateFormat("d MMM yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        raw
    }
}