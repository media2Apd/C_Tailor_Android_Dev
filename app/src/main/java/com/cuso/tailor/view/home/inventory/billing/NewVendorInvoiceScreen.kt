@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.billing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*

data class InvoiceItemRow(
    val id: String,
    val name: String,
    val sku: String,
    var account: String,
    var taxRate: String,
    var quantity: String,
    var rate: String,
    val totalAmount: Double
)

@Composable
fun NewVendorInvoiceScreen(
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onSaveDraft: () -> Unit = {},
    onSaveAndSend: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var selectedVendor by remember { mutableStateOf("") }
    var isVendorExpanded by remember { mutableStateOf(false) }
    val vendorOptions = remember {
        listOf(
            "Arjun Textiles",
            "Dhana Garments",
            "Apex Global Solutions",
            "Tailoring Business Vendor"
        )
    }

    var invoiceNumber by remember { mutableStateOf("INV-00002") }
    var referenceOrderNumber by remember { mutableStateOf("") }

    var selectedTerms by remember { mutableStateOf("Due on Receipt") }
    var isTermsExpanded by remember { mutableStateOf(false) }
    val termsOptions = remember { listOf("Due on Receipt", "Net 15", "Net 30", "Net 60") }

    var dueDate by remember { mutableStateOf("15/02/2026") }
    var notesText by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("0.00") }
    var itemSearchQuery by remember { mutableStateOf("") }

    var activeAccountDropdownRowId by remember { mutableStateOf<String?>(null) }
    var activeTaxDropdownRowId by remember { mutableStateOf<String?>(null) }
    val accountOptions = remember { listOf("Goods", "Raw Materials", "Services", "Operating Expense") }
    val taxOptions = remember { listOf("0%", "5%", "8%", "12%", "18%") }

    var invoiceRows by remember {
        mutableStateOf(
            listOf(
                InvoiceItemRow("1", "Precision Chronograph", "WCH-2023-001", "Goods", "8%", "12", "450.00", 5400.00),
                InvoiceItemRow("2", "Precision Chronograph", "WCH-2023-001", "Goods", "8%", "12", "450.00", 5400.00)
            )
        )
    }

    // Attached files state
    var attachedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // File picker launcher supporting Images, PDF, Docs, etc.
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            attachedFiles = (attachedFiles + uris).distinct()
        }
    }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                TitleBar(title = "New Vendor Invoice", onClose = onCancel)
            }
        },
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            // ── Section 1: General Information Card ──
            VendorInvoiceSectionCard {
                SectionCardHeader(
                    title = "1. GENERAL INFORMATION",
                    trailingBadgeText = "Bill Entry"
                )

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                FormDropdown(
                    label = "Vendor Name",
                    isRequired = true,
                    value = selectedVendor,
                    expanded = isVendorExpanded,
                    onExpandChange = { isVendorExpanded = it },
                    options = vendorOptions,
                    onOptionSelected = { selectedVendor = it }
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.6f))
                Text(
                    text = "+ Add New Vendor",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.clickable { }
                )

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                FormLabel(text = "Invoice #", isRequired = true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .size(tokens.fieldHeight)
                            .border(
                                1.dp,
                                dividerColor,
                                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = mutedText,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                FormLabel(text = "Reference / Order Number")
                FormTextField(
                    value = referenceOrderNumber,
                    onValueChange = { referenceOrderNumber = it },
                    placeholder = "e.g. PO-8821"
                )

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                ) {
                    FormDropdown(
                        label = "Terms",
                        value = selectedTerms,
                        expanded = isTermsExpanded,
                        onExpandChange = { isTermsExpanded = it },
                        options = termsOptions,
                        onOptionSelected = { selectedTerms = it },
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Due Date")
                        FormTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            placeholder = "DD/MM/YYYY"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding))

            // ── Section 2: Line Items Card ──
            VendorInvoiceSectionCard {
                SectionCardHeader(
                    title = "2. LINE ITEMS",
                    badgeText = "${invoiceRows.size} Items",
                    trailingContent = {
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                            colors = ButtonDefaults.buttonColors(containerColor = background_light_purple),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(tokens.buttonHeight * 0.7f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize * 0.8f)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Add Bulk",
                                fontSize = tokens.caption,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // SearchFilterBar integration (with padding negation so it aligns with card edges)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .layout { measurable, constraints ->
                            val padPx = tokens.screenPadding.roundToPx()
                            val expandedConstraints = constraints.copy(
                                minWidth = constraints.minWidth + padPx * 2,
                                maxWidth = constraints.maxWidth + padPx * 2
                            )
                            val placeable = measurable.measure(expandedConstraints)
                            layout(constraints.maxWidth, placeable.height) {
                                placeable.placeRelative(-padPx, 0)
                            }
                        }
                ) {
                    SearchFilterBar(
                        query = itemSearchQuery,
                        onQueryChange = { itemSearchQuery = it },
                        placeholder = "Search items...",
                        isSearchBarAlone = true,
                        borderColor = dividerColor,
                        height = tokens.fieldHeight
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Filter rows based on search query
                val displayedInvoiceRows = remember(invoiceRows, itemSearchQuery) {
                    if (itemSearchQuery.isBlank()) {
                        invoiceRows
                    } else {
                        invoiceRows.filter {
                            it.name.contains(itemSearchQuery, ignoreCase = true) ||
                                    it.sku.contains(itemSearchQuery, ignoreCase = true)
                        }
                    }
                }

                // List container with spacing between cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
                ) {
                    displayedInvoiceRows.forEach { row ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                                .background(whiteBg)
                                .border(
                                    width = 1.dp,
                                    color = dividerColor,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f)
                                )
                                .padding(tokens.screenPadding)
                        ) {
                            // Header: Item Icon + Name + SKU + Delete Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(tokens.fieldHeight)
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                        .background(background_light_purple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(tokens.iconSize * 1.1f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(tokens.extraPadding))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = row.name,
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "SKU: ${row.sku}",
                                        fontSize = tokens.label,
                                        color = mutedText
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        invoiceRows = invoiceRows.filter { it.id != row.id }
                                    },
                                    modifier = Modifier.size(tokens.iconSize * 1.3f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete Row",
                                        tint = mutedText,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(tokens.extraPadding))

                            // Account & Tax Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                FormDropdown(
                                    label = "Account",
                                    value = row.account,
                                    expanded = activeAccountDropdownRowId == row.id,
                                    onExpandChange = { isExpanded ->
                                        activeAccountDropdownRowId = if (isExpanded) row.id else null
                                    },
                                    options = accountOptions,
                                    onOptionSelected = { chosen ->
                                        invoiceRows = invoiceRows.map {
                                            if (it.id == row.id) it.copy(account = chosen) else it
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                FormDropdown(
                                    label = "Tax",
                                    value = row.taxRate,
                                    expanded = activeTaxDropdownRowId == row.id,
                                    onExpandChange = { isExpanded ->
                                        activeTaxDropdownRowId = if (isExpanded) row.id else null
                                    },
                                    options = taxOptions,
                                    onOptionSelected = { chosen ->
                                        invoiceRows = invoiceRows.map {
                                            if (it.id == row.id) it.copy(taxRate = chosen) else it
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(tokens.extraPadding))

                            // Quantity, Rate, Amount Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Qty Field with Alert Border
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel(text = "Qty")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(tokens.fieldHeight)
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                            .background(yellowBg.copy(alpha = 0.5f))
                                            .border(
                                                1.5.dp,
                                                yellowText,
                                                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                            )
                                            .padding(horizontal = tokens.cardPadding * 0.6f),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BasicTextField(
                                            value = row.quantity,
                                            onValueChange = { newQty ->
                                                invoiceRows = invoiceRows.map {
                                                    if (it.id == row.id) it.copy(quantity = newQty) else it
                                                }
                                            },
                                            textStyle = TextStyle(
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            ),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = yellowText,
                                            modifier = Modifier.size(tokens.iconSize * 0.85f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(tokens.extraPadding))

                                // Rate Field
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel(text = "Rate")
                                    FormTextField(
                                        value = row.rate,
                                        onValueChange = { newRate ->
                                            invoiceRows = invoiceRows.map {
                                                if (it.id == row.id) it.copy(rate = newRate) else it
                                            }
                                        },
                                        keyboardType = KeyboardType.Number
                                    )
                                }

                                Spacer(modifier = Modifier.width(tokens.extraPadding))

                                // Calculated Total Amount
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "AMOUNT",
                                        fontSize = tokens.label,
                                        fontWeight = FontWeight.SemiBold,
                                        color = mutedText,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(tokens.screenPadding * 0.4f))
                                    Text(
                                        text = "%,.2f".format(row.totalAmount),
                                        fontSize = tokens.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Add New Row Button
                    OutlinedButton(
                        onClick = {
                            val nextIndex = invoiceRows.size + 1
                            invoiceRows = invoiceRows + InvoiceItemRow(
                                id = nextIndex.toString(),
                                name = "Precision Chronograph",
                                sku = "WCH-2023-001",
                                account = "Goods",
                                taxRate = "8%",
                                quantity = "1",
                                rate = "450.00",
                                totalAmount = 450.00
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = light_blue.copy(alpha = 0.4f),
                            contentColor = Primary
                        ),
                        border = BorderStroke(1.dp, light_blue_border)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add New Row",
                            fontSize = tokens.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding))

            // ── Section 3: Notes & Files Card ──
            VendorInvoiceSectionCard {
                SectionCardHeader(
                    title = "3. NOTES & FILES"
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                FormLabel(text = "Customer notes")
                FormTextArea(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = "Add optional notes for the customer...",
                    minLines = 3,
                    maxLines = 4,
                    borderColor = dividerColor,
                    focusedBorderColor = Primary
                )

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                FormLabel(text = "Attach File to Invoice")
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))

                ImageUploadSection(
                    isImage = false,
                    selectedImages = attachedFiles,
                    onBrowseClick = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "image/*",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    },
                    onRemoveImage = { fileToRemove ->
                        attachedFiles = attachedFiles.filter { it != fileToRemove }
                    },
                    documentUploadText = "Tap to upload PDF, PNG, JPG (up to 10MB)",
                    previewHeaderTitle = "ATTACHED INVOICE FILES",
                    uploadBoxHeight = tokens.buttonHeight * 2.2f
                )
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding))

            // ── Section 4: Credit Summary Card ──
            VendorInvoiceSectionCard {

                Text(
                    text = "CREDIT SUMMARY",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = headerGrey
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sub Total",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "$5,525.00",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discount",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    dividerColor,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.3f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "% | $",
                                fontSize = tokens.label,
                                color = mutedText
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .width(tokens.buttonHeight * 1.5f)
                                .height(tokens.fieldHeight * 0.75f)
                                .border(
                                    1.dp,
                                    dividerColor,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.3f)
                                )
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            BasicTextField(
                                value = discountText,
                                onValueChange = { discountText = it },
                                textStyle = TextStyle(
                                    fontSize = tokens.caption,
                                    textAlign = TextAlign.End,
                                    color = TextPrimary
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tax Total (8%)",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "$442.00",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Credit",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$5,967.00",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current Balance",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Text(
                        text = "$12,450.00",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Remaining Balance",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Text(
                        text = "$6,483.00",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding * 1.5f))

            // ── Bottom Form Action Buttons ──
            FormActionButtons(
                cancelText = "Cancel",
                draftText = "Save Draft",
                primaryText = "Save & Send",
                onCancel = onCancel,
                onSaveDraft = onSaveDraft,
                onPrimaryClick = onSaveAndSend,
                primaryColor = Primary,
                borderColor = dividerColor,
                textColor = TextPrimary
            )

            Spacer(modifier = Modifier.height(tokens.screenPadding))
        }
    }
}

/**
 * Reusable Section Card container used across all invoice sections.
 */
@Composable
private fun VendorInvoiceSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .border(1.dp, dividerColor, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding),
        content = content
    )
}

@Composable
fun SectionCardHeader(
    modifier: Modifier = Modifier,
    title: String,
    badgeText: String? = null,
    badgeBgColor: Color = light_blue,
    badgeTextColor: Color = TextSecondary,
    trailingBadgeText: String? = null,
    trailingBadgeBgColor: Color = background_light_purple,
    trailingBadgeTextColor: Color = Primary,
    trailingText: String? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title + Optional Title Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Bold,
                color = headerGrey
            )
            if (!badgeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                        .background(badgeBgColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = tokens.label,
                        color = badgeTextColor
                    )
                }
            }
        }

        // Trailing Section (Custom Action / Badge / Text)
        when {
            trailingContent != null -> {
                trailingContent()
            }
            !trailingBadgeText.isNullOrBlank() -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                        .background(trailingBadgeBgColor)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = trailingBadgeText,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Bold,
                        color = trailingBadgeTextColor
                    )
                }
            }
            !trailingText.isNullOrBlank() -> {
                Text(
                    text = trailingText,
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }
        }
    }
}