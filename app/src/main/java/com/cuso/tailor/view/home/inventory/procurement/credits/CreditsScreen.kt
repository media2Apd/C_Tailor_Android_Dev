@file:Suppress("unused")

package com.cuso.tailor.view.home.inventory.procurement.credits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.PagingUiState
import com.cuso.tailor.utils.formatCurrency
import com.cuso.tailor.view.composable.*

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
    val amount: String = "${formatCurrency()} 50,000",
    val balance: String = "${formatCurrency()} 20,000",
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

    val creditList = remember {
        listOf(
            CreditItemModel(id = "1"),
            CreditItemModel(id = "2"),
            CreditItemModel(id = "3"),
            CreditItemModel(id = "4"),
            CreditItemModel(id = "5")
        )
    }

    val filteredCredits = remember(creditList, searchQuery) {
        if (searchQuery.isBlank()) creditList
        else creditList.filter {
            it.customerName.contains(searchQuery, ignoreCase = true) ||
                    it.voucherNo.contains(searchQuery, ignoreCase = true) ||
                    it.billNo.contains(searchQuery, ignoreCase = true)
        }
    }

    val pagingState = remember(filteredCredits) {
        PagingUiState(
            items = filteredCredits,
            isInitialLoading = false,
            isLoadingMore = false,
            canLoadMore = false
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
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Customers...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 2.dp)

                PaginatedDataList(
                    state = pagingState,
                    onLoadMore = { },
                    modifier = Modifier.fillMaxSize()
                ) { item ->
                    val isChecked = selectedItemIds.contains(item.id)
                    Column {
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
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

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
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
        ) {
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
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Text(
                    text = "  •  ${item.date}",
                    fontSize = tokens.bodySmall,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.customerInitial,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.customerName,
                        fontSize = tokens.bodyMedium,
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
                        fontSize = tokens.caption
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AMOUNT",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = item.amount,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
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
                        fontSize = tokens.caption
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = grey_border)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bill: ",
                    fontSize = tokens.caption,
                    color = mutedText
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(grey_border)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.billNo,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = textSubdued
                    )
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = "Reason: ",
                    fontSize = tokens.caption,
                    color = mutedText
                )
                Text(
                    text = item.reason,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }
        }
    }
}

@Composable
private fun StatusDotBadge(
    text: String,
    bgColor: Color = greenBg,
    textColor: Color = greentext,
    dotColor: Color = greentext
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