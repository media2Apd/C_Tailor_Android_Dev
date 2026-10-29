@file:Suppress("UNUSED_PARAMETER")

package com.cuso.tailor.view.home.finance.receipts

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.CustomerPaymentItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Customer Receipts List Screen with adaptive token layout and DataCard integration.
 */
@Composable
fun CustomerReceiptsListScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCreateReceiptClick: () -> Unit = {},
    onReceiptClick: (CustomerPaymentItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val receipts by viewModel.receiptsList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingReceipts.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreReceipts.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreReceipts.collectAsStateWithLifecycle()

    // Fetch initial list of receipts on entry
    LaunchedEffect(Unit) {
        viewModel.fetchCustomerReceipts(page = 1)
    }

    FabScaffold(
        fab = FabConfig(
            label = "Record Payment",
            icon = Icons.Default.Add,
            onClick = onCreateReceiptClick
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Header Section
            TitleBar(
                title = "Payment Receipts",
                onClose = onClose
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            // Search Bar Filter
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.fetchCustomerReceipts(page = 1, search = it.ifBlank { null })
                },
                placeholder = "Search receipt number, customer..."
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            // Content List Section
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading && receipts.isEmpty()) {
                    ListSkeleton()
                } else if (receipts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No payment receipts found",
                            fontSize = tokens.bodyMedium,
                            color = mutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = tokens.screenPadding * 4)
                    ) {
                        items(receipts, key = { it.id }) { receipt ->
                            ReceiptCardItem(
                                item = receipt,
                                onClick = { onReceiptClick(receipt) }
                            )
                        }

                        // Infinite scroll pagination loader
                        if (canLoadMore) {
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMoreCustomerReceipts()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(tokens.screenPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoadingMore) {
                                        ThreeDotLoading()
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

/**
 * Reusable card item rendered via DataCard and DataCardField.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun ReceiptCardItem(
    item: CustomerPaymentItem,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Extract customer initials for avatar
    val customerName = item.customer?.fullName?.trim()?.ifBlank { "Customer" } ?: "Customer"
    val initials = remember(customerName) {
        val parts = customerName.split("\\s+".toRegex()).filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
            parts.isNotEmpty() -> parts[0].take(1).uppercase()
            else -> "C"
        }
    }

    // Format ISO date to standard readable format
    val formattedDate = remember(item.paymentDate) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val parsedDate = parser.parse(item.paymentDate)
            if (parsedDate != null) {
                SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault()).format(parsedDate)
            } else {
                item.paymentDate.take(10)
            }
        } catch (_: Exception) {
            item.paymentDate.take(10)
        }
    }

    // Resolve invoice or order identifier
    val invoiceNo = item.invoice?.invoiceNumber?.ifBlank { null }
        ?: item.salesOrder?.orderCode?.ifBlank { null }
        ?: "INV-0011"

    val paymentTypeBadge = if (item.isAdvancePayment || item.invoice == null) "ADVANCE PAYMENT" else "INVOICE PAYMENT"
    val formattedReceiptAmount = "${currencySymbol()} ${String.format("%,.0f", item.amount)}"
    val formattedUnusedBalance = "${currencySymbol()} ${String.format("%,.0f", item.unallocatedAmount)}"

    // Dropdown overflow actions
    val menuActions = remember {
        listOf(
            MenuAction(
                label = "View Details",
                icon = Icons.Outlined.Visibility,
                textColor = TextPrimary,
                tint = TextSecondary,
                onClick = onClick
            ),
            MenuAction(
                label = "Delete",
                icon = Icons.Outlined.Delete,
                textColor = redText,
                tint = redText,
                onClick = {}
            )
        )
    }

    // Structured metadata rows using DataCardField model
    val metaFieldsLeft = listOf(
        DataCardField(
            label = "Rcpt:",
            text = item.paymentNumber.ifBlank { "" },
            textColor = TextLog,
            labelColor = TextSecondary,
            valueFontWeight = FontWeight.Normal
        ),
        DataCardField(
            label = "Ref:",
            text = item.referenceNumber?.ifBlank { "-" } ?: "",
            textColor = TextLog,
            labelColor = TextSecondary,
            valueFontWeight = FontWeight.Normal
        )
    )

    val metaFieldsRight = listOf(
        DataCardField(
            label = "Date:",
            text = formattedDate,
            textColor = headerGrey,
            labelColor = TextSecondary,
            valueFontWeight = FontWeight.Normal
        ),
        DataCardField(
            label = "Branch:",
            text = item.depositToAccount?.accountName?.ifBlank { "" } ?: "",
            textColor = headerGrey,
            labelColor = TextSecondary,
            valueFontWeight = FontWeight.Normal
        )
    )

    DataCard(
        item = item,
        onClick = { onClick() },
        showHeaderDivider = false,
        showActionsInHeader = false,
        actions = emptyList(), // Actions rendered directly inside the content row
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Customer Identity, Reused Status Badge and Action Menu Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Initials Avatar
                    Box(
                        modifier = Modifier
                            .size(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                            .background(primary_light),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = tokens.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(tokens.extraPadding))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                        ) {
                            Text(
                                text = customerName,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = blackTitle
                            )

                            // Reused StatusBadge for Payment Mode (e.g. Cash)
                            StatusBadge(
                                text = item.paymentMode.ifBlank { "Cash" },
                                bgColor = greenBg,
                                textColor = greentext,
                                showDot = false,
                                cornerRadius = tokens.cardCornerRadius * 0.4f
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Invoice / Reference Code
                        Text(
                            text = invoiceNo,
                            fontSize = tokens.caption,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }

                    // Status Badge with ActionDropdownMenu right next to it
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                    ) {
                        StatusBadge(
                            text = paymentTypeBadge,
                            bgColor = primary_light,
                            textColor = Primary,
                            showDot = false,
                            cornerRadius = tokens.cardCornerRadius
                        )

                        ActionDropdownMenu(
                            actions = menuActions,
                            icon = Icons.Default.MoreVert
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

                // Middle 2-Column Metadata Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column (Receipt & Ref)
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)) {
                        metaFieldsLeft.forEach { field ->
                            Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)) {
                                Text(
                                    text = field.label.orEmpty(),
                                    fontSize = tokens.bodySmall,
                                    color = field.labelColor,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = field.text,
                                    fontSize = tokens.bodySmall,
                                    color = field.textColor,
                                    fontWeight = field.valueFontWeight
                                )
                            }
                        }
                    }

                    // Right Column (Date & Branch)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                    ) {
                        metaFieldsRight.forEach { field ->
                            Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)) {
                                Text(
                                    text = field.label.orEmpty(),
                                    fontSize = tokens.bodySmall,
                                    color = field.labelColor,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = field.text,
                                    fontSize = tokens.bodySmall,
                                    color = field.textColor,
                                    fontWeight = field.valueFontWeight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                // Bottom Balance and Amount Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left: UNUSED BALANCE
                    Column {
                        Text(
                            text = "UNUSED BALANCE",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedUnusedBalance,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    // Right: RECEIPT AMOUNT
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "RECEIPT AMOUNT",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formattedReceiptAmount,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(tokens.extraPadding * 0.3f))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = iconMuted,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }
                    }
                }
            }
        }
    )
}