@file:Suppress("UNUSED_VARIABLE", "SpellCheckingInspection")
package com.cuso.tailor.view.home.finance.purchase

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.VendorCreditItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.FinanceViewModel

@SuppressLint("DefaultLocale")
@Composable
fun VendorCreditListScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCreditClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val credits by viewModel.vendorCredits.collectAsState()
    val isLoading by viewModel.isLoadingVendorCredits.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchVendorCredits(page = 1)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                Modifier.fillMaxWidth()
            ) {
                TitleBar(title = "All Credits", onClose = onBack)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search and filter component
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.fetchVendorCredits(page = 1, search = it)
                },
                placeholder = "Search SO number, customer...",
                showFilterIcon = true,
                onFilterClick = { /* Handle Filter drawer */ }
            )

            if (isLoading && credits.isEmpty()) {
                ListSkeleton()
            } else if (credits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No vendor credits found.",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(credits, key = { it.id }) { item ->
                        VendorCreditCard(
                            item = item,
                            onClick = { onCreditClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ── VENDOR CREDIT CARD (REUSING DATACARD) ──
// ─────────────────────────────────────────────────────────────
@SuppressLint("DefaultLocale")
@Composable
private fun VendorCreditCard(
    item: VendorCreditItemDto,
    onClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val platformLocale = LocalLocale.current.platformLocale

    val vendorName = item.supplierSnapshot?.name?.ifBlank { "—" } ?: "—"
    val creditNumber = item.vendorCreditNumber.ifBlank { "—" }
    val refBill = item.supplierCreditReference.ifBlank { "—" }
    val reason = item.lines.firstOrNull()?.itemDescription?.ifBlank { "—" } ?: "—"
    val isOpen = item.status.equals("Open", ignoreCase = true)

    val menuActions = listOf(
        MenuAction(label = "View Details", onClick = onClick)
    )

    DataCard(
        item = item,
        title = vendorName,
        titleFontWeight = FontWeight.Bold,
        titleColor = TextPrimary,
        topBadgeText = item.status,
        topBadgeInline = true,
        topBadgeBgColor = if (isOpen) greenBg else grey_border,
        topBadgeTextColor = if (isOpen) greentext else TextSecondary,
        topBadgeDotColor = if (isOpen) greentext else TextSecondary,
        topBadgeShowDot = true,
//        actions = menuActions,
        onClick = { onClick() },
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Credit Number Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Credit No",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                    Text(
                        text = creditNumber,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Reference Number Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ref Number",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                    Text(
                        text = refBill,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Highlighted Metric Box: Reason, Amount, Balance
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary_background,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reason column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reason",
                                fontSize = 11.sp,
                                color = mutedText
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = reason,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Amount column
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Amount",
                                fontSize = 11.sp,
                                color = mutedText
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "₹${String.format(platformLocale, "%,.2f", item.grandTotal)}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        // Balance column
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Balance",
                                fontSize = 11.sp,
                                color = mutedText
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "₹${String.format(platformLocale, "%,.2f", item.remainingBalance)}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = grey_border, thickness = 1.dp)
                Spacer(Modifier.height(8.dp))

                // Footer Date Row
                Text(
                    text = "Date : ${formatCreditDate(item.vendorCreditDate)}",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// ── DATE FORMAT HELPER ──
// ─────────────────────────────────────────────────────────────
private fun formatCreditDate(isoDate: String): String {
    if (isoDate.isBlank()) return "—"
    return try {
        val datePart = isoDate.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthIdx = (parts[1].toIntOrNull() ?: 1) - 1
            val monthName = months.getOrElse(monthIdx) { parts[1] }
            "${parts[2]} $monthName ${parts[0]}"
        } else isoDate
    } catch (_: Exception) {
        isoDate
    }
}