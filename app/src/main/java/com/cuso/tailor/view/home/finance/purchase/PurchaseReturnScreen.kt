@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection","AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.finance.purchase

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.PurchaseReturnDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.FinanceViewModel

import com.cuso.tailor.view.composable.DeleteModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@SuppressLint("DefaultLocale")
@Composable
fun FinanceReturnScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCreateReturnClick: () -> Unit = {},
    onReturnClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val returns by viewModel.purchaseReturns.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Holds the item selected for deletion
    var itemToDelete by remember { mutableStateOf<PurchaseReturnDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchPurchaseReturns()
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { returnItem ->
        DeleteModel(
            title = "Delete Return Request",
            message = "You're going to delete the return \"${returnItem.rmaNumber.ifBlank { returnItem.id }}\". Are you sure?",
            checkboxText = "I confirm this deletion",
            confirmText = "Yes, Delete!",
            cancelText = "No, Keep It.",
            onDismiss = { itemToDelete = null },
            onDelete = {
                val returnId = returnItem.id
                itemToDelete = null
                viewModel.deletePurchaseReturn(
                    returnId = returnId,
                    onSuccess = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                Modifier.fillMaxWidth()
            ) {
                TitleBar(title = "All Returns", onClose = onBack)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            fab = FabConfig(
                label = "Create Return",
                icon = Icons.Default.Add,
                onClick = onCreateReturnClick
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search and filter component
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.fetchPurchaseReturns(search = searchQuery)
                    },
                    placeholder = "Search SO number, customer...",
                    showFilterIcon = true,
                    onFilterClick = { /* Handle Filter drawer */ }
                )

                if (isLoading && returns.isEmpty()) {
                    ListSkeleton()
                } else if (returns.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No returns found.",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 72.dp)
                    ) {
                        items(returns, key = { it.id }) { item ->
                            PurchaseReturnCard(
                                item = item,
                                onClick = { onReturnClick(item.id) },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ── PURCHASE RETURN CARD (REUSING DATACARD) ──
// ─────────────────────────────────────────────────────────────
@Composable
private fun PurchaseReturnCard(
    item: PurchaseReturnDto,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val vendorName = item.supplier?.name?.ifBlank { "Vendor Name" } ?: "Vendor Name"
    val poNumber = item.po?.poNumber ?: "—"
    val isApproved = item.status.equals("Approved", ignoreCase = true)

    val menuActions = listOf(
        MenuAction(label = "View Details", onClick = onClick),
        MenuAction(label = "Delete", onClick = onDelete)
    )

    DataCard(
        item = item,
        title = vendorName,
        code = poNumber,
        titleFontWeight = FontWeight.Bold,
        titleColor = TextPrimary,
        topBadgeText = item.status,
        topBadgeInline = true,
        topBadgeBgColor = if (isApproved) greenBg else grey_border,
        topBadgeTextColor = if (isApproved) greentext else TextSecondary,
        topBadgeDotColor = if (isApproved) greentext else TextSecondary,
        topBadgeShowDot = true,
        actions = menuActions,
        onClick = { onClick() },
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary_background,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Column 1: Return ID / RMA
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Return ID",
                                fontSize = 11.sp,
                                color = mutedText
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = item.rmaNumber.ifBlank { "—" },
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Column 2: Order ID / PO Number
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Order ID",
                                fontSize = 11.sp,
                                color = mutedText
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = poNumber,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Column 3: Type / Receive Status
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Receive Status",
                                fontSize = 11.sp,
                                color = mutedText
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = item.receiveStatus.ifBlank { "—" },
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Footer Row: Return Date & Refund Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date : ${formatReturnDate(item.returnDate)}",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Refund : ",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                        Text(
                            text = item.refundStatus.ifBlank { "—" },
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.refundStatus.equals("Refunded", ignoreCase = true)) greentext else redText
                        )
                    }
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// ── DATE FORMAT HELPER ──
// ─────────────────────────────────────────────────────────────
private fun formatReturnDate(isoDate: String): String {
    if (isoDate.isBlank()) return "—"
    return try {
        val datePart = isoDate.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthIdx = (parts[1].toIntOrNull() ?: 1) - 1
            val monthName = months.getOrElse(monthIdx) { parts[1] }
            "${parts[2]} $monthName, ${parts[0]}"
        } else isoDate
    } catch (_: Exception) {
        isoDate
    }
}