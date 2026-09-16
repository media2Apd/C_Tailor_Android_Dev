@file:Suppress("unused")

package com.cuso.mobile.view.home.inventory.procurement.credits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*

// ─────────────────────────────────────────────────────────────
// DATA MODEL
// ─────────────────────────────────────────────────────────────

data class CreditItemModel(
    val id: String,
    val voucherNo: String = "VC-23454",
    val date: String = "12 Feb 2026",
    val status: String = "Approved",
    val customerInitial: String = "H",
    val customerName: String = "Siva",
    val createdBy: String = "Rahman",
    val amount: String = "₹ 50,000",
    val balance: String = "₹ 20,000",
    val billNo: String = "BILL-88940",
    val reason: String = "Price Adjustments"
)

// ─────────────────────────────────────────────────────────────
// SCREEN COMPOSABLE
// ─────────────────────────────────────────────────────────────

@Composable
fun AllCreditsScreen(
    onClose: () -> Unit,
    onCreditClick: (CreditItemModel) -> Unit = {},
    onOptionsClick: (CreditItemModel) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }

    // Mock Sample Data matching the screenshot
    val creditList = remember {
        listOf(
            CreditItemModel(id = "1"),
            CreditItemModel(id = "2"),
            CreditItemModel(id = "3"),
            CreditItemModel(id = "4"),
            CreditItemModel(id = "5")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "All Credits",
                    onClose = onClose
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search and Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Customers...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 1.dp)

                // Credits List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(creditList, key = { _, item -> item.id }) { _, item ->
                        val isChecked = selectedItemIds.contains(item.id)
                        CreditCardItem(
                            item = item,
                            isChecked = isChecked,
                            tokens = tokens,
                            onCheckedChange = { checked ->
                                selectedItemIds = if (checked) {
                                    selectedItemIds + item.id
                                } else {
                                    selectedItemIds - item.id
                                }
                            },
                            onClick = { onCreditClick(item) },
                            onOptionsClick = { onOptionsClick(item) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// CREDIT CARD ITEM
// ─────────────────────────────────────────────────────────────

@Composable
private fun CreditCardItem(
    item: CreditItemModel,
    isChecked: Boolean,
    tokens: AppDesignTokens,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Row 1: Checkbox, Voucher No, Date, Status Badge, More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = item.voucherNo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                Text(
                    text = "  •  ${item.date}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )

                Spacer(Modifier.weight(1f))

                StatusDotBadge(text = item.status)

                Spacer(Modifier.width(4.dp))

                IconButton(
                    onClick = onOptionsClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = mutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(5.dp))
            HorizontalDivider(color = grey_border)
            Spacer(Modifier.height(5.dp))

            // Row 2: Customer Avatar, Customer Name & Created By | Amount & Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer Avatar Initial
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.customerInitial,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5)
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Customer Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.customerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = mutedText)) {
                                append("Created By: ")
                            }
                            withStyle(style = SpanStyle(color = title_color, fontWeight = FontWeight.Medium)) {
                                append(item.createdBy)
                            }
                        },
                        fontSize = 11.sp
                    )
                }

                // Amount & Balance Info
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AMOUNT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = mutedText,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = item.amount,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = mutedText)) {
                                append("Balance: ")
                            }
                            withStyle(style = SpanStyle(color = title_color, fontWeight = FontWeight.SemiBold)) {
                                append(item.balance)
                            }
                        },
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = grey_border)
            Spacer(Modifier.height(6.dp))

            // Row 3: Bill ID Tag & Reason
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bill Section
                Text(
                    text = "Bill: ",
                    fontSize = 11.sp,
                    color = mutedText
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.billNo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }

                Spacer(Modifier.weight(1f))

                // Reason Section
                Text(
                    text = "Reason: ",
                    fontSize = 11.sp,
                    color = mutedText
                )
                Text(
                    text = item.reason,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE BADGE
// ─────────────────────────────────────────────────────────────

@Composable
private fun StatusDotBadge(
    text: String,
    bgColor: Color = Color(0xFFE8F8F0),
    textColor: Color = Color(0xFF10B981),
    dotColor: Color = Color(0xFF10B981)
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}