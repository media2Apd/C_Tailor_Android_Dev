package com.cuso.tailor.view.home.finance.credit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.CreditNoteItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CreditNotesListScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val creditNotes by viewModel.creditNotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<CreditNoteItemDto?>(null) }

    // Re-fetch credit notes list whenever this screen is displayed or returned to
    LaunchedEffect(Unit) {
        viewModel.fetchCreditNotes()
    }

    // Reusable Delete Confirmation Dialog
    if (itemToDelete != null) {
        DeleteModel(
            title = "Delete Credit Note",
            message = "Are you sure you want to delete credit note ${itemToDelete?.creditNoteNumber}? This will remove it from the system.",
            onDismiss = { itemToDelete = null },
            onDelete = {
                itemToDelete?.id?.let { id ->
                    viewModel.deleteCreditNote(id) {
                        itemToDelete = null
                        viewModel.fetchCreditNotes()
                    }
                }
            }
        )
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "New Credit Note",
            icon = Icons.Default.Add,
            onClick = onNavigateToCreate
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TitleBar(title = "Credit", onClose = onBack)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search and Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(whiteBg)
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .padding(horizontal = tokens.cardPadding * 0.6f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = mutedText,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                            FormTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    viewModel.fetchCreditNotes(it)
                                },
                                placeholder = "Search SO number, customer...",
                                containerColor = Color.Transparent,
                                borderColor = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(whiteBg)
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filter",
                            tint = TitleColor,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                if (isLoading && creditNotes.isEmpty()) {
                    ListSkeleton()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = tokens.buttonHeight * 1.5f)
                    ) {
                        items(creditNotes, key = { it.id }) { item ->
                            val customerName = item.customerSnapshot?.name
                                ?: item.customerId?.fullName
                                ?: ""
                            val invoiceCode = item.invoiceId ?: "—"
                            val formattedDate = formatIsoToDisplay(item.creditNoteDate)

                            val statusUpper = item.status.uppercase(Locale.US).trim()

                            // Transform status text to "Closed" when fully applied
                            val displayStatus = if (statusUpper == "FULLY_APPLIED" || statusUpper == "CLOSED") {
                                "Closed"
                            } else {
                                item.status.replace("_", " ")
                            }

                            // Dynamic Status Color Resolution:
                            // - Partially Paid / Partially Applied -> Primary Brand Color
                            // - Void -> Red
                            // - Closed / Fully Applied -> Grey
                            // - Open -> Green
                            // - Draft / Default -> Yellow
                            val (badgeBgColor, badgeTextColor) = when {
                                statusUpper.contains("PARTIAL") -> Pair(primary_light, Primary)
                                statusUpper.contains("VOID") -> Pair(redBg, redText)
                                statusUpper.contains("CLOSED") || statusUpper == "FULLY_APPLIED" || statusUpper == "APPLIED" -> Pair(light_grey, headerGrey)
                                statusUpper == "OPEN" -> Pair(greenBg, greentext)
                                else -> Pair(yellowBg, yellowText)
                            }

                            DataCard(
                                item = item,
                                title = customerName,
                                titleFontWeight = FontWeight.Medium,
                                topBadgeText = displayStatus,
                                topBadgeBgColor = badgeBgColor,
                                topBadgeTextColor = badgeTextColor,
                                topBadgeInline = true,
                                actions = listOf(
                                    MenuAction("View Details", onClick = { onNavigateToDetail(item.id) }),
                                    MenuAction("Delete", textColor = redText, onClick = { itemToDelete = item })
                                ),
                                content = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Credit No",
                                                fontSize = tokens.bodySmall,
                                                color = mutedText
                                            )
                                            Text(
                                                text = item.creditNoteNumber,
                                                fontSize = tokens.bodySmall,
                                                color = TitleColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Ref Number",
                                                fontSize = tokens.bodySmall,
                                                color = mutedText
                                            )
                                            Text(
                                                text = item.id.takeLast(6),
                                                fontSize = tokens.bodySmall,
                                                color = TitleColor
                                            )
                                        }

                                        Spacer(Modifier.height(tokens.extraPadding))

                                        // Summary Container
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                                .background(Primary_background)
                                                .padding(tokens.cardPadding * 0.6f)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(horizontalAlignment = Alignment.Start) {
                                                    Text(
                                                        text = "Invoice Number",
                                                        fontSize = tokens.caption,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                                    Text(
                                                        text = invoiceCode,
                                                        fontSize = tokens.bodySmall,
                                                        color = TitleColor
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = "Amount",
                                                        fontSize = tokens.caption,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                                    Text(
                                                        text = "₹${String.format(Locale.US, "%,.2f", item.grandTotal)}",
                                                        fontSize = tokens.bodySmall,
                                                        color = TitleColor,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "Balance",
                                                        fontSize = tokens.caption,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                                    Text(
                                                        text = "₹${String.format(Locale.US, "%,.2f", item.remainingBalance)}",
                                                        fontSize = tokens.bodySmall,
                                                        color = TitleColor,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                                        Text(
                                            text = "Date : $formattedDate",
                                            fontSize = tokens.caption,
                                            color = headerGrey
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Utility function to format ISO date strings
private fun formatIsoToDisplay(isoString: String): String {
    if (isoString.isBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val date = parser.parse(isoString)
        date?.let { formatter.format(it) } ?: isoString.take(10)
    } catch (_: Exception) {
        isoString.take(10)
    }
}